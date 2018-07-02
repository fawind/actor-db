package store.actors;

import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.metrics.ClusterMetricsChanged;
import akka.cluster.metrics.ClusterMetricsExtension;
import akka.cluster.metrics.NodeMetrics;
import akka.cluster.metrics.StandardMetrics;
import akka.cluster.metrics.StandardMetrics.Cpu;
import akka.cluster.metrics.StandardMetrics.HeapMemory;

public class MetricsListener extends AbstractDBActor {

    public static final String ACTOR_NAME = "metrics-listener";
    private Cluster cluster = Cluster.get(getContext().getSystem());
    private ClusterMetricsExtension extension = ClusterMetricsExtension.get(getContext().getSystem());

    public static Props props() {
        return Props.create(MetricsListener.class, MetricsListener::new);
    }


    // Subscribe unto ClusterMetricsEvent events.
    @Override
    public void preStart() {
        extension.subscribe(getSelf());
    }

    // Unsubscribe from ClusterMetricsEvent events.
    @Override
    public void postStop() {
        extension.unsubscribe(getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClusterMetricsChanged.class, clusterMetrics -> {
                    for (NodeMetrics nodeMetrics : clusterMetrics.getNodeMetrics()) {
                        if (nodeMetrics.address().equals(cluster.selfAddress())) {
//                            logHeap(nodeMetrics);
//                            logCpu(nodeMetrics);
                            logAll(nodeMetrics);
                        }
                    }
                })
                .match(CurrentClusterState.class, message -> {
                    // Ignore.
                })
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void logAll(NodeMetrics nodeMetrics) {
        log.info("ALL metrics: {}", nodeMetrics.toString());

    }

    private void logHeap(NodeMetrics nodeMetrics) {
        HeapMemory heap = StandardMetrics.extractHeapMemory(nodeMetrics);
        if (heap != null) {
            log.info("Used heap: {} MB", ((double) heap.used()) / 1024 / 1024);
        }
    }

    private void logCpu(NodeMetrics nodeMetrics) {
        Cpu cpu = StandardMetrics.extractCpu(nodeMetrics);
        if (cpu != null && cpu.systemLoadAverage().isDefined()) {
            log.info("Load: {} ({} processors)", cpu.systemLoadAverage().get(),
                    cpu.processors());
        }
    }

}