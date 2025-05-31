package dev.pixelib.jstomp;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class StompClientTest {
    
    private StompClient stompClient;
    
    @Mock
    private StompConnectionListener mockConnectionListener;
    
    @Mock
    private StompMessageHandler mockMessageHandler;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create StompClient with test URI
        URI serverUri = URI.create("ws://localhost:8080/ws");
        stompClient = new StompClient(serverUri);
    }
    
    @Test
    void shouldCreateClientWithUri() {
        URI uri = URI.create("ws://localhost:8080/ws");
        StompClient client = new StompClient(uri);
        
        assertThat(client).isNotNull();
        assertThat(client.isConnected()).isFalse();
    }
    
    @Test
    void shouldCreateClientWithOkHttpClientAndUri() {
        OkHttpClient okHttpClient = new OkHttpClient();
        URI uri = URI.create("ws://localhost:8080/ws");
        StompClient client = new StompClient(okHttpClient, uri);
        
        assertThat(client).isNotNull();
        assertThat(client.isConnected()).isFalse();
    }
    
    @Test
    void shouldSetConnectionListener() {
        stompClient.setConnectionListener(mockConnectionListener);
        
        // Verify no exceptions are thrown
        assertThat(stompClient).isNotNull();
    }
    
    @Test
    void shouldAddHeaders() {
        stompClient.addHeader("custom-header", "custom-value");
        stompClient.addHeader("another-header", "another-value");
        
        // Verify no exceptions are thrown
        assertThat(stompClient).isNotNull();
    }
    
    @Test
    void shouldReturnConnectedStatus() {
        // Initially not connected
        assertThat(stompClient.isConnected()).isFalse();
    }
    
    @Test 
    void shouldNotSendWhenNotConnected() {
        // Try to send without connecting
        assertThatThrownBy(() -> stompClient.send("/queue/test", "message"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not connected");
    }
    
    @Test
    void shouldNotSendWithHeadersWhenNotConnected() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        
        assertThatThrownBy(() -> stompClient.send("/queue/test", "message", headers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not connected");
    }
    
    @Test
    void shouldNotSubscribeWhenNotConnected() {
        assertThatThrownBy(() -> stompClient.subscribe("/queue/test", mockMessageHandler))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not connected");
    }
    
    @Test
    void shouldNotUnsubscribeWhenNotConnected() {
        assertThatThrownBy(() -> stompClient.unsubscribe("sub-123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not connected");
    }
    
    @Test
    void shouldHandleDisconnectWhenNotConnected() {
        // Should not throw exception when disconnecting while not connected
        assertThatCode(() -> stompClient.disconnect()).doesNotThrowAnyException();
    }
}
