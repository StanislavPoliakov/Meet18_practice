package home.stanislavpoliakov.meet18_practice.data.network;

import home.stanislavpoliakov.meet18_practice.domain.Weather;
import home.stanislavpoliakov.meet18_practice.domain.DomainContract;
import io.reactivex.Observable;
import retrofit2.Response;

public class NetworkGateway implements DomainContract.NetworkOperations {
    private static final String TAG = "meet18_logs";

    /**
     * RxJava
     * @param cityLocation координаты города
     * @return
     */
    @Override
    public Observable<Weather> fetchData(String cityLocation) {
        return getWeather(cityLocation)
                .map(Response::body);
    }

    /**
     * RxJava
     * @param locationPoint
     * @return
     */
    private Observable<Response<Weather>> getWeather(String locationPoint){
        RetrofitHelper helper = new RetrofitHelper();
        return Observable.fromCallable(() -> helper.getService().getWeather(locationPoint).execute());
    }
}
