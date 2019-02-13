package home.stanislavpoliakov.meet18_practice.dagger2;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * Модуль получения контекста.
 * Используется в формировании модуля базы данных. Можно было бы сделать и через конструктор
 * модуля базы, но я решил сделать так, а потом прицепить его к модулю базы. Ради обучения
 */
@Module
public class ContextModule {
    private Context context;

    /**
     * Будем устанавливать контекст через Component.builder
     * @param context
     */
    public ContextModule(Context context) {
        this.context = context;
    }

    @Provides
    public Context provideContext() {
        return this.context;
    }

}
