package com.github.fawind.hakkandb.core;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class Table extends AbstractActor {

    private String name;
    private String layout;

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }

    static Props props(String tableName, String layout) {
        return Props.create(Table.class, () -> new Table(tableName, layout));
    }

    private Table(String name, String layout) {
        this.name = name;
        this.layout = layout;
    }

    /*
    protected:

    void handleInsert(Row row)
    void handleSelectAll()
    void handleSelectColumn(String... columns)
    void handleSelectWhere(String column, FilterFn filter)


    RangeMap<Integer, Partition> partitions = TreeRangeMap.create();
     */
}
