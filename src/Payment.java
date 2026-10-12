public class Payment implements Payable {

    // Basic information for each payment
    private int paymentID;
    private double amount;
    private String paymentMethod;

    // Creates a payment with the information provided
    public Payment(int paymentID, double amount, String paymentMethod) {
        this.paymentID = paymentID;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    // Returns the payment ID
    public int getPaymentID() {
        return paymentID;
    }

    // Returns the payment amount
    public double getAmount() {
        return amount;
    }

    // Returns the payment method
    public String getPaymentMethod() {
        return paymentMethod;
    }

    // Processes the payment in the console simulation
    @Override
    public void processPayment() {
        System.out.printf("Payment %d processed: $%.2f using %s.%n",
                paymentID, amount, paymentMethod);
    }

    // Refunds the payment in the console simulation
    @Override
    public void refundPayment() {
        System.out.printf("Payment %d refunded: $%.2f using %s.%n",
                paymentID, amount, paymentMethod);
    }

    // Displays the payment receipt
    @Override
    public void displayReceipt() {
        System.out.println("Payment Receipt");
        System.out.println("Payment ID: " + paymentID);
        System.out.printf("Amount: $%.2f%n", amount);
        System.out.println("Payment Method: " + paymentMethod);
    }
}
