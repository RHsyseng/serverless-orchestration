package com.redhat.syseng.serverless.orchestrator.services;

import javax.json.Json;
import javax.json.JsonObject;

import com.redhat.syseng.serverless.orchestrator.model.CorrelationToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonPathServiceTest {

    @Test
    public void testCorrelationToken() {
        JsonObject json = Json.createObjectBuilder().add("example", Json.createObjectBuilder().add("id", "abcd-efgh:2").build()).build();
        CorrelationToken token = JsonPathUtils.getCorrelationToken("$.example.id", json);
        assertEquals("abcd-efgh", token.getId());
        assertEquals("2", token.getVersion());
    }

    @Test
    public void testCorrelationTokenMissingVersion() {
        JsonObject json = Json.createObjectBuilder().add("example", Json.createObjectBuilder().add("id", "abcd-efgh:").build()).build();
        assertThrows(IllegalArgumentException.class, () -> JsonPathUtils.getCorrelationToken("$.example.id", json));
    }

    @Test
    public void testCorrelationTokenMissingId() {
        JsonObject json = Json.createObjectBuilder().add("example", Json.createObjectBuilder().add("id", ":2").build()).build();
        assertThrows(IllegalArgumentException.class, () -> JsonPathUtils.getCorrelationToken("$.example.id", json));
    }

    @Test
    public void testCorrelationTokenMissingIdAndVersion() {
        JsonObject json = Json.createObjectBuilder().add("example", Json.createObjectBuilder().add("id", "").build()).build();
        assertThrows(IllegalArgumentException.class, () -> JsonPathUtils.getCorrelationToken("$.example.id", json));
    }

    @Test
    public void testCorrelationTokenPathNotFound() {
        JsonObject json = Json.createObjectBuilder().add("example", "3").build();
        assertNull(JsonPathUtils.getCorrelationToken("$.example.id", json));
    }
}
