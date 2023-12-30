package it.polito.ezshop.data;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnTransactionClass extends BalanceOperationClass{
    private int quantity;
    private float returnedValue;
    private Integer parentTransactionId;

    public void setProducts(Map<Integer, Integer> products) {
        this.products = products;
    }

    /*This map will contain the id of products in the return transaction and their amount*/
    private Map<Integer, Integer> products; //<id, amount>
    private List<String> RFIDReturns;
    private boolean isClosed;
    private boolean isPayed;


    public ReturnTransactionClass(Integer parentTransactionId){
        this.parentTransactionId = parentTransactionId;
        type = TYPE_DEBIT;
        this.products = new HashMap<>();
        this.isClosed = false;
        this.isPayed = false;
        this.RFIDReturns = new ArrayList<>();
    }

    /*This is the full constructor that will be used when reading all data from the database*/
    public ReturnTransactionClass(Integer balanceId, LocalDate date, String type, Integer parentTransactionId){
        this.balanceId = balanceId;
        this.date = date;
        this.type = type;
        this.parentTransactionId = parentTransactionId;
        this.products = new HashMap<>();
        this.RFIDReturns = new ArrayList<>();
    }

    /** @deprecated  */
    public void addProduct(Integer productId, int amount){
        if(products.containsKey(productId)){
            products.replace(productId, amount + (int) products.get(productId));
        } else {
            products.put(productId, amount);
        }
    }

    public void addRFIDProduct(String RFID){
        RFIDReturns.add(RFID);
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public void setPayed(boolean payed) {
        isPayed = payed;
    }

    public Integer getParentTransactionId() {
        return parentTransactionId;
    }

    /** @deprecated  */
    public Map<Integer, Integer> getProducts(){
        return products;
    }

    public List<String> getRFIDReturns(){
        return RFIDReturns;
    }
}
