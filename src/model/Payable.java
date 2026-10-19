package model;

public interface Payable {

    // Processes the payment
    void processPayment();

    // Refunds the payment
    void refundPayment();

    // Displays the payment receipt
    void displayReceipt();
}
