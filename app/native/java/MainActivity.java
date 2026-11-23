package se.mattaljano.nylongitarr;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(NgNativePlugin.class);
        super.onCreate(savedInstanceState);
        // låt appen spela upp video i bakgrunden (behövs när jamvideon sparas med pilar)
        try { getBridge().getWebView().getSettings().setMediaPlaybackRequiresUserGesture(false); } catch (Exception e) {}
        take(getIntent());
    }

    // Jamma-widgeten öppnar appen med ng_action=jam
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        take(intent);
        NgNativePlugin.fire();
    }

    private void take(Intent i) {
        if (i == null) return;
        String a = i.getStringExtra("ng_action");
        if (a != null) { NgNativePlugin.pending = a; i.removeExtra("ng_action"); }
    }

    // När kompet spelar får webbsidan fortsätta köra även om skärmen släcks eller du byter app
    private void keepRunning() {
        if (!NgNativePlugin.playing || getBridge() == null) return;
        WebView w = getBridge().getWebView();
        if (w != null) { w.onResume(); w.resumeTimers(); }
    }

    @Override
    public void onPause() { super.onPause(); keepRunning(); }

    @Override
    public void onStop() { super.onStop(); keepRunning(); }
}
