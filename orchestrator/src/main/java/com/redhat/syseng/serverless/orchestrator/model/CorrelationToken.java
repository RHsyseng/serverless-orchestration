package com.redhat.syseng.serverless.orchestrator.model;

import java.util.Arrays;

public class CorrelationToken {

    private static final String SEPARATOR = ":";

    private final String id;
    private final String version;

    private CorrelationToken(String id, String version) {
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public static CorrelationToken fromString(String token) {
        String[] parts = token.split(SEPARATOR);
        if (parts.length == 2) {
            Arrays.stream(parts).forEach(p -> {
                if(p.isEmpty()) {
                    throw new IllegalArgumentException("Invalid token " + token);
                }
            });
            return new CorrelationToken(parts[0], parts[1]);
        }
        throw new IllegalArgumentException("Invalid token " + token);
    }

    @Override
    public String toString() {
        return id + SEPARATOR + version;
    }
}
