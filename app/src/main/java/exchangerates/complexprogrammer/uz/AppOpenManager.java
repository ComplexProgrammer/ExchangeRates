package exchangerates.complexprogrammer.uz;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.concurrent.TimeUnit;

import exchangerates.complexprogrammer.uz.ads.AdManager;

/**
 * App open ad shown only when the user comes back to the app after a while,
 * never on a cold start and at most once every {@link #MIN_INTERVAL_MS}.
 */
public class AppOpenManager implements DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String AD_UNIT_ID = "ca-app-pub-7773722896374259/8468911837";
    private static final long MIN_INTERVAL_MS = TimeUnit.HOURS.toMillis(4);
    private static final long AD_EXPIRY_MS = TimeUnit.HOURS.toMillis(4);

    private final Application application;
    private AppOpenAd appOpenAd;
    private long loadTime;
    private boolean loading;
    private boolean wasInBackground;
    private Activity currentActivity;

    public AppOpenManager(Application application) {
        this.application = application;
        application.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (wasInBackground) showIfAllowed();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        wasInBackground = true;
    }

    private void showIfAllowed() {
        if (!AdManager.isReady() || AdManager.isAdFree(application) || currentActivity == null) return;
        if (!isAdAvailable()) {
            fetchAd();
            return;
        }
        if (AdManager.isShowingFullscreen() || !AdManager.fullscreenGapPassed(application, MIN_INTERVAL_MS)) {
            return;
        }
        AppOpenAd ad = appOpenAd;
        appOpenAd = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                AdManager.setShowingFullscreen(application, false);
                fetchAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                AdManager.setShowingFullscreen(application, false);
                fetchAd();
            }
        });
        AdManager.setShowingFullscreen(application, true);
        ad.show(currentActivity);
    }

    public void fetchAd() {
        if (loading || isAdAvailable() || !AdManager.isReady() || AdManager.isAdFree(application)) return;
        loading = true;
        AppOpenAd.load(application, AD_UNIT_ID, new AdRequest.Builder().build(), new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                loading = false;
                appOpenAd = ad;
                loadTime = System.currentTimeMillis();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                loading = false;
            }
        });
    }

    private boolean isAdAvailable() {
        return appOpenAd != null && System.currentTimeMillis() - loadTime < AD_EXPIRY_MS;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (currentActivity == activity) currentActivity = null;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }
}
