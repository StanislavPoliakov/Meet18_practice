package home.stanislavpoliakov.meet18_practice.domain;

import android.support.annotation.WorkerThread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import home.stanislavpoliakov.meet18_practice.WeatherApplication;
import io.reactivex.Observable;

public class UseCaseInteractor implements DomainContract.UseCase {
    private static final String TAG = "meet17_logs";
    @Inject DomainContract.NetworkOperations networkGateway;
    @Inject DomainContract.DatabaseOperations databaseGateway;

    /**
     * В конструкторе просим dagger инъектировать зависимости из компонента (который создается на
     * уровне приложения)
     */
    public UseCaseInteractor() {
        WeatherApplication.getInteractorComponent().inject(this);
    }

    /** RxJava
     * Callback из Presenter.
     *
     * @param cityLocation координаты города в формате String
     */
    @WorkerThread
    @Override
    public Observable<Weather> getData(String cityLocation) {
        return networkGateway.fetchData(cityLocation)
                .doOnNext(databaseGateway::saveData)
                .map(w -> databaseGateway.loadData());
    }
}
