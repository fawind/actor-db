package com.github.fawind.hakkandb.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class CLI {
    public static void main(String[] args) {

        /*
        - read input
        - parse action type
        - pass action to Master


        holds actorSystem and Master for now
         */

        ActorSystem system = ActorSystem.create();
        ActorRef master = system.actorOf(Master.props());

        master.tell(new Master.CreateTableMsg("foo", "int,int,string"), null);

    }
}
