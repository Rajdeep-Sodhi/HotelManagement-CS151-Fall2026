package model;

import exception.HotelException;
import java.time.LocalDate;

public class CheckPayment extends Payment {

    private final String name;
    private final String address;
    private final String checkNumber;
    private final LocalDate checkDate;

    CheckPayment(int id, Payable purchase, double amountDue, String name, String address, String checkNumber,
            LocalDate checkDate) throws HotelException {
        super(id, purchase, amountDue);

        this.checkNumber = checkNumber;
        this.address = address;
        this.name = name;
        this.checkDate = checkDate;

    }

    @Override
    protected void validateTender() throws HotelException {
        if (checkDate.isAfter(LocalDate.now())) {
            throw new HotelException("Check date is not valid");
        }
    }

    private String getCheckNumber() {
        return checkNumber;
    }

    private String getAddress() {
        return address;
    }

    private LocalDate getCheckDate() {
        return checkDate;
    }

    private String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("CheckPayment{name=%s, address=%s, checkNumber=%d, checkDate=%s, amount=%.2f}",
                getName(), getAddress(), getCheckNumber(), getCheckDate(), amount());
    }

}
