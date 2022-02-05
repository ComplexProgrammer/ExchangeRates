package exchangerates.complexprogrammer.uz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Objects;

import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.ChangeLanguage;
import exchangerates.complexprogrammer.uz.services.Utils;

public class ViewByItem extends AppCompatActivity {

    Response Response;
    ImageView imageDiff,flag_image,uzb_flag_image;
    TextView Code,Ccy,CcyNm,Nominal,Rate,Diff,Date,sum;
    ImageView mIcon;
    Button mFollow;
    RelativeLayout relativeLayout;
    private static final String AD_UNIT_ID = "ca-app-pub-7773722896374259/4469855441";
    private static final String TAG = "MainActivity";
    private InterstitialAd interstitialAd;
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
        loadAd();
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
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
            Response = (Response) intent.getSerializableExtra("data");
            String country_flag_url = "http://195.158.6.195:4444/Content/flag_image/"+ Response.getCcy()+".svg";
            String uzb_flag_url = "http://195.158.6.195:4444/Content/flag_image/UZS.svg";
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
}