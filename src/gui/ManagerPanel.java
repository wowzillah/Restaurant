package gui;

import model.*;
import model.MenuItem;
import model.enums.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManagerPanel extends JPanel {
    private final MainFrame mainFrame;
    private final RestaurantContext context;

    //Table Models for auto-refreshing UI
    private DefaultTableModel staffTableModel;
    private DefaultTableModel menuTableModel;
    private DefaultTableModel tableTableModel;
    private DefaultTableModel inventoryTableModel;
    //Financial Dashboard Labels
    private JLabel totalRevenueLabel;
    private JLabel totalOrdersLabel;

    public ManagerPanel(MainFrame mainFrame, RestaurantContext context){
        this.mainFrame = mainFrame;
        this.context = context;

        setupUI();
        refreshAllViews();
    }

    private void setupUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. NORTH: Header & Navigation
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Manager Back-Office Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton floorBtn = new JButton("View Floor Plan");
        floorBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.FLOOR_PLAN_PANEL));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(255, 153, 153));
        logoutBtn.addActionListener(e -> {
            context.setCurrentLoggedInUser(null);
            mainFrame.showScreen(MainFrame.LOGIN_PANEL);
        });

        navButtons.add(floorBtn);
        navButtons.add(logoutBtn);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navButtons, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER: Administration Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab("👥 Staff Management", createStaffTab());
        tabbedPane.addTab("🍔 Menu Engineering", createMenuTab());
        tabbedPane.addTab("🪑 Floor Management", createTableTab());
        tabbedPane.addTab("📦 Inventory Management", createInventoryTab()); // NEW TAB!
        tabbedPane.addTab("📈 Financial Analytics", createAnalyticsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // TAB 1: STAFF MANAGEMENT
    // ==========================================
    private JPanel createStaffTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Role", "Active Status"};
        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable staffTable = new JTable(staffTableModel);
        staffTable.setRowHeight(25);
        panel.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addStaffBtn = new JButton("+ Add Employee");
        addStaffBtn.setBackground(new Color(102, 204, 102));
        addStaffBtn.setForeground(Color.WHITE);
        addStaffBtn.addActionListener(e -> showAddEmployeeDialog());

        btnPanel.add(addStaffBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showAddEmployeeDialog() {
        JTextField nameField = new JTextField();
        JTextField pinField = new JTextField();
        JComboBox<Role> roleBox = new JComboBox<>(Role.values());

        Object[] form = {
                "Employee Full Name:", nameField,
                "4-Digit PIN Code:", pinField,
                "Assigned Role:", roleBox
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Register New Employee", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String pin = pinField.getText().trim();
            Role role = (Role) roleBox.getSelectedItem();

            if (name.isEmpty() || pin.length() != 4) {
                JOptionPane.showMessageDialog(this, "Invalid entry! Name required and PIN must be exactly 4 digits.");
                return;
            }

            int newId = context.getEmployeeService().getEmployees().size() + 1;
            Employee newEmp = new Employee(newId, role, name, pin);
            context.getEmployeeService().registerEmployee(newEmp);
            context.saveAllData(); // Persist to employees.dat!
            refreshAllViews();
        }
    }

    // ==========================================
    // TAB 2: MENU ENGINEERING
    // ==========================================
    private JPanel createMenuTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Item Name", "Category", "Price ($)"};
        menuTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable menuTable = new JTable(menuTableModel);
        menuTable.setRowHeight(25);
        panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addItemBtn = new JButton("+ Add Menu Item");
        addItemBtn.setBackground(new Color(102, 204, 102));
        addItemBtn.setForeground(Color.WHITE);
        addItemBtn.addActionListener(e -> showAddMenuItemDialog());

        btnPanel.add(addItemBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showAddMenuItemDialog() {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();
        JComboBox<MenuCategory> catBox = new JComboBox<>(MenuCategory.values());

        Object[] form = {
                "Dish Name:", nameField,
                "Description:", descField,
                "Price ($):", priceField,
                "Category:", catBox
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Dish to Catalog", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                MenuCategory cat = (MenuCategory) catBox.getSelectedItem();

                int newId = context.getMenuService().getMenuCatalog().size() + 1;
                MenuItem newItem = new MenuItem(newId, name, desc, price, MenuStatus.APPROVED, cat);
                context.getMenuService().addMenuItem(newItem);
                context.saveAllData(); // Persist to menu.dat!
                refreshAllViews();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price must be a valid number (e.g., 14.50)!");
            }
        }
    }

    // ==========================================
    // TAB 3: FINANCIAL ANALYTICS
    // ==========================================
    private JPanel createAnalyticsTab() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JPanel revenueCard = new JPanel(new BorderLayout());
        revenueCard.setBorder(BorderFactory.createTitledBorder("Total Gross Revenue (Inc. Tax)"));
        totalRevenueLabel = new JLabel("$0.00", SwingConstants.CENTER);
        totalRevenueLabel.setFont(new Font("Arial", Font.BOLD, 48));
        totalRevenueLabel.setForeground(new Color(0, 153, 76));
        revenueCard.add(totalRevenueLabel, BorderLayout.CENTER);

        JPanel countCard = new JPanel(new BorderLayout());
        countCard.setBorder(BorderFactory.createTitledBorder("Total Settled Check Count"));
        totalOrdersLabel = new JLabel("0 Checks", SwingConstants.CENTER);
        totalOrdersLabel.setFont(new Font("Arial", Font.BOLD, 36));
        countCard.add(totalOrdersLabel, BorderLayout.CENTER);

        panel.add(revenueCard);
        panel.add(countCard);
        return panel;
    }

    /**
     * Refreshes UI tables and financial numbers.
     */
    public void refreshAllViews() {

        // Refresh Inventory Grid
        if (inventoryTableModel != null) {
            inventoryTableModel.setRowCount(0);
            for (Ingredient i : context.getInventoryService().getInventory()) {
                // Smart UI: Add a visual warning flag if stock is low!
                String stockDisplay = i.getStockQuantity() + (i.isLowStock() ? "  ⚠️ LOW" : "");

                inventoryTableModel.addRow(new Object[]{
                        i.getId(),
                        i.getName(),
                        stockDisplay,
                        i.getLowStockThreshold(),
                        String.format("%.2f", i.getUnitCost())
                });
            }
        }
        // Refresh Staff
        staffTableModel.setRowCount(0);
        for (Employee emp : context.getEmployeeService().getEmployees()) {
            staffTableModel.addRow(new Object[]{emp.getId(), emp.getName(), emp.getRole(), emp.isActive() ? "Active" : "Disabled"});
        }

        // Refresh Menu
        menuTableModel.setRowCount(0);
        for (MenuItem item : context.getMenuService().getMenuCatalog()) {
            menuTableModel.addRow(new Object[]{item.getId(), item.getName(), item.getCategory(), String.format("%.2f", item.getPrice())});
        }
        // 🪑 REFRESH FLOOR TABLES
        if (tableTableModel != null) {
            tableTableModel.setRowCount(0);
            for (model.Table t : context.getTableService().getTables()) {
                tableTableModel.addRow(new Object[]{
                        t.getTableNumber(),
                        t.getCapacity(),
                        t.getStatus()
                });
            }
        }

        // Refresh Financials
        double totalRev = context.getOrderService().getTotalRevenue();
        int totalChecks = context.getOrderService().getOrderHistory().size();
        totalRevenueLabel.setText("$" + String.format("%.2f", totalRev));
        totalOrdersLabel.setText(totalChecks + " Checks");
    }

    // ==========================================
// TAB 3: FLOOR & TABLE MANAGEMENT
// ==========================================
    private JPanel createTableTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Table Number", "Max Capacity (Guests)", "Current Status"};
        tableTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tableGrid = new JTable(tableTableModel);
        tableGrid.setRowHeight(25);
        panel.add(new JScrollPane(tableGrid), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addTableBtn = new JButton("+ Add New Table");
        addTableBtn.setBackground(new Color(102, 204, 102)); // Soft Green
        addTableBtn.setForeground(Color.WHITE);
        addTableBtn.addActionListener(e -> showAddTableDialog());

        JButton removeTableBtn = new JButton("- Remove Selected Table");
        removeTableBtn.setBackground(new Color(255, 153, 153)); // Soft Red
        removeTableBtn.addActionListener(e -> handleRemoveTable(tableGrid));

        btnPanel.add(addTableBtn);
        btnPanel.add(removeTableBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showAddTableDialog() {
        JTextField numField = new JTextField();
        JTextField capacityField = new JTextField();

        Object[] form = {
                "Table Number (e.g., 5):", numField,
                "Number of Guests / Capacity:", capacityField
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Table", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int tableNum = Integer.parseInt(numField.getText().trim());
                int capacity = Integer.parseInt(capacityField.getText().trim());

                if (capacity <= 0 || tableNum <= 0) {
                    JOptionPane.showMessageDialog(this, "Table number and capacity must be positive numbers!");
                    return;
                }

                // Prevent duplicate table numbers
                if (context.getTableService().getTableByNumber(tableNum).isPresent()) {
                    JOptionPane.showMessageDialog(this, "Table #" + tableNum + " already exists!");
                    return;
                }

                model.Table newTable = new model.Table(tableNum, tableNum, capacity);
                context.getTableService().addTable(newTable);
                context.saveAllData(); // Save to tables.dat!
                refreshAllViews();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid whole numbers!");
            }
        }
    }

    private void handleRemoveTable(JTable tableGrid) {
        int selectedRow = tableGrid.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a table to remove!");
            return;
        }

        int tableNumber = (int) tableTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently remove Table #" + tableNumber + "?",
                "Remove Table",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Attempt to remove it (will return false if guests are sitting there!)
            boolean removed = context.getTableService().removeTable(tableNumber);

            if (removed) {
                context.saveAllData();
                refreshAllViews();
                JOptionPane.showMessageDialog(this, "Table #" + tableNumber + " removed successfully.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete Table #" + tableNumber + " because it is currently OCCUPIED!\nClose the table's check first.",
                        "Deletion Blocked",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // ==========================================
// TAB 4: INVENTORY MANAGEMENT
// ==========================================
    private JPanel createInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Ingredient Name", "Stock Level", "Warning Threshold", "Unit Cost ($)"};
        inventoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable invTable = new JTable(inventoryTableModel);
        invTable.setRowHeight(25);
        panel.add(new JScrollPane(invTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addStockBtn = new JButton("+ Register New Ingredient");
        addStockBtn.setBackground(new Color(102, 204, 102));
        addStockBtn.setForeground(Color.WHITE);
        addStockBtn.addActionListener(e -> showAddStockDialog());

        btnPanel.add(addStockBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void showAddStockDialog() {
        JTextField nameField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField thresholdField = new JTextField();
        JTextField costField = new JTextField();

        Object[] form = {
                "Ingredient Name (e.g., Ground Beef):", nameField,
                "Initial Stock Quantity:", stockField,
                "Low Stock Warning Threshold:", thresholdField,
                "Cost per Unit ($):", costField
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Ingredient", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int stock = Integer.parseInt(stockField.getText().trim());
                int threshold = Integer.parseInt(thresholdField.getText().trim());
                double cost = Double.parseDouble(costField.getText().trim());

                if (name.isEmpty() || stock < 0 || threshold < 0 || cost < 0) {
                    JOptionPane.showMessageDialog(this, "Fields cannot be empty and numbers must be positive!");
                    return;
                }

                int newId = context.getInventoryService().getInventory().size() + 1;
                Ingredient newIng = new Ingredient(newId, name, stock, threshold, cost);

                context.getInventoryService().addIngredient(newIng);
                context.saveAllData(); // Save to disk!
                refreshAllViews();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for stock, threshold, and cost!");
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) refreshAllViews();
    }
}
