package store.configuration;

import api.configuration.EnvConfig;
import com.typesafe.config.Config;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatastoreConfig {
    private final EnvConfig envConfig;
    private final Config akkaConfig;
}
