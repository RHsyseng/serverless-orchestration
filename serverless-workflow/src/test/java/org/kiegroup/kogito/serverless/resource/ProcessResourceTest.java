package org.kiegroup.kogito.serverless.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ProcessResourceTest {

    @Test
    public void testCreateInstance() {
        given()
            .when().get("/process")
            .then()
            .statusCode(200);
    }
}
