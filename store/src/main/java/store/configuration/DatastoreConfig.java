package store.configuration;

import api.configuration.EnvConfig;
import com.typesafe.config.Config;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Builder
@Setter(AccessLevel.NONE)
public class DatastoreConfig {
    // Environment configs
    private final EnvConfig envConfig;
    private final Config akkaConfig;
}
