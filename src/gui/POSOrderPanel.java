package gui;

import model.*;
import model.MenuItem;
import model.enums.MenuCategory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class POSOrderPanel extends JPanel {
    private final MainFrame mainFrame;
    private final RestaurantContext context;

    private Table activeTable;
    private Order activeOrder;

    // UI Components
    private JLabel headerLabel;
    private DefaultTableModel ticketTableModel;
    private JTable ticketTable;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;

    public POSOrderPanel(MainFrame mainFrame, RestaurantContext context) {
        this.mainFrame = mainFrame;
        this.context = context;

        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. LEFT PANEL: Ticket / Order Summary (40% width)
        JPanel leftPanel = createTicketPanel();
        leftPanel.setPreferredSize(new Dimension(420, 0));
        add(leftPanel, BorderLayout.WEST);

        // 2. CENTER/RIGHT PANEL: Tabbed Menu Catalog (60% width)
        JTabbedPane menuTabbedPane = createMenuCatalogTabs();
        add(menuTabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTicketPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Active Order Check"));

        // North: Table Header
        headerLabel = new JLabel("Table #- | Order #-");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(headerLabel, BorderLayout.NORTH);

        // Center: Itemized Table
        String[] columns = {"Item", "Qty", "Price ($)"};
        ticketTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent manual typing in cells
            }
        };
        ticketTable = new JTable(ticketTableModel);
        ticketTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ticketTable.setRowHeight(28);
        panel.add(new JScrollPane(ticketTable), BorderLayout.CENTER);

        // South: Totals & Action Buttons
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));

        JPanel totalsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        subtotalLabel = new JLabel("Subtotal: $0.00", SwingConstants.RIGHT);
        taxLabel = new JLabel("Tax (20%): $0.00", SwingConstants.RIGHT);
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        totalsPanel.add(subtotalLabel);
        totalsPanel.add(taxLabel);
        totalsPanel.add(totalLabel);
        southPanel.add(totalsPanel, BorderLayout.NORTH);

        JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JButton backBtn = new JButton("Back to Floor");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setBackground(Color.LIGHT_GRAY);
        backBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.FLOOR_PLAN_PANEL));

        // NEW BUTTON: Void / Remove
        JButton removeBtn = new JButton("Void (-1)");
        removeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        removeBtn.setBackground(new Color(255, 153, 153)); // Soft Red
        removeBtn.setFocusPainted(false);
        removeBtn.addActionListener(e -> handleRemoveItem());

        JButton payBtn = new JButton("Pay & Settle");
        payBtn.setFont(new Font("Arial", Font.BOLD, 14));
        payBtn.setBackground(new Color(102, 204, 102)); // Soft Green
        payBtn.setForeground(Color.WHITE);
        payBtn.addActionListener(e -> handlePayment());

        actionButtonsPanel.add(backBtn);
        actionButtonsPanel.add(payBtn);
        actionButtonsPanel.add(removeBtn);
        southPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JTabbedPane createMenuCatalogTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));

        for (MenuCategory category : MenuCategory.values()) {
            // 1. Create our grid just like before
            JPanel categoryGridPanel = new JPanel(new GridLayout(0, 3, 12, 12));
            categoryGridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            List<MenuItem> items = context.getMenuService().getAvailableItemsByCategory(category);
            for (MenuItem item : items) {
                JButton itemBtn = createMenuItemButton(item);

                // NEW: Give the button a preferred "square-ish" minimum size!
                itemBtn.setPreferredSize(new Dimension(120, 100));

                categoryGridPanel.add(itemBtn);
            }

            // 2. THE MAGIC FIX: Wrap the grid inside a new panel with BorderLayout
            JPanel wrapperPanel = new JPanel(new BorderLayout());

            // 3. Anchor the grid to the NORTH. This stops the vertical stretching!
            wrapperPanel.add(categoryGridPanel, BorderLayout.NORTH);

            // 4. Add the wrapper to the scroll pane, not the raw grid
            tabbedPane.addTab(category.name(), new JScrollPane(wrapperPanel));
        }

        return tabbedPane;
    }

    private JButton createMenuItemButton(MenuItem item) {
        String htmlText = "<html><center>"
                + "<b>" + item.getName() + "</b><br><br>"
                + "<font color='blue'>$" + String.format("%.2f", item.getPrice()) + "</font>"
                + "</center></html>";

        JButton btn = new JButton(htmlText);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> handleAddItem(item));
        return btn;
    }

    private void handleAddItem(MenuItem item) {
        if (activeOrder == null) return;

        // Add item to active order logic via OrderService
        context.getOrderService().addItemToOrder(activeOrder, item, 1, "");
        context.saveAllData(); // Persist changes to disk!
        refreshTicketView();
    }

    private void handlePayment() {
        if (activeOrder == null || activeOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order is empty! Nothing to pay.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm payment of $" + String.format("%.2f", activeOrder.calculateTotal() * 1.20) + " and close Table #" + activeTable.getTableNumber() + "?",
                "Settle Bill",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            context.getOrderService().settleOrder(activeOrder, activeTable);
            context.saveAllData(); // Persist closed table state!

            JOptionPane.showMessageDialog(this, "Payment processed successfully! Table cleared.");
            mainFrame.showScreen(MainFrame.FLOOR_PLAN_PANEL);
        }
    }

    /**
     * Called whenever we open this screen for a specific table.
     */
    public void setActiveTable(Table table) {
        this.activeTable = table;
        this.activeOrder = table.getCurrentOrder();

        headerLabel.setText("Table #" + table.getTableNumber() + " | Order #" + (activeOrder != null ? activeOrder.getId() : "N/A"));
        refreshMenuCatalogUI();
        refreshTicketView();
    }

    /**
     * Rebuilds the tabbed menu on the right side of the split screen.
     */
    private void refreshMenuCatalogUI() {
        // Locate the center component (our JTabbedPane) and remove it
        BorderLayout layout = (BorderLayout) getLayout();
        Component centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent != null) {
            remove(centerComponent);
        }

        // Recreate the tabs using the latest data from MenuService and add it back
        JTabbedPane updatedTabs = createMenuCatalogTabs();
        add(updatedTabs, BorderLayout.CENTER);

        // Tell Swing to re-render the layout
        revalidate();
        repaint();
    }

    private void refreshTicketView() {
        ticketTableModel.setRowCount(0); // Clear current table rows

        if (activeOrder == null) return;

        for (OrderItem orderItem : activeOrder.getItems()) {
            Object[] rowData = {
                    orderItem.getMenuItem().getName(),
                    orderItem.getQuantity(),
                    String.format("%.2f", orderItem.getSubtotal())
            };
            ticketTableModel.addRow(rowData);
        }

        // Calculate and update totals
        double subtotal = activeOrder.calculateTotal();
        double tax = subtotal * 0.20;
        double total = subtotal + tax;

        subtotalLabel.setText("Subtotal: $" + String.format("%.2f", subtotal));
        taxLabel.setText("Tax (20%): $" + String.format("%.2f", tax));
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    private void handleRemoveItem() {
        if (activeOrder == null) return;

        // Get the row currently highlighted by the server on the JTable
        int selectedRow = ticketTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please tap an item row on the receipt table first!");
            return;
        }

        // Decrement or remove the item
        activeOrder.removeOrDecrementItem(selectedRow);
        context.saveAllData(); // Persist changes to disk!
        refreshTicketView();

        // UX Touch Polish: Re-highlight the row if it still exists so the server
        // can rapidly tap "Void (-1)" multiple times in a row!
        if (selectedRow < ticketTable.getRowCount()) {
            ticketTable.setRowSelectionInterval(selectedRow, selectedRow);
        } else if (ticketTable.getRowCount() > 0) {
            ticketTable.setRowSelectionInterval(ticketTable.getRowCount() - 1, ticketTable.getRowCount() - 1);
        }
    }
}
