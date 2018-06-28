package store.configuration;

import api.configuration.AkkaConfigLoader;
import api.configuration.EnvConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import store.Datastore;

import java.util.MissingResourceException;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class DatastoreModule extends AbstractModule {

    private static final String AKKA_REMOTE_CONFIG = "akka.conf";

    private static Injector injector;

    private final EnvConfig envConfig;

    public DatastoreModule(EnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    public static Datastore createInstance(EnvConfig envConfig) {
        injector = Guice.createInjector(new DatastoreModule(envConfig));
        return injector.getInstance(Datastore.class);
    }

    public static <T> T inject(Class<T> type) {
        if (injector == null) {
            throw new IllegalStateException("Injector not initialized");
        }
        return injector.getInstance(type);
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
        return AkkaConfigLoader.loadAkkaConfig(AKKA_REMOTE_CONFIG, envConfig);
    }
}
