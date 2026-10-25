package exchangerates.complexprogrammer.uz.services;

import java.util.List;

import exchangerates.complexprogrammer.uz.models.Response;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
public interface CbuApi {
    /** All current rates from Central Bank archive JSON endpoint. */
    @GET("uz/arkhiv-kursov-valyut/json/")
    Call<List<Response>> getAllRates();

    /** Rate of one currency valid on the given date (yyyy-MM-dd). */
    @GET("uz/arkhiv-kursov-valyut/json/{ccy}/{date}/")
    Call<List<Response>> rateOnDate(@Path("ccy") String ccy, @Path("date") String date);
}
