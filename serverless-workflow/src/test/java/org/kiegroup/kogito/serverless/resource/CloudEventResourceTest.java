package org.kiegroup.kogito.serverless.resource;

import java.net.URI;
import java.util.UUID;

import javax.json.Json;
import javax.ws.rs.core.MediaType;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kiegroup.kogito.serverless.serialization.CloudEventMessageBodyReader;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class CloudEventResourceTest {

    @Test
    public void testCloudEvent() {
        given()
            .when()
            .content(Json.createObjectBuilder().add("name", "Paul").build())
            .header(CloudEventMessageBodyReader.EVENT_ID_HEADER, UUID.randomUUID())
            .header(CloudEventMessageBodyReader.EVENT_SOURCE_HEADER, URI.create("http://knative-eventing.com").toString())
            .header(CloudEventMessageBodyReader.EVENT_TYPE_HEADER, "kogito-event")
            .header(CloudEventMessageBodyReader.EVENT_SPECVERSION_HEADER, "0.0.1")
            .contentType(MediaType.APPLICATION_JSON)
            .post("/")
            .then()
            .statusCode(202);
    }
}
