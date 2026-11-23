package se.mattaljano.nylongitarr;

import android.Manifest;
import android.app.Notification;
import androidx.core.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;

// Spelar in ett jam direkt från widgeten, utan att appen öppnas.
// Inspelningen sparas i appens mapp och hämtas in till Jamma nästa gång appen öppnas.
public class NgJamService extends Service {
    static final String ACTION_TOGGLE = "se.mattaljano.nylongitarr.JAM_TOGGLE";
    static final String CH = "ng_jam";
    private MediaRecorder rec;
    private File file;
    private long startWall;

    @Override
    public IBinder onBind(Intent i) { return null; }

    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("ng_jam", 0); }

    static boolean recording(Context c) { return prefs(c).getLong("start", 0) > 0; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        channel();
        Notification n = note(getString(R.string.app_name) + " · Jamma", "Spelar in … tryck på widgeten för att sluta");
        if (rec != null) {
            try { if (Build.VERSION.SDK_INT >= 30) startForeground(78, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE); else startForeground(78, n); } catch (Exception e) { }
            stopRec(); stopSelf(); return START_NOT_STICKY;
        }
        n = note(getString(R.string.app_name) + " · Jamma", "Spelar in … tryck på widgeten för att sluta");
        try {
            if (Build.VERSION.SDK_INT >= 30) startForeground(78, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
            else startForeground(78, n);
        } catch (Exception e) { fail(); stopSelf(); return START_NOT_STICKY; }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { fail(); stopForeground(true); stopSelf(); return START_NOT_STICKY; }
        try {
            File dir = new File(getFilesDir(), "jam"); dir.mkdirs();
            startWall = System.currentTimeMillis();
            file = new File(dir, "jam-" + startWall + ".m4a");
            rec = Build.VERSION.SDK_INT >= 31 ? new MediaRecorder(this) : new MediaRecorder();
            rec.setAudioSource(MediaRecorder.AudioSource.MIC);
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            rec.setAudioEncodingBitRate(128000);
            rec.setAudioSamplingRate(44100);
            rec.setOutputFile(file.getAbsolutePath());
            rec.prepare();
            rec.start();
            prefs(this).edit().putLong("start", SystemClock.elapsedRealtime()).apply();
        } catch (Exception e) {
            rec = null; fail(); stopForeground(true); stopSelf(); return START_NOT_STICKY;
        }
        NgWidgetJam.updateAll(this);
        return START_NOT_STICKY;
    }

    private void stopRec() {
        long dur = 0;
        try { rec.stop(); dur = System.currentTimeMillis() - startWall; } catch (Exception e) { if (file != null) file.delete(); file = null; }
        try { rec.release(); } catch (Exception e) { }
        rec = null;
        if (file != null && dur > 1500) {
            try {
                SharedPreferences p = prefs(this);
                JSONArray a = new JSONArray(p.getString("pending", "[]"));
                JSONObject o = new JSONObject();
                o.put("path", "jam/" + file.getName()); o.put("at", startWall); o.put("dur", dur / 1000.0);
                a.put(o);
                p.edit().putString("pending", a.toString()).apply();
            } catch (Exception e) { }
        } else if (file != null) file.delete();
        prefs(this).edit().putLong("start", 0).apply();
        stopForeground(true);
        NgWidgetJam.updateAll(this);
    }

    @Override
    public void onDestroy() {
        if (rec != null) stopRec();
        prefs(this).edit().putLong("start", 0).apply();
        NgWidgetJam.updateAll(this);
        super.onDestroy();
    }

    private void fail() {
        prefs(this).edit().putLong("start", 0).apply();
        NgWidgetJam.updateAll(this);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        open.putExtra("ng_action", "jam");
        PendingIntent pi = PendingIntent.getActivity(this, 3, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification n = new NotificationCompat.Builder(this, CH).setSmallIcon(R.drawable.ic_stat_ng)
            .setContentTitle("Kunde inte spela in från widgeten")
            .setContentText("Tryck här för att jamma i appen. Tillåt mikrofonen om appen frågar.")
            .setContentIntent(pi).setAutoCancel(true).build();
        nm.notify(79, n);
    }

    private void channel() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm.getNotificationChannel(CH) == null) nm.createNotificationChannel(new NotificationChannel(CH, "Jamma", NotificationManager.IMPORTANCE_LOW));
    }

    private Notification note(String title, String text) {
        Intent stop = new Intent(this, NgJamService.class).setAction(ACTION_TOGGLE);
        PendingIntent pi = PendingIntent.getService(this, 4, stop, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CH).setSmallIcon(R.drawable.ic_stat_ng).setContentTitle(title).setContentText(text)
            .setOngoing(true).setUsesChronometer(true).setWhen(System.currentTimeMillis())
            .addAction(0, "Stoppa", pi).build();
    }
}
