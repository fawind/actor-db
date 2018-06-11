package api.messages;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;

@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor
public class LamportId implements Serializable {

    // Used for serialization
    private LamportId() {}

    private String clientId;
    private String clientRequestId;
    private long stamp;
}
