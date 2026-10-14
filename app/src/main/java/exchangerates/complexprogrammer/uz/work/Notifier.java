package exchangerates.complexprogrammer.uz.work;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Set;

import exchangerates.complexprogrammer.uz.DetailActivity;
import exchangerates.complexprogrammer.uz.MainActivity;
import exchangerates.complexprogrammer.uz.R;
import exchangerates.complexprogrammer.uz.models.RateAlert;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.AppPrefs;
import exchangerates.complexprogrammer.uz.services.Formatters;
import exchangerates.complexprogrammer.uz.services.LocaleHelper;
import exchangerates.complexprogrammer.uz.services.RatesRepository;

public class Notifier {
    private static final String CHANNEL_DAILY = "daily";
    private static final String CHANNEL_ALERTS = "alerts";
    private static final int DAILY_ID = 1;

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(CHANNEL_DAILY,
                context.getString(R.string.channel_daily), NotificationManager.IMPORTANCE_DEFAULT));
        manager.createNotificationChannel(new NotificationChannel(CHANNEL_ALERTS,
                context.getString(R.string.channel_alerts), NotificationManager.IMPORTANCE_HIGH));
    }

    /** Lists favorite currencies (USD, EUR, RUB by default) for the new date. */
    static void showDaily(Context base, List<Response> rates) {
        Context context = LocaleHelper.localized(base);
        Set<String> favorites = AppPrefs.getFavorites(context);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        StringBuilder summary = new StringBuilder();
        int lines = 0;
        for (Response rate : RatesRepository.sortFavoritesFirst(rates, favorites)) {
            if (lines >= 5 || (lines >= 3 && !favorites.contains(rate.getCcy()))) break;
            String line = Formatters.nominal(rate) + " = " + Formatters.moneyWithUnit(context, rate.getRateValue())
                    + "  " + Formatters.signed(context, rate.getDiffValue());
            style.addLine(line);
            if (lines < 2) summary.append(lines == 0 ? "" : " · ").append(line);
            lines++;
        }
        String title = context.getString(R.string.daily_title, rates.get(0).getDate());
        Intent intent = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notify(context, DAILY_ID, new NotificationCompat.Builder(context, CHANNEL_DAILY)
                .setSmallIcon(R.drawable.ic_stat_rates)
                .setContentTitle(title)
                .setContentText(summary)
                .setStyle(style.setBigContentTitle(title))
                .setContentIntent(PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true));
    }

    static void showAlert(Context base, RateAlert alert, Response rate) {
        Context context = LocaleHelper.localized(base);
        String text = context.getString(alert.above ? R.string.alert_triggered_above : R.string.alert_triggered_below,
                Formatters.nominal(rate), Formatters.money(context, alert.value),
                Formatters.moneyWithUnit(context, rate.getRateValue()));
        Intent intent = new Intent(context, DetailActivity.class).putExtra(DetailActivity.EXTRA_RATE, rate);
        int id = 100 + Math.abs((alert.ccy + alert.value).hashCode() % 10000);
        notify(context, id, new NotificationCompat.Builder(context, CHANNEL_ALERTS)
                .setSmallIcon(R.drawable.ic_stat_rates)
                .setContentTitle(context.getString(R.string.alert_set))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent.getActivity(context, id, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true));
    }

    @SuppressLint("MissingPermission")
    private static void notify(Context context, int id, NotificationCompat.Builder builder) {
        if (!canNotify(context)) return;
        builder.setColor(ContextCompat.getColor(context, R.color.brand));
        NotificationManagerCompat.from(context).notify(id, builder.build());
    }

    public static boolean canNotify(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }
}
