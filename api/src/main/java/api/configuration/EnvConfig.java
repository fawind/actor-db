package api.configuration;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;

@Data
@Builder
public class EnvConfig {
    @Builder.Default
    private final String hostname = "127.0.0.1";
    @Builder.Default
    private final int port = 2552;
    @Builder.Default
    private final String propertiesLocation = "datastore";
    @Builder.Default
    private final List<String> seedNodes = ImmutableList.of("127.0.0.1:2552");
    @Builder.Default
    private final int readQuorum = 1;
    @Builder.Default
    private int writeQuorum = 1;
    @Builder.Default
    private int partitionCapacity = 100;
    @Builder.Default
    private boolean isBenchmarkTable = false;

    public static EnvConfig withDefaults() {
        return EnvConfig.builder().build();
    }

    public static EnvConfig withIpAndPort(String ip, int port) {
        return EnvConfig.builder().hostname(ip).port(port).build();
    }

    public static EnvConfig withPort(int port) {
        return EnvConfig.builder().port(port).build();
    }

    public ImmutableList<String> getSeedNodes() {
        return seedNodes.stream()
                .map(address -> format("akka.tcp://actor-db@%s", address))
                .collect(toImmutableList());
    }
}
