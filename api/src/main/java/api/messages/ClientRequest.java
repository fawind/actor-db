package api.messages;

import api.commands.Command;
import api.messages.LamportId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;

@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor
public class ClientRequest implements Serializable {

    private Command command;
    private String clientRequestId;
    private LamportId lamportId;

    // Used for serialization
    private ClientRequest() {}
}
