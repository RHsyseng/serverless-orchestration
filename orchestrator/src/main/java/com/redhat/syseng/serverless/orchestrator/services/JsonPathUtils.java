package com.redhat.syseng.serverless.orchestrator.services;

import javax.json.JsonObject;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.redhat.syseng.serverless.orchestrator.model.CorrelationToken;

public class JsonPathUtils {

    public static CorrelationToken getCorrelationToken(String path, JsonObject json) {
        if(json == null || path == null) {
            return null;
        }
        try {
            return CorrelationToken.fromString(JsonPath.parse(json.toString()).read(path));
        } catch (PathNotFoundException e) {
            return null;
        }
    }
}
