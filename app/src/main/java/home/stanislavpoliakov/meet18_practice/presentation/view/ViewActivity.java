package home.stanislavpoliakov.meet18_practice.presentation.view;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.UiThread;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import home.stanislavpoliakov.meet18_practice.R;
import home.stanislavpoliakov.meet18_practice.WeatherApplication;
import home.stanislavpoliakov.meet18_practice.dagger2.DaggerViewComponent;
import home.stanislavpoliakov.meet18_practice.dagger2.ViewComponent;
import home.stanislavpoliakov.meet18_practice.dagger2.ViewModule;
import home.stanislavpoliakov.meet18_practice.domain.DomainContract;
import home.stanislavpoliakov.meet18_practice.presentation.ViewContract;
import home.stanislavpoliakov.meet18_practice.presentation.presenter.BriefData;

public class ViewActivity extends AppCompatActivity implements ViewContract, Callback {
    private static final String TAG = "meet18_logs";
    private TextView weatherLabel;
    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private Spinner spinner;
    private List<BriefData> data;
    @Inject DomainContract.Presenter mPresenter;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private ViewComponent viewComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_activity);

        // Компонент уровня View определяем прям в Activity
        viewComponent = DaggerViewComponent.builder()
                .viewModule(new ViewModule(this))
                .build();
        viewComponent.inject(this);

        initUIViews();
    }

    /**
     * Метод установки метки над списком RecyclerView, содержит IANA timeZone
     * @param label
     */
    @Override
    public void setLabel(String label) {
        weatherLabel.setText(label);
    }

    /**
     * Неиспользуемый метод
     * @param cities
     */
    @Deprecated
    @Override
    public void setUserChoice(List<String> cities) {
    }

    /**
     * Поскольку схема параллельных вычислений изменилась (из HandlerThread в AsyncTask), то
     * принудительный запуск в UI-Thread нам больше не нужен, но я оставил как напоминание
     * @param wBriefData
     */
    @UiThread
    @Override
    public void displayBrief(List<BriefData> wBriefData) {
        //runOnUiThread(() -> {
            if (mAdapter == null) initRecyclerView(wBriefData);
            else updateRecyclerView(wBriefData);
        //});
    }

    /**
     * См. выше
     * @param detailInfo
     */
    @UiThread
    @Override
    public void showDetails(Bundle detailInfo) {
        //runOnUiThread(() -> {
            DetailFragment fragment = DetailFragment.newInstance();
            fragment.setArguments(detailInfo);
            fragmentManager.beginTransaction()
                    .add(fragment, "Detail")
                    .commitNow();
       // });
    }

    /**
     * Метод инициализации UI-компонентов
     */
    @Override
    public void initUIViews() {
        initLabel();
        //initRecyclerView(); //Это как напоминание, что RecyclerView инициализируется не отсюда
        initSpinner();

    }

    private void initLabel() {
        weatherLabel = findViewById(R.id.weatherLabel);
    }

    /**
     * Метод инициализации RecyclerView
     * @param wBriefData данные для отображения
     */
    private void initRecyclerView(List<BriefData> wBriefData) {
        this.data = wBriefData;
        recyclerView = findViewById(R.id.recyclerView);
        mAdapter = new MyAdapter(this, data);
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    /**
     * Метод обновления RecyclerView
     * @param newData
     */
    private void updateRecyclerView(List<BriefData> newData) {
        mAdapter.onNewData(newData);
    }

    /**
     * Метод инициализации спиннера и его адаптера
     */
    private void initSpinner() {
        spinner = findViewById(R.id.spinner);
        ArrayList<String> cities = this.getIntent().getStringArrayListExtra("cities");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item, cities);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(spinnerAdapter.getPosition("Москва")); // по умолчанию "Москва"

        // Если выбрали другой город - начинаем загрузку данных
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String cityName = (String) spinner.getSelectedItem();
                mPresenter.onSpinnerSelected(cityName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
        });
    }

    /**
     * При уничтожении Activity осовобождаем выделенные Components (здесь и на уровне приложения)
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewComponent = null;
        ((WeatherApplication) getApplication()).destroyComponents();
    }

    @Override
    public void viewHolderClicked(int itemPosition) {
        mPresenter.onViewHolderSelected(itemPosition);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, ViewActivity.class);
    }
}
