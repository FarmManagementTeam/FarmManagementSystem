package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Product;
import model.Farmer;
import database.DatabaseManager;
import java.time.LocalDate;

public class AddProductDialogController {
    private Farmer currentFarmer;
    private ProductsController productsController;

    @FXML private TextField nameField;
    @FXML private DatePicker plantingDatePicker;
    @FXML private TextField areaField;
    @FXML private TextField harvestTimerField;

    public void setFarmer(Farmer farmer) {
        this.currentFarmer = farmer;
    }

    public void setProductsController(ProductsController controller) {
        this.productsController = controller;
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) return;

        try {
            String name = nameField.getText().trim();
            LocalDate plantingDate = plantingDatePicker.getValue();
            double area = Double.parseDouble(areaField.getText().trim());
            int harvestTimer = Integer.parseInt(harvestTimerField.getText().trim());

            int warehouseID = DatabaseManager.getWarehouseIDByFarmerID(currentFarmer.getFarmersID());
            
            Product newProduct = new Product(1, name, area, plantingDate);
            newProduct.setHarvestTimer(harvestTimer);
            
            boolean success = DatabaseManager.addProductToWarehouse(warehouseID, newProduct);
            
            if (success) {
                showAlert("Başarılı", "Ürün başarıyla eklendi!");
                closeDialog();
                productsController.initialize(); // Tabloyu yenile
            } else {
                showAlert("Hata", "Ürün eklenirken bir hata oluştu!");
            }

        } catch (NumberFormatException e) {
            showAlert("Hata", "Alan ve hasat süresi sayısal değer olmalıdır!");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Hata", "Ürün adı boş olamaz!");
            return false;
        }

        if (plantingDatePicker.getValue() == null) {
            showAlert("Hata", "Ekim tarihi seçilmelidir!");
            return false;
        }

        if (areaField.getText().trim().isEmpty()) {
            showAlert("Hata", "Kullanılan alan boş olamaz!");
            return false;
        }

        if (harvestTimerField.getText().trim().isEmpty()) {
            showAlert("Hata", "Hasat süresi boş olamaz!");
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 