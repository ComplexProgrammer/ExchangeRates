package exchangerates.complexprogrammer.uz.ads;

import android.app.Activity;
import android.os.Build;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/** Google UMP consent flow; required before requesting ads in regions with privacy regulations. */
public class ConsentManager {

    public static void gather(Activity activity, Runnable onCanRequestAds) {
        if (activity == null || isActivityDead(activity)) return;

        try {
            ConsentInformation info = UserMessagingPlatform.getConsentInformation(activity);
            boolean[] done = {false};
            Runnable finish = () -> activity.runOnUiThread(() -> {
                if (isActivityDead(activity)) return;
                if (!done[0] && info.canRequestAds()) {
                    done[0] = true;
                    onCanRequestAds.run();
                }
            });

            info.requestConsentInfoUpdate(
                    activity,
                    new ConsentRequestParameters.Builder().build(),
                    () -> activity.runOnUiThread(() -> {
                        if (isActivityDead(activity)) return;
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                activity,
                                error -> activity.runOnUiThread(finish)
                        );
                    }),
                    error -> activity.runOnUiThread(finish)
            );

            // Consent from a previous session lets ads start without waiting for the network.
            activity.runOnUiThread(finish);
        } catch (Exception e) {
            // If consent gathering fails for any reason, try running onCanRequestAds safely
            activity.runOnUiThread(() -> {
                if (!isActivityDead(activity)) {
                    onCanRequestAds.run();
                }
            });
        }
    }

    public static boolean isPrivacyOptionsRequired(Activity activity) {
        if (activity == null || isActivityDead(activity)) return false;
        try {
            return UserMessagingPlatform.getConsentInformation(activity).getPrivacyOptionsRequirementStatus()
                    == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
        } catch (Exception e) {
            return false;
        }
    }

    public static void showPrivacyOptions(Activity activity) {
        if (activity == null || isActivityDead(activity)) return;
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity, error -> {
            });
        } catch (Exception ignored) {
        }
    }

    private static boolean isActivityDead(Activity activity) {
        return activity.isFinishing() || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed());
    }
}
