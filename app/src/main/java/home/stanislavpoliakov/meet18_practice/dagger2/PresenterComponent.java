package home.stanislavpoliakov.meet18_practice.dagger2;

import dagger.Component;
import home.stanislavpoliakov.meet18_practice.presentation.presenter.Presenter;

/**
 * Компонент зависимостей Presenter-уровня
 */
@ApplicationScope
@Component(modules = {PresenterModule.class, ContextModule.class})
public interface PresenterComponent {

    void inject(Presenter presenter);
}
