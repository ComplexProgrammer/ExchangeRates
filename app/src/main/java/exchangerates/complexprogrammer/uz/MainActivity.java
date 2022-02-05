package exchangerates.complexprogrammer.uz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.NetworkUtil;
import exchangerates.complexprogrammer.uz.services.ApiClient;
import exchangerates.complexprogrammer.uz.services.ChangeLanguage;
import exchangerates.complexprogrammer.uz.services.Utils;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private List<Response> ResponseList =new ArrayList<>();
    GridView gridView;
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
                        interstitialAd.show(MainActivity.this);
                        //MainActivity.this.interstitialAd = interstitialAd;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
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
        ChangeLanguage.setLocal(this,ChangeLanguage.getLangCode(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        loadAd();
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#FF018786"));
        getSupportActionBar().setTitle(getString(R.string.app_name));
        //getSupportActionBar().setBackgroundDrawable(colorDrawable);

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
                    startActivity(new Intent(MainActivity.this,ViewByItem.class).putExtra("data", ResponseList.get(position)));
                    String message= ResponseList.get(position).getCcy();
                    Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    public void LoadData(){
        Call<List<Response>> _response = ApiClient.getInterface().GetExchangeRates();

        _response.enqueue(new Callback<List<Response>>() {
            @Override
            public void onResponse(Call<List<Response>> call, retrofit2.Response<List<Response>> response) {
                Log.e("onResponse","onResponse");

                if(response.isSuccessful()){
                    ResponseList =response.body();
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
            public void onFailure(Call<List<Response>> call, Throwable t) {
                startActivity(new Intent(MainActivity.this, ServerErrorActivity.class));

            }
        });
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=layoutInflater.inflate(R.layout.row_grid_items,parent,false);
            }
            ImageView imageView = convertView.findViewById(R.id.imageview);
            ImageView imageDiff = convertView.findViewById(R.id.imageDiff);
            ImageView country_flag = convertView.findViewById(R.id.country_flag);
            // url for .svg image
            String url = "http://195.158.6.195:4444/Content/flag_image/UZS.svg";

            // this method will load svg
            // image in your imageview

            TextView Code=convertView.findViewById(R.id.Code);
            TextView Ccy=convertView.findViewById(R.id.Ccy);
            TextView CcyNm=convertView.findViewById(R.id.CcyNm);
            TextView Nominal=convertView.findViewById(R.id.Nominal);
            TextView Rate=convertView.findViewById(R.id.Rate);
            TextView Diff=convertView.findViewById(R.id.Diff);
            TextView Date=convertView.findViewById(R.id.Date);
            String country_flag_url = "http://195.158.6.195:4444/Content/flag_image/"+ ResponseList.get(position).getCcy()+".svg";
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
            //String diff=ResponseList.get(position).getDiff();
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
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/complexprogrammer"));
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