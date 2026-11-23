package se.mattaljano.nylongitarr;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

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
}
