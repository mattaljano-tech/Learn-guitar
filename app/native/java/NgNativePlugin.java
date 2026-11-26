package se.mattaljano.nylongitarr;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

// Liten brygga mellan webbappen och Android: widgetarnas data, Jamma-widgetens knapp
// och "spelar just nu" (så att ljudet får fortsätta med skärmen släckt)
@CapacitorPlugin(name = "NgNative")
public class NgNativePlugin extends Plugin {
    public static volatile boolean playing = false;
    public static volatile String pending = null;
    private static NgNativePlugin inst;

    @Override
    public void load() { inst = this; }

    static void fire() {
        if (inst != null && pending != null) inst.notifyListeners("action", new JSObject());
    }

    @PluginMethod
    public void getAction(PluginCall call) {
        JSObject r = new JSObject();
        r.put("action", pending);
        pending = null;
        call.resolve(r);
    }

    // Inspelningar gjorda från Jamma-widgeten som appen inte hämtat än
    @PluginMethod
    public void takeJams(PluginCall call) {
        android.content.SharedPreferences p = NgJamService.prefs(getContext());
        JSObject r = new JSObject();
        r.put("list", p.getString("pending", "[]"));
        p.edit().putString("pending", "[]").apply();
        call.resolve(r);
    }

    @PluginMethod
    public void setPlaying(PluginCall call) {
        playing = Boolean.TRUE.equals(call.getBoolean("on", false));
        call.resolve();
    }

    @PluginMethod
    public void setWidget(PluginCall call) {
        String json = call.getString("json", "{}");
        getContext().getSharedPreferences("ng_widget", 0).edit().putString("data", json).apply();
        NgWidget.updateAll(getContext());
        call.resolve();
    }
    // Vrid skärmen: "landscape", "portrait", "auto" (följer telefonen även om autorotering är av) eller "user" (som telefonen är inställd)
    @PluginMethod
    public void setOrientation(PluginCall call) {
        String m = call.getString("mode", "user");
        int o = "landscape".equals(m) ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
              : "portrait".equals(m) ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
              : "auto".equals(m) ? ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
              : ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
        if (getActivity() != null) getActivity().runOnUiThread(() -> getActivity().setRequestedOrientation(o));
        call.resolve();
    }

    // Spara en fil (som appen lagt i cache) direkt i telefonen: film -> Filmer/NylonGitarr, ljud -> Musik/NylonGitarr,
    // annat -> Hämtade filer. Video och ljud packas om först (MediaMuxer) så att längden står rätt i filen –
    // annars visar många spelare bara de första sekunderna av en inspelning från webbläsaren.
    @PluginMethod
    public void saveToDevice(PluginCall call) {
        final String path = call.getString("path"), name = call.getString("name", "fil"), mime = call.getString("mime", "application/octet-stream");
        if (path == null) { call.reject("path saknas"); return; }
        if (Build.VERSION.SDK_INT < 29) { call.reject("old-android"); return; }
        new Thread(() -> {
            File src = new File(getContext().getCacheDir(), path), fixed = null;
            try {
                boolean av = mime.startsWith("video/") || mime.startsWith("audio/");
                String outMime = mime, outName = name;
                if (av) {
                    boolean webm = mime.contains("webm");
                    File t = new File(getContext().getCacheDir(), "remux-" + System.currentTimeMillis() + (webm ? ".webm" : ".mp4"));
                    try { remux(src, t, webm); fixed = t; } catch (Exception e) { t.delete(); }
                }
                ContentResolver cr = getContext().getContentResolver();
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, outName);
                v.put(MediaStore.MediaColumns.MIME_TYPE, outMime.split(";")[0]);
                Uri col; String dir;
                if (mime.startsWith("video/")) { col = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); dir = Environment.DIRECTORY_MOVIES + "/NylonGitarr"; }
                else if (mime.startsWith("audio/")) { col = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); dir = Environment.DIRECTORY_MUSIC + "/NylonGitarr"; }
                else { col = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); dir = Environment.DIRECTORY_DOWNLOADS + "/NylonGitarr"; }
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, dir);
                v.put(MediaStore.MediaColumns.IS_PENDING, 1);
                Uri u = cr.insert(col, v);
                if (u == null) throw new Exception("kunde inte skapa filen");
                try (InputStream in = new FileInputStream(fixed != null ? fixed : src); OutputStream out = cr.openOutputStream(u)) {
                    byte[] b = new byte[1 << 16]; int n;
                    while ((n = in.read(b)) > 0) out.write(b, 0, n);
                }
                ContentValues done = new ContentValues();
                done.put(MediaStore.MediaColumns.IS_PENDING, 0);
                cr.update(u, done, null, null);
                JSObject r = new JSObject();
                r.put("uri", u.toString());
                r.put("folder", dir);
                r.put("remuxed", fixed != null);
                call.resolve(r);
            } catch (Exception e) {
                call.reject(String.valueOf(e.getMessage()));
            } finally {
                if (fixed != null) fixed.delete();
            }
        }).start();
    }

    // Packa om en fil i cache (för delning), så att mottagarens spelare ser hela längden. Ger tillbaka en file://-adress.
    @PluginMethod
    public void remuxCache(PluginCall call) {
        final String path = call.getString("path"), mime = call.getString("mime", "");
        if (path == null) { call.reject("path saknas"); return; }
        new Thread(() -> {
            File src = new File(getContext().getCacheDir(), path);
            File out = new File(getContext().getCacheDir(), "delning-" + path);
            try {
                remux(src, out, mime.contains("webm"));
                JSObject r = new JSObject();
                r.put("uri", "file://" + out.getAbsolutePath());
                call.resolve(r);
            } catch (Exception e) {
                out.delete();
                call.reject(String.valueOf(e.getMessage()));
            }
        }).start();
    }

    private static void remux(File in, File out, boolean webm) throws Exception {
        MediaExtractor ex = new MediaExtractor();
        MediaMuxer mx = null;
        try {
            ex.setDataSource(in.getAbsolutePath());
            int n = ex.getTrackCount();
            if (n == 0) throw new Exception("inga spår");
            mx = new MediaMuxer(out.getAbsolutePath(), webm ? MediaMuxer.OutputFormat.MUXER_OUTPUT_WEBM : MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int[] map = new int[n]; int maxSize = 1 << 20;
            for (int i = 0; i < n; i++) {
                MediaFormat f = ex.getTrackFormat(i);
                map[i] = mx.addTrack(f);
                ex.selectTrack(i);
                if (f.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) maxSize = Math.max(maxSize, f.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
                if (!webm && f.containsKey("rotation-degrees")) mx.setOrientationHint(f.getInteger("rotation-degrees"));
            }
            mx.start();
            ByteBuffer buf = ByteBuffer.allocate(Math.max(maxSize, 4 << 20));
            MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();
            long t0 = -1; int count = 0;
            while (true) {
                bi.offset = 0;
                bi.size = ex.readSampleData(buf, 0);
                if (bi.size < 0) break;
                long t = ex.getSampleTime();
                if (t0 < 0) t0 = Math.max(0, t);
                bi.presentationTimeUs = Math.max(0, t - t0);
                bi.flags = (ex.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0 ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0;
                mx.writeSampleData(map[ex.getSampleTrackIndex()], buf, bi);
                count++;
                ex.advance();
            }
            if (count == 0) throw new Exception("tom fil");
            mx.stop();
        } finally {
            try { if (mx != null) mx.release(); } catch (Exception e) {}
            ex.release();
        }
    }
}
