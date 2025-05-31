package dev.pixelib.jstomp;

public class DebugFrameParsing {
    public static void main(String[] args) {
        String frameString = "CONNECTED\n" +
                            "version:1.2\n" +
                            "server:ActiveMQ/5.15.0\n" +
                            "\n\0";
        
        System.out.println("Original frame string:");
        System.out.println("'" + frameString + "'");
        System.out.println("Length: " + frameString.length());
        
        // Remove null terminator if present
        if (frameString.endsWith("\0")) {
            frameString = frameString.substring(0, frameString.length() - 1);
        }
        
        System.out.println("\nAfter removing null terminator:");
        System.out.println("'" + frameString + "'");
        
        String[] lines = frameString.split("\n");
        System.out.println("\nLines after split:");
        for (int i = 0; i < lines.length; i++) {
            System.out.println("Line " + i + ": '" + lines[i] + "'");
        }
        
        StompFrame frame = StompFrame.parse("CONNECTED\n" +
                            "version:1.2\n" +
                            "server:ActiveMQ/5.15.0\n" +
                            "\n\0");
        
        System.out.println("\nParsed frame:");
        System.out.println("Command: " + frame.getCommand());
        System.out.println("Headers: " + frame.getHeaders());
        System.out.println("Body: '" + frame.getBody() + "'");
        System.out.println("Body length: " + frame.getBody().length());
    }
}
