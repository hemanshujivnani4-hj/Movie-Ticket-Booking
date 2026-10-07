import javax.swing.*;
import java.awt.*;

public class MovieTicketBookingGUI extends JFrame {

    // Same seating array as the console version: 'O' = open, 'X' = booked
    private final char[][] seats = new char[3][5];
    private final boolean[][] selected = new boolean[3][5];
    private final JButton[][] seatButtons = new JButton[3][5];

    private final String[] movies = {"Wake Up Sid", "Rockstar", "Yeh Jawaani Hai Deewani"};
    private final String[] categories = {"Regular - Rs.150", "Premium - Rs.250", "VIP - Rs.400"};
    private final String[] categoryNames = {"Regular", "Premium", "VIP"};
    private final double[] prices = {150, 250, 400};

    private final JTextField nameField = new JTextField();
    private final JComboBox<String> movieBox = new JComboBox<>(movies);
    private final JComboBox<String> categoryBox = new JComboBox<>(categories);
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
    private final JLabel totalLabel = new JLabel();
    private final JLabel selectedLabel = new JLabel();
    private final JTextArea receipt = new JTextArea(10, 24);
    private final JButton confirmBtn = new JButton("Confirm Booking");

    private static final Color OPEN_COLOR = new Color(76, 175, 80);
    private static final Color SELECTED_COLOR = new Color(255, 167, 38);
    private static final Color BOOKED_COLOR = new Color(229, 57, 53);

