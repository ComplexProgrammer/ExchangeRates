package exchangerates.complexprogrammer.uz.ads;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.concurrent.TimeUnit;

import exchangerates.complexprogrammer.uz.services.AppPrefs;

/**
 * Single place for ad policy, so ads stay predictable for users:
 * - nothing is requested before consent is resolved;
 * - banners use the refresh rate set in the AdMob console (no manual reload loops);
 * - an interstitial is shown at most on every {@link #INTERSTITIAL_EVERY}-th currency opened
 *   and never sooner than {@link #MIN_FULLSCREEN_GAP_MS} after the previous full-screen ad;
 * - a rewarded video turns all ads off for 24 hours.
 */
public class AdManager {
    private static final String BANNER_ID = "ca-app-pub-7773722896374259/6894028173";
    private static final String INTERSTITIAL_ID = "ca-app-pub-7773722896374259/4469855441";
    private static final String REWARDED_ID = "ca-app-pub-7773722896374259/5549680626";
    public static final String NATIVE_ID = "ca-app-pub-7773722896374259/3121724539";

    private static final int INTERSTITIAL_EVERY = 4;
    static final long MIN_FULLSCREEN_GAP_MS = TimeUnit.MINUTES.toMillis(2);
    private static final long AD_FREE_DURATION_MS = TimeUnit.HOURS.toMillis(24);

    private static boolean initialized;
    private static boolean showingFullscreen;
    private static InterstitialAd interstitial;
    private static RewardedAd rewarded;
    private static boolean loadingInterstitial;
    private static boolean loadingRewarded;

    /** Resolves consent (UMP) and initializes the SDK once. {@code onReady} runs only if ads may be requested. */
    public static void init(Activity activity, Runnable onReady) {
        if (activity == null || isActivityDead(activity)) return;
        ConsentManager.gather(activity, () -> {
            if (isActivityDead(activity)) return;
            if (!initialized) {
                initialized = true;
                try {
                    MobileAds.initialize(activity.getApplicationContext(), status -> {
                    });
                } catch (Exception ignored) {
                }
            }
            if (!isActivityDead(activity) && onReady != null) {
                onReady.run();
            }
        });
    }

    public static boolean isReady() {
        return initialized;
    }

    public static boolean isAdFree(Context context) {
        return System.currentTimeMillis() < AppPrefs.getAdFreeUntil(context);
    }

    public static boolean isShowingFullscreen() {
        return showingFullscreen;
    }

    public static void setShowingFullscreen(Context context, boolean showing) {
        showingFullscreen = showing;
        if (showing) AppPrefs.setLastFullscreenAd(context, System.currentTimeMillis());
    }

    public static boolean fullscreenGapPassed(Context context, long gapMs) {
        return System.currentTimeMillis() - AppPrefs.getLastFullscreenAd(context) > gapMs;
    }

    // Banner

    public static void loadBanner(Activity activity, FrameLayout container) {
        if (activity == null || container == null || isActivityDead(activity)) return;
        if (!initialized || isAdFree(activity)) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
            return;
        }
        if (container.getChildCount() > 0) return;
        try {
            container.setVisibility(View.VISIBLE);
            AdView adView = new AdView(activity);
            adView.setAdUnitId(BANNER_ID);
            adView.setAdSize(adaptiveSize(activity));
            container.addView(adView);
            adView.loadAd(new AdRequest.Builder().build());
        } catch (Exception e) {
            container.setVisibility(View.GONE);
        }
    }

    private static boolean isActivityDead(Activity activity) {
        return activity.isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && activity.isDestroyed());
    }

    private static AdSize adaptiveSize(Activity activity) {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int widthDp = (int) (metrics.widthPixels / metrics.density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp);
    }

    // Interstitial

    public static void preloadInterstitial(Context context) {
        if (!initialized || isAdFree(context) || interstitial != null || loadingInterstitial) return;
        loadingInterstitial = true;
        InterstitialAd.load(context.getApplicationContext(), INTERSTITIAL_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        loadingInterstitial = false;
                        interstitial = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        loadingInterstitial = false;
                        interstitial = null;
                    }
                });
    }

    /**
     * Called when the user opens a currency. Runs {@code next} right away, or after an
     * interstitial if one is due and already loaded; the user never waits for an ad to load.
     */
    public static void openWithOptionalInterstitial(Activity activity, Runnable next) {
        int opens = AppPrefs.incrementDetailOpens(activity);
        boolean due = opens % INTERSTITIAL_EVERY == 0 && fullscreenGapPassed(activity, MIN_FULLSCREEN_GAP_MS);
        InterstitialAd ad = interstitial;
        if (!due || ad == null || isAdFree(activity) || showingFullscreen) {
            next.run();
            preloadInterstitial(activity);
            return;
        }
        interstitial = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                showingFullscreen = false;
                next.run();
                preloadInterstitial(activity);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                showingFullscreen = false;
                next.run();
                preloadInterstitial(activity);
            }
        });
        setShowingFullscreen(activity, true);
        ad.show(activity);
    }

    // Rewarded: "24 hours without ads"

    public static void preloadRewarded(Context context) {
        if (!initialized || rewarded != null || loadingRewarded) return;
        loadingRewarded = true;
        RewardedAd.load(context.getApplicationContext(), REWARDED_ID, new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        loadingRewarded = false;
                        rewarded = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        loadingRewarded = false;
                        rewarded = null;
                    }
                });
    }

    /** Returns false when no video is loaded yet. */
    public static boolean showRewardedForAdFree(Activity activity, Runnable onRewarded) {
        RewardedAd ad = rewarded;
        if (ad == null) {
            preloadRewarded(activity);
            return false;
        }
        rewarded = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                showingFullscreen = false;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                showingFullscreen = false;
            }
        });
        setShowingFullscreen(activity, true);
        ad.show(activity, reward -> {
            AppPrefs.setAdFreeUntil(activity, System.currentTimeMillis() + AD_FREE_DURATION_MS);
            interstitial = null;
            onRewarded.run();
        });
        return true;
    }
}
