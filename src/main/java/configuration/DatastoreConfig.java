package configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatastoreConfig {

    private static final String AKKA_HOSTNAME_FIELD = "akka.remote.netty.tcp.hostname";
    private static final String AKKA_PORT_FIELD = "akka.remote.netty.tcp.port";

    private final EnvConfig envConfig;
    private final Config akkaConfig;

    public Config getAkkaConfig() {
        return akkaConfig
                .withValue(AKKA_HOSTNAME_FIELD, ConfigValueFactory.fromAnyRef(envConfig.getHostname()))
                .withValue(AKKA_PORT_FIELD, ConfigValueFactory.fromAnyRef(envConfig.getPort()));
    }
}
