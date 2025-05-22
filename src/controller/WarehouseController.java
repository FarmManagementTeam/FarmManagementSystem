package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableValue;
import model.Product;
import model.Farmer;
import database.DatabaseManager;
import java.util.List;
import java.util.stream.Collectors;

public class WarehouseController {
    private Farmer currentFarmer;

    @FXML private Label totalProductsLabel;
    @FXML private Label harvestedProductsLabel;
    @FXML private Label usedAreaLabel;

    @FXML private TableView<Product> upcomingHarvestTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> plantingDateColumn;
    @FXML private TableColumn<Product, String> harvestDateColumn;
    @FXML private TableColumn<Product, Long> daysUntilHarvestColumn;
    @FXML private TableColumn<Product, Double> expectedYieldColumn;

    @FXML private TableView<Product> harvestedProductsTable;
    @FXML private TableColumn<Product, String> harvestedNameColumn;
    @FXML private TableColumn<Product, String> harvestedDateColumn;
    @FXML private TableColumn<Product, Double> actualYieldColumn;
    @FXML private TableColumn<Product, Double> areaUsedColumn;

    public void initialize() {
        setupTableColumns();
    }

    public void setFarmer(Farmer farmer) {
        this.currentFarmer = farmer;
        loadWarehouseData();
    }

    private void setupTableColumns() {
        // Yaklaşan hasatlar tablosu
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        plantingDateColumn.setCellValueFactory(new PropertyValueFactory<>("plantingDate"));
        harvestDateColumn.setCellValueFactory(new PropertyValueFactory<>("harvestDate"));
        daysUntilHarvestColumn.setCellValueFactory(cellData -> 
            new SimpleLongProperty(cellData.getValue().getDaysUntilHarvest()).asObject());
        expectedYieldColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().calculateExpectedYield()).asObject());

        // Hasat edilmiş ürünler tablosu
        harvestedNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        harvestedDateColumn.setCellValueFactory(new PropertyValueFactory<>("harvestDate"));
        actualYieldColumn.setCellValueFactory(new PropertyValueFactory<>("actualYield"));
        areaUsedColumn.setCellValueFactory(new PropertyValueFactory<>("areaUsed"));
    }

    private void loadWarehouseData() {
        if (currentFarmer == null) return;

        int warehouseID = DatabaseManager.getWarehouseIDByFarmerID(currentFarmer.getFarmersID());
        List<Product> allProducts = DatabaseManager.getProductsByWarehouse(warehouseID);

        // İstatistikleri güncelle
        int totalProducts = allProducts.size();
        int harvestedCount = (int) allProducts.stream().filter(Product::isHarvested).count();
        double totalArea = allProducts.stream().mapToDouble(Product::getAreaUsed).sum();

        totalProductsLabel.setText(String.valueOf(totalProducts));
        harvestedProductsLabel.setText(String.valueOf(harvestedCount));
        usedAreaLabel.setText(String.format("%.2f", totalArea));

        // Yaklaşan hasatları filtrele ve sırala
        List<Product> upcomingHarvests = allProducts.stream()
            .filter(p -> !p.isHarvested())
            .sorted((p1, p2) -> p1.getHarvestDate().compareTo(p2.getHarvestDate()))
            .collect(Collectors.toList());

        // Hasat edilmiş ürünleri filtrele
        List<Product> harvestedList = allProducts.stream()
            .filter(Product::isHarvested)
            .collect(Collectors.toList());

        // Tabloları güncelle
        upcomingHarvestTable.getItems().clear();
        upcomingHarvestTable.getItems().addAll(upcomingHarvests);

        harvestedProductsTable.getItems().clear();
        harvestedProductsTable.getItems().addAll(harvestedList);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 