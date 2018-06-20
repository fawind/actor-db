package api.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

public class AkkaConfigLoader {

    private static final String AKKA_HOSTNAME_FIELD = "akka.remote.netty.tcp.hostname";
    private static final String AKKA_PORT_FIELD = "akka.remote.netty.tcp.port";

    public static Config loadAkkaConfig(String conifgPath, EnvConfig envConfig) {
        return ConfigFactory.load(conifgPath)
                .withValue(AKKA_HOSTNAME_FIELD, ConfigValueFactory.fromAnyRef(envConfig.getHostname()))
                .withValue(AKKA_PORT_FIELD, ConfigValueFactory.fromAnyRef(envConfig.getPort()));
    }

    private AkkaConfigLoader() {}
}
