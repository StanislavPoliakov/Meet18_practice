package home.stanislavpoliakov.meet18_practice.dagger2;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import home.stanislavpoliakov.meet18_practice.data.database.DatabaseGateway;
import home.stanislavpoliakov.meet18_practice.data.network.NetworkGateway;
import home.stanislavpoliakov.meet18_practice.domain.DomainContract;

/**
 * Поясняем dagger'-у, что Context для базы данных надо взять из соответствующего модуля
 */
@Module(includes = ContextModule.class)
public class InteractorModule {

    /**
     * Этим методом мы "обещаем" предоставить NetworkGateway
     * @return
     */
    @ApplicationScope
    @Provides
    public DomainContract.NetworkOperations provideNetworkGateway() {
        return new NetworkGateway();
    }

    /**
     * А этим - DatabaseGateway
     * @param context получаем из ContextModule
     * @return
     */
    @ApplicationScope
    @Provides
    public DomainContract.DatabaseOperations provideDatabaseGateway(Context context) {
        return new DatabaseGateway(context);
    }
}
