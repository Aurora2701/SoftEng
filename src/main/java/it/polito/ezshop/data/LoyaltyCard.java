package it.polito.ezshop.data;

public class LoyaltyCard {

    private String ID;
    private Integer points;
    private Integer cardOwnerID;

    public LoyaltyCard(String ID, Integer points, Integer cardOwner) {

        if (ID != null && !ID.isEmpty()) this.ID = ID;
        if(points < 0) this.points = 0;
        else this.points = points;
        this.cardOwnerID = cardOwner; // user ID of the card owner

    }

    public LoyaltyCard(String id) {
        if (id != null && id.length() == 10 && id.matches("[0-9]+")) this.ID = id;
        this.points = 0;
        this.cardOwnerID = -1;
    }

    public void addPoints(Integer value) {
        this.points += value;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {

        if (ID == null || ID.length() != 10 || !ID.matches("[0-9]+"))
            return;

        this.ID = ID;

    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {

        if(points < 0) this.points = 0;
        else this.points = points;

    }

    public Integer getCardOwnerID() { return cardOwnerID; }

    public void setCardOwnerID(Integer cardOwnerID) {
        this.cardOwnerID = cardOwnerID;
    }


}
