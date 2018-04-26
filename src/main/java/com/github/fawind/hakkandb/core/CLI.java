package com.github.fawind.hakkandb.core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

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
        master.tell(new Master.CreateTableMsg("foo", "int,int,string"), cliActor);

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
