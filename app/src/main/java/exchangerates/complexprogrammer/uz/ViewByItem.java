package exchangerates.complexprogrammer.uz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import java.util.Objects;

import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.ChangeLanguage;
import exchangerates.complexprogrammer.uz.services.Utils;

public class ViewByItem extends AppCompatActivity implements OnUserEarnedRewardListener  {

    Response Response;
    ImageView imageDiff,flag_image,uzb_flag_image;
    TextView Code,Ccy,CcyNm,Nominal,Rate,Diff,Date,sum;
    ImageView mIcon;
    Button mFollow;
    RelativeLayout relativeLayout;
    private final String APP_ID = "app47ccb4f1349341b5a2";
    private final String ZONE_ID = "vz4b1cba6afa79468984";
    private AdColonyAdViewListener listener;
    private AdColonyAdOptions adOptions;
    private RelativeLayout adContainer;
    private static final String AD_UNIT_ID = "ca-app-pub-7773722896374259/4469855441";
    private static final String TAG = "MainActivity";
    private InterstitialAd interstitialAd;
    private RewardedAd mRewardedAd;
    private RewardedInterstitialAd rewardedInterstitialAd;
    public void loadAd() {
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
                        interstitialAd.show(ViewByItem.this);
                        //MainActivity.this.interstitialAd = interstitialAd;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        ViewByItem.this.interstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        ViewByItem.this.interstitialAd = null;
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
                        // Handle the error
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
    public void LoadRewardedAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-7773722896374259/5549680626",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.toString());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");
                        if (mRewardedAd != null) {
                            mRewardedAd.show(ViewByItem.this, new OnUserEarnedRewardListener() {
                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                    // Handle the reward.
                                    Log.d(TAG, "The user earned the reward.");
                                    int rewardAmount = rewardItem.getAmount();
                                    String rewardType = rewardItem.getType();
                                    Log.d("rewardAmount", rewardAmount+"");
                                    Log.d("rewardType", rewardType+"");
                                }
                            });
                            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent();
//                                    loadAd();
                                }
                            });
                        } else {
                            Log.d(TAG, "The rewarded ad wasn't ready yet.");
                        }
                    }
                });

    }
    public void loadRewardedInterstitialAd(){
        RewardedInterstitialAd.load(ViewByItem.this, "ca-app-pub-7773722896374259/4867703081",
                new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        Log.d(TAG, "Ad was loaded.");
                        rewardedInterstitialAd = ad;
                        rewardedInterstitialAd.show(/* Activity */ ViewByItem.this,/*
    OnUserEarnedRewardListener */ ViewByItem.this);
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        Log.d(TAG, loadAdError.toString());
                        rewardedInterstitialAd = null;
                    }
                });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_by_item);
        relativeLayout=findViewById(R.id.relativeLayout);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int SCREEN_WIDTH=displayMetrics.widthPixels;
        int SCREEN_HEIGHT=displayMetrics.heightPixels;
        relativeLayout.setMinimumHeight(SCREEN_HEIGHT);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                loadAd();
//                LoadRewardedAd();
                loadRewardedInterstitialAd();
            }
        }, 5000);
        adContainer = findViewById(R.id.ad_container);
        AdColonyAppOptions appOptions = new AdColonyAppOptions();

        // Configure AdColony in your launching Activity's onCreate() method so that cached ads can
        // be available as soon as possible.
        AdColony.configure(this, appOptions, APP_ID);

        listener = new AdColonyAdViewListener() {
            @Override
            public void onRequestFilled(AdColonyAdView adColonyAdView) {
                Log.d(TAG, "onRequestFilled");
                adContainer.addView(adColonyAdView);
//                relativeLayout.addView(adColonyAdView);

            }

            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                super.onRequestNotFilled(zone);
                Log.d(TAG, "onRequestNotFilled");
            }

            @Override
            public void onOpened(AdColonyAdView ad) {
                super.onOpened(ad);
                Log.d(TAG, "onOpened");
            }

            @Override
            public void onClosed(AdColonyAdView ad) {
                super.onClosed(ad);
                Log.d(TAG, "onClosed");
            }

            @Override
            public void onClicked(AdColonyAdView ad) {
                super.onClicked(ad);
                Log.d(TAG, "onClicked");
            }

            @Override
            public void onLeftApplication(AdColonyAdView ad) {
                super.onLeftApplication(ad);
                Log.d(TAG, "onLeftApplication");
            }
        };
        adOptions = new AdColonyAdOptions();

        //Request Ad
        AdColony.requestAdView(ZONE_ID, listener, AdColonyAdSize.BANNER, adOptions);
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-7773722896374259/3121724539")
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd NativeAd) {
                        // Show the ad.

                        TemplateView template = findViewById(R.id.my_template);
                        template.setNativeAd(NativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
//        adLoader.loadAd(new AdRequest.Builder().build());
        adLoader.loadAds(new AdRequest.Builder().build(), 5);
        mIcon = findViewById(R.id.ivProfile);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        RoundedBitmapDrawable mDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        mDrawable.setCircular(true);
        mDrawable.setColorFilter(ContextCompat.getColor(ViewByItem.this, R.color.colorAccent), PorterDuff.Mode.DST_OVER);
        mIcon.setImageDrawable(mDrawable);

        mFollow = findViewById(R.id.btnFollow);
        mFollow.setText(getString(R.string.back));
        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        Intent intent=getIntent();
        Code=findViewById(R.id.Code);
        Ccy=findViewById(R.id.Ccy);
        CcyNm=findViewById(R.id.CcyNm);
        Nominal=findViewById(R.id.Nominal);
        Rate=findViewById(R.id.Rate);
        sum=findViewById(R.id.sum);
        Diff=findViewById(R.id.Diff);
        imageDiff=findViewById(R.id.imageDiff);
        flag_image=findViewById(R.id.flag_image);
        uzb_flag_image=findViewById(R.id.uzb_flag_image);

        Date=findViewById(R.id.Date);
        if(intent.getExtras()!=null){
            Response = (Response) intent.getSerializableExtra("ResponseData");
            String country_flag_url = getString(R.string.file_url) + "/flag_image/"+ Response.getCcy()+".svg";
            String uzb_flag_url = getString(R.string.file_url) + "/flag_image/UZS.svg";
            Utils.fetchSvg(this, country_flag_url, flag_image);
            Utils.fetchSvg(this, uzb_flag_url, uzb_flag_image);
            Ccy.setText(Response.getCcy());
           // Code.setText(Response.getCode());
            String langCode= ChangeLanguage.getLangCode(getBaseContext());
            switch (langCode) {
                case "uz":
                    CcyNm.setText(Response.getCcyNm_UZ());
                    break;
                case "uzc":
                    CcyNm.setText(Response.getCcyNm_UZC());
                    break;
                case "en":
                    CcyNm.setText(Response.getCcyNm_EN());
                    break;
                case "ru":
                    CcyNm.setText(Response.getCcyNm_RU());
                    break;
            }
            Nominal.setText(Response.getNominal());
            Rate.setText(Response.getRate());
            sum.setText(getString(R.string.sum));
            Float diff=Float.parseFloat(Response.getDiff());
            if(diff>0){
                Diff.setText("+"+diff.toString());
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
            Date.setText(Response.getDate());
        }
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

    }
}