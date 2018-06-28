package store.configuration;

import api.configuration.EnvConfig;
import com.typesafe.config.Config;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatastoreConfig {
    // Environment configs
    private final EnvConfig envConfig;
    private final Config akkaConfig;

    // Quorum configs
    @Builder.Default
    private final int readQuorum = 1;
    @Builder.Default
    private final int writeQuorum = 1;

    // Partition configs
    @Builder.Default
    private final int partitionCapacity = 100;
}
