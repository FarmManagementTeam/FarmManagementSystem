package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import model.Product;
import model.Farmer;
import database.DatabaseManager;
import java.util.List;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.scene.Parent;

public class ProductsController {
    private Farmer currentFarmer;
    
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> plantingDateColumn;
    @FXML private TableColumn<Product, String> harvestDateColumn;
    @FXML private TableColumn<Product, Double> areaUsedColumn;
    @FXML private TableColumn<Product, Boolean> harvestedColumn;
    @FXML private TableColumn<Product, Void> actionsColumn;

    public void initialize() {
        setupTableColumns();
    }

    public void setFarmer(Farmer farmer) {
        this.currentFarmer = farmer;
        loadProducts();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        plantingDateColumn.setCellValueFactory(new PropertyValueFactory<>("plantingDate"));
        harvestDateColumn.setCellValueFactory(new PropertyValueFactory<>("harvestDate"));
        areaUsedColumn.setCellValueFactory(new PropertyValueFactory<>("areaUsed"));
        harvestedColumn.setCellValueFactory(new PropertyValueFactory<>("harvested"));
        
        // Özel durum gösterimi için
        harvestedColumn.setCellFactory(column -> new TableCell<Product, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Hasat Edildi" : "Ekim Aşamasında");
                }
            }
        });

        // İşlem butonları için
        actionsColumn.setCellFactory(column -> new TableCell<Product, Void>() {
            private final Button harvestButton = new Button("Hasat Et");
            private final Button deleteButton = new Button("Sil");
            private final HBox buttons = new HBox(5, harvestButton, deleteButton);

            {
                harvestButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                harvestButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleHarvest(product);
                });

                deleteButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDelete(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    harvestButton.setDisable(product.isHarvested());
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadProducts() {
        if (currentFarmer == null) return;

        int warehouseID = DatabaseManager.getWarehouseIDByFarmerID(currentFarmer.getFarmersID());
        List<Product> products = DatabaseManager.getProductsByWarehouse(warehouseID);
        productsTable.getItems().clear();
        productsTable.getItems().addAll(products);
    }

    @FXML
    private void showAddProductDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddProductDialog.fxml"));
            Parent root = loader.load();
            
            AddProductDialogController controller = loader.getController();
            controller.setFarmer(currentFarmer);
            controller.setProductsController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Yeni Ürün Ekle");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(productsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Dialog penceresi açılırken bir hata oluştu!");
        }
    }

    private void handleHarvest(Product product) {
        if (product.isHarvested()) {
            showAlert("Uyarı", "Bu ürün zaten hasat edilmiş!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ürün Hasadı");
        alert.setHeaderText(null);
        alert.setContentText(product.getName() + " ürününü hasat etmek istediğinizden emin misiniz?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (DatabaseManager.harvestProduct(product.getID())) {
                showAlert("Başarılı", "Ürün başarıyla hasat edildi!");
                loadProducts(); // Tabloyu güncelle
            } else {
                showAlert("Hata", "Hasat işlemi sırasında bir hata oluştu!");
            }
        }
    }

    private void handleDelete(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ürün Silme");
        alert.setHeaderText(null);
        alert.setContentText("Bu ürünü silmek istediğinizden emin misiniz?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // TODO: Implement delete functionality
            showAlert("Bilgi", "Silme işlemi yakında eklenecek!");
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