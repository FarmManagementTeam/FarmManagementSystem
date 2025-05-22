package model;

/**
 * Console implementation of Notifiable interface
 * Prints notifications to the console
 */
public class ConsoleNotifier implements Notifiable {
    @Override
    public void sendingNotification(String message) {
        System.out.println("NOTIFICATION: " + message);
    }
    
    // Example usage:
    public static void main(String[] args) {
        Notifiable notifier = new ConsoleNotifier();
        notifier.sendingNotification("Product added successfully.");
    }
} 