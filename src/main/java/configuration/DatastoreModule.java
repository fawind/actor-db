package configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import core.Datastore;

public class DatastoreModule extends AbstractModule {

    public static Datastore createDatastoreInstance() {
        Injector injector = Guice.createInjector(new DatastoreModule());
        return injector.getInstance(Datastore.class);
    }

    @Override
    public void configure() {
        bind(Datastore.class);
    }
}
