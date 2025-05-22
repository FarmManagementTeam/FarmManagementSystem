package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Farmer;  // Farmer sınıfının paketine göre düzelt
import database.DatabaseManager; // Sınıfın paketine göre düzelt

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Hata", "Alanlar boş bırakılamaz.");
            return;
        }

        // Önce veritabanı bağlantısını test et
        if (!DatabaseManager.testConnection()) {
            showAlert("Hata", "Veritabanına bağlanılamıyor. Lütfen bağlantınızı kontrol edin.");
            return;
        }

        Farmer farmer = DatabaseManager.loginFarmer(email, password);

        if (farmer != null) {
            try {
                System.out.println("Giriş başarılı. Ana sayfaya yönlendiriliyor...");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
                Parent root = loader.load();
                
                MainController mainController = loader.getController();
                if (mainController == null) {
                    throw new Exception("MainController yüklenemedi!");
                }
                
                mainController.setFarmer(farmer);
                
                Stage stage = (Stage) emailField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Ana Sayfa - " + farmer.getName());
                stage.setMaximized(true);
                System.out.println("Ana sayfa yüklendi.");
            } catch (Exception e) {
                System.out.println("Ana sayfa yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
                showAlert("Hata", "Ana sayfa yüklenirken bir hata oluştu: " + e.getMessage());
            }
        } else {
            showAlert("Hata", "E-posta veya şifre yanlış.");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Kayıt Ekranı");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Sayfa yüklenirken bir hata oluştu!");
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
