package model;

import exception.HotelException;

public interface Payable {

    public double getAmountDue(Session viewer) throws HotelException;

    public void pay(Payment payment, Session actor) throws HotelException;

}
