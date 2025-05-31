package dev.pixelib.jstomp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StompExceptionTest {
    
    @Test
    void shouldCreateExceptionWithMessage() {
        String errorMessage = "Something went wrong";
        StompException exception = new StompException(errorMessage);
        
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getCause()).isNull();
    }
    
    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String errorMessage = "Something went wrong";
        Throwable cause = new RuntimeException("Root cause");
        StompException exception = new StompException(errorMessage, cause);
        
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
