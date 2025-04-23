package entite;

public class Commande {
    private int id;
    private int userId;
    private int packId;
    private double amount;
    private String commandeDate;
    private String paymentMethod;
    private String expiryDate;
    private String status;

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
    public void setAmount(double amount) { this.amount = amount; }
    public String getCommandeDate() { return commandeDate; }
    public void setCommandeDate(String commandeDate) { this.commandeDate = commandeDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}