package dev.pixelib.jstomp;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StompMessageTest {
    
    @Test
    void shouldCreateMessage() {
        Map<String, String> headers = Map.of(
                "message-id", "123",
                "content-type", "text/plain"
        );
        
        StompMessage message = new StompMessage("/queue/test", "Hello, World!", headers);
        
        assertThat(message.getDestination()).isEqualTo("/queue/test");
        assertThat(message.getBody()).isEqualTo("Hello, World!");
        assertThat(message.getHeaders()).containsExactlyInAnyOrderEntriesOf(headers);
        assertThat(message.getHeader("message-id")).isEqualTo("123");
        assertThat(message.getHeader("content-type")).isEqualTo("text/plain");
        assertThat(message.getHeader("non-existent")).isNull();
    }
    
    @Test
    void shouldHandleEmptyBody() {
        StompMessage message = new StompMessage("/queue/test", "", Map.of());
        
        assertThat(message.getBody()).isEmpty();
    }
    
    @Test
    void shouldHaveStringRepresentation() {
        StompMessage message = new StompMessage("/queue/test", "Hello", Map.of("id", "123"));
        
        String toString = message.toString();
        assertThat(toString).contains("destination='/queue/test'");
        assertThat(toString).contains("body='Hello'");
        assertThat(toString).contains("headers={id=123}");
    }
}
