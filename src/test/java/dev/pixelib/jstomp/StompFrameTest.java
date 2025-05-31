package dev.pixelib.jstomp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StompFrameTest {
    
    @Test
    void shouldCreateConnectFrame() {
        StompFrame frame = new StompFrame(StompCommand.CONNECT);
        frame.addHeader("accept-version", "1.2");
        frame.addHeader("host", "localhost");
        
        assertThat(frame.getCommand()).isEqualTo(StompCommand.CONNECT);
        assertThat(frame.getHeader("accept-version")).isEqualTo("1.2");
        assertThat(frame.getHeader("host")).isEqualTo("localhost");
    }
    
    @Test
    void shouldCreateSendFrameWithBody() {
        StompFrame frame = new StompFrame(StompCommand.SEND);
        frame.addHeader("destination", "/queue/test");
        frame.setBody("Hello, World!");
        
        assertThat(frame.getCommand()).isEqualTo(StompCommand.SEND);
        assertThat(frame.getHeader("destination")).isEqualTo("/queue/test");
        assertThat(frame.getBody()).isEqualTo("Hello, World!");
    }
    
    @Test
    void shouldSerializeFrameToString() {
        StompFrame frame = new StompFrame(StompCommand.SEND);
        frame.addHeader("destination", "/queue/test");
        frame.addHeader("content-type", "text/plain");
        frame.setBody("Hello, World!");
        
        String frameString = frame.toString();
        
        assertThat(frameString).contains("SEND\n");
        assertThat(frameString).contains("destination:/queue/test\n");
        assertThat(frameString).contains("content-type:text/plain\n");
        assertThat(frameString).contains("Hello, World!");
        assertThat(frameString).endsWith("\0");
    }
    
    @Test
    void shouldParseFrameFromString() {
        String frameString = "SEND\n" +
                            "destination:/queue/test\n" +
                            "content-type:text/plain\n" +
                            "\n" +
                            "Hello, World!\0";
        
        StompFrame frame = StompFrame.parse(frameString);
        
        assertThat(frame.getCommand()).isEqualTo(StompCommand.SEND);
        assertThat(frame.getHeader("destination")).isEqualTo("/queue/test");
        assertThat(frame.getHeader("content-type")).isEqualTo("text/plain");
        assertThat(frame.getBody()).isEqualTo("Hello, World!");
    }
    
    @Test
    void shouldParseConnectedFrame() {
        String frameString = "CONNECTED\n" +
                            "version:1.2\n" +
                            "server:ActiveMQ/5.15.0\n" +
                            "\n\0";
        
        StompFrame frame = StompFrame.parse(frameString);
        
        assertThat(frame.getCommand()).isEqualTo(StompCommand.CONNECTED);
        assertThat(frame.getHeader("version")).isEqualTo("1.2");
        assertThat(frame.getHeader("server")).isEqualTo("ActiveMQ/5.15.0");
        assertThat(frame.getBody()).isEmpty();
    }
    
    @Test
    void shouldEscapeHeaderValues() {
        StompFrame frame = new StompFrame(StompCommand.SEND);
        frame.addHeader("test", "value:with\\newlines\nand\rcarriage");
        
        String frameString = frame.toString();
        assertThat(frameString).contains("test:value\\cwith\\\\newlines\\nand\\rcarriage");
    }
    
    @Test
    void shouldThrowExceptionForInvalidCommand() {
        assertThatThrownBy(() -> StompFrame.parse("INVALID\n\n\0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown STOMP command: INVALID");
    }
    
    @Test
    void shouldThrowExceptionForEmptyFrame() {
        assertThatThrownBy(() -> StompFrame.parse(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Frame string cannot be null or empty");
    }
    

}
