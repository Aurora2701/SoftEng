package it.polito.ezshop.data;

import java.time.LocalDate;

public class BalanceOperationClass implements BalanceOperation{

    protected Integer balanceId;
    protected LocalDate date;
    protected double money;
    protected String type;  //"Debit" o "Credit
    public final static String TYPE_CREDIT = "CREDIT";
    public final static String TYPE_DEBIT = "DEBIT";
//    public final static String TYPE_ORDER = "ORDER";
//    public final static String TYPE_SALE = "SALE";
//    public final static String TYPE_RETURN = "RETURN";

    public BalanceOperationClass(){
        date = LocalDate.now();
    }

    @Override
    public int getBalanceId() {
        return balanceId;
    }

    @Override
    public void setBalanceId(int balanceId) {
        if (balanceId < 0)
            return;
        this.balanceId = balanceId;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public void setDate(LocalDate date) {
        if (date == null)
            return;
        this.date = date;
    }

    @Override
    public double getMoney() {
        return money;
    }

    @Override
    public void setMoney(double money) {
        if (money < 0)
            return;
        this.money = money;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        if (type == null || type.isEmpty())
            return;
        this.type = type;
    }
}
