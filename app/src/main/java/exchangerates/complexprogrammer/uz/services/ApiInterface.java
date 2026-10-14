package exchangerates.complexprogrammer.uz.services;

import exchangerates.complexprogrammer.uz.models.ResponseData;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("GetExchangeRates/")
    Call<ResponseData> GetExchangeRates();
}
