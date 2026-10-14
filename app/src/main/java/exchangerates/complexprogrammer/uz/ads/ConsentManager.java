package exchangerates.complexprogrammer.uz.ads;

import android.app.Activity;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/** Google UMP consent flow; required before requesting ads in regions with privacy regulations. */
public class ConsentManager {

    public static void gather(Activity activity, Runnable onCanRequestAds) {
        ConsentInformation info = UserMessagingPlatform.getConsentInformation(activity);
        boolean[] done = {false};
        Runnable finish = () -> {
            if (!done[0] && info.canRequestAds()) {
                done[0] = true;
                onCanRequestAds.run();
            }
        };
        info.requestConsentInfoUpdate(activity, new ConsentRequestParameters.Builder().build(),
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, error -> finish.run()),
                error -> finish.run());
        // Consent from a previous session lets ads start without waiting for the network.
        finish.run();
    }

    public static boolean isPrivacyOptionsRequired(Activity activity) {
        return UserMessagingPlatform.getConsentInformation(activity).getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public static void showPrivacyOptions(Activity activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, error -> {
        });
    }
}
