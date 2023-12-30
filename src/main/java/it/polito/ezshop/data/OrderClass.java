package it.polito.ezshop.data;

import java.sql.SQLException;
import java.time.LocalDate;

import static it.polito.ezshop.utils.DatabaseQuery.createOrder;

public class OrderClass implements Order{
    private int balanceId;
    private LocalDate date;
    private double money;
    private String type;
    private String productCode;
    private double pricePerUnit;
    private int quantity;
    private String status;
    int orderId;

    public OrderClass(String prodCode, double price, int qty, String stat) {
        productCode = prodCode;
        pricePerUnit = price;
        quantity = qty;
        status = stat;
        type = "Debit";
        date = LocalDate.now();
        money = quantity*pricePerUnit;
//        createOrder(this);
    }

    public OrderClass(int balanceId, String date, String prodCode, double price, int qty, String stat, int ordId) {
        //PER DB  - creazione da elementi già esistenti, per riempire le mappe all'inizio
        productCode = prodCode;
        pricePerUnit = price;
        quantity = qty;
        status = stat;
        this.balanceId = balanceId;
        type = "Debit";
        this.date = LocalDate.parse(date);
        money = quantity*pricePerUnit;
        orderId = ordId;
    }

    @Override
    public Integer getBalanceId() { //HO CAMBIATO BalanceOperation !!!
        return balanceId;
    }

    @Override
    public void setBalanceId(Integer balanceId) {
        if (balanceId == null || balanceId < 0)
            return;
        this.balanceId = balanceId;
    }

    @Override
    public String getProductCode() {
        return productCode;
    }

    @Override
    public void setProductCode(String productCode) {
        if (productCode == null || productCode.isEmpty())
            return;
        this.productCode = productCode;
    }

    @Override
    public double getPricePerUnit() {
        return pricePerUnit;
    }

    @Override
    public void setPricePerUnit(double pricePerUnit) {
        if (pricePerUnit < 0)
            return;
        this.pricePerUnit = pricePerUnit;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(int quantity) {
        if (quantity <= 0)
            return;
        this.quantity = quantity;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        if (status == null || status.isEmpty())
            return;
        this.status = status;
    }

    @Override
    public Integer getOrderId() {
        return orderId;
    }

    @Override
    public void setOrderId(Integer orderId) {
        if (orderId == null || orderId < 0)
            return;
        this.orderId = orderId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        if (date == null)
            return;
        this.date = date;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        if (money < 0)
            return;
        this.money = money;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.isEmpty())
            return;
        this.type = type;
    }
}
