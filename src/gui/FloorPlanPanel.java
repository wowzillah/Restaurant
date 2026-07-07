package gui;

import model.Employee;
import model.Table;
import model.enums.TableStatus;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FloorPlanPanel extends JPanel {
    private final MainFrame mainFrame;
    private final RestaurantContext context;

    private JLabel userStatusLabel;
    private JPanel gridPanel;

    public FloorPlanPanel(MainFrame mainFrame, RestaurantContext context) {
        this.mainFrame = mainFrame;
        this.context = context;

        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. NORTH: Header Navigation Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel("Restaurant Floor Layout");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userStatusLabel = new JLabel("Logged in as: -");
        userStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> logout());

        userInfoPanel.add(userStatusLabel);
        userInfoPanel.add(Box.createHorizontalStrut(10));
        userInfoPanel.add(logoutBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER: Dynamic Table Grid (3 columns, flexible rows)
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        add(gridPanel, BorderLayout.CENTER);
    }

    /**
     * Re-builds the table grid buttons and updates logged-in user text.
     */
    public void refreshFloor() {
        // Update user header
        Employee currentUser = context.getCurrentLoggedInUser();
        if (currentUser != null) {
            userStatusLabel.setText("Server: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        }

        // Clear existing grid UI
        gridPanel.removeAll();

        // Fetch tables from TableService and create a styled button for each
        List<Table> tables = context.getTableService().getTables();
        for (Table table : tables) {
            JButton tableBtn = createTableButton(table);
            gridPanel.add(tableBtn);
        }

        // Tell Swing to re-render the UI layout properly
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton createTableButton(Table table) {
        // HTML formatting inside Swing allows multi-line text on buttons!
        String buttonText = "<html><center>"
                + "<font size='5'><b>Table #" + table.getTableNumber() + "</b></font><br><br>"
                + "Capacity: " + table.getCapacity() + " guests<br>"
                + "<b>Status: " + table.getStatus() + "</b>"
                + "</center></html>";

        JButton btn = new JButton(buttonText);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Color mapping using our Phase 1 TableStatus enum
        switch (table.getStatus()) {
            case AVAILABLE:
                btn.setBackground(new Color(153, 255, 153)); // Soft Green
                break;
            case OCCUPIED:
                btn.setBackground(new Color(255, 153, 153)); // Soft Red
                break;
            case RESERVED:
                btn.setBackground(new Color(255, 255, 153)); // Soft Yellow
                break;
            case OUT_OF_SERVICE:
            default:
                btn.setBackground(Color.LIGHT_GRAY);
                btn.setEnabled(false);
                break;
        }

        btn.addActionListener(e -> handleTableClick(table));
        return btn;
    }

    private void handleTableClick(Table table) {
        Employee waiter = context.getCurrentLoggedInUser();

        if (table.getStatus() == TableStatus.AVAILABLE) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Open a new check for Table #" + table.getTableNumber() + "?",
                    "Open Table",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                context.getOrderService().openOrder(table, waiter);
                context.saveAllData();
                refreshFloor();

                // Navigate directly to the ordering screen!
                mainFrame.openOrderScreen(table);
            }
        } else if (table.getStatus() == TableStatus.OCCUPIED) {
            // Table already active -> jump directly to the ordering screen!
            mainFrame.openOrderScreen(table);
        }
    }

    private void logout() {
        context.setCurrentLoggedInUser(null);
        mainFrame.showScreen(MainFrame.LOGIN_PANEL);
    }

    /**
     * PRO TIP: Override setVisible so whenever CardLayout flips to this screen,
     * it automatically calls refreshFloor() to show updated table colors!
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            refreshFloor();
        }
    }
}