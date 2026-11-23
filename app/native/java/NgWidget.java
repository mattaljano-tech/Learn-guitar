package se.mattaljano.nylongitarr;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import org.json.JSONObject;

// Hemskärmswidget: streak, dagens mål och en knapp som öppnar appen
public class NgWidget extends AppWidgetProvider {
    protected int layout() { return R.layout.ng_widget; }

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, views(ctx, layout()));
    }

    static RemoteViews views(Context ctx, int layout) {
        String streak = "0", goal = "Öppna appen och kör igång", label = "dagar i rad", button = "Kör igång", level = "";
        try {
            JSONObject j = new JSONObject(ctx.getSharedPreferences("ng_widget", 0).getString("data", "{}"));
            streak = String.valueOf(j.optInt("streak", 0));
            goal = j.optString("goal", goal);
            label = j.optString("label", label);
            button = j.optString("button", button);
            level = j.optString("level", "");
        } catch (Exception e) { }
        RemoteViews v = new RemoteViews(ctx.getPackageName(), layout);
        v.setTextViewText(R.id.ng_streak, streak);
        v.setTextViewText(R.id.ng_label, label);
        v.setTextViewText(R.id.ng_goal, goal);
        v.setTextViewText(R.id.ng_button, button);
        v.setTextViewText(R.id.ng_level, level);
        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.ng_root, pi);
        v.setOnClickPendingIntent(R.id.ng_button, pi);
        return v;
    }

    static void updateAll(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        for (int id : mgr.getAppWidgetIds(new ComponentName(ctx, NgWidget.class))) mgr.updateAppWidget(id, views(ctx, R.layout.ng_widget));
        for (int id : mgr.getAppWidgetIds(new ComponentName(ctx, NgWidgetSmall.class))) mgr.updateAppWidget(id, views(ctx, R.layout.ng_widget_small));
        for (int id : mgr.getAppWidgetIds(new ComponentName(ctx, NgWidgetJam.class))) mgr.updateAppWidget(id, NgWidgetJam.jamViews(ctx));
    }
}
