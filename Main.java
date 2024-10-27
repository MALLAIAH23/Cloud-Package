import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final StockManager stockManager = new StockManager("inventory.txt");

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("======= Stock Management System =======");
            System.out.println("1. Add Item");
            System.out.println("2. Remove Item");
            System.out.println("3. Update Item Quantity and Price");
            System.out.println("4. Display Inventory");
            System.out.println("5. Search for Item");
            System.out.println("6. Purchase Item");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addItem();
                    break;
                case 2:
                    removeItem();
                    break;
                case 3:
                    updateItemQuantityAndPrice();
                    break;
                case 4:
                    displayInventory();
                    break;
                case 5:
                    searchItem();
                    break;
                case 6:
                    purchaseItem();
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        } while (choice != 0);

        // Close scanner
        scanner.close();
    }

    private static void addItem() {
        System.out.println("======= Add Item =======");
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter item quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter item price: ");
        double price = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        stockManager.addItem(new StationaryItem(name, quantity, price));
        System.out.println("Item added successfully.");
    }

    private static void removeItem() {
        System.out.println("======= Remove Item =======");
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();

        stockManager.removeItem(name);
        System.out.println("Item removed successfully.");
    }

    private static void updateItemQuantityAndPrice() {
        System.out.println("======= Update Item Quantity and Price =======");
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new quantity: ");
        int newQuantity = scanner.nextInt();
        System.out.print("Enter new price: ");
        double newPrice = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        stockManager.updateItemQuantityAndPrice(name, newQuantity, newPrice);
    }

    private static void displayInventory() {
        System.out.println("======= Inventory =======");
        stockManager.displayItemsInTable();
        System.out.println("Total Stocks: " + stockManager.getTotalStocks());
        System.out.println("Total Price: " + stockManager.getTotalPrice());
    }

    private static void searchItem() {
        System.out.println("======= Search Item =======");
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();

        StationaryItem searchedItem = stockManager.searchItem(name);
        if (searchedItem != null) {
            System.out.println("Found: " + searchedItem);
        } else {
            System.out.println("Item not found.");
        }
    }

    private static void purchaseItem() {
        System.out.println("======= Purchase Item =======");
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter quantity to purchase: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        stockManager.purchaseItem(name, quantity);
    }
}

class StockManager {
    private ArrayList<StationaryItem> items;
    private FileHandler fileHandler;

    public StockManager(String fileName) {
        this.fileHandler = new FileHandler(fileName);
        this.items = fileHandler.readItemsFromFile();
    }

    public void addItem(StationaryItem item) {
        items.add(item);
        fileHandler.writeItemsToFile(items);
    }

    public void removeItem(String itemName) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equalsIgnoreCase(itemName)) {
                items.remove(i);
                fileHandler.writeItemsToFile(items);
                return;
            }
        }
        System.out.println("Item not found.");
    }

    public void updateItemQuantityAndPrice(String itemName, int newQuantity, double newPrice) {
        boolean found = false;
        for (StationaryItem item : items) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                item.setQuantity(newQuantity);
                item.setPrice(newPrice);
                fileHandler.writeItemsToFile(items);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Item not found.");
        } else {
            System.out.println("Quantity and price updated successfully.");
        }
    }

    public void purchaseItem(String itemName, int quantity) {
        StationaryItem item = searchItem(itemName);
        if (item != null) {
            if (item.getQuantity() >= quantity) {
                item.setQuantity(item.getQuantity() - quantity);
                fileHandler.writeItemsToFile(items);
                System.out.println("Purchase successful.");
            } else {
                System.out.println("Insufficient quantity in stock.");
            }
        } else {
            System.out.println("Item not found.");
        }
    }

    public void displayItemsInTable() {
        System.out.println("==========================================");
        System.out.printf("%-20s %-10s %-10s%n", "Item", "Quantity", "Price");
        System.out.println("==========================================");
        for (StationaryItem item : items) {
            System.out.printf("%-20s %-10d %-10.2f%n", item.getName(), item.getQuantity(), item.getPrice());
        }
        System.out.println("==========================================");
    }

    public int getTotalStocks() {
        int totalStocks = 0;
        for (StationaryItem item : items) {
            totalStocks += item.getQuantity();
        }
        return totalStocks;
    }

    public double getTotalPrice() {
        double totalPrice = 0;
        for (StationaryItem item : items) {
            totalPrice += item.getQuantity() * item.getPrice();
        }
        return totalPrice;
    }

    public StationaryItem searchItem(String itemName) {
        for (StationaryItem item : items) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }
}

class FileHandler {
    private String fileName;

    public FileHandler(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<StationaryItem> readItemsFromFile() {
        ArrayList<StationaryItem> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Skip the first line which contains column headers
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                double price = Double.parseDouble(parts[2]);
                items.add(new StationaryItem(name, quantity, price));
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return items;
    }

    public void writeItemsToFile(ArrayList<StationaryItem> items) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.printf("%-20s %-10s %-10s%n", "Item", "Quantity", "Price");
            for (StationaryItem item : items) {
                writer.printf("%-20s %-10d %-10.2f%n", item.getName(), item.getQuantity(), item.getPrice());
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}

class StationaryItem {
    private String name;
    private int quantity;
    private double price;

    public StationaryItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Quantity: " + quantity + ", Price: " + price;
    }
}
