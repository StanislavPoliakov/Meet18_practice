package home.stanislavpoliakov.meet18_practice.dagger2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Scope;

/**
 * Создаем Scope для всех наших инъекций.
 * Поскольку логика приложения подразумевает одновременное существование всех компонентов
 * (View у нас одна; Presenter, соответственно, тоже один; постоянно используется соединение с
 * сетью и базой данных, - значит мы будем реализовывать, как по науке, один Scope на все приложение :)))))
 */
@Scope
@Retention(RetentionPolicy.CLASS)
public @interface ApplicationScope {
}
