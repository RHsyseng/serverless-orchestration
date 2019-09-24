package org.kiegroup.kogito.serverless.model;

import javax.json.JsonObject;

public class Data {

    public final JsonObject value;

    public Data(JsonObject value) {
        this.value = value;
    }
}
