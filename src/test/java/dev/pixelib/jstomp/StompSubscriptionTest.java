package dev.pixelib.jstomp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StompSubscriptionTest {
    
    @Test
    void shouldCreateSubscription() {
        StompMessageHandler handler = message -> { /* do nothing */ };
        StompSubscription subscription = new StompSubscription("sub-1", "/queue/test", handler);
        
        assertThat(subscription.getId()).isEqualTo("sub-1");
        assertThat(subscription.getDestination()).isEqualTo("/queue/test");
        assertThat(subscription.getMessageHandler()).isSameAs(handler);
    }
    
    @Test
    void shouldHaveStringRepresentation() {
        StompMessageHandler handler = message -> { /* do nothing */ };
        StompSubscription subscription = new StompSubscription("sub-1", "/queue/test", handler);
        
        String toString = subscription.toString();
        assertThat(toString).contains("id='sub-1'");
        assertThat(toString).contains("destination='/queue/test'");
    }
}
