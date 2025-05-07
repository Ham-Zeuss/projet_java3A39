package entite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Commande {
    private int id;
    private int userId;
    private int packId;
    private double amount;
    private String commandeDate;
    private String paymentMethod;
    private String expiryDate;
    private String status;

    // Default constructor
    public Commande() {}

    // Constructor without ID (for new orders)
    public Commande(int userId, int packId, double amount, String commandeDate,
                    String paymentMethod, String expiryDate, String status) {
        this.userId = userId;
        this.packId = packId;
        this.amount = amount;
        this.commandeDate = commandeDate;
        this.paymentMethod = paymentMethod;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    // Full constructor
    public Commande(int id, int userId, int packId, double amount, String commandeDate,
                    String paymentMethod, String expiryDate, String status) {
        this.id = id;
        this.userId = userId;
        this.packId = packId;
        this.amount = amount;
        this.commandeDate = commandeDate;
        this.paymentMethod = paymentMethod;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getPackId() { return packId; }
    public void setPackId(int packId) { this.packId = packId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        this.amount = amount;
    }

    public String getCommandeDate() { return commandeDate; }
    public void setCommandeDate(String commandeDate) { this.commandeDate = commandeDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        List<String> validStatuses = Arrays.asList("Pending", "Completed", "Cancelled");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        this.status = status;
    }

    // Utility method to check if the order has expired
    public boolean isExpired() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date expiry = sdf.parse(expiryDate);
            return expiry.before(new Date());
        } catch (ParseException e) {
            System.err.println("Error parsing expiry date: " + e.getMessage());
            return false;
        }
    }

    // Overridden toString method
    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", userId=" + userId +
                ", packId=" + packId +
                ", amount=" + amount +
                ", commandeDate='" + commandeDate + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    // Overridden equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commande commande = (Commande) o;
        return id == commande.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}