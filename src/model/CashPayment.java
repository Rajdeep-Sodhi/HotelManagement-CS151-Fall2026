package model;

import exception.HotelException;

/** Validates cash payments and calculates change. */
public final class CashPayment extends Payment
{
	private final double cashPaid; //checks cash already paid.
	private double change; //keeps track of the change from paid cash.

	//Constructor
	CashPayment(int id, Payable purchase, double amountDue, double cashPaid)
			throws HotelException
	{
		super(id, purchase, amountDue);
		this.cashPaid = Hotel.roundMoney(cashPaid);
		change = 0.0;
	}

	// makes sure more cash is paid than the rent
	@Override
	protected void validateTender() throws HotelException
	{
		long paidCents = Math.round(cashPaid * 100.0);
		long dueCents = Math.round(amount() * 100.0);

		if (paidCents < dueCents)
		{
			throw new HotelException("Insufficient cash. Amount due: "
					+ String.format("%.2f", amount()));
		}

		change = (paidCents - dueCents) / 100.0;
	}

	//calls superclass's receipt method to give reciept to user.
	@Override
	public String receipt(Session session) throws HotelException
	{
		
		return super.receipt(session)
				+ ", cash tendered=" + String.format("%.2f", cashPaid)
				+ ", change=" + String.format("%.2f", change);
	}

	//call's super's toString() method.
	@Override
	public String toString()
	{
		return super.toString();
	}
}
