package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import database.DatabaseManager;
import javafx.scene.control.Button;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField areaSizeField;

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String areaSizeText = areaSizeField.getText().trim();

        // Validation checks
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || areaSizeText.isEmpty()) {
            showAlert("Hata", "Tüm alanları doldurunuz!");
            return;
        }

        // Email validation
        if (!email.contains("@")) {
            showAlert("Hata", "Geçersiz e-posta adresi! '@' işareti içermelidir.");
            emailField.setStyle("-fx-border-color: red;");
            return;
        } else {
            emailField.setStyle(""); // Reset style if valid
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Hata", "Şifreler eşleşmiyor!");
            return;
        }

        try {
            double areaSize = Double.parseDouble(areaSizeText);
            if (areaSize <= 0) {
                showAlert("Hata", "Arazi büyüklüğü pozitif bir sayı olmalıdır!");
                return;
            }

            // Register the farmer
            boolean success = DatabaseManager.registerFarmer(name, surname, email, password, areaSize);
            
            if (success) {
                showAlert("Başarılı", "Kayıt başarıyla tamamlandı!");
                goToLogin();
            } else {
                showAlert("Hata", "Kayıt sırasında bir hata oluştu. Lütfen tekrar deneyin.");
            }

        } catch (NumberFormatException e) {
            showAlert("Hata", "Arazi büyüklüğü geçerli bir sayı olmalıdır!");
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Giriş Ekranı");
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