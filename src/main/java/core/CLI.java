package core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import messages.CreateTableMsg;
import messages.InsertMsg;
import messages.InsertRowMsg;

public class CLI {

    private ActorSystem actorSystem;
    private ActorRef master;
    private ActorRef cliActor;

    private CLI() {
        actorSystem = ActorSystem.create("hakkanDB");
        master = actorSystem.actorOf(Master.props(), "Master");
        cliActor = actorSystem.actorOf(CLIActor.props(), "CLIActor");
    }

    private void start() throws InterruptedException {
        master.tell(new CreateTableMsg("foo", "int,int,string"), cliActor);
        master.tell(new InsertMsg("foo", new Row("1", "abc", "23")), cliActor);
        master.tell(new InsertMsg("foo", new Row("2", "bcd", "34")), cliActor);

        Thread.sleep(1000);
        actorSystem.terminate();
    }

    public static void main(String[] args) throws InterruptedException {

        /*
        - read input
        - parse action type
        - pass action to Master


        holds actorSystem and Master for now
         */

        CLI cli = new CLI();
        cli.start();
    }
}
