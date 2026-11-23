package se.mattaljano.nylongitarr;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONObject;

// Jamma-widgeten: valfri storlek. Ett tryck börjar spela in direkt (utan att appen öppnas), ett tryck till sparar.
public class NgWidgetJam extends AppWidgetProvider {
    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, jamViews(ctx));
    }

    static void updateAll(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        for (int id : mgr.getAppWidgetIds(new ComponentName(ctx, NgWidgetJam.class))) mgr.updateAppWidget(id, jamViews(ctx));
    }

    static RemoteViews jamViews(Context ctx) {
        String label = "Jamma", recTxt = "Tryck för att spara", open = "Öppna appen";
        try {
            JSONObject j = new JSONObject(ctx.getSharedPreferences("ng_widget", 0).getString("data", "{}"));
            label = j.optString("jam", label); recTxt = j.optString("jamStop", recTxt); open = j.optString("jamOpen", open);
        } catch (Exception e) { }
        boolean on = NgJamService.recording(ctx);
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.ng_widget_jam);
        v.setImageViewResource(R.id.ng_jam_img, on ? R.drawable.ng_jam_icon_rec : R.drawable.ng_jam_icon);
        v.setTextViewText(R.id.ng_jam_label, on ? recTxt : label);
        v.setViewVisibility(R.id.ng_jam_timer, on ? View.VISIBLE : View.GONE);
        if (on) v.setChronometer(R.id.ng_jam_timer, NgJamService.prefs(ctx).getLong("start", 0), null, true);
        else v.setChronometer(R.id.ng_jam_timer, 0, null, false);
        v.setTextViewText(R.id.ng_jam_open, open);
        Intent t = new Intent(ctx, NgJamService.class).setAction(NgJamService.ACTION_TOGGLE);
        int fl = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pt = android.os.Build.VERSION.SDK_INT >= 26 ? PendingIntent.getForegroundService(ctx, 5, t, fl) : PendingIntent.getService(ctx, 5, t, fl);
        v.setOnClickPendingIntent(R.id.ng_jam_root, pt);
        Intent o = new Intent(ctx, MainActivity.class);
        o.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        o.putExtra("ng_action", "jamlist");
        PendingIntent po = PendingIntent.getActivity(ctx, 2, o, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.ng_jam_open, po);
        return v;
    }
}
