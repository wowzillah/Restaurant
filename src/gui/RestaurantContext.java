package gui;

import model.*;
import model.enums.MenuCategory;
import model.enums.MenuStatus;
import model.enums.Role;
import persistence.PersistenceManager;
import service.*;

import java.util.List;
import java.util.Optional;

public class RestaurantContext {
    private final EmployeeService employeeService;
    private final MenuService menuService;
    private final TableService tableService;
    private final OrderService orderService;
    private final PersistenceManager persistenceManager;
    private final InventoryService inventoryService;

    //in order to track employees already logged in
    private Employee currentLoggedInUser;

    //local file names for persistence
    private static final String EMPLOYEES_FILE = "employees.dat";
    private static final String MENU_FILE = "menu.dat";
    private static final String TABLES_FILE = "tables.dat";
    private static final String ORDERHISTORY_FILE = "history.dat";
  //  private static final String ACTIVE_ORDERS_FILE = "active_orders.dat";
    private static final String INVENTORY_FILE = "inventory.dat";

    public RestaurantContext(){
        this.employeeService = new EmployeeService();
        this.menuService = new MenuService();
        this.tableService = new TableService();
        this.orderService = new OrderService();
        this.persistenceManager = new PersistenceManager();
        this.inventoryService = new InventoryService();

        initializeData();
    }

    private void initializeData() {
        boolean loadedAnything = loadAllData();
        // If no saved data exists on disk, generate our default setup and save it!
        if (!loadedAnything) {
            System.out.println("No saved data found. Seeding initial mock data...");
            seedMockData();
            saveAllData();
        }
    }

    public void saveAllData() {
        System.out.println("Saving restaurant state to disk...");
        persistenceManager.saveToFile(EMPLOYEES_FILE, employeeService.getEmployees());
        persistenceManager.saveToFile(MENU_FILE, menuService.getMenuCatalog());
        persistenceManager.saveToFile(TABLES_FILE, tableService.getTables());
        persistenceManager.saveToFile(ORDERHISTORY_FILE, orderService.getOrderHistory());
        //persistenceManager.saveToFile(ACTIVE_ORDERS_FILE, orderService.getActiveOrders());
        persistenceManager.saveToFile(INVENTORY_FILE, inventoryService.getInventory());
    }

    private void seedMockData() {
        // 1. Seed Employees
        employeeService.registerEmployee(new Employee(1, Role.WAITER,"Alice (Waiter)","1111"));
        employeeService.registerEmployee(new Employee(2,  Role.KITCHEN,"Chef Bob", "2222"));
        employeeService.registerEmployee(new Employee(3,  Role.MANAGER,"Clara (Manager)", "3333"));

        // 2. Seed Tables
        tableService.addTable(new Table(1, 1, 2)); // Table #1 (2-top)
        tableService.addTable(new Table(2, 2, 2)); // Table #2 (2-top)
        tableService.addTable(new Table(3, 3, 4)); // Table #3 (4-top)
        tableService.addTable(new Table(4, 4, 6)); // Table #4 (6-top)

        // 3. Seed Menu Items
        menuService.addMenuItem(new MenuItem(1, "French Onion Soup", "Classic soup with melted Gruyère", 8.50, MenuStatus.APPROVED,MenuCategory.STARTER));
        menuService.addMenuItem(new MenuItem(2, "Smoked Sausage & Beans", "Southern style slow-cooked beans", 16.00,MenuStatus.APPROVED, MenuCategory.MAIN_COURSE));
        menuService.addMenuItem(new MenuItem(3, "Noisette d'Agneau", "Pan-seared lamb loin with herbs", 24.50, MenuStatus.APPROVED,MenuCategory.MAIN_COURSE));
        menuService.addMenuItem(new MenuItem(4, "Crème Brûlée", "Rich vanilla custard with caramelized sugar", 7.00,MenuStatus.APPROVED, MenuCategory.DESSERT));
        menuService.addMenuItem(new MenuItem(5, "Red Wine (Glass)", "Bordeaux house red", 6.50, MenuStatus.APPROVED,MenuCategory.BEVERAGE));
        // 4. Seed Inventory & Recipes (Add this to the bottom of seedMockData)
        Ingredient onion = new model.Ingredient(1, "Yellow Onions", 10, 5, 0.50);
        Ingredient gruyere = new model.Ingredient(2, "Gruyere Cheese", 1, 3, 2.00); // CRITICAL: Only 1 left in stock!

        inventoryService.addIngredient(onion);
        inventoryService.addIngredient(gruyere);

// Attach the recipe to the French Onion Soup (Item ID 1)
        java.util.Optional<MenuItem> soupOpt = menuService.getItemById(1);
        if (soupOpt.isPresent()) {
            MenuItem soup = soupOpt.get();
            soup.addIngredientToRecipe(onion, 2);    // Takes 2 onions
            soup.addIngredientToRecipe(gruyere, 1);  // Takes 1 cheese
        }
    }

    // Getters for our Services
    public EmployeeService getEmployeeService() { return employeeService; }
    public MenuService getMenuService() { return menuService; }
    public TableService getTableService() { return tableService; }
    public OrderService getOrderService() { return orderService; }

    // Getters & Setters for the current user session
    public Employee getCurrentLoggedInUser() { return currentLoggedInUser; }
    public void setCurrentLoggedInUser(Employee user) { this.currentLoggedInUser = user; }


    private boolean loadAllData() {

        Optional<List<Employee>> loadedEmployees = persistenceManager.loadFromFile(EMPLOYEES_FILE);
        Optional<List<MenuItem>> loadedMenu = persistenceManager.loadFromFile(MENU_FILE);
        Optional<List<Table>> loadedTables = persistenceManager.loadFromFile(TABLES_FILE);
        Optional<List<Order>> loadedHistory = persistenceManager.loadFromFile(ORDERHISTORY_FILE);
        //Optional<List<Order>> loadedActiveOrders = persistenceManager.loadFromFile(ACTIVE_ORDERS_FILE);
        boolean foundData = false;

        Optional<List<Ingredient>> loadedInventory = persistenceManager.loadFromFile(INVENTORY_FILE);
        if (loadedInventory.isPresent()) {
            inventoryService.setInventory(loadedInventory.get());
            foundData = true;
        }

        if (loadedEmployees.isPresent()) {
            employeeService.setEmployees(loadedEmployees.get());
            foundData = true;
        }
        if (loadedMenu.isPresent()) {
            menuService.setMenuCatalog(loadedMenu.get());
            foundData = true;
        }
        if (loadedTables.isPresent()) {
            List<Table> tables = loadedTables.get();
            tableService.setTables(tables);
            List<Order> restoredActiveOrders = new java.util.ArrayList<>();
            for (Table table : tables) {
                if (table.getCurrentOrder() != null) {
                    restoredActiveOrders.add(table.getCurrentOrder());
                }
            }
            orderService.setActiveOrders(restoredActiveOrders);

            foundData = true;
        }
        if (loadedHistory.isPresent()) {
            orderService.setOrderHistory(loadedHistory.get());
            foundData = true;
        }

        return foundData;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }
}
