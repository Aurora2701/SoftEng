package it.polito.ezshop.data;

import it.polito.ezshop.utils.Validator;

import static it.polito.ezshop.utils.Validator.checkPositionFormat;

public class ProductTypeClass implements ProductType {

    private Integer id;
    private String productDescription;
    private String barCode;
    private Double pricePerUnit;
    private String note;
    private Integer quantity;
    private String location;



    public ProductTypeClass(String description, String productCode, double pricePerUnit, String note) {
        this.productDescription = description;
        this.barCode = productCode;
        this.pricePerUnit = pricePerUnit;
        this.note = note;

        this.id = 0;
        this.quantity=0;
        this.location="";

    }

    @Override
    public Integer getId () {
        return id;
    }

    @Override
    public void setId (Integer id) {
        if (id>0)
            this.id = id;
    }

    @Override
    public String getProductDescription () {
        return productDescription;
    }

    @Override
    public void setProductDescription (String productDescription) {
        if (productDescription != null && !productDescription.isEmpty())
            this.productDescription = productDescription;
    }

    @Override
    public String getBarCode () {
        return barCode;
    }

    @Override
    public void setBarCode (String barCode) {
        if (barCode != null && !barCode.isEmpty())
            this.barCode = barCode;
    }

    @Override
    public Double getPricePerUnit () {
        return pricePerUnit;
    }

    public void setPricePerUnit (Double pricePerUnit) {
        if (pricePerUnit != null && pricePerUnit>0)
            this.pricePerUnit = pricePerUnit;
    }

    @Override
    public String getNote () {
        return note;
    }

    @Override
    public void setNote (String note) {
        this.note = note;
    }

    @Override
    public Integer getQuantity () {
        return quantity;
    }

    @Override
    public void setQuantity (Integer quantity) {
        if (quantity == null || quantity>=0)
            this.quantity = quantity;
    }

    @Override
    public String getLocation () {
        return location;
    }

    @Override
    public void setLocation (String location) {
            this.location = location;
    }
}
