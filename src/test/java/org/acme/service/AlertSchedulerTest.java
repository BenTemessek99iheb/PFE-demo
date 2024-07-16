package org.acme.service;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.websocket.*;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@QuarkusTest
class AlertSchedulerTest {

    private static final Logger LOG = Logger.getLogger(AlertScheduler.class);

    private static final String WS_URI = "ws://localhost:8081/alerts";
    private static CountDownLatch messageLatch;
    private static String receivedMessage;

    @ClientEndpoint
    public static class WebSocketClientEndpoint {

        @OnOpen
        public void onOpen(Session session) {
            LOG.info("Connected to endpoint: " + session.getBasicRemote());
        }

        @OnMessage
        public void onMessage(String message) {
            LOG.info("Received message: " + message);
            receivedMessage = message;
            messageLatch.countDown();
        }

        @OnError
        public void onError(Session session, Throwable t) {
            LOG.error("WebSocket error: ", t);
        }
    }

    @Test
    public void testWebSocket() throws Exception {
        messageLatch = new CountDownLatch(1);

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(WebSocketClientEndpoint.class, URI.create(WS_URI));

        RestAssured.given()
                .when()
                .get("/alerts/test")
                .then()
                .statusCode(200);

        assertTrue(messageLatch.await(10, TimeUnit.SECONDS), "Timeout waiting for message");
        assertEquals("Alert triggered: Test alert", receivedMessage);
    }


}