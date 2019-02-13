package home.stanislavpoliakov.meet18_practice.presentation.presenter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import home.stanislavpoliakov.meet18_practice.WeatherApplication;
import home.stanislavpoliakov.meet18_practice.domain.DomainContract;
import home.stanislavpoliakov.meet18_practice.domain.Weather;
import home.stanislavpoliakov.meet18_practice.presentation.ViewContract;
import static home.stanislavpoliakov.meet18_practice.presentation.presenter.Convert.*;


public class Presenter implements DomainContract.Presenter{
    private static final String TAG = "meet18_logs";
    private ViewContract mView;
    @Inject DomainContract.UseCase useCaseInteractor; // Interactor
    @Inject Context context;
    private String timeZone;
    private List<Bundle> details;

    public Presenter(ViewContract view) {
        this.mView = view;

        //В конструкторе просим dagger инжектировать зависимости
        WeatherApplication.getPresenterComponent().inject(this);
    }

    /**
     * Конвертируем данные в данные для отображения
     * @param weather данные из базы
     * @return данные для отображения
     */
    private List<BriefData> getBriefData(Weather weather) {

        return Stream.of(weather.daily.data)
                .map(data -> {
                    BriefData briefData = new BriefData();
                    briefData.setTemperatureMin(data.temperatureMin);
                    briefData.setTemperatureMax(data.temperatureMax);
                    briefData.setTime(data.time);
                    return briefData;
                }).collect(Collectors.toList());
    }

    /**
     * Для детальной информации собираем список бандлов.
     * @param weather данные из базы
     * @return List<Bundle>
     */
    private List<Bundle> getDetails(Weather weather) {
        return Stream.of(weather.daily.data)
                .map(data -> {
                    setZoneId(timeZone);
                    Bundle detailInfo = new Bundle();
                    detailInfo.putString("time", toFormattedZoneDate(data.time));
                    detailInfo.putString("summary", data.summary);
                    detailInfo.putString("sunriseTime", toFormattedZoneTime(data.sunriseTime));
                    detailInfo.putString("sunsetTime", toFormattedZoneTime(data.sunsetTime));
                    detailInfo.putString("precipInfo", toPrecipInfo(data.precipProbability, data.precipIntensity));
                    detailInfo.putString("precipInfoMax", toPrecipMax(data.precipIntensityMax, data.precipIntensityMaxTime));
                    detailInfo.putString("precipType", "Тип осадков: " + data.precipType);
                    detailInfo.putString("humdew", toHumidyAndDewPoint(data.humidity, data.dewPoint));
                    detailInfo.putString("pressure", "Давление: " + toMercury(data.pressure));
                    detailInfo.putString("windInfo", toWindInfo(data.windBearing, data.windSpeed, data.windGust));
                    detailInfo.putString("cloudCover", "Облачность: " + toPercent(data.cloudCover));
                    detailInfo.putString("uvIndex", "Ультрафиолетовый индекс: " + data.uvIndex);
                    detailInfo.putString("tempMinInfo", toTempMinInfo(data.temperatureMin, data.temperatureMinTime));
                    detailInfo.putString("tempMaxInfo", toTempMaxInfo(data.temperatureMax, data.temperatureMaxTime));
                    //detailInfo.putString("timeZone", timeZone);
                    return detailInfo;
                }).collect(Collectors.toList());
    }

    /**
     * Метод отображения загруженных из базы данных
     * Подготавливаем данные, устанавливаем Label и запускаем методы отображения
     * @param weather данные
     */
    @Override
    public void show(Weather weather) {
        timeZone = weather.timezone;
        mView.setLabel(timeZone);
        displayBriefData(getBriefData(weather));
        details = getDetails(weather);

    }

    /**
     * Метод отображения данных в RecyclerView
     * @param briefData сокращенные данные
     */
    private void displayBriefData(List<BriefData> briefData) {
        mView.displayBrief(briefData);
    }

    /**
     * Метод отображения детальной информации в отдельном фрагменте
     * @param detailInfo детальная информация по выбранному дню
     */
    private void displayDetails(Bundle detailInfo) {
        mView.showDetails(detailInfo);
    }

    /**
     * Callback из View.
     * Получаем название города, получаем координаты и отправляем в UseCase для обработки
     * @param cityName название города
     */
    @Override
    public void onSpinnerSelected(String cityName) {
        try {
            String cityLocation = getCoordinates(cityName);

            //Определяем и запускаем AsyncTask для работы с сетью и базой
            FetchDataTask fetchDataTask = new FetchDataTask();
            fetchDataTask.execute(cityLocation);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Метод получения координат локации (города, а в случае с Бали - острова)
     * @param cityName название локации
     * @return координаты
     * @throws IOException
     */
    private String getCoordinates(String cityName) throws IOException {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList = geocoder.getFromLocationName(cityName, 1);
        Address address = addressList.get(0);
        StringBuilder builder = new StringBuilder();
        builder.append(address.getLatitude())
                .append(", ")
                .append(address.getLongitude());
        return builder.toString();
    }

    /**
     * Для запуска work-потока, с результатом в UI идеально подошел AsyncTask
     */
    private class FetchDataTask extends AsyncTask<String, Void, Weather> {
        @Override
        protected Weather doInBackground(String... strings) {
            return useCaseInteractor.getData(strings[0]);
        }

        @Override
        protected void onPostExecute(Weather weather) {
            show(weather);
        }
    }

    /**
     * Callback из View.
     * Обрабатываем нажатие на ViewHolder
     * @param itemPosition позиция в списке отображения
     */
    @Override
    public void onViewHolderSelected(int itemPosition) {
        displayDetails(details.get(itemPosition));
    }
}
