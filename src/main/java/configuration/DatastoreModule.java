package configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import core.Datastore;

public class DatastoreModule extends AbstractModule {

    private static final String AKKA_REMOTE_CONFIG = "akka.conf";
    private final EnvConfig envConfig;

    public DatastoreModule(EnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    public static Datastore createInstance() {
        return DatastoreModule.createInstance(EnvConfig.withDefaults());
    }

    public static Datastore createInstance(EnvConfig envConfig) {
        Injector injector = Guice.createInjector(new DatastoreModule(envConfig));
        return injector.getInstance(Datastore.class);
    }

    @Override
    public void configure() {
        bind(Datastore.class);
    }

    @Provides
    public DatastoreConfig getDatastoreConfig() {
        return DatastoreConfig.builder()
                .envConfig(envConfig)
                .akkaConfig(getAkkaConfig())
                .build();
    }

    private Config getAkkaConfig() {
        return ConfigFactory.load(AKKA_REMOTE_CONFIG);
    }
}
