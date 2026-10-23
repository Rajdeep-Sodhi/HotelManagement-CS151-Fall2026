package model;

import exception.HotelException;

public abstract class Payment{
        private final int paymentId;
        private final Payable target;
        private final double amount;
        private boolean processed;

        /* Package-private constructor validates a positive ID, a non-null purchase, and a
            finite positive amount rounded to cents. */
        Payment(int id, Payable target, double amount) throws HotelException{
            if (id <= 0) {
                throw new HotelException("Payment ID must be positive.");
            }
            if (target == null) {
                throw new HotelException("A payment must have a purchase target.");
            }
            if (!Double.isFinite(amount) || amount <= 0) {
                throw new HotelException("Payment amount must be a finite positive value.");
            }
            if (cents(amount) <= 0 || amount != centsToAmount(cents(amount))) {
                throw new HotelException("Payment amount must use no more than two decimal places.");
            }

            paymentId = id;
            this.target = target;
            this.amount = centsToAmount(cents(amount));
            processed = false;
        }

        /* Package-private. Require matching target and equal rounded-cent amounts, and reject
            a processed payment; validate tender, then mark processed. No target mutation. */
        final void process(Payable purchase, double expectedAmount) throws HotelException{
            if (processed) {
                throw new HotelException("Payment has already been processed.");
            }
            if (purchase != target) {
                throw new HotelException("Payment does not belong to this purchase.");
            }
            if (!Double.isFinite(expectedAmount)
                    || cents(expectedAmount) != cents(amount)) {
                throw new HotelException("Payment must cover the full balance.");
            }

            validateTender();
            processed = true;
        }

        /* Cash and card delay their different validation until processing. */
        protected abstract void validateTender() throws HotelException;

        /* Internal subclass helper; no mutable state is exposed. */
        protected final double amount(){
            return amount;
        }

        /* Authorize through the purchase before displaying a processed payment's ID, amount,
            tender type, and cash change where applicable. */
        public String receipt(Session viewer) throws HotelException{
            if (!processed) {
                throw new HotelException("Payment has not been processed.");
            }

            // This call performs the purchase's normal ownership/authorization check.
            target.getAmountDue(viewer);

            return String.format("Payment ID: %d%nAmount: $%.2f%nTender: %s",
                    paymentId, amount, getClass().getSimpleName());
        }

        public int getPaymentId(){
            return paymentId;
        }

        Payable target(){
            return target;
        }

        /* Package-private amount for the manager's summary; reject an unprocessed payment. */
        double processedAmount(){
            if (!processed) {
                throw new IllegalStateException("Payment has not been processed.");
            }
            return amount;
        }

        @Override
        public String toString(){
            return String.format("Payment{id=%d, processed=%s, tender=%s}",
                    paymentId, processed, getClass().getSimpleName());
        }

        private static long cents(double value) {
            return Math.round(value * 100.0);
        }

        private static double centsToAmount(long value) {
            return value / 100.0;
        }
}
