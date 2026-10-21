package exchangerates.complexprogrammer.uz.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import exchangerates.complexprogrammer.uz.models.RateAlert;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.AppPrefs;
import exchangerates.complexprogrammer.uz.services.RatesRepository;
import exchangerates.complexprogrammer.uz.widget.RatesWidgetProvider;

/** Refreshes rates in the background: updates the cache and widget, fires alerts and the daily notification. */
public class RatesWorker extends Worker {
    private static final String PERIODIC = "rates_periodic";
    private static final String ONE_TIME = "rates_now";

    public RatesWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(RatesWorker.class, 3, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static void runNow(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(RatesWorker.class).setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueueUniqueWork(ONE_TIME, ExistingWorkPolicy.REPLACE, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        List<Response> rates = RatesRepository.fetchSync(context);
        if (rates == null) return Result.retry();

        RatesWidgetProvider.updateAll(context, rates);
        checkAlerts(context, rates);
        checkDaily(context, rates);
        return Result.success();
    }

    /** Called from the UI after a successful refresh, so alerts react without waiting for the worker. */
    public static void onRatesUpdated(Context context, List<Response> rates) {
        RatesWidgetProvider.updateAll(context, rates);
        checkAlerts(context, rates);
    }

    private static void checkAlerts(Context context, List<Response> rates) {
        List<RateAlert> alerts = AppPrefs.getAlerts(context);
        if (alerts.isEmpty()) return;
        List<RateAlert> remaining = new ArrayList<>();
        for (RateAlert alert : alerts) {
            Response rate = RatesRepository.find(rates, alert.ccy);
            if (rate != null && alert.isTriggeredBy(rate.getRateValue())) {
                Notifier.showAlert(context, alert, rate);
            } else {
                remaining.add(alert);
            }
        }
        if (remaining.size() != alerts.size()) AppPrefs.setAlerts(context, remaining);
    }

    private static void checkDaily(Context context, List<Response> rates) {
        if (rates == null || rates.isEmpty()) return;
        String date = rates.get(0).getDate();
        if (!AppPrefs.isDailyEnabled(context) || date.equals(AppPrefs.getLastNotifiedDate(context))) return;
        AppPrefs.setLastNotifiedDate(context, date);
        Notifier.showDaily(context, rates);
    }
}
