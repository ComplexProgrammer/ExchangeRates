package exchangerates.complexprogrammer.uz;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import exchangerates.complexprogrammer.uz.models.NetworkUtil;
import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.ApiClient;
import exchangerates.complexprogrammer.uz.services.GlideApp;
import exchangerates.complexprogrammer.uz.services.Utils;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;
    private List<Response> ResponseList=new ArrayList<>();
    GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        String status = NetworkUtil.getConnectivityStatusString(this);
        if(status=="No internet is available"){
            startActivity(new Intent(this, NoInternetActivity.class));
        }
        else {
            LoadData();
        }
    }
    public void LoadData(){
        gridView=findViewById(R.id.gridView);
        Call<List<Response>> _response = ApiClient.getInterface().GetExchangeRates();
        _response.enqueue(new Callback<List<Response>>() {
            @Override
            public void onResponse(Call<List<Response>> call, retrofit2.Response<List<Response>> response) {
                if(response.isSuccessful()){
                    ResponseList=response.body();
                    CustomAdapter customAdapter=new CustomAdapter(ResponseList,getApplicationContext());
                    gridView.setAdapter(customAdapter);

                }
                else {
                    String message="Xatolik yuz berdi. keyinroq yana urinib ko'rig";
                    Toast.makeText(getBaseContext(),message,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Response>> call, Throwable t) {

            }
        });
    }
    public class CustomAdapter extends BaseAdapter {
        private List<Response> ResponseList;
        private Context context;
        private LayoutInflater layoutInflater;

        public CustomAdapter(List<Response> ResponseList,Context context) {
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
            String url = "http://complexprogrammer.uz:4444/Content/flag_image/UZS.svg";

            // this method will load svg
            // image in your imageview

            TextView Code=convertView.findViewById(R.id.Code);
            TextView Ccy=convertView.findViewById(R.id.Ccy);
            TextView CcyNm=convertView.findViewById(R.id.CcyNm);
            TextView Nominal=convertView.findViewById(R.id.Nominal);
            TextView Rate=convertView.findViewById(R.id.Rate);
            TextView Diff=convertView.findViewById(R.id.Diff);
            TextView Date=convertView.findViewById(R.id.Date);
            String country_flag_url = "http://complexprogrammer.uz:4444/Content/flag_image/"+ResponseList.get(position).getCcy()+".svg";
            Log.e("country_flag_url",country_flag_url);
            Utils.fetchSvg(context, country_flag_url, country_flag);
            Utils.fetchSvg(context, url, imageView);
            Code.setText(ResponseList.get(position).getCode());
            Ccy.setText(ResponseList.get(position).getCcy());
            CcyNm.setText(ResponseList.get(position).getCcyNm_UZ());
            Nominal.setText(ResponseList.get(position).getNominal());
            Rate.setText(ResponseList.get(position).getRate()+"  so'm");
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
}