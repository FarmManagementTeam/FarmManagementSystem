package model;
import java.time.LocalDate;

public class Notification {
    private int notificationsID;
    private int userID;
    private String message;
    private LocalDate date;
    private boolean isRead;


    public Notification(int notificationsID, int userID, String message, LocalDate date, boolean isRead) {
        this.notificationsID = notificationsID;
        this.userID = userID;
        this.message = message;
        this.date = date;
        this.isRead = isRead;
    }


    public int getNotificationsID() {
        return notificationsID;
    }


    public void setNotificationsID(int notificationsID) {
        this.notificationsID = notificationsID;
    }

    public int getUserID() {
    return userID;
    }

    public void setUserID(int userID) {
    this.userID = userID;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public LocalDate getDate() {
        return date;
    }


    public void setDate(LocalDate date) {
        this.date = date;
    }


    public boolean isRead() {
        return isRead;
    }


    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}