    public MovieTicketBookingGUI() {
        super("Movie Ticket Booking");

        // 1. Create seating array
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                seats[i][j] = 'O';
            }
        }

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildSeatPanel(), BorderLayout.CENTER);
        add(buildReceiptPanel(), BorderLayout.EAST);

        // Live fare updates
        categoryBox.addActionListener(e -> updateSummary());
        qtySpinner.addChangeListener(e -> {
            clearSelection(); // quantity changed, so start seat picking again
            updateSummary();
        });
        confirmBtn.addActionListener(e -> confirmBooking());

        updateSummary();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    // ---------- UI builders ----------

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Booking Details"));
        p.add(new JLabel("Customer Name:"));
        p.add(nameField);
        p.add(new JLabel("Movie:"));
        p.add(movieBox);
        p.add(new JLabel("Ticket Category:"));
        p.add(categoryBox);
        p.add(new JLabel("Number of Tickets:"));
        p.add(qtySpinner);
        return p;
    }

    private JPanel buildSeatPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(5, 5));
        wrapper.setBorder(BorderFactory.createTitledBorder("Select Seats"));

        JLabel screen = new JLabel("SCREEN THIS WAY", SwingConstants.CENTER);
        screen.setOpaque(true);
        screen.setBackground(Color.DARK_GRAY);
        screen.setForeground(Color.WHITE);
        wrapper.add(screen, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(seats.length, seats[0].length, 6, 6));
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                JButton b = new JButton("R" + (i + 1) + "S" + (j + 1));
                b.setOpaque(true);
                b.setBorderPainted(false);
                b.setForeground(Color.WHITE);
                b.setPreferredSize(new Dimension(70, 50));
                final int r = i, c = j;
                b.addActionListener(e -> seatClicked(r, c));
                seatButtons[i][j] = b;
                grid.add(b);
                refreshSeat(i, j);
            }
        }
        wrapper.add(grid, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
        legend.add(legendItem("Available", OPEN_COLOR));
        legend.add(legendItem("Selected", SELECTED_COLOR));
        legend.add(legendItem("Booked", BOOKED_COLOR));
        wrapper.add(legend, BorderLayout.SOUTH);
        return wrapper;
    }

    private JLabel legendItem(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setIcon(new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, 14, 14);
            }
            public int getIconWidth() { return 14; }
            public int getIconHeight() { return 14; }
        });
        return l;
    }

    private JPanel buildReceiptPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Summary"));

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(selectedLabel);
        top.add(totalLabel);
        p.add(top, BorderLayout.NORTH);

        receipt.setEditable(false);
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        p.add(new JScrollPane(receipt), BorderLayout.CENTER);

        p.add(confirmBtn, BorderLayout.SOUTH);
        return p;
    }

    // ---------- Seat logic ----------

    private void seatClicked(int r, int c) {
        if (seats[r][c] == 'X') {
            JOptionPane.showMessageDialog(this, "Seat is already booked. Please pick another.");
            return;
        }
        if (selected[r][c]) {
            selected[r][c] = false;
        } else {
            if (selectedCount() >= getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        "You have already selected " + getQuantity() + " seat(s).\n"
                        + "Deselect one or increase the number of tickets.");
                return;
            }
            selected[r][c] = true;
        }
        refreshSeat(r, c);
        updateSummary();
    }

    private void refreshSeat(int r, int c) {
        JButton b = seatButtons[r][c];
        if (seats[r][c] == 'X') {
            b.setBackground(BOOKED_COLOR);
            b.setEnabled(false);
        } else if (selected[r][c]) {
            b.setBackground(SELECTED_COLOR);
            b.setEnabled(true);
        } else {
            b.setBackground(OPEN_COLOR);
            b.setEnabled(true);
        }
    }

    private void clearSelection() {
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                if (selected[i][j]) {
                    selected[i][j] = false;
                    refreshSeat(i, j);
                }
            }
        }
    }

    private int selectedCount() {
        int n = 0;
        for (boolean[] row : selected)
            for (boolean s : row)
                if (s) n++;
        return n;
    }

    private int availableCount() {
        int n = 0;
        for (char[] row : seats)
            for (char s : row)
                if (s == 'O') n++;
        return n;
    }

    private int getQuantity() {
        return (Integer) qtySpinner.getValue();
    }

    // Fare calculation method (same as console version)
    static double calculateFare(double price, int quantity) {
        return price * quantity;
    }

    private void updateSummary() {
        double price = prices[categoryBox.getSelectedIndex()];
        double total = calculateFare(price, getQuantity());
        totalLabel.setText("Total Fare: Rs." + total);
        selectedLabel.setText("Seats selected: " + selectedCount() + " / " + getQuantity());
    }

    // ---------- Booking ----------

    private void confirmBooking() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name.");
            return;
        }
        int quantity = getQuantity();
        if (selectedCount() != quantity) {
            JOptionPane.showMessageDialog(this,
                    "Please select exactly " + quantity + " seat(s). You have selected " + selectedCount() + ".");
            return;
        }

        // Book the selected seats and build the seat list
        StringBuilder bookedSeats = new StringBuilder();
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                if (selected[i][j]) {
                    seats[i][j] = 'X';
                    selected[i][j] = false;
                    if (bookedSeats.length() > 0) bookedSeats.append(" | ");
                    bookedSeats.append("Row ").append(i + 1).append(", Seat ").append(j + 1);
                    refreshSeat(i, j);
                }
            }
        }

        int catIndex = categoryBox.getSelectedIndex();
        double price = prices[catIndex];
        double total = calculateFare(price, quantity);

        receipt.setText(
                "==== BOOKING CONFIRMATION ====\n"
                + "Customer Name : " + name + "\n"
                + "Movie         : " + movieBox.getSelectedItem() + "\n"
                + "Seat          : " + bookedSeats + "\n"
                + "Category      : " + categoryNames[catIndex] + "\n"
                + "Ticket Price  : Rs." + price + "\n"
                + "Quantity      : " + quantity + "\n"
                + "Total Fare    : Rs." + total + "\n"
                + "Status        : BOOKING CONFIRMED\n");

        // Keep the quantity within the number of seats still available
        int available = availableCount();
        if (available == 0) {
            confirmBtn.setEnabled(false);
            confirmBtn.setText("Sold Out");
        } else {
            SpinnerNumberModel model = (SpinnerNumberModel) qtySpinner.getModel();
            model.setMaximum(available);
            if (getQuantity() > available) qtySpinner.setValue(available);
        }
        updateSummary();
        JOptionPane.showMessageDialog(this, "Booking confirmed!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MovieTicketBookingGUI().setVisible(true));
    }
}