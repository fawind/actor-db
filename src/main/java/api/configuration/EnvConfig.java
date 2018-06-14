package api.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnvConfig {
    @Builder.Default
    private final String hostname = "127.0.0.1";
    @Builder.Default
    private final int port = 2552;

    public static EnvConfig withDefaults() {
        return EnvConfig.builder().build();
    }

    public static EnvConfig withPort(int port) {
        return EnvConfig.builder().port(port).build();
    }
}
