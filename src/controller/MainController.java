package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Product;
import model.Notification;
import model.Farmer;
import database.DatabaseManager;
import java.util.List;
import javafx.scene.layout.VBox;

public class MainController {
    private Farmer currentFarmer;

    @FXML private Label userNameLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label activeCropsLabel;
    @FXML private Label totalAreaLabel;
    @FXML private VBox contentArea;
    
    @FXML private TableView<Product> recentProductsTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> plantingDateColumn;
    @FXML private TableColumn<Product, String> harvestDateColumn;
    @FXML private TableColumn<Product, Double> areaUsedColumn;
    
    @FXML private ListView<String> notificationList;

    @FXML
    private VBox notificationPanel;

    public void initialize() {
        if (recentProductsTable != null) {
            setupTableColumns();
        }
        
        // Start periodic harvest date check (check every hour)
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                DatabaseManager.checkAndNotifyHarvestDates();
            }
        }, 0, 3600000); // Initial delay 0, period 1 hour (3600000 milliseconds)
    }

    public void setFarmer(Farmer farmer) {
        if (farmer == null) {
            showAlert("Hata", "Çiftçi bilgileri yüklenemedi!");
            return;
        }
        
        this.currentFarmer = farmer;
        if (userNameLabel != null) {
            userNameLabel.setText(farmer.getName() + " " + farmer.getSurname());
        }
        loadDashboardData();
        
        // Check harvest dates immediately when farmer logs in
        DatabaseManager.checkAndNotifyHarvestDates();
    }

    private void setupTableColumns() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        plantingDateColumn.setCellValueFactory(new PropertyValueFactory<>("plantingDate"));
        harvestDateColumn.setCellValueFactory(new PropertyValueFactory<>("harvestDate"));
        areaUsedColumn.setCellValueFactory(new PropertyValueFactory<>("areaUsed"));
    }

    private void loadDashboardData() {
        if (currentFarmer == null) {
            System.out.println("Çiftçi bilgisi bulunamadı!");
            return;
        }

        try {
            System.out.println("Çiftçi ID: " + currentFarmer.getFarmersID());
            int warehouseID = DatabaseManager.getWarehouseIDByFarmerID(currentFarmer.getFarmersID());
            
            if (warehouseID == -1) {
                showAlert("Hata", "Depo bilgisi bulunamadı!");
                return;
            }
            
            System.out.println("Depo ID: " + warehouseID);
            List<Product> products = DatabaseManager.getProductsByWarehouse(warehouseID);
            System.out.println("Bulunan ürün sayısı: " + products.size());
            
            // Update statistics
            int totalProducts = products.size();
            int activeProducts = (int) products.stream().filter(p -> !p.isHarvested()).count();
            double totalArea = currentFarmer.getAreaSize();

            if (totalProductsLabel != null) {
                totalProductsLabel.setText(String.valueOf(totalProducts));
            }
            if (activeCropsLabel != null) {
                activeCropsLabel.setText(String.valueOf(activeProducts));
            }
            if (totalAreaLabel != null) {
                totalAreaLabel.setText(String.format("%.2f", totalArea));
            }

            // Update recent products table
            if (recentProductsTable != null) {
                recentProductsTable.getItems().clear();
                recentProductsTable.getItems().addAll(products);
                System.out.println("Ürün tablosu güncellendi");
            } else {
                System.out.println("HATA: recentProductsTable null!");
            }

            // Load notifications
            if (notificationList != null) {
                List<Notification> notifications = DatabaseManager.getUnreadNotificationsByUserID(currentFarmer.getUsersID());
                notificationList.getItems().clear();
                for (Notification notification : notifications) {
                    notificationList.getItems().add(notification.getMessage());
                }
                System.out.println("Bildirimler güncellendi");
            }
        } catch (Exception e) {
            System.out.println("Hata detayı: " + e.getMessage());
            e.printStackTrace();
            showAlert("Hata", "Veriler yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Giriş Ekranı");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Çıkış yapılırken bir hata oluştu!");
        }
    }

    @FXML
    private void showDashboard() {
        try {
            // Ana sayfa görünümünü yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Parent dashboardView = loader.load();
            
            // Yeni controller'a mevcut çiftçi bilgisini aktar
            MainController newController = loader.getController();
            newController.setFarmer(currentFarmer);
            
            // Ana pencereyi güncelle
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(dashboardView);
            stage.setScene(scene);
            stage.setTitle("Ana Sayfa - " + currentFarmer.getName());
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Ana sayfa yüklenirken bir hata oluştu!");
        }
    }

    @FXML
    private void showProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProductsView.fxml"));
            VBox productView = loader.load();
            
            ProductsController controller = loader.getController();
            controller.setFarmer(currentFarmer);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(productView);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Ürünler sayfası yüklenirken bir hata oluştu!");
        }
    }

    @FXML
    private void showWarehouse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WarehouseView.fxml"));
            VBox warehouseView = loader.load();
            
            WarehouseController controller = loader.getController();
            controller.setFarmer(currentFarmer);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(warehouseView);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Depo sayfası yüklenirken bir hata oluştu!");
        }
    }

    @FXML
    private void showNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NotificationsView.fxml"));
            Parent notificationsView = loader.load();
            
            NotificationsController controller = loader.getController();
            controller.setCurrentUser(currentFarmer.getUsersID());
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(notificationsView);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Bildirimler sayfası yüklenirken bir hata oluştu!");
        }
    }

    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProfileView.fxml"));
            Parent profileView = loader.load();
            
            ProfileController controller = loader.getController();
            controller.setFarmer(currentFarmer);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(profileView);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Profil sayfası yüklenirken bir hata oluştu!");
        }
    }

    @FXML
    private void toggleNotificationPanel() {
        if (notificationPanel != null) {
            notificationPanel.setVisible(!notificationPanel.isVisible());
            notificationPanel.setManaged(!notificationPanel.isManaged());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
