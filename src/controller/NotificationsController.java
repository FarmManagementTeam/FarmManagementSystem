package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.Notification;
import database.DatabaseManager;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.time.format.DateTimeFormatter;

public class NotificationsController implements Initializable {

    @FXML
    private ListView<HBox> notificationsListView;

    @FXML
    private ComboBox<String> filterComboBox;

    private ObservableList<Notification> notifications;
    private FilteredList<Notification> filteredNotifications;
    private int currentUserID; // Aktif kullanıcının ID'si

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ComboBox'ı başlat
        filterComboBox.setItems(FXCollections.observableArrayList("Tümü", "Okunmamış", "Okunmuş"));
        filterComboBox.setValue("Tümü");

        // Filtreleme olayını dinle
        filterComboBox.setOnAction(event -> filterNotifications());

        // Bildirimleri yükle
        loadNotifications();

        // Filtreleme değişikliklerini dinle
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterNotifications();
        });
    }

    public void setCurrentUser(int userID) {
        this.currentUserID = userID;
        loadNotifications(); // Kullanıcı ID'si set edildiğinde bildirimleri yükle
    }

    private void loadNotifications() {
        // Veritabanından tüm bildirimleri al
        List<Notification> notifList = DatabaseManager.getAllNotificationsByUserID(currentUserID);
        notifications = FXCollections.observableArrayList(notifList);
        
        // ListView'i güncelle
        updateListView();
    }

    private void updateListView() {
        notificationsListView.getItems().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Notification notification : notifications) {
            // Her bildirim için bir HBox oluştur
            HBox notificationBox = new HBox(10); // 10 pixel spacing
            
            // Bildirim mesajı ve tarihi
            Label messageLabel = new Label(notification.getMessage());
            Label dateLabel = new Label(notification.getDate().format(formatter));
            dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
            
            // Okundu/Okunmadı durumu için checkbox
            CheckBox readCheckBox = new CheckBox();
            readCheckBox.setSelected(notification.isRead());
            readCheckBox.setOnAction(event -> markAsRead(notification, readCheckBox.isSelected()));
            
            // Silme butonu
            Button deleteButton = new Button("Sil");
            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
            deleteButton.setOnAction(event -> deleteNotification(notification));
            
            // HBox'a elementleri ekle
            notificationBox.getChildren().addAll(readCheckBox, messageLabel, dateLabel, deleteButton);
            
            notificationsListView.getItems().add(notificationBox);
        }
    }

    private void filterNotifications() {
        String filter = filterComboBox.getValue();
        notificationsListView.getItems().clear();

        for (Notification notification : notifications) {
            if (filter.equals("Tümü") ||
                (filter.equals("Okunmamış") && !notification.isRead()) ||
                (filter.equals("Okunmuş") && notification.isRead())) {
                
                // Filtreye uyan bildirimleri göster
                updateListView();
            }
        }
    }

    @FXML
    private void markAllAsRead() {
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                DatabaseManager.markNotificationAsRead(notification.getNotificationsID());
            }
        }
        updateListView();
    }

    private void markAsRead(Notification notification, boolean isRead) {
        notification.setRead(isRead);
        DatabaseManager.markNotificationAsRead(notification.getNotificationsID());
        filterNotifications(); // Listeyi güncelle
    }

    private void deleteNotification(Notification notification) {
        DatabaseManager.deleteNotification(notification.getNotificationsID());
        notifications.remove(notification);
        updateListView();
    }

    // ListView'i yenileme metodu
    public void refreshNotifications() {
        loadNotifications();
    }
} 