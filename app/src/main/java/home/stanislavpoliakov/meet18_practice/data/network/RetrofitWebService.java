package home.stanislavpoliakov.meet18_practice.data.network;

import home.stanislavpoliakov.meet18_practice.domain.Weather;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitWebService {

    //Токен здесь. Пусть его retrofit подтянет через аннтоцию
    @GET("2028fd6e9ece283ff30f8a5a8f2597db/{locationPoint}")
    Call<Weather> getWeather(@Path("locationPoint") String locationPoint);
}
