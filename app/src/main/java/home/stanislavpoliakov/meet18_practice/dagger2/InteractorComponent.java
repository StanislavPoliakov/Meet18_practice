package home.stanislavpoliakov.meet18_practice.dagger2;

import dagger.Component;
import home.stanislavpoliakov.meet18_practice.domain.UseCaseInteractor;

/**
 * Компонент Domain-уровня. В нем мы установим NetworkGateway и DatabaseGateway
 */
@ApplicationScope
@Component(modules = InteractorModule.class)
public interface InteractorComponent {

    void inject(UseCaseInteractor useCaseInteractor);
}
