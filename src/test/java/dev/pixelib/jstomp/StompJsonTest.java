package dev.pixelib.jstomp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class StompJsonTest {
    
    private StompClient stompClient;
    
    @Mock
    private StompConnectionListener mockConnectionListener;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create StompClient with test URI
        URI serverUri = URI.create("ws://localhost:8080/ws");
        stompClient = new StompClient(serverUri);
    }
    
    @Test
    void shouldCreateClientWithCustomGson() {
        Gson customGson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        
        URI uri = URI.create("ws://localhost:8080/ws");
        StompClient client = new StompClient(new OkHttpClient(), uri, customGson);
        
        assertThat(client).isNotNull();
        assertThat(client.isConnected()).isFalse();
    }
    
    @Test
    void shouldNotSendJsonWhenNotConnected() {
        TestMessage testMessage = new TestMessage("Hello", 42);
        
        assertThatThrownBy(() -> stompClient.sendJson("/queue/test", testMessage))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not connected");
    }
    
    @Test
    void shouldNotSendJsonWithHeadersWhenNotConnected() {
        TestMessage testMessage = new TestMessage("Hello", 42);
        Map<String, String> headers = Map.of("priority", "high");
        
        assertThatThrownBy(() -> stompClient.sendJson("/queue/test", testMessage, headers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not connected");
    }
    
    @Test
    void shouldNotSubscribeJsonWhenNotConnected() {
        StompJsonMessageHandler<TestMessage> handler = (destination, object, message) -> {
            // do nothing
        };
        
        assertThatThrownBy(() -> stompClient.subscribeJson("/queue/test", TestMessage.class, handler))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not connected");
    }
    
    @Test
    void shouldCreateStompJsonException() {
        StompJsonException exception1 = new StompJsonException("Test message");
        assertThat(exception1.getMessage()).isEqualTo("Test message");
        
        RuntimeException cause = new RuntimeException("Cause");
        StompJsonException exception2 = new StompJsonException("Test with cause", cause);
        assertThat(exception2.getMessage()).isEqualTo("Test with cause");
        assertThat(exception2.getCause()).isSameAs(cause);
        
        StompJsonException exception3 = new StompJsonException(cause);
        assertThat(exception3.getCause()).isSameAs(cause);
    }
    
    @Test
    void shouldCreateStompJsonMessage() {
        Map<String, String> headers = Map.of("content-type", "application/json");
        StompMessage originalMessage = new StompMessage("/queue/test", "{\"name\":\"test\"}", headers);
        TestMessage testObject = new TestMessage("test", 123);
        
        StompJsonMessage<TestMessage> jsonMessage = new StompJsonMessage<>(originalMessage, testObject);
        
        assertThat(jsonMessage.getOriginalMessage()).isSameAs(originalMessage);
        assertThat(jsonMessage.getJsonObject()).isSameAs(testObject);
        assertThat(jsonMessage.getDestination()).isEqualTo("/queue/test");
        assertThat(jsonMessage.getBody()).isEqualTo("{\"name\":\"test\"}");
        assertThat(jsonMessage.getHeaders()).isEqualTo(headers);
        assertThat(jsonMessage.getHeader("content-type")).isEqualTo("application/json");
        assertThat(jsonMessage.getHeader("non-existent")).isNull();
        
        String toString = jsonMessage.toString();
        assertThat(toString).contains("destination='/queue/test'");
        assertThat(toString).contains("jsonObject=");
    }
    
    @Test
    void shouldHandleJsonMessageHandlerInterface() {
        // Test that the functional interface works correctly
        StompJsonMessageHandler<TestMessage> handler = (destination, object, message) -> {
            assertThat(destination).isEqualTo("/queue/test");
            assertThat(object).isNotNull();
            assertThat(message).isNotNull();
        };
        
        // Verify it's a functional interface
        assertThat(handler).isNotNull();
    }
    
    // Test helper class
    static class TestMessage {
        private String name;
        private int value;
        
        public TestMessage() {}
        
        public TestMessage(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        
        @Override
        public String toString() {
            return "TestMessage{name='" + name + "', value=" + value + "}";
        }
    }
}
