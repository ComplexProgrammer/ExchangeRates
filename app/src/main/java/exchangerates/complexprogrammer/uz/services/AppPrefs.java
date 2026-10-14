package exchangerates.complexprogrammer.uz.services;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import exchangerates.complexprogrammer.uz.models.RateAlert;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.models.ResponseData;

/** All persisted user state. Keys shared with preferences.xml are public. */
public class AppPrefs {
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_THEME = "theme";
    public static final String KEY_DAILY = "daily_notification";
    public static final String KEY_ALERTS = "alerts";
    public static final String KEY_AD_FREE = "ad_free";

    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_ALERTS_JSON = "alerts_json";
    private static final String KEY_AD_FREE_UNTIL = "ad_free_until";
    private static final String KEY_CACHED_RATES = "cached_rates";
    private static final String KEY_LAST_NOTIFIED_DATE = "last_notified_date";
    private static final String KEY_DETAIL_OPENS = "detail_opens";
    private static final String KEY_LAST_FULLSCREEN_AD = "last_fullscreen_ad";
    private static final String KEY_LANGUAGE_MIGRATED = "language_migrated";

    private static final Gson gson = new Gson();

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    // Language & theme

    /** BCP-47 tag chosen by the user ("uz", "uz-Cyrl", "ru", "en") or "system". */
    public static String getLanguage(Context context) {
        return get(context).getString(KEY_LANGUAGE, "system");
    }

    public static String getTheme(Context context) {
        return get(context).getString(KEY_THEME, "system");
    }

    /**
     * Carries over the language picked in versions before 3.0. Their default was English for
     * everybody, so only an explicit non-English choice is kept; everyone else follows the device.
     * Returns the tag to apply, or null when nothing needs to change.
     */
    public static String migrateLegacyLanguage(Context context) {
        SharedPreferences prefs = get(context);
        if (prefs.getBoolean(KEY_LANGUAGE_MIGRATED, false)) return null;
        String legacy = context.getSharedPreferences("C0mplexLanguage", Context.MODE_PRIVATE)
                .getString("langCode", null);
        String tag = null;
        if ("uz".equals(legacy)) tag = "uz";
        else if ("uzc".equals(legacy)) tag = "uz-Cyrl";
        else if ("ru".equals(legacy)) tag = "ru";
        SharedPreferences.Editor editor = prefs.edit().putBoolean(KEY_LANGUAGE_MIGRATED, true);
        if (tag != null) editor.putString(KEY_LANGUAGE, tag);
        editor.apply();
        return tag;
    }

    // Favorites

    public static Set<String> getFavorites(Context context) {
        Set<String> stored = get(context).getStringSet(KEY_FAVORITES, null);
        if (stored == null) {
            // Sensible default so the most used currencies are on top from the first launch.
            Set<String> defaults = new HashSet<>();
            Collections.addAll(defaults, "USD", "EUR", "RUB");
            return defaults;
        }
        return new HashSet<>(stored);
    }

    public static boolean toggleFavorite(Context context, String ccy) {
        Set<String> favorites = getFavorites(context);
        boolean nowFavorite = !favorites.remove(ccy);
        if (nowFavorite) favorites.add(ccy);
        get(context).edit().putStringSet(KEY_FAVORITES, favorites).apply();
        return nowFavorite;
    }

    // Alerts

    public static List<RateAlert> getAlerts(Context context) {
        String json = get(context).getString(KEY_ALERTS_JSON, null);
        if (json == null) return new ArrayList<>();
        try {
            List<RateAlert> alerts = gson.fromJson(json, new TypeToken<List<RateAlert>>() {}.getType());
            return alerts == null ? new ArrayList<>() : alerts;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void setAlerts(Context context, List<RateAlert> alerts) {
        get(context).edit().putString(KEY_ALERTS_JSON, gson.toJson(alerts)).apply();
    }

    public static void addAlert(Context context, RateAlert alert) {
        List<RateAlert> alerts = getAlerts(context);
        alerts.add(alert);
        setAlerts(context, alerts);
    }

    // Notifications

    public static boolean isDailyEnabled(Context context) {
        return get(context).getBoolean(KEY_DAILY, false);
    }

    public static String getLastNotifiedDate(Context context) {
        return get(context).getString(KEY_LAST_NOTIFIED_DATE, "");
    }

    public static void setLastNotifiedDate(Context context, String date) {
        get(context).edit().putString(KEY_LAST_NOTIFIED_DATE, date).apply();
    }

    // Ads

    public static long getAdFreeUntil(Context context) {
        return get(context).getLong(KEY_AD_FREE_UNTIL, 0);
    }

    public static void setAdFreeUntil(Context context, long time) {
        get(context).edit().putLong(KEY_AD_FREE_UNTIL, time).apply();
    }

    public static int incrementDetailOpens(Context context) {
        int count = get(context).getInt(KEY_DETAIL_OPENS, 0) + 1;
        get(context).edit().putInt(KEY_DETAIL_OPENS, count).apply();
        return count;
    }

    public static long getLastFullscreenAd(Context context) {
        return get(context).getLong(KEY_LAST_FULLSCREEN_AD, 0);
    }

    public static void setLastFullscreenAd(Context context, long time) {
        get(context).edit().putLong(KEY_LAST_FULLSCREEN_AD, time).apply();
    }

    // Offline cache

    public static List<Response> getCachedRates(Context context) {
        String json = get(context).getString(KEY_CACHED_RATES, null);
        if (json == null) return null;
        try {
            ResponseData data = gson.fromJson(json, ResponseData.class);
            return data == null || data.data == null || data.data.isEmpty() ? null : data.data;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setCachedRates(Context context, List<Response> rates) {
        ResponseData data = new ResponseData();
        data.data = rates;
        get(context).edit().putString(KEY_CACHED_RATES, gson.toJson(data)).apply();
    }
}
