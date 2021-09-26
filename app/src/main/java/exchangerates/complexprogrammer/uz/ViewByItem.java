package exchangerates.complexprogrammer.uz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import exchangerates.complexprogrammer.uz.models.Response;
import exchangerates.complexprogrammer.uz.services.ChangeLanguage;
import exchangerates.complexprogrammer.uz.services.Utils;

public class ViewByItem extends AppCompatActivity {

    Response Response;
    ImageView flag_image,uzb_flag_image;
    TextView Code,Ccy,CcyNm,Nominal,Rate,Diff,Date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_by_item);
        Intent intent=getIntent();
        flag_image=findViewById(R.id.flag_image);
        uzb_flag_image=findViewById(R.id.uzb_flag_image);
        Code=findViewById(R.id.Code);
        Ccy=findViewById(R.id.Ccy);
        CcyNm=findViewById(R.id.CcyNm);
        Nominal=findViewById(R.id.Nominal);
        Rate=findViewById(R.id.Rate);
        Diff=findViewById(R.id.Diff);
        Date=findViewById(R.id.Date);
        if(intent.getExtras()!=null){
            Response = (Response) intent.getSerializableExtra("data");
            String country_flag_url = "http://complexprogrammer.uz:4444/Content/flag_image/"+ Response.getCcy()+".svg";
            String uzb_flag_url = "http://complexprogrammer.uz:4444/Content/flag_image/UZS.svg";
            Utils.fetchSvg(this, country_flag_url, flag_image);
            Utils.fetchSvg(this, uzb_flag_url, uzb_flag_image);
            Ccy.setText(Response.getCcy());
            Code.setText(Response.getCode());
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
            Diff.setText(Response.getDiff());
            Date.setText(Response.getDate());

        }
    }
}