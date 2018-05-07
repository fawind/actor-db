package configuration;

import com.typesafe.config.Config;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatastoreConfig {
    private final Config akkaConfig;
}
