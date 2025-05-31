# JStomp

A Java STOMP (Simple Text Oriented Messaging Protocol) client library built on top of OkHTTP WebSocket. This library provides a simple and efficient way to connect to STOMP-enabled message brokers like ActiveMQ, RabbitMQ, and others.

## Features

- Built on top of OkHTTP for robust WebSocket communication
- Support for STOMP 1.2 protocol
- Asynchronous connection handling with CompletableFuture
- Message subscription and publishing
- Connection state management
- Proper header escaping according to STOMP specification
- Comprehensive test coverage

## Requirements

- Java 21 or higher
- Gradle 8.5 or higher

## Usage

### Basic Connection and Messaging

```java
import dev.pixelib.jstomp.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

// Create a STOMP client
StompClient client = new StompClient(URI.create("ws://localhost:61614/stomp"));

// Set up connection listener (optional)
client.setConnectionListener(new StompConnectionListener() {
    @Override
    public void onConnected() {
        System.out.println("Connected to STOMP server");
    }
    
    @Override
    public void onDisconnected() {
        System.out.println("Disconnected from STOMP server");
    }
    
    @Override
    public void onError(Throwable error) {
        System.err.println("STOMP error: " + error.getMessage());
    }
});

// Connect to the server
CompletableFuture<Void> connectionFuture = client.connect();
connectionFuture.join(); // Wait for connection

// Subscribe to a destination
String subscriptionId = client.subscribe("/topic/messages", message -> {
    System.out.println("Received: " + message.getBody());
    System.out.println("From: " + message.getDestination());
});

// Send a message
client.send("/topic/messages", "Hello, STOMP!");

// Unsubscribe
client.unsubscribe(subscriptionId);

// Disconnect
client.disconnect();
```

### Custom Headers

```java
// Add custom headers for authentication
client.addHeader("login", "username");
client.addHeader("passcode", "password");

// Send message with custom headers
Map<String, String> headers = Map.of(
    "content-type", "application/json",
    "priority", "high"
);
client.send("/topic/messages", "{\"message\": \"Hello JSON\"}", headers);
```

### Using Custom OkHTTP Client

```java
OkHttpClient customHttpClient = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build();

StompClient client = new StompClient(customHttpClient, URI.create("ws://localhost:61614/stomp"));
```

## Building

To build the project:

```bash
./gradlew build
```

To run tests:

```bash
./gradlew test
```

To generate Javadocs:

```bash
./gradlew javadoc
```

## Dependencies

- **OkHTTP 4.12.0** - For WebSocket communication
- **SLF4J 2.0.9** - For logging
- **JUnit 5** - For testing
- **Mockito** - For mocking in tests
- **AssertJ** - For fluent assertions in tests

## STOMP Protocol Support

This library supports the STOMP 1.2 specification with the following commands:

### Client Commands
- `CONNECT` - Connect to server
- `SEND` - Send message to destination  
- `SUBSCRIBE` - Subscribe to destination
- `UNSUBSCRIBE` - Unsubscribe from destination
- `DISCONNECT` - Disconnect from server
- `ACK` - Acknowledge message
- `NACK` - Negative acknowledge message
- `BEGIN` - Begin transaction
- `COMMIT` - Commit transaction
- `ABORT` - Abort transaction

### Server Commands
- `CONNECTED` - Connection acknowledgment
- `MESSAGE` - Message from subscription
- `RECEIPT` - Receipt acknowledgment
- `ERROR` - Error from server

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for your changes
5. Ensure all tests pass
6. Submit a pull request

## Examples

See the `src/test/java/dev/pixelib/jstomp/example/` directory for more usage examples.
