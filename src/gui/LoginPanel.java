package gui;

import model.Employee;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static model.enums.Role.*;

public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final RestaurantContext context;

    private JPasswordField pinField;
    private JLabel statusLabel;

    public LoginPanel(MainFrame mainFrame, RestaurantContext context){
        this.mainFrame = mainFrame;
        this.context = context;
        
        setupUI();
    }

    private void setupUI() {
        // Use BorderLayout to divide our screen into Header, Center (Numpad), and Footer
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        // 1. NORTH: Header & PIN Display
        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        JLabel titleLabel = new JLabel("Restaurant POS Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        pinField = new JPasswordField(10);
        pinField.setFont(new Font("Arial", Font.BOLD, 32));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setEditable(false); // Force users to click our Numpad buttons!

        statusLabel = new JLabel("Enter your 4-digit PIN", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statusLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(pinField);
        headerPanel.add(statusLabel);
        add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER: 4x3 Numeric Keypad Grid
        JPanel numpadPanel = new JPanel(new GridLayout(4, 3, 15, 15));

        // Define button layout order (Standard Calculator/POS grid)
        String[] buttons = {
                "7", "8", "9",
                "4", "5", "6",
                "1", "2", "3",
                "C", "0", "OK"
        };

        Font buttonFont = new Font("Arial", Font.BOLD, 24);

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(buttonFont);
            button.setFocusPainted(false);

            // Style special action buttons differently
            if ("C".equals(text)) {
                button.setBackground(new Color(255, 102, 102)); // Soft Red
                button.setForeground(Color.WHITE);
                button.addActionListener(e -> clearPin());
            } else if ("OK".equals(text)) {
                button.setBackground(new Color(102, 204, 102)); // Soft Green
                button.setForeground(Color.WHITE);
                button.addActionListener(e -> attemptLogin());
            } else {
                // Numeric digits 0-9
                button.addActionListener(e -> appendDigit(text));
            }

            numpadPanel.add(button);
        }

        add(numpadPanel, BorderLayout.CENTER);
    }

    private void appendDigit(String digit) {
        // Limit PIN length to 4 digits
        String currentPin = new String(pinField.getPassword());
        if (currentPin.length() < 4) {
            pinField.setText(currentPin + digit);
            statusLabel.setText(" "); // Clear error text
        }

        // Auto-login if they reach 4 digits (optional POS convenience feature!)
        if (new String(pinField.getPassword()).length() == 4) {
            attemptLogin();
        }
    }

    private void clearPin() {
        pinField.setText("");
        statusLabel.setText("Enter your 4-digit PIN");
        statusLabel.setForeground(Color.GRAY);
    }

    private void attemptLogin() {
        String enteredPin = new String(pinField.getPassword());

        if (enteredPin.isEmpty()) {
            return;
        }

        // Call our EmployeeService from Phase 2 to verify the PIN!
        Optional<Employee> authResult = context.getEmployeeService().authenticateByPin(enteredPin);

        if (authResult.isPresent()) {
            Employee loggedInUser = authResult.get();
            context.setCurrentLoggedInUser(loggedInUser);

            clearPin(); // Reset field for next time someone logs out

            // Route to the appropriate screen based on Role!
            switch (loggedInUser.getRole()) {
                case KITCHEN:
                    mainFrame.showScreen(MainFrame.KITCHEN_PANEL);
                    break;

                case MANAGER:
                    mainFrame.showScreen(MainFrame.MANAGER_PANEL); // Direct access for Manager!
                    break;
                case WAITER:
                default:
                    mainFrame.showScreen(MainFrame.FLOOR_PLAN_PANEL);
                    break;
            }
        } else {
            // Login failed! Show feedback on UI
            statusLabel.setText("Invalid PIN! Try again.");
            statusLabel.setForeground(Color.RED);
            pinField.setText(""); // Auto-clear field on failure
        }
    }
}
