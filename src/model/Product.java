package model;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Product {
	private int productID;
	private String productName;
	private LocalDate plantingDate;  
	private double areaUsed;              // as square meter type (m^2) 
	private int harvestTimer;
	private LocalDate harvestDate;
	private boolean harvested = false;    // Not harvested as default
	private int warehouseID;              // Warehouse ID where the product is stored





	
	public Product(int ID, String name, double areaUsed, LocalDate plantingDate) {
		
		if (areaUsed < 0) {
		    throw new IllegalArgumentException("Area used cannot be negative");
		}
		
		this.productID = ID;
		this.productName = name;
		this.plantingDate =  plantingDate;                                 // We take the date from the user
		this.areaUsed = areaUsed;
		this.harvestTimer = getHarvestTimeByID(ID);                        // It goes to getHarvestTimeByID method to get the time duration by its ID
        this.harvestDate = plantingDate.plusDays(harvestTimer);            // Calculates harvest date by adding harvest duration to planting date.
        
	}

	

	public double calculateExpectedYield() {
		System.out.println("1.Tomato  2.Onion  3.Strawberry  4.Courgette  5.Cucumber  6.Corn  7.Wheat  8.Potato");
		switch (getID()) {
		
		case 1: 
			// tomato 
			return 8.1 * getAreaUsed();
		
		case 2:
			// onion
			return 4.3 * getAreaUsed();
		
		case 3: 
			
			// strawberry
			return 5.6 * getAreaUsed();
		
		case 4:
			// courgette // kabak
			return 4.1 * getAreaUsed();
			
		case 5:
			//cucumber
			return 2.8 * getAreaUsed();
					
		case 6:
			// corn
			return 0.9 * getAreaUsed();
			
			
		case 7:
			// wheat
			return 0.3 * getAreaUsed();
			
		case 8: 
			//potato
			return 3.7 * getAreaUsed();
			
		default:
		return 3.0 *getAreaUsed();
		 
		
		}
		
	}
	
	// It gives the needed time duration according to the plant type.
	public int getHarvestTimeByID(int ID) {     
        switch (ID) {
            case 1: return 70;  // Tomato 
            case 2: return 120; // Onion  
            case 3: return 45;  // Strawberry 
            case 4: return 45;  // Courgette 
            case 5: return 65;  // Cucumber 
            case 6: return 120; // Corn 
            case 7: return 240; // Wheat
            case 8: return 100; // Potato
            default: return 0;
        }
    }

	
	public long getDaysUntilHarvest() {                                 // Gives the remaining time for harvest. 
	    return ChronoUnit.DAYS.between(LocalDate.now(), harvestDate);
	}
	
	public boolean isHarvested() {
     return harvested;
    }


    // getters and setters

   public void setHarvested(boolean harvested) {
    this.harvested = harvested;
	 }
    public LocalDate getHarvestDate() {
        return harvestDate;
    }

	public int getID() {
		return productID;
	}

	public void setID(int productID) {
		this.productID = productID;
	}

	public String getName() {
		return productName;
	}

	public void setName(String productName) {
		this.productName = productName;
	}

	public LocalDate getPlantingDate() {
		return plantingDate;
	}

	public void setPlantingDate(LocalDate plantingDate) {
		this.plantingDate = plantingDate;
	}

	public double getAreaUsed() {
		return areaUsed;
	}


	public void setAreaUsed(double areaUsed) {
		if (areaUsed < 0) {
		    throw new IllegalArgumentException("Area used cannot be negative");
		}
			this.areaUsed = areaUsed;
			
	}

	public int getHarvestTimer() {
		return harvestTimer;
	}


	public void setHarvestTimer(int harvestTimer) {
		this.harvestTimer = harvestTimer;
	}


	public void setHarvestDate(LocalDate harvestDate) {
		this.harvestDate = harvestDate;
	}
	
	
	public String toString() {
	    return "Product{" +
	            "ID=" + productID +
	            ", name='" + productName + '\'' +
	            ", plantingDate=" + plantingDate +
	            ", areaUsed=" + areaUsed +
	            ", harvestTimer=" + harvestTimer +
	            ", harvestDate=" + harvestDate +
	            ", daysUntilHarvest=" + getDaysUntilHarvest() +
                ", Predicted yield=" + calculateExpectedYield() +
				", harvested=" + (harvested ? "YES\u2705" : "Waiting\u23F3") +   // this numbers for adding emojis
	            '}';
	}

	public int getWarehouseID() {
		return warehouseID;
	}

	public void setWarehouseID(int warehouseID) {
		this.warehouseID = warehouseID;
	}

}
