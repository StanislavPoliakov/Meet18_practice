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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
                    detailInfo.putString("image", setUrl(timeZone));
                    //detailInfo.putString("timeZone", timeZone);
                    return detailInfo;
                }).collect(Collectors.toList());
    }

    private String setUrl(String timeZone) {
        switch (timeZone) {
            case "Europe/Moscow":
                return "https://www.svyaznoy.travel/img/scontent/images/Москва_1.jpg?v2";
            case "Asia/Vladivostok":
                return "https://media.tvzvezda.ru/news/vstrane_i_mire/content/201810100451-91k1.htm/1.jpg";
            case "Asia/Bangkok":
                return "http://29palms.ru/photo/vip/thailand/bangkok_040116/resized/001_Klub_puteshestviy_Pavla_Aksenova_Tailand_Bangkok_Foto_potowizard_-_Depositphotos.jpg";
            case "Asia/Makassar":
                return "https://www.votpusk.ru/country/ctimages/new/id01.jpg";
            case "Asia/Dubai":
                return "https://nsp.ru/files/newsImages/6b/47/18928_big.jpg";
            case "Atlantic/Canary":
                return "https://static.tonkosti.ru/images/3/32/Главная_улица_Санта-Крус-де-Тенерифе.jpg";
            case "America/New_York":
                return "http://www.forumdaily.com/wp-content/uploads/2018/08/Depositphotos_3958211_m-2015-1.jpg";
        }
        return null;
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
            getWeatherData(cityLocation);
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
     * Метод получения данных из Interactor. RxJava
     * @param cityLocation
     */
    private void getWeatherData(String cityLocation) {
        useCaseInteractor.getData(cityLocation)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::show);
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
