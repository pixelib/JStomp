package dev.pixelib.jstomp;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a STOMP frame.
 */
public class StompFrame {
    
    private static final String NULL_BYTE = "\0";
    private static final String LINE_FEED = "\n";
    
    private final StompCommand command;
    private final Map<String, String> headers;
    private String body;
    
    /**
     * Creates a new STOMP frame with the specified command.
     * 
     * @param command the STOMP command
     */
    public StompFrame(StompCommand command) {
        this.command = command;
        this.headers = new HashMap<>();
        this.body = "";
    }
    
    /**
     * Gets the command of this frame.
     * 
     * @return the command
     */
    public StompCommand getCommand() {
        return command;
    }
    
    /**
     * Adds a header to this frame.
     * 
     * @param name the header name
     * @param value the header value
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
    
    /**
     * Gets a header value by name.
     * 
     * @param name the header name
     * @return the header value, or null if not found
     */
    public String getHeader(String name) {
        return headers.get(name);
    }
    
    /**
     * Gets all headers.
     * 
     * @return a map of all headers
     */
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }
    
    /**
     * Sets the body of this frame.
     * 
     * @param body the body content
     */
    public void setBody(String body) {
        this.body = body != null ? body : "";
    }
    
    /**
     * Gets the body of this frame.
     * 
     * @return the body content
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Converts this frame to its string representation.
     * 
     * @return the string representation of this frame
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Command
        sb.append(command.name()).append(LINE_FEED);
        
        // Headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(escapeHeaderValue(header.getKey()))
              .append(":")
              .append(escapeHeaderValue(header.getValue()))
              .append(LINE_FEED);
        }
        
        // Empty line to separate headers from body
        sb.append(LINE_FEED);
        
        // Body
        sb.append(body);
        
        // Null terminator
        sb.append(NULL_BYTE);
        
        return sb.toString();
    }
    
    /**
     * Parses a STOMP frame from its string representation.
     * 
     * @param frameString the string representation
     * @return the parsed frame
     * @throws IllegalArgumentException if the frame is invalid
     */
    public static StompFrame parse(String frameString) {
        if (frameString == null || frameString.isEmpty()) {
            throw new IllegalArgumentException("Frame string cannot be null or empty");
        }
        
        // Remove null terminator if present
        if (frameString.endsWith(NULL_BYTE)) {
            frameString = frameString.substring(0, frameString.length() - 1);
        }
        
        String[] lines = frameString.split(LINE_FEED, -1);
        if (lines.length == 0) {
            throw new IllegalArgumentException("Invalid frame: no command found");
        }
        
        // Parse command
        String commandString = lines[0].trim();
        StompCommand command;
        try {
            command = StompCommand.valueOf(commandString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown STOMP command: " + commandString);
        }
        
        StompFrame frame = new StompFrame(command);
        
        // Parse headers
        int bodyStartIndex = 1;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                bodyStartIndex = i + 1;
                break;
            }
            
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = unescapeHeaderValue(line.substring(0, colonIndex));
                String value = unescapeHeaderValue(line.substring(colonIndex + 1));
                frame.addHeader(name, value);
            }
        }
        
        // Parse body
        if (bodyStartIndex < lines.length) {
            StringBuilder bodyBuilder = new StringBuilder();
            for (int i = bodyStartIndex; i < lines.length; i++) {
                if (i > bodyStartIndex) {
                    bodyBuilder.append(LINE_FEED);
                }
                bodyBuilder.append(lines[i]);
            }
            frame.setBody(bodyBuilder.toString());
        }
        
        return frame;
    }
    
    private static String escapeHeaderValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace(":", "\\c")
                   .replace("\r", "\\r");
    }
    
    private static String unescapeHeaderValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\r", "\r")
                   .replace("\\c", ":")
                   .replace("\\n", "\n")
                   .replace("\\\\", "\\");
    }
}
