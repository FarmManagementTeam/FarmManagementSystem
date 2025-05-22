package model;
import java.time.LocalDate; 
import java.util.List;
import java.util.ArrayList;

public class NotificationService{
  public NotificationService(){
    //empty default constructor
  }

  //sends notifications for products ready to be harvested
public void sendHarvestNotifications (Farmer farmer) {
    List<Product> allProducts = farmer.getWarehouse().getProducts();
    List<Product> readyToHarvest = new ArrayList<>();

    for (Product p : allProducts) {
      if (isReadyToHarvest(p)) {
          readyToHarvest.add(p);
      }
  }

    if (readyToHarvest.isEmpty()){
      System.out.println("Products aren't ready to get harvested.");
    } else {
      for(Product p : readyToHarvest){
        System.out.println("<<Harvesting Time>> Product '" +  p.getName() + "' is ready to be harvested!");
      }
    }
  }

  private boolean isReadyToHarvest(Product product) {
    return LocalDate.now().isAfter(product.getPlantingDate().plusDays(5)) && !product.isHarvested(); //we can later adjust this
}


  //sends notification for products that needs irrigation (has placeholder logic)
public void sendIrrigationNotification(Farmer farmer){
  List<Product> allProducts = farmer.getWarehouse().getProducts(); // Assuming getWarehouse() returns a Warehouse
  List<Product> needsWater = new ArrayList <>();

  for(Product p : allProducts){
    //placeholder logic for irrigation - needs an update in the final code
    if(LocalDate.now().isAfter(p.getPlantingDate().plusDays(3)) && !p.isHarvested()){ //If it’s been more than 3 days since the planting date and it hasn’t been harvested yet, then the plant might need irrigation.
      needsWater.add(p);
    } 
  }

  if (needsWater.isEmpty()){
    System.out.println("No products need irrigation.");
  } else {
    for(Product p : needsWater){
      System.out.println("<<Irrigation Time>> Product '" + p.getName() + "' needs irrigation!");
    }
  }
 }
}