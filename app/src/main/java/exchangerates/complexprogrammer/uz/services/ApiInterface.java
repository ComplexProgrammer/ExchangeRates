package exchangerates.complexprogrammer.uz.services;

import java.util.List;

import exchangerates.complexprogrammer.uz.models.Response;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("Api/C0mplexApi/GetExchangeRates/")
    Call<List<Response>> GetExchangeRates();
}
