package exchangerates.complexprogrammer.uz;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import exchangerates.complexprogrammer.uz.ads.AdManager;
import exchangerates.complexprogrammer.uz.ads.ConsentManager;
import exchangerates.complexprogrammer.uz.models.RateAlert;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.AppPrefs;
import exchangerates.complexprogrammer.uz.services.Formatters;
import exchangerates.complexprogrammer.uz.services.LocaleHelper;
import exchangerates.complexprogrammer.uz.work.Notifier;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
        AdManager.init(this, () -> AdManager.preloadRewarded(this));
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (!granted) {
                        SwitchPreferenceCompat daily = findPreference(AppPrefs.KEY_DAILY);
                        if (daily != null) daily.setChecked(false);
                        Toast.makeText(requireContext(), R.string.notification_permission_denied, Toast.LENGTH_LONG).show();
                    }
                });

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            Context context = requireContext();

            Preference language = findPreference(AppPrefs.KEY_LANGUAGE);
            language.setOnPreferenceChangeListener((pref, value) -> {
                LocaleHelper.setLanguage(context, (String) value);
                return true;
            });

            Preference theme = findPreference(AppPrefs.KEY_THEME);
            theme.setOnPreferenceChangeListener((pref, value) -> {
                MyApplication.applyTheme((String) value);
                return true;
            });

            Preference daily = findPreference(AppPrefs.KEY_DAILY);
            daily.setOnPreferenceChangeListener((pref, value) -> {
                if ((Boolean) value) {
                    // Start from today's rates so the first notification is for genuinely new ones.
                    List<Response> cached = AppPrefs.getCachedRates(context);
                    if (cached != null) AppPrefs.setLastNotifiedDate(context, cached.get(0).getDate());
                    if (Build.VERSION.SDK_INT >= 33 && !Notifier.canNotify(context)) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                }
                return true;
            });

            findPreference(AppPrefs.KEY_ALERTS).setOnPreferenceClickListener(pref -> {
                showAlerts();
                return true;
            });

            findPreference(AppPrefs.KEY_AD_FREE).setOnPreferenceClickListener(pref -> {
                if (AdManager.isAdFree(context)) return true;
                boolean shown = AdManager.showRewardedForAdFree(requireActivity(), this::updateSummaries);
                if (!shown) Toast.makeText(context, R.string.ad_not_ready, Toast.LENGTH_SHORT).show();
                return true;
            });

            Preference privacy = findPreference("privacy");
            if (privacy != null) {
                privacy.setVisible(ConsentManager.isPrivacyOptionsRequired(requireActivity()));
                privacy.setOnPreferenceClickListener(pref -> {
                    ConsentManager.showPrivacyOptions(requireActivity());
                    return true;
                });
            }

            Preference privacyPolicy = findPreference("privacy_policy");
            if (privacyPolicy != null) {
                privacyPolicy.setOnPreferenceClickListener(pref ->
                        open(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url)))));
            }

            findPreference("telegram").setOnPreferenceClickListener(pref ->
                    open(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ComplexProgrammer"))));
            findPreference("email").setOnPreferenceClickListener(pref ->
                    open(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getString(R.string.email)))
                            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " + versionName(context))));
            findPreference("site").setOnPreferenceClickListener(pref ->
                    open(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + getString(R.string.site_url)))));
            findPreference("version").setSummary(getString(R.string.version, versionName(context)));
        }

        @Override
        public void onResume() {
            super.onResume();
            updateSummaries();
        }

        private void updateSummaries() {
            Context context = getContext();
            if (context == null) return;
            int alerts = AppPrefs.getAlerts(context).size();
            findPreference(AppPrefs.KEY_ALERTS).setSummary(alerts == 0
                    ? getString(R.string.pref_alerts_empty)
                    : getString(R.string.pref_alerts_summary, alerts));
            findPreference(AppPrefs.KEY_AD_FREE).setSummary(AdManager.isAdFree(context)
                    ? getString(R.string.pref_ad_free_active, Formatters.dateTime(context, AppPrefs.getAdFreeUntil(context)))
                    : getString(R.string.pref_ad_free_summary));
        }

        /** Lists active alerts; picking one offers to delete it. */
        private void showAlerts() {
            Context context = requireContext();
            List<RateAlert> alerts = AppPrefs.getAlerts(context);
            if (alerts.isEmpty()) {
                Toast.makeText(context, R.string.pref_alerts_empty, Toast.LENGTH_LONG).show();
                return;
            }
            String[] items = new String[alerts.size()];
            for (int i = 0; i < alerts.size(); i++) {
                RateAlert alert = alerts.get(i);
                items[i] = getString(alert.above ? R.string.alert_item_above : R.string.alert_item_below,
                        alert.ccy, Formatters.moneyWithUnit(context, alert.value));
            }
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.pref_alerts)
                    .setItems(items, (dialog, which) -> new MaterialAlertDialogBuilder(context)
                            .setMessage(items[which])
                            .setPositiveButton(R.string.delete, (d, w) -> {
                                List<RateAlert> current = AppPrefs.getAlerts(context);
                                if (which < current.size()) current.remove(which);
                                AppPrefs.setAlerts(context, current);
                                updateSummaries();
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        private boolean open(Intent intent) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
            return true;
        }

        private static String versionName(Context context) {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                return "";
            }
        }
    }
}
