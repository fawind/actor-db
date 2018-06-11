package model;

import java.io.Serializable;
import java.util.Objects;

public class LamportId implements Serializable, Comparable<LamportId> {
    public final static LamportId INVALID_LAMPORT_ID = new LamportId(-1, -1);
    private final long clientId;
    private final long stamp;

    public LamportId(long clientId, long stamp) {
        this.clientId = clientId;
        this.stamp = stamp;
    }

    public long getClientId() {
        return clientId;
    }

    public long getStamp() {
        return stamp;
    }

    public LamportId increment() {
        return incrementTo(stamp + 1);
    }

    public LamportId incrementTo(long x) {
        return new LamportId(clientId, x);
    }

    @Override
    public int compareTo(LamportId other) {
        return stamp == other.stamp
                ? Long.compare(clientId, other.clientId)
                : Long.compare(stamp, other.stamp);
    }

    public boolean isGreaterThan(LamportId other) {
        return compareTo(other) > 0;
    }

    @Override
    public String toString() {
        return "LamportId{" +
                "clientId=" + clientId +
                ", stamp=" + stamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LamportId lamportId = (LamportId) o;
        return clientId == lamportId.clientId &&
                stamp == lamportId.stamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, stamp);
    }
}
