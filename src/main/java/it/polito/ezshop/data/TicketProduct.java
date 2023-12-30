package it.polito.ezshop.data;

import java.util.ArrayList;
import java.util.List;

public class TicketProduct implements TicketEntry{
    private String barcode;
    private List<String> rfid;
    private String description;
    private int amount;
    private double price;
    private double discount;

    public TicketProduct(String barCode, String rfid, String productDescription, int amount, double pricePerUnit, double discountRate){
        barcode = barCode;
        this.rfid = new ArrayList<>();
        this.rfid.add(rfid);
        description = productDescription;
        this.amount = amount;
        price = pricePerUnit;
        discount = discountRate;
    }
    public TicketProduct(String barCode, String productDescription, int amount, double pricePerUnit, double discountRate){
        barcode = barCode;
        this.rfid = new ArrayList<>();
        description = productDescription;
        this.amount = amount;
        price = pricePerUnit;
        discount = discountRate;
    }

    @Override
    public String getBarCode() {
        return barcode;
    }

    @Override
    public void setBarCode(String barCode) {
        if (barCode == null || barCode.isEmpty())
            return;
        barcode = barCode;
    }

    @Override
    public String getProductDescription() {
        return description;
    }

    @Override
    public void setProductDescription(String productDescription) {
        if (productDescription == null || productDescription.isEmpty())
            return;
        description = productDescription;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        if (amount < 0)
            return;
        this.amount = amount;
    }

    @Override
    public double getPricePerUnit() {
        return price;
    }

    @Override
    public void setPricePerUnit(double pricePerUnit) {
        if (pricePerUnit < 0)
            return;
        price = pricePerUnit;
    }

    @Override
    public double getDiscountRate() {
        return discount;
    }

    @Override
    public void setDiscountRate(double discountRate) {
        if (discountRate < 0 || discountRate >= 1)
            return;
        discount = discountRate;
    }

    public List<String> getRfid() {
        return rfid;
    }

    public void addRFID(String RFID){
        if (RFID != null && !RFID.isEmpty()) {
            rfid.add(RFID);
            amount++;
        }
    }

    public boolean removeRFID(String RFID){
        if(!rfid.contains(RFID)){
            return false;
        }
        rfid.remove(RFID);
        amount--;
        return true;
    }
}
