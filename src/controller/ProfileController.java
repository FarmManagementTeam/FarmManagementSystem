package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Farmer;
import java.time.format.DateTimeFormatter;

public class ProfileController {
    private Farmer currentFarmer;

    @FXML private Label nameLabel;
    @FXML private Label surnameLabel;
    @FXML private Label emailLabel;
    @FXML private Label areaSizeLabel;
    @FXML private Label registrationDateLabel;

    public void setFarmer(Farmer farmer) {
        this.currentFarmer = farmer;
        loadFarmerData();
    }

    private void loadFarmerData() {
        if (currentFarmer == null) return;

        nameLabel.setText(currentFarmer.getName());
        surnameLabel.setText(currentFarmer.getSurname());
        emailLabel.setText(currentFarmer.getEmail());
        areaSizeLabel.setText(String.format("%.2f", currentFarmer.getAreaSize()));
        
        // Format and display registration date if available
        if (currentFarmer.getRegistrationDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            registrationDateLabel.setText(currentFarmer.getRegistrationDate().format(formatter));
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