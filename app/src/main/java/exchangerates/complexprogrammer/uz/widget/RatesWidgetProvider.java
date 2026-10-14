package exchangerates.complexprogrammer.uz.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import java.util.List;

import exchangerates.complexprogrammer.uz.MainActivity;
import exchangerates.complexprogrammer.uz.R;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.AppPrefs;
import exchangerates.complexprogrammer.uz.services.Formatters;
import exchangerates.complexprogrammer.uz.services.LocaleHelper;
import exchangerates.complexprogrammer.uz.services.RatesRepository;
import exchangerates.complexprogrammer.uz.work.RatesWorker;

/** Home screen widget with the first three favorite currencies (USD, EUR, RUB by default). */
public class RatesWidgetProvider extends AppWidgetProvider {
    private static final int[] ROWS = {R.id.widget_row_1, R.id.widget_row_2, R.id.widget_row_3};
    private static final int[] CODES = {R.id.widget_code_1, R.id.widget_code_2, R.id.widget_code_3};
    private static final int[] RATES = {R.id.widget_rate_1, R.id.widget_rate_2, R.id.widget_rate_3};
    private static final int[] DIFFS = {R.id.widget_diff_1, R.id.widget_diff_2, R.id.widget_diff_3};

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        List<Response> cached = AppPrefs.getCachedRates(context);
        if (cached != null) render(context, manager, ids, cached);
        RatesWorker.runNow(context);
    }

    public static void updateAll(Context context, List<Response> rates) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, RatesWidgetProvider.class));
        if (ids.length > 0) render(context, manager, ids, rates);
    }

    private static void render(Context base, AppWidgetManager manager, int[] ids, List<Response> rates) {
        Context context = LocaleHelper.localized(base);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_rates);
        List<Response> sorted = RatesRepository.sortFavoritesFirst(rates, AppPrefs.getFavorites(context));

        views.setTextViewText(R.id.widget_date, sorted.isEmpty() ? "" : sorted.get(0).getDate());
        for (int i = 0; i < ROWS.length; i++) {
            if (i >= sorted.size()) {
                views.setViewVisibility(ROWS[i], View.GONE);
                continue;
            }
            Response rate = sorted.get(i);
            views.setViewVisibility(ROWS[i], View.VISIBLE);
            views.setTextViewText(CODES[i], Formatters.nominal(rate));
            views.setTextViewText(RATES[i], Formatters.money(context, rate.getRateValue()));
            views.setTextViewText(DIFFS[i], Formatters.signed(context, rate.getDiffValue()));
            views.setTextColor(DIFFS[i], ContextCompat.getColor(context, Formatters.trendColor(rate.getDiffValue())));
        }

        Intent intent = new Intent(context, MainActivity.class);
        views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        manager.updateAppWidget(ids, views);
    }
}
