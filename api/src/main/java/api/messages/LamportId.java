package api.messages;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;

@Data
@Setter(AccessLevel.NONE)
public class LamportId implements Serializable, Comparable<LamportId> {

    public final static LamportId INVALID_LAMPORT_ID = new LamportId(null, -1);

    private String clientId;
    private long stamp;

    public LamportId(String clientId) {
        this(clientId, 0);
    }

    private LamportId(String clientId, long stamp) {
        this.clientId = clientId;
        this.stamp = stamp;
    }

    public LamportId incrementedCopy() {
        return incrementedToCopy(stamp + 1);
    }

    public LamportId incrementedToCopy(long newStamp) {
        return new LamportId(clientId, newStamp);
    }

    public LamportId max(LamportId other) {
        return this.isGreaterThan(other) ? this : other;
    }

    public LamportId maxIdCopy(LamportId other) {
        return this.incrementedToCopy(this.max(other).stamp);
    }

    @Override
    public int compareTo(LamportId other) {
        return stamp == other.stamp
                ? clientId.compareTo(other.clientId)
                : Long.compare(stamp, other.stamp);
    }

    public boolean isGreaterThan(LamportId other) {
        return compareTo(other) > 0;
    }
}
