package it.polito.ezshop.data;

public class CreditCardClass {
  private final String cardNumber;
  private Double credit;

    public CreditCardClass (String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public double getCardCredit() {
        return credit;
    }

    public void setCardCredit(double newCredit) {
        if (newCredit>=0)
            this.credit = newCredit;
    }
}

