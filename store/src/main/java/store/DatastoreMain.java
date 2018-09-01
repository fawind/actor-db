package store;

import api.configuration.EnvConfig;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import store.configuration.DatastoreModule;

import java.util.ArrayList;
import java.util.List;

public class DatastoreMain {

    public static void main(String[] args) {
        StoreArgs arguments = new StoreArgs();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        EnvConfig storeEnvConfig = EnvConfig.builder()
                .hostname(arguments.getStoreHost())
                .port(arguments.getStorePort())
                .seedNodes(arguments.getSeedNodes())
                .readQuorum(arguments.getReadQuorum())
                .writeQuorum(arguments.getWriteQuorum())
                .extendedQuorum(arguments.getExtendedQuorum())
                .partitionCapacity(arguments.getPartitionCapacity())
                .isBenchmarkTable(arguments.isBenchmark())
                .build();

        new DatastoreMain().run(storeEnvConfig);
    }

    public void run(EnvConfig storeEnvConfig) {
        Datastore datastore = DatastoreModule.createInstance(storeEnvConfig);
        datastore.start();
    }

    @Data
    public static class StoreArgs {
        @Parameter(names = {"--storeHost", "-h"}, description = "Host address of the store")
        private String storeHost = "127.0.0.1";
        @Parameter(names = {"--storePort", "-p"}, description = "Port of the store")
        private int storePort = 2552;
        @Parameter(names = {"--seedNode", "-s"}, description = "Seed node address")
        private List<String> seedNodes = ImmutableList.of("127.0.0.1:2552");
        @Parameter(names = {"--readQuorum", "-r"}, description = "Read quorum count")
        private int readQuorum = 1;
        @Parameter(names = {"--writeQuorum", "-w"}, description = "Write quorum count")
        private int writeQuorum = 1;
        @Parameter(names = {"--extendedQuorum", "-e"}, description = "Extended quorum count")
        private int extendedQuorum = 0;
        @Parameter(names = {"--partitionCapacity", "-c"}, description = "Max partition capacity")
        private int partitionCapacity = 100;
        @Parameter(names = {"--benchmark", "-b"}, description = "Create benchmark table")
        private boolean isBenchmark  = false;
    }
}
