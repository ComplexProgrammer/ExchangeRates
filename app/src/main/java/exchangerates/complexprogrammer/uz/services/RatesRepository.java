package exchangerates.complexprogrammer.uz.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import exchangerates.complexprogrammer.uz.models.HistoryPoint;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.models.ResponseData;
import retrofit2.Call;
import retrofit2.Callback;

/** Latest rates with an offline cache, plus rate history for the chart. */
public class RatesRepository {

    public interface RatesCallback {
        void onRates(@NonNull List<Response> rates);

        void onError();
    }

    public interface HistoryCallback {
        void onHistory(@Nullable List<HistoryPoint> points);
    }

    public enum Period {
        WEEK(8, 1), MONTH(31, 1), YEAR(27, 14);

        final int points;
        final int stepDays;

        Period(int points, int stepDays) {
            this.points = points;
            this.stepDays = stepDays;
        }
    }

    private static final Map<String, List<HistoryPoint>> historyCache = new HashMap<>();

    public static void fetch(Context context, RatesCallback callback) {
        Context app = context.getApplicationContext();
        ApiClient.getInterface(app).GetExchangeRates().enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(@NonNull Call<ResponseData> call, @NonNull retrofit2.Response<ResponseData> response) {
                ResponseData body = response.body();
                if (response.isSuccessful() && body != null && body.data != null && !body.data.isEmpty()) {
                    AppPrefs.setCachedRates(app, body.data);
                    callback.onRates(body.data);
                } else {
                    fetchFromCbu(app, callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseData> call, @NonNull Throwable t) {
                fetchFromCbu(app, callback);
            }
        });
    }

    private static void fetchFromCbu(Context app, RatesCallback callback) {
        ApiClient.getCbu(app).getAllRates().enqueue(new Callback<List<Response>>() {
            @Override
            public void onResponse(@NonNull Call<List<Response>> call, @NonNull retrofit2.Response<List<Response>> response) {
                List<Response> body = response.body();
                if (response.isSuccessful() && body != null && !body.isEmpty()) {
                    AppPrefs.setCachedRates(app, body);
                    callback.onRates(body);
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Response>> call, @NonNull Throwable t) {
                callback.onError();
            }
        });
    }

    /** Blocking variant for background work. Returns null on failure. */
    @Nullable
    public static List<Response> fetchSync(Context context) {
        Context app = context.getApplicationContext();
        try {
            retrofit2.Response<ResponseData> response = ApiClient.getInterface(app).GetExchangeRates().execute();
            ResponseData body = response.body();
            if (response.isSuccessful() && body != null && body.data != null && !body.data.isEmpty()) {
                AppPrefs.setCachedRates(app, body.data);
                return body.data;
            }
        } catch (IOException | RuntimeException ignored) {
        }

        // Fallback to Central Bank directly
        try {
            retrofit2.Response<List<Response>> cbuResponse = ApiClient.getCbu(app).getAllRates().execute();
            List<Response> body = cbuResponse.body();
            if (cbuResponse.isSuccessful() && body != null && !body.isEmpty()) {
                AppPrefs.setCachedRates(app, body);
                return body;
            }
        } catch (IOException | RuntimeException ignored) {
        }
        return null;
    }

    /** Favorites first (in API order), then everything else. */
    public static List<Response> sortFavoritesFirst(List<Response> rates, Set<String> favorites) {
        List<Response> first = new ArrayList<>();
        List<Response> rest = new ArrayList<>();
        for (Response rate : rates) {
            (favorites.contains(rate.getCcy()) ? first : rest).add(rate);
        }
        first.addAll(rest);
        return first;
    }

    @Nullable
    public static Response find(List<Response> rates, String ccy) {
        if (rates == null) return null;
        for (Response rate : rates) {
            if (rate.getCcy().equals(ccy)) return rate;
        }
        return null;
    }

    /**
     * Loads rate history from the Central Bank archive, one request per sampled date.
     * Results are kept in memory for the session.
     */
    public static void history(Context context, String ccy, Period period, HistoryCallback callback) {
        String key = ccy + "/" + period.name();
        List<HistoryPoint> cached = historyCache.get(key);
        if (cached != null) {
            callback.onHistory(cached);
            return;
        }

        SimpleDateFormat requestFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < period.points; i++) {
            dates.add(requestFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, -period.stepDays);
        }

        // Keyed by the rate's own date, so weekends that repeat Friday's rate collapse into one point.
        Map<Long, Double> byDate = Collections.synchronizedMap(new LinkedHashMap<>());
        int[] remaining = {dates.size()};
        Handler main = new Handler(Looper.getMainLooper());
        Runnable finish = () -> {
            if (--remaining[0] > 0) return;
            List<HistoryPoint> points = new ArrayList<>();
            synchronized (byDate) {
                for (Map.Entry<Long, Double> entry : byDate.entrySet()) {
                    points.add(new HistoryPoint(entry.getKey(), entry.getValue()));
                }
            }
            Collections.sort(points, (a, b) -> Long.compare(a.time, b.time));
            if (points.size() >= 2) {
                historyCache.put(key, points);
                callback.onHistory(points);
            } else {
                callback.onHistory(null);
            }
        };

        CbuApi api = ApiClient.getCbu(context.getApplicationContext());
        for (String date : dates) {
            api.rateOnDate(ccy, date).enqueue(new Callback<List<Response>>() {
                @Override
                public void onResponse(@NonNull Call<List<Response>> call, @NonNull retrofit2.Response<List<Response>> response) {
                    List<Response> body = response.body();
                    if (response.isSuccessful() && body != null && !body.isEmpty()) {
                        Response rate = body.get(0);
                        Date parsed = Formatters.parseApiDate(rate.getDate());
                        if (parsed != null && rate.getRateValue() > 0) {
                            byDate.put(parsed.getTime(), rate.getRateValue());
                        }
                    }
                    main.post(finish);
                }

                @Override
                public void onFailure(@NonNull Call<List<Response>> call, @NonNull Throwable t) {
                    main.post(finish);
                }
            });
        }
    }
}
