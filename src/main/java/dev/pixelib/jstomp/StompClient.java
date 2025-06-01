package dev.pixelib.jstomp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A STOMP client implementation built on top of OkHTTP WebSocket.
 * 
 * This client provides a simple interface to connect to STOMP servers,
 * send messages, and subscribe to destinations.
 */
public class StompClient {
    
    private static final Logger logger = LoggerFactory.getLogger(StompClient.class);
    
    private final OkHttpClient httpClient;
    private final URI serverUri;
    private final Map<String, String> headers;
    private final Map<String, StompSubscription> subscriptions;
    private final AtomicLong messageIdCounter;
    private final AtomicBoolean connected;
    private final Gson gson;
    
    private WebSocket webSocket;
    private StompConnectionListener connectionListener;
    
    /**
     * Creates a new STOMP client.
     * 
     * @param serverUri the WebSocket URI to connect to
     */
    public StompClient(URI serverUri) {
        this(new OkHttpClient(), serverUri);
    }
    
    /**
     * Creates a new STOMP client with a custom OkHTTP client.
     * 
     * @param httpClient the OkHTTP client to use
     * @param serverUri the WebSocket URI to connect to
     */
    public StompClient(OkHttpClient httpClient, URI serverUri) {
        this(httpClient, serverUri, new GsonBuilder().create());
    }
    
    /**
     * Creates a new STOMP client with a custom OkHTTP client and Gson instance.
     * 
     * @param httpClient the OkHTTP client to use
     * @param serverUri the WebSocket URI to connect to
     * @param gson the Gson instance to use for JSON serialization/deserialization
     */
    public StompClient(OkHttpClient httpClient, URI serverUri, Gson gson) {
        this.httpClient = httpClient;
        this.serverUri = serverUri;
        this.headers = new ConcurrentHashMap<>();
        this.subscriptions = new ConcurrentHashMap<>();
        this.messageIdCounter = new AtomicLong(0);
        this.connected = new AtomicBoolean(false);
        this.gson = gson;
    }
    
    /**
     * Sets a connection listener to receive connection events.
     * 
     * @param listener the connection listener
     */
    public void setConnectionListener(StompConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    /**
     * Adds a header to be sent with the CONNECT frame.
     * 
     * @param name the header name
     * @param value the header value
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
    
    /**
     * Connects to the STOMP server.
     * 
     * @return a CompletableFuture that completes when the connection is established
     */
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        Request request = new Request.Builder()
                .url(serverUri.toString())
                .build();
        
        webSocket = httpClient.newWebSocket(request, new StompWebSocketListener(future));
        
        return future;
    }
    
    /**
     * Disconnects from the STOMP server.
     */
    public void disconnect() {
        if (webSocket != null && connected.get()) {
            StompFrame disconnectFrame = new StompFrame(StompCommand.DISCONNECT);
            sendFrame(disconnectFrame);
            webSocket.close(1000, "Normal closure");
            connected.set(false);
        }
    }
    
    /**
     * Sends a message to the specified destination.
     * 
     * @param destination the destination to send to
     * @param message the message body
     */
    public void send(String destination, String message) {
        send(destination, message, Map.of());
    }
    
    /**
     * Sends a message to the specified destination with custom headers.
     * 
     * @param destination the destination to send to
     * @param message the message body
     * @param headers additional headers
     */
    public void send(String destination, String message, Map<String, String> headers) {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to server");
        }
        
        StompFrame frame = new StompFrame(StompCommand.SEND);
        frame.addHeader("destination", destination);
        headers.forEach(frame::addHeader);
        frame.setBody(message);
        
