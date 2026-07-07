package gui;

import model.Order;
import model.OrderItem;
import model.enums.OrderStatus;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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
        setBackground(Color.DARK_GRAY); // Dark Mode for the Kitchen

        //1.NORTH: Header & Navigation
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.DARK_GRAY);

        JLabel titleLabel = new JLabel("👨‍🍳 Kitchen Display System (KDS)");
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

        // 2. CENTER: The Ticket Rail (Horizontal Scrolling)
        ticketContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        ticketContainer.setBackground(Color.DARK_GRAY);

        JScrollPane scrollPane = new JScrollPane(ticketContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);
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
