package home.stanislavpoliakov.meet18_practice.domain;

import io.reactivex.Observable;

public interface DomainContract {


    /**
     * Интерфейс презентора
     */
    interface Presenter {

        /**
         * Метод отображения данных, полученных из базы
         * @param weather данные
         */
        void show(Weather weather);

        /**
         * Callback из View. Обработка выбора города из списка
         * @param cityName название города
         */
        void onSpinnerSelected(String cityName);

        /**
         * Callback из View. Обработка нажатия на конкретный день
         * @param itemPosition позиция в списке отображения
         */
        void onViewHolderSelected(int itemPosition);
    }


    /**
     * Интерфейс вариантов использования
     */
    interface UseCase {

        /**
         * Callback из Presenter. Метод, запускающий работу после выбора пользователем города,
         * данные о котором необходимо загрузить из сети, сохранить в базе и получить из базы для
         * дальнейшей отрисовки
         * @param cityLocation координаты города в формате String
         */
        Observable<Weather> getData(String cityLocation);
    }


    /**
     * Интерфейс шлюза базы данных
     */
    interface DatabaseOperations {

        /**
         * Метод сохранения данных в базе
         * @param weather - данные, которые необходимо сохранить
         */
        void saveData(Weather weather);

        /**
         * Метод получения данных из базы
         * @return данные
         */
        Weather loadData();
    }

    /**
     * Интерфейс сетевого шлюза
     */
    interface NetworkOperations {

        /**
         * Метод получения данных из сети (API). RxJava
         * @param cityLocation координаты города
         * @return данные о погоде
         */
        Observable<Weather> fetchData(String cityLocation);
    }
}
