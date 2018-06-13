package client.config;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import client.ClientRequestFactory;
import client.DatastoreClient;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Set;
import java.util.UUID;

import static com.google.inject.name.Names.named;

public class DatastoreClientModule extends AbstractModule {

    private static final String AKKA_CLIENT_CONFIG = "akkaClient.conf";
    private final ActorPath remoteClusterPath;

    public DatastoreClientModule(String remoteClusterPath) {
        this.remoteClusterPath = ActorPaths.fromString(remoteClusterPath);
    }

    public static DatastoreClient createInstance(String remoteClusterPath) {
        Injector injector = Guice.createInjector(new DatastoreClientModule(remoteClusterPath));
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
        return ConfigFactory.load(AKKA_CLIENT_CONFIG);
    }

    private Set<ActorPath> getInitialContacts() {
        return ImmutableSet.of(remoteClusterPath);
    }

    private String getClientId() {
        return UUID.randomUUID().toString();
    }
}
