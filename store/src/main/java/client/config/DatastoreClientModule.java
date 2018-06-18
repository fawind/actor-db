package client.config;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import api.configuration.AkkaConfigLoader;
import api.configuration.EnvConfig;
import client.ClientRequestFactory;
import client.DatastoreClient;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;

import java.util.Set;
import java.util.UUID;

import static com.google.inject.name.Names.named;
import static java.lang.String.format;

public class DatastoreClientModule extends AbstractModule {

    private static final String AKKA_CLIENT_CONFIG = "akkaClient.conf";
    private static final String DATASTORE_PATH_TEMPLATE = "akka.tcp://actors-db@%s:%d/system/receptionist";

    private final EnvConfig clientEnvConfig;
    private final EnvConfig storeEnvConfig;

    public DatastoreClientModule(EnvConfig clientEnvConfig, EnvConfig storeEnvConfig) {
        this.clientEnvConfig = clientEnvConfig;
        this.storeEnvConfig = storeEnvConfig;
    }

    public static DatastoreClient createInstance(EnvConfig clientEnvConfig, EnvConfig storeEnvConfig) {
        Injector injector = Guice.createInjector(new DatastoreClientModule(clientEnvConfig, storeEnvConfig));
        return injector.getInstance(DatastoreClient.class);
    }

    @Override
    public void configure() {
        bind(DatastoreClient.class);
        bind(ClientRequestFactory.class);
        bindConstant().annotatedWith(named("ClientId")).to(getClientId());
    }

    @Provides
    public DatastoreClientConfig getDatastoreClientConfig() {
        return DatastoreClientConfig.builder()
                .akkaConfig(getAkkaConfig())
                .initialContacts(getInitialContacts())
                .build();
    }

    private Config getAkkaConfig() {
        return AkkaConfigLoader.loadAkkaConfig(AKKA_CLIENT_CONFIG, clientEnvConfig);
    }

    private Set<ActorPath> getInitialContacts() {
        return ImmutableSet.of(getDatastoreActorPath());
    }

    private String getClientId() {
        return UUID.randomUUID().toString();
    }

    private ActorPath getDatastoreActorPath() {
        return ActorPaths.fromString(
                format(DATASTORE_PATH_TEMPLATE, storeEnvConfig.getHostname(), storeEnvConfig.getPort()));
    }
}
