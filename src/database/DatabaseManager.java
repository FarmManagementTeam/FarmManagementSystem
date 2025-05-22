package database;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.time.LocalDate;

import model.Farmer;
import model.Notification;
import model.Product;
import model.Notifiable;
import model.ConsoleNotifier;

public class DatabaseManager {

    // Establishes a connection to the database
    public static Connection connectDB() {
        // Railway veritabanı bağlantı bilgileri
        String url = "jdbc:mysql://centerbeam.proxy.rlwy.net:49364/railway";
        String user = "root";
        String password = "NTYcpPHRlSFHDJeBbbHHKBrtkLZVdYJk";

        try {
            // JDBC sürücüsünü yükle
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Veritabanına bağlan
            Connection conn = DriverManager.getConnection(url, user, password);
            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC sürücüsü bulunamadı!");
            e.printStackTrace();
            return null;

        } catch (SQLException e) {
            System.out.println("Veritabanına bağlanırken hata oluştu: " + e.getMessage());
            return null;
        }
    }

    public static int getWarehouseIDByFarmerID(int farmerID) {
    try (Connection conn = connectDB()) {
        if (conn == null) {
            System.out.println("Veritabanı bağlantısı kurulamadı!");
            return -1;
        }
        
        String query = "SELECT warehouseID FROM warehouse WHERE warehouseID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, farmerID);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            int warehouseID = rs.getInt("warehouseID");
            System.out.println("Depo ID bulundu: " + warehouseID);
            return warehouseID;
        } else {
            System.out.println("Bu çiftçi için depo bulunamadı (ID: " + farmerID + ")");
            return -1;
        }
    } catch (SQLException e) {
        System.out.println("Depo ID'si alınırken hata: " + e.getMessage());
        e.printStackTrace();
        return -1;
    }
}
    public static boolean addProductToWarehouse(int warehouseID, Product product) {
        try (Connection conn = connectDB()) {
            if (conn == null) return false;

            conn.setAutoCommit(false);
            try {
                String insertQuery = "INSERT INTO product (productName, plantingDate, areaUsed, harvestTimer, harvestDate, harvested, warehouseID) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, product.getName());
                stmt.setDate(2, Date.valueOf(product.getPlantingDate()));
                stmt.setDouble(3, product.getAreaUsed());
                stmt.setInt(4, product.getHarvestTimer());
                stmt.setDate(5, Date.valueOf(product.getHarvestDate()));
                stmt.setBoolean(6, product.isHarvested());
                stmt.setInt(7, warehouseID);

                stmt.executeUpdate();
                
                // Commit the product insertion first
                conn.commit();
                
                // Send notification in a separate transaction
                // Get the farmer's userID for notification
                int userID = getUserIDByWarehouseID(warehouseID);
                if (userID != -1) {
                    // Add product notification
                    Notification productNotif = new Notification(
                        0, // ID will be auto-generated
                        userID,
                        product.getName() + " ürünü başarıyla eklendi. Hasat tarihi: " + 
                        product.getHarvestDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        LocalDate.now(),
                        false
                    );
                    addNotification(productNotif);
                }
                
                System.out.println("Product added to warehouse.");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Failed to add product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<Product> getProductsByWarehouse(int warehouseID) {
    List<Product> products = new ArrayList<>();

    try (Connection conn = connectDB()) {
        if (conn == null) return products;

        String query = "SELECT * FROM product WHERE warehouseID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, warehouseID);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Product p = new Product(
                rs.getInt("productID"),
                rs.getString("productName"),
                rs.getDouble("areaUsed"),
                rs.getDate("plantingDate").toLocalDate()
            );
            p.setHarvested(rs.getBoolean("harvested"));
            p.setHarvestTimer(rs.getInt("harvestTimer"));
            p.setHarvestDate(rs.getDate("harvestDate").toLocalDate());

            products.add(p);
        }

    } catch (SQLException e) {
        System.out.println("Failed to fetch products.");
        e.printStackTrace();
    }

    return products;
}
    public static boolean addNotification(Notification notif) {
    try (Connection conn = connectDB()) {
        if (conn == null) return false;

        String query = "INSERT INTO notifications (userID, message, date, isRead) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, notif.getUserID());
        stmt.setString(2, notif.getMessage());
        stmt.setDate(3, Date.valueOf(notif.getDate()));
        stmt.setBoolean(4, notif.isRead());

        stmt.executeUpdate();
        System.out.println("Notification sent.");
        return true;

    } catch (SQLException e) {
        System.out.println("Failed to send notification.");
        e.printStackTrace();
        return false;
    }
}
    public static List<Notification> getUnreadNotificationsByUserID(int userID) {
    List<Notification> list = new ArrayList<>();

    try (Connection conn = connectDB()) {
        if (conn == null) return list;

        String query = "SELECT * FROM notifications WHERE userID = ? AND isRead = false";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userID);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Notification notif = new Notification(
                rs.getInt("notificationsID"),
                rs.getInt("userID"),
                rs.getString("message"),
                rs.getDate("date").toLocalDate(),
                rs.getBoolean("isRead")
            );
            list.add(notif);
        }

    } catch (SQLException e) {
        System.out.println("Failed to retrieve notifications.");
        e.printStackTrace();
    }

    return list;
}

    public static boolean registerProduct(String productName, Date plantingDate, double areaUsed, int harvestTimer, int warehouseID) {
        try (Connection conn = connectDB()) {
            if (conn == null) return false;

            // Hasat tarihi: plantingDate + harvestTimer (gün cinsinden)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(plantingDate);
            calendar.add(Calendar.DAY_OF_YEAR, harvestTimer);
            java.sql.Date harvestDate = new java.sql.Date(calendar.getTimeInMillis());

            String insertProduct = "INSERT INTO product (productName, plantingDate, areaUsed, harvestTimer, harvestDate, harvested, warehouseID) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertProduct);
            stmt.setString(1, productName);
            stmt.setDate(2, new java.sql.Date(plantingDate.getTime()));
            stmt.setDouble(3, areaUsed);
            stmt.setInt(4, harvestTimer);
            stmt.setDate(5, harvestDate); // Otomatik hesaplanan hasat tarihi
            stmt.setBoolean(6, false);    // İlk eklemede henüz hasat edilmedi
            stmt.setInt(7, warehouseID);

            stmt.executeUpdate();
            System.out.println("Product registered successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("Error registering product.");
            e.printStackTrace();
            return false;
        }
    }

    // Registers a new farmer into the database
    public static boolean registerFarmer(String name, String surname, String email, String password, double areaSize) {
        Connection conn = null;
        try {
            conn = connectDB();
            if (conn == null) return false;

            // Disable auto-commit to start transaction
            conn.setAutoCommit(false);

            try {
                // Check if the email is already registered
                String checkQuery = "SELECT * FROM users WHERE email = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("This email is already registered. Please log in.");
                    return false;
                }

                // Insert the user into the 'users' table
                String insertUser = "INSERT INTO users (name, surname, email, password) VALUES (?, ?, ?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, name);
                userStmt.setString(2, surname);
                userStmt.setString(3, email);
                userStmt.setString(4, password);
                userStmt.executeUpdate();

                // Retrieve the generated usersID
                ResultSet keys = userStmt.getGeneratedKeys();
                if (keys.next()) {
                    int usersID = keys.getInt(1);

                    // Insert the farmer-specific data into the 'farmers' table
                    String insertFarmer = "INSERT INTO farmers (farmersID, areaSize) VALUES (?, ?)";
                    PreparedStatement farmerStmt = conn.prepareStatement(insertFarmer);
                    farmerStmt.setInt(1, usersID);
                    farmerStmt.setDouble(2, areaSize);
                    farmerStmt.executeUpdate();

                    // Now insert warehouse row
                    String insertWarehouse = "INSERT INTO warehouse (warehouseID) VALUES (?)";
                    PreparedStatement warehouseStmt = conn.prepareStatement(insertWarehouse);
                    warehouseStmt.setInt(1, usersID);
                    warehouseStmt.executeUpdate();

                    // Commit the main transaction first
                    conn.commit();

                    // Now add the welcome notification in a separate transaction
                    try (Connection notifConn = connectDB()) {
                        if (notifConn != null) {
                            Notification welcomeNotif = new Notification(
                                0, // ID will be auto-generated
                                usersID,
                                "Hoş geldiniz " + name + "! Çiftlik Yönetim Sistemine başarıyla kayıt oldunuz.",
                                LocalDate.now(),
                                false
                            );
                            addNotification(welcomeNotif);
                        }
                    } catch (SQLException e) {
                        // Log notification error but don't fail the registration
                        System.out.println("Bildirim gönderilirken hata oluştu: " + e.getMessage());
                    }

                    System.out.println("Registration successful! Welcome, " + name + ".");
                    return true;
                } else {
                    conn.rollback();
                    System.out.println("Error: Could not retrieve user ID.");
                    return false;
                }

            } catch (SQLException e) {
                if (conn != null) {
                    conn.rollback();
                }
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Registration failed due to an error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Logs in a farmer by verifying credentials
    public static Farmer loginFarmer(String email, String password) {
        try (Connection conn = connectDB()) {
            if (conn == null) return null;

            // Check email and password in the 'users' table
            String userQuery = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, email);
            userStmt.setString(2, password);
            ResultSet userRs = userStmt.executeQuery();

            if (userRs.next()) {
                int usersID = userRs.getInt("usersID");
                String name = userRs.getString("name");
                String surname = userRs.getString("surname");

                // Check if the user is also a farmer
                String farmerQuery = "SELECT * FROM farmers WHERE farmersID = ?";
                PreparedStatement farmerStmt = conn.prepareStatement(farmerQuery);
                farmerStmt.setInt(1, usersID);
                ResultSet farmerRs = farmerStmt.executeQuery();

                if (farmerRs.next()) {
                    double areaSize = farmerRs.getDouble("areaSize");
                    LocalDate registrationDate = LocalDate.now(); // Default to today's date
                    
                    System.out.println("Login successful. Welcome back, " + name + "!");
                    return new Farmer(usersID, name, surname, email, password, usersID, areaSize, registrationDate);
                } else {
                    System.out.println("Login successful, but no farmer data found for this user.");
                }

            } else {
                System.out.println("Incorrect email or password. Please try again.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred during login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean testConnection() {
        try (Connection conn = connectDB()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Veritabanı bağlantısı başarılı!");
                return true;
            } else {
                System.out.println("Veritabanı bağlantısı başarısız!");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı bağlantı hatası: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean markNotificationAsRead(int notificationID) {
        try (Connection conn = connectDB()) {
            if (conn == null) return false;

            String query = "UPDATE notifications SET isRead = true WHERE notificationsID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, notificationID);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.out.println("Failed to mark notification as read.");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteNotification(int notificationID) {
        try (Connection conn = connectDB()) {
            if (conn == null) return false;

            String query = "DELETE FROM notifications WHERE notificationsID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, notificationID);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.out.println("Failed to delete notification.");
            e.printStackTrace();
            return false;
        }
    }

    public static List<Notification> getAllNotificationsByUserID(int userID) {
        List<Notification> list = new ArrayList<>();

        try (Connection conn = connectDB()) {
            if (conn == null) return list;

            String query = "SELECT * FROM notifications WHERE userID = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notif = new Notification(
                    rs.getInt("notificationsID"),
                    rs.getInt("userID"),
                    rs.getString("message"),
                    rs.getDate("date").toLocalDate(),
                    rs.getBoolean("isRead")
                );
                list.add(notif);
            }

        } catch (SQLException e) {
            System.out.println("Failed to retrieve notifications.");
            e.printStackTrace();
        }

        return list;
    }

    public static boolean harvestProduct(int productID) {
        try (Connection conn = connectDB()) {
            if (conn == null) return false;

            // Ürünü hasat edildi olarak işaretle
            String updateQuery = "UPDATE product SET harvested = true WHERE productID = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setInt(1, productID);
            
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                // Hasat bildirimi oluştur
                Product product = getProductByID(productID);
                if (product != null) {
                    int userID = getUserIDByWarehouseID(product.getWarehouseID());
                    if (userID != -1) {
                        Notification notification = new Notification(
                            0, // ID otomatik oluşturulacak
                            userID,
                            product.getName() + " ürününüz hasat edildi.",
                            java.time.LocalDate.now(),
                            false
                        );
                        addNotification(notification);
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println("Ürün hasat edilirken hata oluştu.");
            e.printStackTrace();
            return false;
        }
    }

    public static Product getProductByID(int productID) {
        try (Connection conn = connectDB()) {
            if (conn == null) return null;

            String query = "SELECT * FROM product WHERE productID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, productID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = new Product(
                    rs.getInt("productID"),
                    rs.getString("productName"),
                    rs.getDouble("areaUsed"),
                    rs.getDate("plantingDate").toLocalDate()
                );
                product.setHarvested(rs.getBoolean("harvested"));
                product.setHarvestTimer(rs.getInt("harvestTimer"));
                product.setHarvestDate(rs.getDate("harvestDate").toLocalDate());
                product.setWarehouseID(rs.getInt("warehouseID"));
                return product;
            }

        } catch (SQLException e) {
            System.out.println("Ürün bilgisi alınırken hata oluştu.");
            e.printStackTrace();
        }
        return null;
    }

    public static int getUserIDByWarehouseID(int warehouseID) {
        try (Connection conn = connectDB()) {
            if (conn == null) return -1;

            String query = "SELECT farmersID FROM warehouse WHERE warehouseID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, warehouseID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("farmersID");
            }

        } catch (SQLException e) {
            System.out.println("Kullanıcı ID'si alınırken hata oluştu.");
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean updateFarmer(Farmer farmer, boolean updatePassword) {
        try (Connection conn = connectDB()) {
            if (conn == null) return false;

            conn.setAutoCommit(false); // Transaction başlat

            try {
                // Önce email kontrolü yap (mevcut email hariç)
                String checkQuery = "SELECT * FROM users WHERE email = ? AND usersID != ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, farmer.getEmail());
                checkStmt.setInt(2, farmer.getUsersID());
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    System.out.println("Bu email adresi başka bir kullanıcı tarafından kullanılıyor.");
                    return false;
                }

                // Users tablosunu güncelle
                StringBuilder updateUserQuery = new StringBuilder("UPDATE users SET name = ?, surname = ?, email = ?");
                if (updatePassword) {
                    updateUserQuery.append(", password = ?");
                }
                updateUserQuery.append(" WHERE usersID = ?");
                
                PreparedStatement userStmt = conn.prepareStatement(updateUserQuery.toString());
                int paramIndex = 1;
                userStmt.setString(paramIndex++, farmer.getName());
                userStmt.setString(paramIndex++, farmer.getSurname());
                userStmt.setString(paramIndex++, farmer.getEmail());
                if (updatePassword) {
                    userStmt.setString(paramIndex++, farmer.getPassword());
                }
                userStmt.setInt(paramIndex, farmer.getUsersID());
                userStmt.executeUpdate();

                // Farmers tablosunu güncelle
                String updateFarmerQuery = "UPDATE farmers SET areaSize = ? WHERE farmersID = ?";
                PreparedStatement farmerStmt = conn.prepareStatement(updateFarmerQuery);
                farmerStmt.setDouble(1, farmer.getAreaSize());
                farmerStmt.setInt(2, farmer.getFarmersID());
                farmerStmt.executeUpdate();

                conn.commit(); // Transaction'ı tamamla
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Hata durumunda geri al
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Çiftçi bilgileri güncellenirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Checks for products that are close to harvest time and sends notifications
    public static void checkAndNotifyHarvestDates() {
        try (Connection conn = connectDB()) {
            if (conn == null) return;

            // Get all non-harvested products
            String query = "SELECT p.*, w.warehouseID FROM product p " +
                         "JOIN warehouse w ON p.warehouseID = w.warehouseID " +
                         "WHERE p.harvested = false";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            LocalDate today = LocalDate.now();
            
            while (rs.next()) {
                LocalDate harvestDate = rs.getDate("harvestDate").toLocalDate();
                String productName = rs.getString("productName");
                int warehouseID = rs.getInt("warehouseID");
                
                // Calculate days until harvest
                long daysUntilHarvest = java.time.temporal.ChronoUnit.DAYS.between(today, harvestDate);
                
                // If harvest is within 3 days
                if (daysUntilHarvest <= 3 && daysUntilHarvest >= 0) {
                    int userID = getUserIDByWarehouseID(warehouseID);
                    if (userID != -1) {
                        String message;
                        if (daysUntilHarvest == 0) {
                            message = productName + " ürününüz bugün hasat için hazır!";
                        } else {
                            message = productName + " ürününüzün hasadına " + daysUntilHarvest + " gün kaldı!";
                        }
                        
                        Notification harvestNotif = new Notification(
                            0,
                            userID,
                            message,
                            today,
                            false
                        );
                        addNotification(harvestNotif);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Hasat kontrol bildirimleri gönderilirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
