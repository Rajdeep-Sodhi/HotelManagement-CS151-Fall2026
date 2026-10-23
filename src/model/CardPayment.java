
package model;

import exception.HotelException;

public final class CardPayment extends Payment{
        // Simulates approval/decline
        private final boolean approved;

        CardPayment(int id, Payable target, double amount, boolean approved) throws HotelException{
            super(id, target, amount);
            this.approved = approved;
        }

        @Override
        protected void validateTender() throws HotelException{
            if (!approved) {
                throw new HotelException("Card payment was declined.");
            }
        }
        
        @Override
        public String toString(){
            return String.format("CardPayment{id=%d, approved=%s, processed=%s}",
                    getPaymentId(), approved, isProcessed());
        }
}
