package com.redhat.syseng.serverless.orchestrator.model;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

import org.kie.kogito.Model;

public class Message implements Model {

    public static final String CORRELATION_TOKEN_PARAM = "correlation-token";
    public static final String DATA_PARAM = "data";
    public static final String ERROR_PARAM = "error";
    public static final String STATUS_PARAM = "status";

    private String correlationToken;
    private JsonObject data;
    private JsonObject error;
    private String status;

    public String getCorrelationToken() {
        return correlationToken;
    }

    public Message setCorrelationToken(String correlationToken) {
        this.correlationToken = correlationToken;
        return this;
    }

    public JsonObject getData() {
        return data;
    }

    public Message setData(JsonObject data) {
        this.data = data;
        return this;
    }

    public JsonObject getError() {
        return error;
    }

    public Message setError(JsonObject error) {
        this.error = error;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Message setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> variables = new HashMap<>();
        variables.put(CORRELATION_TOKEN_PARAM, correlationToken);
        variables.put(DATA_PARAM, data);
        variables.put(ERROR_PARAM, error);
        variables.put(STATUS_PARAM, status);
        return variables;
    }

    @Override
    public void fromMap(Map<String, Object> variables) {
        if (variables.containsKey(CORRELATION_TOKEN_PARAM)) {
            this.correlationToken = (String) variables.get(CORRELATION_TOKEN_PARAM);
        }
        if (variables.containsKey(DATA_PARAM)) {
            this.data = (JsonObject) variables.get(DATA_PARAM);
        }
        if (variables.containsKey(ERROR_PARAM)) {
            this.error = (JsonObject) variables.get(ERROR_PARAM);
        }
        if (variables.containsKey(STATUS_PARAM)) {
            this.status = (String) variables.get(STATUS_PARAM);
        }
    }
}
