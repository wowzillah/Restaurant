package gui;

import model.Table;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final RestaurantContext context;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    // Keep references to panels that need dynamic updates
    private POSOrderPanel posOrderPanel;

    // Panel Identifiers as constants
    public static final String LOGIN_PANEL = "LOGIN";
    public static final String FLOOR_PLAN_PANEL = "FLOOR_PLAN";
    public static final String POS_ORDER_PANEL = "POS_ORDER";
    public static final String KITCHEN_PANEL = "KITCHEN";
    public static final String MANAGER_PANEL = "MANAGER";

    public MainFrame() {
        this.context = new RestaurantContext();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);

        setupFrame();
        initializePanels();
    }

    private void setupFrame() {
        setTitle("Vanilla Java POS - Restaurant System");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initializePanels() {
        // Instantiate panels
        posOrderPanel = new POSOrderPanel(this, context);

        mainPanel.add(new LoginPanel(this, context), LOGIN_PANEL);
        mainPanel.add(new FloorPlanPanel(this, context), FLOOR_PLAN_PANEL);
        mainPanel.add(new ManagerPanel(this, context), MANAGER_PANEL);
        mainPanel.add(posOrderPanel, POS_ORDER_PANEL);
        mainPanel.add(new KitchenPanel(this, context), KITCHEN_PANEL);
        add(mainPanel);
        cardLayout.show(mainPanel, LOGIN_PANEL);
    }

    public void showScreen(String screenName) {
        cardLayout.show(mainPanel, screenName);
    }

    /**
     * Helper method called by FloorPlanPanel when a server clicks a table!
     */
    public void openOrderScreen(Table table) {
        posOrderPanel.setActiveTable(table);
        showScreen(POS_ORDER_PANEL);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}