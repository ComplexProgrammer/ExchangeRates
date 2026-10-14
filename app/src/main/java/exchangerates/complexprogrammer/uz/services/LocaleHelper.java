package exchangerates.complexprogrammer.uz.services;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/** Per-app language on top of AppCompat's locale API, which also covers Android 12 and below. */
public class LocaleHelper {

    public static void setLanguage(Context context, String tag) {
        AppPrefs.get(context).edit().putString(AppPrefs.KEY_LANGUAGE, tag).apply();
        AppCompatDelegate.setApplicationLocales("system".equals(tag)
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(tag));
    }

    /** Locale the UI is shown in: the user's choice, otherwise the device language. */
    public static Locale getLocale(Context context) {
        String tag = AppPrefs.getLanguage(context);
        if ("system".equals(tag)) {
            Locale system = LocaleListCompat.getAdjustedDefault().get(0);
            return system != null ? system : Locale.getDefault();
        }
        return Locale.forLanguageTag(tag);
    }

    /** Which API name field to show: "uz", "uzc", "ru" or "en". */
    public static String getContentLanguage(Context context) {
        Locale locale = getLocale(context);
        switch (locale.getLanguage()) {
            case "uz":
                return "Cyrl".equals(locale.getScript()) ? "uzc" : "uz";
            case "ru":
                return "ru";
            default:
                return "en";
        }
    }

    /**
     * A context whose resources follow the chosen language. Needed outside activities
     * (notifications, widget), where AppCompat does not apply the per-app locale.
     */
    public static Context localized(Context context) {
        if ("system".equals(AppPrefs.getLanguage(context))) return context;
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(getLocale(context));
        return context.createConfigurationContext(config);
    }
}
