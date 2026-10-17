package exchangerates.complexprogrammer.uz;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Configuration;

import java.io.FileInputStream;

import exchangerates.complexprogrammer.uz.services.AppPrefs;
import exchangerates.complexprogrammer.uz.work.Notifier;
import exchangerates.complexprogrammer.uz.work.RatesWorker;

public class MyApplication extends Application implements Configuration.Provider {
    private static AppOpenManager appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        String process = currentProcessName();
        if (!process.isEmpty() && !getPackageName().equals(process)) return;

        try {
            applyTheme(AppPrefs.getTheme(this));
        } catch (Exception ignored) {
        }

        try {
            Notifier.createChannels(this);
        } catch (Exception ignored) {
        }

        try {
            RatesWorker.schedule(this);
        } catch (Exception ignored) {
        }

        try {
            appOpenManager = new AppOpenManager(this);
        } catch (Exception ignored) {
        }
    }

    /** One of the ad SDKs removes WorkManager's default initializer, so the app supplies the configuration. */
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().build();
    }

    public static void applyTheme(String theme) {
        int mode;
        switch (theme) {
            case "light":
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    /** Called once ads are allowed, so the first app open ad is ready for the next return to the app. */
    public static void onAdsReady() {
        if (appOpenManager != null) appOpenManager.fetchAd();
    }

    private static String currentProcessName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) return Application.getProcessName();
        try (FileInputStream in = new FileInputStream("/proc/self/cmdline")) {
            byte[] buffer = new byte[256];
            int length = in.read(buffer);
            int end = 0;
            while (end < length && buffer[end] != 0) end++;
            return new String(buffer, 0, end);
        } catch (Exception e) {
            return "";
        }
    }
}
