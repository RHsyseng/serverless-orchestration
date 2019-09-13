package org.kiegroup.kogito.serverless.model;

public class NodeRef {

    final int id;
    final String name;
    NodeRef to;
    NodeRef from;

    public NodeRef(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public NodeRef getTo() {
        return to;
    }

    public NodeRef getFrom() {
        return from;
    }

    public void setFrom(NodeRef from) {
        this.from = from;
    }

    public NodeRef setTo(NodeRef to) {
        this.to = to;
        if(to != null) {
            this.to.from = this;
        }
        return this;
    }

    public NodeRef setTo(int id, String name) {
        return setTo(new NodeRef(id, name));
    }

    public boolean hasTo() {
        return to != null;
    }
}
