package store;

import api.configuration.EnvConfig;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.Data;
import store.configuration.DatastoreModule;

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
                .build();

        new DatastoreMain().run(storeEnvConfig);
    }

    public void run(EnvConfig storeEnvConfig) {
        Datastore datastore = DatastoreModule.createInstance(storeEnvConfig);
        datastore.start();
    }

    @Data
    public static class StoreArgs {
        @Parameter(names = "--storeHost", description = "Host address of the store")
        private String storeHost = "127.0.0.1";
        @Parameter(names = "--storePort", description = "Port of the store")
        private int storePort = 2552;
    }
}
