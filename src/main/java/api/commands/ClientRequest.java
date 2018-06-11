package api.commands;

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

    // Used for serialization
    private ClientRequest() {}

    private Command command;
    private LamportId lamportId;
}
