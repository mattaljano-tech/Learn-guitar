package se.mattaljano.nylongitarr;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import org.json.JSONObject;

// Jamma-widgeten: ett tryck öppnar appen i Jamma och börjar lyssna och spela in
public class NgWidgetJam extends AppWidgetProvider {
    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, jamViews(ctx));
    }

    static RemoteViews jamViews(Context ctx) {
        String label = "Jamma";
        try { label = new JSONObject(ctx.getSharedPreferences("ng_widget", 0).getString("data", "{}")).optString("jam", label); } catch (Exception e) { }
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.ng_widget_jam);
        v.setTextViewText(R.id.ng_jam_label, label);
        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        open.putExtra("ng_action", "jam");
        PendingIntent pi = PendingIntent.getActivity(ctx, 2, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.ng_jam_root, pi);
        return v;
    }
}
