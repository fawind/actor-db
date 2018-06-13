package api.messages;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;

@Data
@Setter(AccessLevel.NONE)
public class LamportId implements Serializable, Comparable<LamportId> {

    public final static LamportId INVALID_LAMPORT_ID = new LamportId(null, null, -1);

    private String clientId;
    private String clientRequestId;
    private long stamp;

    public LamportId(String clientId, String clientRequestId, long stamp) {
        this.clientId = clientId;
        this.clientRequestId = clientRequestId;
        this.stamp = stamp;
    }

    public LamportId incrementTo(long x) {
        return new LamportId(clientId, clientRequestId, x);
    }

    public LamportId increment() {
        return incrementTo(stamp + 1);
    }

    @Override
    public int compareTo(LamportId other) {
        return stamp == other.stamp
                ? clientRequestId.compareTo(other.clientRequestId)
                : Long.compare(stamp, other.stamp);
    }

    public boolean isGreaterThan(LamportId other) {
        return compareTo(other) > 0;
    }
}
