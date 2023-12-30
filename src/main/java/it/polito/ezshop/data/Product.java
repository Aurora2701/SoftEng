package it.polito.ezshop.data;

public class Product{
    private String rfid;
    private String productCode;
    protected String status; //"Inventory" - "Sold" - "Returned"
    public final static String STAT_INV = "Inventory";
    public final static String STAT_SOLD = "Sold";
    public final static String STAT_RET = "Returned";

    public Product (String rfid, String productCode, String status) {
        this.rfid = rfid;
        this.status = status;
        this.productCode = productCode;

    }

    public void setRfid (String rfid) {
        if (rfid == null || rfid.isEmpty())
            return;
        if (Long.parseLong(rfid) >= 0 )
            this.rfid = rfid;
    }

    public String getRfid () {
        return rfid;
    }

    public String getStatus () {
        return status;
    }

    public void setStatus (String status) {
        if (status != null && !status.isEmpty() && checkStatus(status))
            this.status = status;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        if (productCode != null && !productCode.isEmpty())
            this.productCode = productCode;
    }

    public boolean checkStatus(String status){
        return status.equals(STAT_INV) || status.equals(STAT_SOLD) || status.equals(STAT_RET);
    }
}