        sendFrame(frame);
    }
    
    /**
     * Sends a JSON object to the specified destination.
     * The object will be serialized to JSON using the configured Gson instance.
     * 
     * @param destination the destination to send to
     * @param object the object to serialize and send
     * @throws StompJsonException if JSON serialization fails
     */
    public void sendJson(String destination, Object object) throws StompJsonException {
        sendJson(destination, object, Map.of());
    }
    
    /**
     * Sends a JSON object to the specified destination with custom headers.
     * The object will be serialized to JSON using the configured Gson instance.
     * The content-type header will be automatically set to "application/json" if not provided.
     * 
     * @param destination the destination to send to
     * @param object the object to serialize and send
     * @param headers additional headers
     * @throws StompJsonException if JSON serialization fails
     */
    public void sendJson(String destination, Object object, Map<String, String> headers) throws StompJsonException {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to server");
        }
        
        try {
            String jsonString = gson.toJson(object);
            
            // Create a copy of headers and add content-type if not present
            Map<String, String> jsonHeaders = new ConcurrentHashMap<>(headers);
            jsonHeaders.putIfAbsent("content-type", "application/json");
            
            send(destination, jsonString, jsonHeaders);
        } catch (Exception e) {
            throw new StompJsonException("Failed to serialize object to JSON", e);
        }
    }
    
    /**
     * Subscribes to a destination.
     * 
     * @param destination the destination to subscribe to
     * @param messageHandler the message handler
     * @return the subscription ID
     */
    public String subscribe(String destination, StompMessageHandler messageHandler) {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to server");
        }
        
        String subscriptionId = "sub-" + messageIdCounter.incrementAndGet();
        
        StompFrame frame = new StompFrame(StompCommand.SUBSCRIBE);
        frame.addHeader("id", subscriptionId);
        frame.addHeader("destination", destination);
        
        StompSubscription subscription = new StompSubscription(subscriptionId, destination, messageHandler);
        subscriptions.put(subscriptionId, subscription);
        
        sendFrame(frame);
        
        return subscriptionId;
    }
    
    /**
     * Subscribes to a destination with automatic JSON deserialization.
     * Messages received on this subscription will be automatically deserialized to the specified type.
     * 
     * @param <T> the type to deserialize JSON messages to
     * @param destination the destination to subscribe to
     * @param clazz the class type to deserialize to
     * @param jsonMessageHandler the JSON message handler
     * @return the subscription ID
     */
    public <T> String subscribeJson(String destination, Class<T> clazz, StompJsonMessageHandler<T> jsonMessageHandler) {
        return subscribeJson(destination, (Type) clazz, jsonMessageHandler);
    }
    
    /**
     * Subscribes to a destination with automatic JSON deserialization using a Type.
     * This method is useful for generic types like List&lt;MyClass&gt;.
     * Messages received on this subscription will be automatically deserialized to the specified type.
     * 
     * @param <T> the type to deserialize JSON messages to
     * @param destination the destination to subscribe to
     * @param type the Type to deserialize to (useful for generics)
     * @param jsonMessageHandler the JSON message handler
     * @return the subscription ID
     */
    public <T> String subscribeJson(String destination, Type type, StompJsonMessageHandler<T> jsonMessageHandler) {
        // Create a wrapper StompMessageHandler that handles JSON deserialization
        StompMessageHandler wrapper = message -> {
            try {
                T jsonObject = gson.fromJson(message.getBody(), type);
                StompJsonMessage<T> jsonMessage = new StompJsonMessage<>(message, jsonObject);
                jsonMessageHandler.onJsonMessage(destination, jsonObject, jsonMessage);
            } catch (Exception e) {
                try {
                    jsonMessageHandler.onJsonMessage(destination, null, new StompJsonMessage<>(message, null));
                } catch (StompJsonException jsonEx) {
                    logger.error("Error in JSON message handler", jsonEx);
                    if (connectionListener != null) {
                        connectionListener.onError(new StompJsonException("Failed to deserialize JSON message", e));
                    }
                }
            }
        };
        
        return subscribe(destination, wrapper);
    }
    
    /**
     * Unsubscribes from a subscription.
     * 
     * @param subscriptionId the subscription ID to unsubscribe from
     */
    public void unsubscribe(String subscriptionId) {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to server");
        }
        
        StompFrame frame = new StompFrame(StompCommand.UNSUBSCRIBE);
        frame.addHeader("id", subscriptionId);
        
        subscriptions.remove(subscriptionId);
        sendFrame(frame);
    }
    
    /**
     * Checks if the client is connected.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected.get();
    }
    
    private void sendFrame(StompFrame frame) {
        String frameString = frame.toString();
        logger.debug("Sending frame: {}", frameString);
        webSocket.send(frameString);
    }
    
    private class StompWebSocketListener extends WebSocketListener {
        
        private final CompletableFuture<Void> connectFuture;
        
        public StompWebSocketListener(CompletableFuture<Void> connectFuture) {
            this.connectFuture = connectFuture;
        }
        
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            logger.debug("WebSocket opened");
            
            // Send CONNECT frame
            StompFrame connectFrame = new StompFrame(StompCommand.CONNECT);
            connectFrame.addHeader("accept-version", "1.2");
            connectFrame.addHeader("host", serverUri.getHost());
            headers.forEach(connectFrame::addHeader);
            
            sendFrame(connectFrame);
        }
        
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            logger.debug("Received message: {}", text);
            
            try {
                StompFrame frame = StompFrame.parse(text);
                handleFrame(frame);
            } catch (Exception e) {
                logger.error("Error parsing STOMP frame", e);
                if (connectionListener != null) {
                    connectionListener.onError(e);
                }
            }
        }
        
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            logger.debug("WebSocket closing: {} - {}", code, reason);
            connected.set(false);
            if (connectionListener != null) {
                connectionListener.onDisconnected();
            }
        }
        
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            logger.error("WebSocket failure", t);
            connected.set(false);
            connectFuture.completeExceptionally(t);
            if (connectionListener != null) {
                connectionListener.onError(t);
            }
        }
        
        private void handleFrame(StompFrame frame) {
            switch (frame.getCommand()) {
                case CONNECTED:
                    connected.set(true);
                    connectFuture.complete(null);
                    if (connectionListener != null) {
                        connectionListener.onConnected();
                    }
                    break;
                    
                case MESSAGE:
                    String subscriptionId = frame.getHeader("subscription");
                    if (subscriptionId != null) {
                        StompSubscription subscription = subscriptions.get(subscriptionId);
                        if (subscription != null) {
                            StompMessage message = new StompMessage(
                                    frame.getHeader("destination"),
                                    frame.getBody(),
                                    frame.getHeaders()
                            );
                            subscription.getMessageHandler().onMessage(message);
                        }
                    }
                    break;
                    
                case ERROR:
                    String errorMessage = frame.getBody();
                    Exception error = new StompException("Server error: " + errorMessage);
                    if (connectionListener != null) {
                        connectionListener.onError(error);
                    }
                    break;
                    
                case RECEIPT:
                    // Handle receipt if needed
                    break;
                    
                default:
                    logger.warn("Unhandled STOMP command: {}", frame.getCommand());
            }
        }
    }
}
