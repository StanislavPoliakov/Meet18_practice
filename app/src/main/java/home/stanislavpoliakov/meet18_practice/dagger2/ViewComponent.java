package home.stanislavpoliakov.meet18_practice.dagger2;

import dagger.Component;
import home.stanislavpoliakov.meet18_practice.presentation.view.ViewActivity;

/**
 * Компонент View-уровня
 */
@ApplicationScope
@Component(modules = {ViewModule.class})
public interface ViewComponent {

    void inject(ViewActivity activity);
}
