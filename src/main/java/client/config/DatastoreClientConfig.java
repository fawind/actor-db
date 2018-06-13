package client.config;

import akka.actor.ActorPath;
import com.typesafe.config.Config;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class DatastoreClientConfig {
    private final Config akkaConfig;
    private final Set<ActorPath> initialContacts;
    @Builder.Default
    private final String clientEndpointPath = "/user/client-endpoint";
}
