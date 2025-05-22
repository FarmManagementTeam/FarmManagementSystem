package model;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import database.DatabaseManager;

import java.sql.Statement;



public class Farmer extends User {
    
    //Instance variables
    private int farmersID ;
    private double areaSize ;
    private Warehouse warehouse ;
    private LocalDate registrationDate;

    //Constructors
    public Farmer(int usersID, String name, String surname, String email, String password,int farmersID, double areaSize){
        super(usersID, name, surname, email, password) ;
        this.farmersID = farmersID ;
        this.areaSize = areaSize ;
        this.warehouse = new Warehouse(this.farmersID) ; //every farmer gets their own warehouse when created
        this.registrationDate = LocalDate.now(); // Set registration date to current date for new farmers
    }

    public Farmer(int usersID, String name, String surname, String email, String password, int farmersID, double areaSize, LocalDate registrationDate) {
        super(usersID, name, surname, email, password);
        this.farmersID = farmersID;
        this.areaSize = areaSize;
        this.warehouse = new Warehouse(this.farmersID);
        this.registrationDate = registrationDate;
    }

   
    //Encapsulation - getter and setter methods
    public int getFarmersID() {
        return farmersID;
    }


    public void setFarmersID(int farmersID) {
        this.farmersID = farmersID;
    }
    
     public double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(double areaSize) {
        this.areaSize = areaSize;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    //Adds the given product to the farmer's warehouse
    public void addProduct(Product product){
        warehouse.addProduct(product) ;  
    }

    //Checks the warehouse for products that are ready to harvest and marks them as harvested 
    public void harvestReadyProducts(){
        warehouse.harvestProduct() ;
    }
    

    //This is a temporary method until SQL integration is done 
   /* @Override
    public boolean login(String email, String password){
        return this.getEmail().equals(email) && this.getPassword().equals(password) ;
    }  **/


    // Registers this farmer using the DatabaseManager
    public boolean register() {
        return DatabaseManager.registerFarmer(
            getName(),
            getSurname(),
            getEmail(),
            getPassword(),
            getAreaSize()
        );
    }

    // Static login method (returns a Farmer object)
    public static Farmer login(String email, String password) {
        return DatabaseManager.loginFarmer(email, password);
    }

}
