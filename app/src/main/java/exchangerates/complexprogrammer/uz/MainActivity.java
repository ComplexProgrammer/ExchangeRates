package exchangerates.complexprogrammer.uz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyAdSize;
import com.adcolony.sdk.AdColonyAdView;
import com.adcolony.sdk.AdColonyAdViewListener;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyZone;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.models.ResponseData;
import exchangerates.complexprogrammer.uz.services.NetworkUtil;
import exchangerates.complexprogrammer.uz.services.ApiClient;
import exchangerates.complexprogrammer.uz.services.ChangeLanguage;
import exchangerates.complexprogrammer.uz.services.Utils;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements OnUserEarnedRewardListener {

    private List<Response> ResponseList =new ArrayList<>();
    GridView gridView;
    private final String APP_ID = "app47ccb4f1349341b5a2";
    private final String ZONE_ID = "vz4b1cba6afa79468984";
    private AdColonyAdViewListener listener;
    private AdColonyAdOptions adOptions;
    private RelativeLayout adContainer;
    private AdColonyAdView _adView;
    private static final String AD_UNIT_ID = "ca-app-pub-7773722896374259/4469855441";
    private static final String TAG = "MainActivity";
    private InterstitialAd interstitialAd;
    private RewardedInterstitialAd rewardedInterstitialAd;
    public Boolean load_ad=true;
    public void loadAd(int position) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                AD_UNIT_ID,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        interstitialAd.show(MainActivity.this);
                        //MainActivity.this.interstitialAd = interstitialAd;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        startActivity(new Intent(MainActivity.this,ViewByItem.class).putExtra("ResponseData", ResponseList.get(position)));
                                        MainActivity.this.interstitialAd = null;

                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        startActivity(new Intent(MainActivity.this,ViewByItem.class).putExtra("ResponseData", ResponseList.get(position)));
                        Log.i(TAG, loadAdError.getMessage());
                        interstitialAd = null;

                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
//                        Toast.makeText(
//                                MainActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT)
//                                .show();
                    }
                });

    }
    public void loadRewardedInterstitialAd(int position){
        RewardedInterstitialAd.load(MainActivity.this, "ca-app-pub-7773722896374259/4867703081",
                new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        Log.d(TAG, "Ad was loaded.");
                        rewardedInterstitialAd = ad;
                        rewardedInterstitialAd.show(/* Activity */ MainActivity.this,/*
    OnUserEarnedRewardListener */ MainActivity.this);
                        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                startActivity(new Intent(MainActivity.this,ViewByItem.class).putExtra("ResponseData", ResponseList.get(position)));
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                            }
                        });
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        Log.d(TAG, loadAdError.toString());
                        rewardedInterstitialAd = null;
                        startActivity(new Intent(MainActivity.this,ViewByItem.class).putExtra("ResponseData", ResponseList.get(position)));
                    }
                });
    }

    public void loadBannerAd(){
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadBannerAd();
            }
        }, 10000);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChangeLanguage.setLocal(this,ChangeLanguage.getLangCode(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
//        adContainer = findViewById(R.id.ad_container);
//        AdColonyAppOptions appOptions = new AdColonyAppOptions();
//
//        // Configure AdColony in your launching Activity's onCreate() method so that cached ads can
//        // be available as soon as possible.
//        AdColony.configure(this, appOptions, APP_ID);
//
//        listener = new AdColonyAdViewListener() {
//            @Override
//            public void onRequestFilled(AdColonyAdView adColonyAdView) {
//                Log.d(TAG, "onRequestFilled");
//                adContainer.addView(adColonyAdView);
//                _adView = adColonyAdView;
//
//            }
//
//            @Override
//            public void onRequestNotFilled(AdColonyZone zone) {
//                super.onRequestNotFilled(zone);
//                Log.d(TAG, "onRequestNotFilled");
//            }
//
//            @Override
//            public void onOpened(AdColonyAdView ad) {
//                super.onOpened(ad);
//                Log.d(TAG, "onOpened");
//            }
//
//            @Override
//            public void onClosed(AdColonyAdView ad) {
//                super.onClosed(ad);
//                Log.d(TAG, "onClosed");
//            }
//
//            @Override
//            public void onClicked(AdColonyAdView ad) {
//                super.onClicked(ad);
//                Log.d(TAG, "onClicked");
//            }
//
//            @Override
//            public void onLeftApplication(AdColonyAdView ad) {
//                super.onLeftApplication(ad);
//                Log.d(TAG, "onLeftApplication");
//            }
//        };
//        adOptions = new AdColonyAdOptions();

        //Request Ad
//        AdColony.requestAdView(ZONE_ID, listener, AdColonyAdSize.BANNER, adOptions);

        loadBannerAd();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        ColorDrawable colorDrawable
//                = new ColorDrawable(Color.parseColor("#90ee90"));
                = new ColorDrawable(getResources().getColor(R.color.SteelBlue));
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        boolean status = NetworkUtil.getConnectivityStatusString(this);
        if(!status){
            startActivity(new Intent(this, NoInternetActivity.class));
        }
        else {
            gridView=findViewById(R.id.gridView);
            LoadData();
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    load_ad=false;
//                    loadRewardedInterstitialAd(position);
                    loadAd(position);
                    String message = ResponseList.get(position).getCcy();
//                    Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    public void LoadData(){
        Call<ResponseData> _response = ApiClient.getInterface().GetExchangeRates();
        _response.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, retrofit2.Response<ResponseData> response) {
                Log.e("onResponse","onResponse");

                if(response.isSuccessful()){
                    ResponseList =response.body().data;
                    Log.e("isSuccessful","isSuccessful");

                    CustomAdapter customAdapter=new CustomAdapter(ResponseList,getApplicationContext());
                    gridView.setAdapter(customAdapter);

                }
                else if (response.errorBody() != null) {
                    // Get response errorBody
                    String errorBody = response.errorBody().toString();
                    Log.e("errorBody",errorBody);
                    Toast.makeText(getBaseContext(),errorBody,Toast.LENGTH_LONG).show();

                }
                else {
                    String message="Xatolik yuz berdi. keyinroq yana urinib ko'rig";
                    Toast.makeText(getBaseContext(),message,Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                startActivity(new Intent(MainActivity.this, ServerErrorActivity.class));

            }
        });
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

    }

    public class CustomAdapter extends BaseAdapter {
        private final List<Response> ResponseList;
        private final Context context;
        private final LayoutInflater layoutInflater;

        public CustomAdapter(List<Response> ResponseList, Context context) {
            this.ResponseList = ResponseList;
            this.context = context;
            this.layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return ResponseList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=layoutInflater.inflate(R.layout.row_grid_items,parent,false);
            }
            ImageView imageView = convertView.findViewById(R.id.imageview);
            ImageView imageDiff = convertView.findViewById(R.id.imageDiff);
            ImageView country_flag = convertView.findViewById(R.id.country_flag);
            // url for .svg image
            String url = getString(R.string.file_url) + "/flag_image/UZS.svg";

            // this method will load svg
            // image in your imageview

            TextView Code=convertView.findViewById(R.id.Code);
            TextView Ccy=convertView.findViewById(R.id.Ccy);
            TextView CcyNm=convertView.findViewById(R.id.CcyNm);
            TextView Nominal=convertView.findViewById(R.id.Nominal);
            TextView Rate=convertView.findViewById(R.id.Rate);
            TextView Diff=convertView.findViewById(R.id.Diff);
            TextView Date=convertView.findViewById(R.id.Date);
            String country_flag_url = getString(R.string.file_url) + "/flag_image/"+ ResponseList.get(position).getCcy()+".svg";
            Utils.fetchSvg(context, country_flag_url, country_flag);
            Utils.fetchSvg(context, url, imageView);
            Code.setText(ResponseList.get(position).getCode());
            Ccy.setText(ResponseList.get(position).getCcy());
            String langCode=ChangeLanguage.getLangCode(getBaseContext());
            switch (langCode) {
                case "uz":
                    CcyNm.setText(ResponseList.get(position).getCcyNm_UZ());
                    break;
                case "uzc":
                    CcyNm.setText(ResponseList.get(position).getCcyNm_UZC());
                    break;
                case "en":
                    CcyNm.setText(ResponseList.get(position).getCcyNm_EN());
                    break;
                case "ru":
                    CcyNm.setText(ResponseList.get(position).getCcyNm_RU());
                    break;
            }
            Rate.setText(ResponseList.get(position).getRate()+" "+getString(R.string.sum));
            Nominal.setText(ResponseList.get(position).getNominal());
            Float diff=Float.parseFloat(ResponseList.get(position).getDiff());
            if(diff>0){
                Diff.setText("+"+ diff);
                Diff.setTextColor(getResources().getColor(R.color.Green));
                imageDiff.setImageResource(R.drawable.ic_baseline_trending_up_24);
            }
            if(diff==0){
                Diff.setText(diff.toString());
                Diff.setTextColor(getResources().getColor(R.color.Gray));
                imageDiff.setImageResource(R.drawable.minus);
            }
            if(diff<0){
                Diff.setText(diff.toString());
                Diff.setTextColor(getResources().getColor(R.color.Red));
                imageDiff.setImageResource(R.drawable.ic_baseline_trending_down_24);
            }

            Date.setText(ResponseList.get(position).getDate());

            return convertView;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.language_menu, menu);
        String language=ChangeLanguage.getLangCode(this);

        MenuItem uzbek = menu.findItem(R.id.uzbek);
        MenuItem uzbekc = menu.findItem(R.id.uzbekc);
        MenuItem english = menu.findItem(R.id.english);
        MenuItem russian = menu.findItem(R.id.russian);
        switch (language){
            case "uz": uzbek.setVisible(false); break;
            case "uzc": uzbekc.setVisible(false); break;
            case "en": english.setVisible(false); break;
            case "ru": russian.setVisible(false); break;

        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.telegram:
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/uzb_valyuta_kurslari_bot"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "No application can handle this request."
                            + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                return true;
            case R.id.about:
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View aboutView = factory.inflate(R.layout.about, null);
                if(NetworkUtil.getConnectivityStatusString(getBaseContext())){
                    AdView mAdView = aboutView.findViewById(R.id.adView);
                    AdRequest adRequest = new AdRequest.Builder()
                            .build();
                    mAdView.loadAd(adRequest);
                }
                TextView versionLabel = aboutView.findViewById(R.id.version_label);
                String versionName = getAppVersionName(getApplicationContext());
                versionLabel.setText(MainActivity.this.getString(R.string.version, versionName));
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setView(aboutView)
                        .setPositiveButton("OK", null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.uzbek:
                ChangeLanguage.setLocal(this,"uz");
                onBackPressed();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.uzbekc:
                ChangeLanguage.setLocal(this,"uzc");
                onBackPressed();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.english:
                ChangeLanguage.setLocal(this,"en");
                onBackPressed();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.russian:
                ChangeLanguage.setLocal(this,"ru");
                onBackPressed();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onBackPressed() {
        super.finish();
    }
}