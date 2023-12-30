package it.polito.ezshop.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SaleTransactionClass extends BalanceOperationClass implements SaleTransaction{
    private int ticketNumber;
    private Map<String, TicketProduct> entries;
    private double discountRate;
    private static int saleCount = 0;
    private boolean isClosed;
    private boolean isPayed;

    public SaleTransactionClass() {
        saleCount++;
        type = TYPE_CREDIT;
        ticketNumber = saleCount;
        date = LocalDate.now();
        money = 0;
        isClosed = false;
        entries = new HashMap<>();
    }

    public SaleTransactionClass(int id, String type, double money, String date, int tNumber, boolean closed, boolean paid, double discount){
        //per il DB, quando crea la mappa a partire dai dati già esistenti
        balanceId = id;
        this.type = type;
        this.money = money;
        this.date = LocalDate.parse(date);
        ticketNumber = tNumber;
        isClosed = closed;
        isPayed = paid;
        discountRate = discount;
        entries = new HashMap<>();
    }

    @Override
    public Integer getTicketNumber() {
        return ticketNumber;
    }

    @Override
    public void setTicketNumber(Integer ticketNumber) {
        if (ticketNumber == null || ticketNumber < 0)
            return;
        this.ticketNumber = ticketNumber;
    }

    @Override
    public List<TicketEntry> getEntries() {
        if (entries == null || entries.isEmpty())
            return new ArrayList<>();
        return entries.values().stream().collect(Collectors.toList());
    }

    public TicketProduct getEntry(String productCode){
        return entries.get(productCode);
    }

    @Override
    public void setEntries(List<TicketEntry> entries) {
        if (entries == null || entries.isEmpty())
            return;
        for (TicketEntry t : entries){
            this.entries.put(t.getBarCode(), (TicketProduct) t);
        }
        calculatePrice();
    }

    public void addEntry(TicketProduct newEntry){
        if (newEntry == null)
            return;
        //se quel barcode esiste già, devo solo aggiungere l'rfid nella entry
        String barcode = newEntry.getBarCode();
        if (entries.containsKey(newEntry.getBarCode())){
            entries.get(barcode).addRFID(newEntry.getRfid().get(0)); //todo: 0 o 1?
        }
        //altrimenti aggiungo la entry così com'è
        else{
            entries.put(newEntry.getBarCode(), newEntry);
        }
        calculatePrice();
    }

    public void removeEntry(String barcode){
        entries.remove(barcode);
        calculatePrice();
    }

    @Override
    public double getDiscountRate() {
        return discountRate;
    }

    @Override
    public void setDiscountRate(double discountRate) {
        if (discountRate < 0 || discountRate >= 1)
            return;
        calculatePrice();
        this.discountRate = discountRate;
    }

    @Override
    public double getPrice() {
        calculatePrice();
        return money;
    }

    private void calculatePrice(){
        money = 0;
        for (TicketEntry tp : entries.values()){
            money += tp.getPricePerUnit() * tp.getAmount() * (1 - tp.getDiscountRate());
        }
        money *= (1 - discountRate);
    }

    @Override
    public void setPrice(double price) {
        if (price <= 0)
            return;
        money = price;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        calculatePrice();
        isClosed = closed;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public void setPayed(boolean payed) {
        calculatePrice();
        isPayed = payed;
    }

    public static void setSaleCount(int count) {
        saleCount = count;
    }

    public void removeEntryRFID(String pid, String rfid) {
        entries.get(pid).getRfid().remove(rfid);
    }

}
