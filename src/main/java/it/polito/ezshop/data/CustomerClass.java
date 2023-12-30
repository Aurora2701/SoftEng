package it.polito.ezshop.data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomerClass implements Customer{
    private Integer ID;
    private Integer points;
    private String name;
    private String customerCard;  // loyaltyCard = customerCard

    public CustomerClass(String Name, String CustomerCard) {

        if (Name != null && !Name.isEmpty()) this.name = Name;
        if (CustomerCard != null && !CustomerCard.isEmpty()) this.customerCard = CustomerCard;
        this.points = 0;

    }

    public CustomerClass(String Name){

        this.name = Name;
        this.customerCard = null;
        this.points = 0;

    }
    public CustomerClass(String Name, Integer id){

        if (Name != null && !Name.isEmpty()) this.name = Name;
        if(id >= 0) this.ID = id;

        this.customerCard = null;
        this.points = 0;


    }

    public CustomerClass(Integer ID, Integer points, String name, String customerCard) {

        if(ID >= 0) this.ID = ID;
        if (name != null && !name.isEmpty()) this.name = name;
        if (customerCard != null && customerCard.length() == 10 && customerCard.matches("[0-9]+")) this.customerCard = customerCard;
        if(points < 0) this.points = 0;
        else this.points = points;


        this.name = name;
        this.customerCard = customerCard;

    }

    @Override
    public String getCustomerName() {
        return name;
    }

    @Override
    public void setCustomerName(String customerName) {

        if (customerName == null || customerName.isEmpty())
            return;
        this.name = customerName;

    }

    @Override
    public String getCustomerCard() {
        return customerCard;
    }

    @Override
    public void setCustomerCard(String customerCard) {

        if (customerCard != null && (customerCard.length() != 10 || !customerCard.matches("[0-9]+")))
            return;

        this.customerCard = customerCard;

    }

    @Override
    public Integer getId() {
        return ID;
    }

    @Override
    public void setId(Integer id) {

        if(id<0) return;
        this.ID = id;

    }

    @Override
    public Integer getPoints() {
        return points;
    }

    @Override
    public void setPoints(Integer points) {

        if(points < 0) this.points = 0;
        else this.points = points;

    }
}
