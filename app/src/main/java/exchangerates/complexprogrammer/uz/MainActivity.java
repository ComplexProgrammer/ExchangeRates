package exchangerates.complexprogrammer.uz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import exchangerates.complexprogrammer.uz.ads.AdManager;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.AppPrefs;
import exchangerates.complexprogrammer.uz.services.LocaleHelper;
import exchangerates.complexprogrammer.uz.services.NetworkUtil;
import exchangerates.complexprogrammer.uz.services.RatesRepository;
import exchangerates.complexprogrammer.uz.ui.RatesAdapter;
import exchangerates.complexprogrammer.uz.widget.RatesWidgetProvider;
import exchangerates.complexprogrammer.uz.work.RatesWorker;

public class MainActivity extends AppCompatActivity implements RatesAdapter.Listener {
    private static final long MAX_SPLASH_MS = 2000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private RatesAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView list;
    private TextView header, offlineBanner, empty;
    private View errorState;
    private CircularProgressIndicator progress;
    private FrameLayout adContainer;

    private List<Response> rates = new ArrayList<>();
    private String query = "";
    private boolean keepSplash = true;
    private boolean loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        String migrated = AppPrefs.migrateLegacyLanguage(this);
        if (migrated != null) {
            // Recreates the activity in the language picked in the old version.
            LocaleHelper.setLanguage(this, migrated);
        }

        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        header = findViewById(R.id.header);
        offlineBanner = findViewById(R.id.offline_banner);
        empty = findViewById(R.id.empty);
        errorState = findViewById(R.id.error_state);
        progress = findViewById(R.id.progress);
        adContainer = findViewById(R.id.ad_container);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        list = findViewById(R.id.list);

        adapter = new RatesAdapter(this);
        list.setAdapter(adapter);
        swipeRefresh.setOnChildScrollUpCallback((parent, child) -> list.canScrollVertically(-1));
        swipeRefresh.setOnRefreshListener(this::refresh);
        findViewById(R.id.retry).setOnClickListener(v -> refresh());

        // Show saved rates instantly; the network refresh happens behind them.
        List<Response> cached = AppPrefs.getCachedRates(this);
        if (cached != null) {
            showRates(cached);
            keepSplash = false;
        }
        splash.setKeepOnScreenCondition(() -> keepSplash);
        handler.postDelayed(() -> keepSplash = false, MAX_SPLASH_MS);

        refresh();

        AdManager.init(this, () -> {
            AdManager.loadBanner(this, adContainer);
            AdManager.preloadInterstitial(this);
            MyApplication.onAdsReady();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setFavorites(AppPrefs.getFavorites(this));
        if (!rates.isEmpty()) render();
        // Hides the banner once the user earned the ad-free period.
        if (AdManager.isReady()) AdManager.loadBanner(this, adContainer);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null && searchItem.getActionView() instanceof SearchView) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint(getString(R.string.search_hint));
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String text) {
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String text) {
                    query = text;
                    render();
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        if (loading) return;
        loading = true;
        if (rates.isEmpty()) {
            progress.setVisibility(View.VISIBLE);
            errorState.setVisibility(View.GONE);
        }
        RatesRepository.fetch(this, new RatesRepository.RatesCallback() {
            @Override
            public void onRates(@NonNull List<Response> fresh) {
                loading = false;
                keepSplash = false;
                if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed())) return;
                swipeRefresh.setRefreshing(false);
                progress.setVisibility(View.GONE);
                offlineBanner.setVisibility(View.GONE);
                showRates(fresh);
                RatesWorker.onRatesUpdated(getApplicationContext(), fresh);
            }

            @Override
            public void onError() {
                loading = false;
                keepSplash = false;
                if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed())) return;
                swipeRefresh.setRefreshing(false);
                progress.setVisibility(View.GONE);
                if (rates.isEmpty()) {
                    errorState.setVisibility(View.VISIBLE);
                    TextView errorText = findViewById(R.id.error_text);
                    if (errorText != null) {
                        errorText.setText(NetworkUtil.isOnline(MainActivity.this)
                                ? R.string.error_loading : R.string.no_internet_is_available);
                    }
                } else {
                    String date = rates.get(0).getDate();
                    offlineBanner.setText(getString(R.string.offline_banner, date != null ? date : ""));
                    offlineBanner.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showRates(List<Response> data) {
        if (data == null || data.isEmpty()) return;
        rates = data;
        errorState.setVisibility(View.GONE);
        String date = data.get(0).getDate();
        header.setText(getString(R.string.rates_for_date, date != null ? date : "") + " · " + getString(R.string.rates_source));
        render();
    }

    private void render() {
        List<Response> filtered = new ArrayList<>();
        for (Response rate : RatesRepository.sortFavoritesFirst(rates, AppPrefs.getFavorites(this))) {
            if (rate.matches(query)) filtered.add(rate);
        }
        adapter.submitList(filtered);
        empty.setVisibility(filtered.isEmpty() && !rates.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRateClick(Response rate) {
        AdManager.openWithOptionalInterstitial(this, () ->
                startActivity(new Intent(this, DetailActivity.class).putExtra(DetailActivity.EXTRA_RATE, rate)));
    }

    @Override
    public void onFavoriteClick(Response rate) {
        AppPrefs.toggleFavorite(this, rate.getCcy());
        adapter.setFavorites(AppPrefs.getFavorites(this));
        render();
        RatesWidgetProvider.updateAll(getApplicationContext(), rates);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
