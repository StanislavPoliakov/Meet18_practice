package home.stanislavpoliakov.meet18_practice.dagger2;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import home.stanislavpoliakov.meet18_practice.domain.DomainContract;
import home.stanislavpoliakov.meet18_practice.domain.UseCaseInteractor;

@Module
public class PresenterModule {

    /**
     * Обещаем предоставить в Presenter элемент Domain-уровня
     * @return
     */
    @ApplicationScope
    @Provides
    public DomainContract.UseCase provideUseCaseInteractor() {
        return new UseCaseInteractor();
    }

}
