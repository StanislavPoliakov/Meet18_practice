package home.stanislavpoliakov.meet18_practice.domain;

import android.support.annotation.WorkerThread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import home.stanislavpoliakov.meet18_practice.WeatherApplication;

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

    /**
     * Callback из Presenter.
     * У нас больше нет workThread, потому что нужно возвращаемое значение из "модели" в "презентер".
     * Вместо этого запускаем в презентере AsyncTask, с помощью которого запускаем работу UseCase. А
     * работу мы запускаем в одном потоке (singleThreadExecutor), потому что это совокупность
     * последовательных задач, и результат отдаем на вызывающую сторону (в Presenter).
     *
     * Executor выбран для того, чтобы вернуть результат из Future, а AsyncTask - для того, чтобы
     * он (AsyncTask) блокировался в ожидании результатов работы
     *
     * @param cityLocation координаты города в формате String
     */
    @WorkerThread
    @Override
    public Weather getData(String cityLocation) {
        //Создаем Executor
        ExecutorService pool = Executors.newSingleThreadExecutor();

        //Описываем дейстия, которые необходимо выполнить
        Callable<Weather> getWeather = () -> {
            Weather weather = networkGateway.fetchData(cityLocation);
            databaseGateway.saveData(weather);

            //Возвращаем результат
            return databaseGateway.loadData();
        };
        try {
            return pool.submit(getWeather).get();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        } finally {

            //Освобождаем Executor
            pool.shutdown();
        }
        return null;
    }
}
