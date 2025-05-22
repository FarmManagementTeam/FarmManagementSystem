package model;

import java.util.ArrayList;
import java.util.List;

//the warehouse class is responsible for managing a list of prdoucts
//It supports operations such as adding, updating, and harvesting products



public class Warehouse {
    //list to store all products in the warehouse 
    private List<Product> products;
    private int warehouseID;
    
    //constructor, initializes an empty product list
    public Warehouse (int warehouseID){
        products = new ArrayList<>();
        this.warehouseID = warehouseID ;
    }

    //getter setter methods
    public List<Product> getProducts() {
        return products;
    }


    //if the input is null, initialize an empty list
    public void setProducts(List<Product> products) {
        if (products != null){
            this.products = products;
        }
        else {
            this.products = new ArrayList<>();
        }
    }


    public int getWarehouseId() {
    return warehouseID;
}

public void setWarehouseId(int warehouseID) {
    this.warehouseID = warehouseID;
}

     //adds a new product to the warehouse 
    public void addProduct (Product p){
        products.add(p);
        System.out.println("product added: "+ p.getName());
    }

    // updates an existing product in the warehouse
    public void updateProduct(Product updatedProduct){
        for (int i= 0; i<products.size(); i++){
            Product p = products.get(i);
            // compare the plant IDs to check if this is the product to update
            if(p.getID() == updatedProduct.getID()){
                products.set(i, updatedProduct);
                // replace the product at index i with the updated one
                System.out.println("product updated: "+updatedProduct.getName());
                return;
            }

        }
        // if no product with matching plantId is found
        System.out.println("product not found, update failed!");
        
    }

    // marks the first non-harvested product in the list as harvested
    public void harvestProduct(){
        for (Product p : products){
            if (!p.isHarvested()){
                p.setHarvested(true);
                System.out.println("product has been harvested: "+ p.getName());
                return;
            }
        }
        //If all products are already harvested, prints a message
        System.out.println("already harvested");
    }

    
    
}