package gui;

import model.Ingredient;
import model.Order;
import model.OrderItem;
import model.enums.MenuCategory;
import model.enums.MenuStatus;
import model.enums.OrderStatus;
import model.MenuItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class KitchenPanel extends JPanel {
    private final MainFrame mainFrame;
    private final RestaurantContext context;

    private JPanel ticketContainer;

    public KitchenPanel(MainFrame mainFrame, RestaurantContext context) {
        this.mainFrame = mainFrame;
        this.context = context;

        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.DARK_GRAY);

        // 1. NORTH: Header & Navigation (Keep this exactly the same)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.DARK_GRAY);

        JLabel titleLabel = new JLabel("👨‍🍳 Kitchen Display & Recipe Lab");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navButtons.setBackground(Color.DARK_GRAY);

        JButton refreshBtn = new JButton("↻ Refresh Board");
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refreshBtn.addActionListener(e -> refreshTickets());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 14));
        logoutBtn.setBackground(new Color(255, 153, 153));
        logoutBtn.addActionListener(e -> {
            context.setCurrentLoggedInUser(null);
            mainFrame.showScreen(MainFrame.LOGIN_PANEL);
        });

        navButtons.add(refreshBtn);
        navButtons.add(logoutBtn);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(navButtons, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER: The Tabs!
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));

        tabbedPane.addTab("🔥 Live Orders", createLiveOrdersTab());
        tabbedPane.addTab("🧪 Recipe Lab (Drafts)", createRecipeLabTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createLiveOrdersTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        ticketContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        ticketContainer.setBackground(Color.DARK_GRAY);

        JScrollPane scrollPane = new JScrollPane(ticketContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRecipeLabTab() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- LEFT SIDE: Dish Details Form ---
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        formPanel.setPreferredSize(new Dimension(300, 0));
        formPanel.setBorder(BorderFactory.createTitledBorder("1. Dish Details"));

        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JComboBox<model.enums.MenuCategory> catBox = new JComboBox<>(model.enums.MenuCategory.values());

        formPanel.add(new JLabel("Dish Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(catBox);

        // --- CENTER: Recipe Builder ---
        JPanel recipePanel = new JPanel(new BorderLayout(10, 10));
        recipePanel.setBorder(BorderFactory.createTitledBorder("2. Raw Ingredients"));

        // Temporary storage for the draft recipe
        java.util.Map<model.Ingredient, Integer> draftRecipe = new java.util.HashMap<>();

        String[] cols = {"Ingredient", "Qty Required"};
        javax.swing.table.DefaultTableModel recipeModel = new javax.swing.table.DefaultTableModel(cols, 0);
        JTable recipeTable = new JTable(recipeModel);
        recipePanel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);

        // Controls to add ingredients
        JPanel addIngPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> ingBox = new JComboBox<>();
        java.util.List<model.Ingredient> allIngredients = context.getInventoryService().getInventory();
        for (model.Ingredient ing : allIngredients) {
            ingBox.addItem(ing.getName());
        }

        JTextField qtyField = new JTextField(5);
        JButton addIngBtn = new JButton("+ Add");

        addIngBtn.addActionListener(e -> {
            int idx = ingBox.getSelectedIndex();
            if (idx == -1) return;
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) throw new NumberFormatException();

                model.Ingredient selected = allIngredients.get(idx);
                draftRecipe.put(selected, qty); // Save to temp map
                recipeModel.addRow(new Object[]{selected.getName(), qty}); // Show on UI
                qtyField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive number!");
            }
        });

        addIngPanel.add(new JLabel("Ingredient:"));
        addIngPanel.add(ingBox);
        addIngPanel.add(new JLabel("Qty:"));
        addIngPanel.add(qtyField);
        addIngPanel.add(addIngBtn);
        recipePanel.add(addIngPanel, BorderLayout.SOUTH);

        // --- BOTTOM: Submit Button ---
        JButton submitBtn = new JButton("📤 Send Draft to Manager for Approval");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 18));
        submitBtn.setBackground(new Color(153, 204, 255));
        submitBtn.setPreferredSize(new Dimension(0, 50));

        submitBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty() || draftRecipe.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please provide a name and at least 1 ingredient!");
                return;
            }

            // Create the new Item! Notice price is 0.0 and status is DRAFT.
            int newId = context.getMenuService().getMenuCatalog().size() + 1;
            MenuItem newDraft = new MenuItem(
                                newId, name, descField.getText().trim(), 0.0,
                    MenuStatus.DRAFT,
                    (MenuCategory) catBox.getSelectedItem()
                        );

            // Transfer the recipe from our temp map to the new dish
            for (Map.Entry<Ingredient, Integer> entry : draftRecipe.entrySet()) {
                newDraft.addIngredientToRecipe(entry.getKey(), entry.getValue());
            }

            context.getMenuService().addMenuItem(newDraft);
            context.saveAllData(); // Save it to the hard drive!

            JOptionPane.showMessageDialog(this, "Recipe Draft Sent! Waiting for Manager approval.");

            // Clear the form
            nameField.setText("");
            descField.setText("");
            draftRecipe.clear();
            recipeModel.setRowCount(0);
        });

        panel.add(formPanel, BorderLayout.WEST);
        panel.add(recipePanel, BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshTickets() {
        ticketContainer.removeAll();

        // Fetch only orders that are PENDING or IN_PREPARATION
        List<Order> kitchenQueue = context.getOrderService().getKitchenQueue();

        if (kitchenQueue.isEmpty()) {
            JLabel emptyLabel = new JLabel("No active orders. Kitchen is clear!");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 24));
            emptyLabel.setForeground(Color.LIGHT_GRAY);
            ticketContainer.add(emptyLabel);
        } else {
            for (Order order : kitchenQueue) {
                JPanel ticketCard = createTicketCard(order);
                ticketContainer.add(ticketCard);
            }
        }

        ticketContainer.revalidate();
        ticketContainer.repaint();
    }

    private JPanel createTicketCard(Order order) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(300, 400)); // Fixed ticket size
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        card.setBackground(Color.WHITE);

        // Card Header (Changes color based on status)
        JPanel header = new JPanel(new GridLayout(2, 1));
        if (order.getStatus() == OrderStatus.PENDING) {
            header.setBackground(new Color(255, 255, 153)); // Yellow for new
        } else {
            header.setBackground(new Color(153, 204, 255)); // Blue for cooking
        }

        JLabel orderInfo = new JLabel("Order #" + order.getId() + " (Table " + order.getTableId() + ")", SwingConstants.CENTER);
        orderInfo.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel statusInfo = new JLabel(order.getStatus().name(), SwingConstants.CENTER);
        statusInfo.setFont(new Font("Arial", Font.BOLD, 14));

        header.add(orderInfo);
        header.add(statusInfo);
        card.add(header, BorderLayout.NORTH);

        // Card Body (The Food Items)
        JTextArea itemsArea = new JTextArea();
        itemsArea.setFont(new Font("Monospaced", Font.BOLD, 16));
        itemsArea.setEditable(false);
        itemsArea.setMargin(new Insets(10, 10, 10, 10));

        for (OrderItem item : order.getItems()) {
            itemsArea.append("[] " + item.getQuantity() + "x " + item.getMenuItem().getName() + "\n");

            if (item.getSpecialInstruction() != null && !item.getSpecialInstruction().isEmpty()) {
                itemsArea.append("   *** " + item.getSpecialInstruction() + " ***\n");
            }
            itemsArea.append("\n"); // Spacing between items
        }
        card.add(new JScrollPane(itemsArea), BorderLayout.CENTER);

        // Card Footer (Action Button)
        JButton actionBtn = new JButton();
        actionBtn.setFont(new Font("Arial", Font.BOLD, 16));
        actionBtn.setFocusPainted(false);

        if (order.getStatus() == OrderStatus.PENDING) {
            actionBtn.setText("👨‍🍳 Start Cooking");
            actionBtn.setBackground(new Color(153, 204, 255));
            actionBtn.addActionListener(e -> bumpTicket(order, OrderStatus.IN_PREPARATION));
        } else {
            actionBtn.setText("✅ Mark as Ready");
            actionBtn.setBackground(new Color(102, 204, 102));
            actionBtn.setForeground(Color.WHITE);
            actionBtn.addActionListener(e -> bumpTicket(order, OrderStatus.READY_TO_SERVE));
        }

        card.add(actionBtn, BorderLayout.SOUTH);
        return card;
    }
    private void bumpTicket(Order order, OrderStatus newStatus) {
        context.getOrderService().updateOrderStatus(order, newStatus);
        context.saveAllData(); // Save the status change to disk!
        refreshTickets(); // Refresh the board instantly
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            refreshTickets();
        }
    }
}
