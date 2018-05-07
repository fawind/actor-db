package configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import core.Datastore;

public class DatastoreModule extends AbstractModule {

    private static final String AKKA_REMOTE_CONFIG = "akka.conf";

    public static Datastore createInstance() {
        Injector injector = Guice.createInjector(new DatastoreModule());
        return injector.getInstance(Datastore.class);
    }

    @Override
    public void configure() {
        bind(Datastore.class);
    }

    @Provides
    @Singleton
    public DatastoreConfig getDatastoreConfig() {
        return DatastoreConfig.builder()
                .akkaConfig(getAkkaConfig())
                .build();
    }

    private Config getAkkaConfig() {
        return ConfigFactory.load(AKKA_REMOTE_CONFIG);
    }
}
