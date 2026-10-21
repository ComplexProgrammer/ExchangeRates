package exchangerates.complexprogrammer.uz;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import exchangerates.complexprogrammer.uz.ads.AdManager;
import exchangerates.complexprogrammer.uz.models.HistoryPoint;
import exchangerates.complexprogrammer.uz.models.RateAlert;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.AppPrefs;
import exchangerates.complexprogrammer.uz.services.FlagLoader;
import exchangerates.complexprogrammer.uz.services.Formatters;
import exchangerates.complexprogrammer.uz.services.LocaleHelper;
import exchangerates.complexprogrammer.uz.services.RatesRepository;
import exchangerates.complexprogrammer.uz.ui.LineChartView;
import exchangerates.complexprogrammer.uz.work.Notifier;
import exchangerates.complexprogrammer.uz.widget.RatesWidgetProvider;

/** One currency: rate summary, two-way converter, history chart, favorite, alert and share. */
public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_RATE = "rate";

    private Response rate;
    private TextInputEditText topInput, bottomInput;
    private TextInputLayout topLayout, bottomLayout;
    private LineChartView chart;
    private View chartProgress, chartError;
    private TextView periodChange;
    private boolean reversed; // false: currency → so'm, true: so'm → currency
    private boolean updating;
    private int historyRequest;

    private final ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rate = (Response) getIntent().getSerializableExtra(EXTRA_RATE);
        if (rate == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(rate.getCcy());
            getSupportActionBar().setSubtitle(rate.getName(LocaleHelper.getContentLanguage(this)));
        }

        bindSummary();
        bindConverter();
        bindChart();

        AdManager.init(this, () -> {
            AdManager.loadBanner(this, findViewById(R.id.ad_container));
            loadNativeAd();
        });
    }

    private void bindSummary() {
        FlagLoader.load(findViewById(R.id.flag), rate.getCcy());
        ((TextView) findViewById(R.id.nominal)).setText(Formatters.nominal(rate) + " =");
        ((TextView) findViewById(R.id.rate)).setText(Formatters.moneyWithUnit(this, rate.getRateValue()));

        TextView diff = findViewById(R.id.diff);
        int color = ContextCompat.getColor(this, Formatters.trendColor(rate.getDiffValue()));
        diff.setText(Formatters.change(this, rate));
        diff.setTextColor(color);
        Drawable rawIcon = androidx.appcompat.content.res.AppCompatResources.getDrawable(this, Formatters.trendIcon(rate.getDiffValue()));
        if (rawIcon != null) {
            Drawable icon = DrawableCompat.wrap(rawIcon.mutate());
            DrawableCompat.setTint(icon, color);
            diff.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        } else {
            diff.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        }

        ((TextView) findViewById(R.id.date)).setText(getString(R.string.rates_for_date, rate.getDate())
                + " · " + getString(R.string.rates_source));
    }

    // Converter

    private void bindConverter() {
        topLayout = findViewById(R.id.top_layout);
        bottomLayout = findViewById(R.id.bottom_layout);
        topInput = findViewById(R.id.top_input);
        bottomInput = findViewById(R.id.bottom_input);
        ImageView swap = findViewById(R.id.swap);

        applyConverterLabels();
        topInput.setText(Formatters.plainAmount(this, reversed ? rate.getUnitRate() : 1));
        convert(topInput, bottomInput, true);

        topInput.addTextChangedListener(watcher(() -> convert(topInput, bottomInput, true)));
        bottomInput.addTextChangedListener(watcher(() -> convert(bottomInput, topInput, false)));
        swap.setOnClickListener(v -> {
            reversed = !reversed;
            applyConverterLabels();
            // Keep the result on top so the user continues from it.
            CharSequence bottom = bottomInput.getText();
            updating = true;
            topInput.setText(bottom);
            updating = false;
            convert(topInput, bottomInput, true);
        });
    }

    private void applyConverterLabels() {
        String currency = rate.getCcy();
        String sum = getString(R.string.sum);
        topLayout.setSuffixText(reversed ? sum : currency);
        bottomLayout.setSuffixText(reversed ? currency : sum);
    }

    /** {@code fromTop}: the top field was edited. */
    private void convert(TextInputEditText source, TextInputEditText target, boolean fromTop) {
        if (updating) return;
        double amount = Formatters.parseAmount(this, String.valueOf(source.getText()));
        String result = "";
        if (!Double.isNaN(amount)) {
            boolean currencyToSum = fromTop != reversed;
            double value = currencyToSum ? amount * rate.getUnitRate() : amount / rate.getUnitRate();
            result = Formatters.plainAmount(this, value);
        }
        updating = true;
        target.setText(result);
        updating = false;
    }

    private static TextWatcher watcher(Runnable onChange) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                onChange.run();
            }
        };
    }

    // History chart

    private void bindChart() {
        chart = findViewById(R.id.chart);
        chartProgress = findViewById(R.id.chart_progress);
        chartError = findViewById(R.id.chart_error);
        periodChange = findViewById(R.id.period_change);
        ChipGroup periods = findViewById(R.id.periods);
        periods.setOnCheckedStateChangeListener((group, ids) -> loadHistory(periodFor(group.getCheckedChipId())));
        loadHistory(periodFor(periods.getCheckedChipId()));
    }

    private RatesRepository.Period periodFor(int chipId) {
        if (chipId == R.id.period_week) return RatesRepository.Period.WEEK;
        if (chipId == R.id.period_year) return RatesRepository.Period.YEAR;
        return RatesRepository.Period.MONTH;
    }

    private void loadHistory(RatesRepository.Period period) {
        int request = ++historyRequest;
        chart.setPoints(null);
        chartError.setVisibility(View.GONE);
        periodChange.setVisibility(View.GONE);
        chartProgress.setVisibility(View.VISIBLE);
        RatesRepository.history(this, rate.getCcy(), period, points -> {
            if (request != historyRequest || isFinishing()) return;
            chartProgress.setVisibility(View.GONE);
            if (points == null) {
                chartError.setVisibility(View.VISIBLE);
                return;
            }
            chart.setPoints(points);
            showPeriodChange(points);
        });
    }

    private void showPeriodChange(List<HistoryPoint> points) {
        double first = points.get(0).rate;
        double last = points.get(points.size() - 1).rate;
        double percent = first == 0 ? 0 : (last - first) / first * 100;
        periodChange.setText(getString(R.string.period_change,
                Formatters.signed(this, last - first) + " (" + Formatters.signedPercent(this, percent) + ")"));
        periodChange.setTextColor(ContextCompat.getColor(this, Formatters.trendColor(last - first)));
        periodChange.setVisibility(View.VISIBLE);
    }

    // Menu: favorite, alert, share

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        updateFavoriteIcon(menu.findItem(R.id.action_favorite));
        return true;
    }

    private void updateFavoriteIcon(MenuItem item) {
        boolean favorite = AppPrefs.getFavorites(this).contains(rate.getCcy());
        item.setIcon(favorite ? R.drawable.ic_star : R.drawable.ic_star_border);
        item.setTitle(getString(favorite ? R.string.favorites_remove : R.string.favorites_add, rate.getCcy()));
        if (favorite && item.getIcon() != null) {
            Drawable icon = DrawableCompat.wrap(item.getIcon().mutate());
            DrawableCompat.setTint(icon, ContextCompat.getColor(this, R.color.favorite));
            item.setIcon(icon);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_favorite) {
            AppPrefs.toggleFavorite(this, rate.getCcy());
            updateFavoriteIcon(item);
            List<Response> cached = AppPrefs.getCachedRates(this);
            if (cached != null) RatesWidgetProvider.updateAll(getApplicationContext(), cached);
            return true;
        } else if (id == R.id.action_alert) {
            showAlertDialog();
            return true;
        } else if (id == R.id.action_share) {
            share();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlertDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_alert, null);
        RadioGroup direction = view.findViewById(R.id.direction);
        TextInputLayout valueLayout = view.findViewById(R.id.value_layout);
        TextInputEditText value = view.findViewById(R.id.value);
        value.setText(Formatters.plainAmount(this, rate.getRateValue()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.alert_dialog_title, Formatters.nominal(rate)))
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double threshold = Formatters.parseAmount(this, String.valueOf(value.getText()));
            if (Double.isNaN(threshold) || threshold <= 0) {
                valueLayout.setError(getString(R.string.alert_invalid));
                return;
            }
            boolean above = direction.getCheckedRadioButtonId() == R.id.above;
            AppPrefs.addAlert(this, new RateAlert(rate.getCcy(), above, threshold));
            Toast.makeText(this, R.string.alert_saved, Toast.LENGTH_SHORT).show();
            requestNotificationPermissionIfNeeded();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && !Notifier.canNotify(this)) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void share() {
        String text = getString(R.string.share_text,
                Formatters.nominal(rate),
                Formatters.moneyWithUnit(this, rate.getRateValue()),
                Formatters.change(this, rate),
                rate.getDate());
        Intent intent = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    // Ads

    private void loadNativeAd() {
        if (AdManager.isAdFree(this)) return;
        TemplateView template = findViewById(R.id.native_ad);
        new AdLoader.Builder(this, AdManager.NATIVE_ID)
                .forNativeAd(nativeAd -> {
                    if (isDestroyed()) {
                        nativeAd.destroy();
                        return;
                    }
                    template.setNativeAd(nativeAd);
                    template.setVisibility(View.VISIBLE);
                })
                .build()
                .loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onDestroy() {
        TemplateView template = findViewById(R.id.native_ad);
        if (template != null) template.destroyNativeAd();
        super.onDestroy();
    }
}
