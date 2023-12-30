package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.OrderClass;
import junit.framework.TestCase;
import org.junit.Test;

import java.time.LocalDate;

public class OrderClassTest extends TestCase {

    @Test
    public void testSetBalanceId() {
        OrderClass o = new OrderClass("453825801121", 4.3, 50, "ISSUED");
        o.setBalanceId(5);
        int balId = o.getBalanceId();
        assertEquals(5, balId);

        o.setBalanceId(-3);
        balId = o.getBalanceId();
        assertEquals(5, balId);

        o.setBalanceId(null);
        balId = o.getBalanceId();
        assertEquals(5, balId);

    }

    @Test
    public void testSetProductCode() {
        OrderClass o = new OrderClass(5, "2021-05-17", "453825801121", 4.3, 50, "ISSUED", 2);
        String newBarCode = "453825801152";

        o.setProductCode(newBarCode);
        String pc = o.getProductCode();
        assertTrue(pc.equals(newBarCode));

        o.setProductCode(null);
        pc = o.getProductCode();
        assertTrue(pc.equals(newBarCode));

        o.setProductCode("");
        pc = o.getProductCode();
        assertTrue(pc.equals(newBarCode));
    }

    @Test
    public void testSetPricePerUnit() {
        OrderClass o = new OrderClass(5, "2021-05-17", "453825801121", 4.3, 50, "ISSUED", 2);
        double newPrice = 3.5;

        o.setPricePerUnit(newPrice);
        double p = o.getPricePerUnit();
        assertTrue(p == newPrice);

        o.setPricePerUnit(-0.3);
        p = o.getPricePerUnit();
        assertTrue( p == newPrice);
    }

    @Test
    public void testSetQuantity() {
        OrderClass o = new OrderClass(5, "2021-05-17", "453825801121", 4.3, 50, "ISSUED", 2);
        int newQuantity = 80;

        o.setQuantity(newQuantity);
        int p = o.getQuantity();
        assertTrue(p == newQuantity);

        o.setPricePerUnit(-25);
        p = o.getQuantity();
        assertTrue(p == newQuantity);
    }

    @Test
    public void testSetStatus() {
        OrderClass o = new OrderClass(5, "2021-05-17", "453825801121", 4.3, 50, "ISSUED", 2);
        String status = "PAYED";

        o.setStatus(status);
        String pc = o.getStatus();
        assertTrue(pc.equals(status));

        o.setProductCode(null);
        pc = o.getStatus();
        assertTrue(pc.equals(status));

        o.setProductCode("");
        pc = o.getStatus();
        assertTrue(pc.equals(status));
    }

    @Test
    public void testSetOrderId() {
        OrderClass o = new OrderClass(5, "2021-05-17", "453825801121", 4.3, 50, "ISSUED", 2);
        int id = 3;

        o.setOrderId(id);
        int o_id = o.getOrderId();
        assertTrue(o_id == id);

        o.setOrderId(null);
        o_id = o.getOrderId();
        assertTrue(o_id == id);

        o.setOrderId(-7);
        o_id = o.getOrderId();
        assertTrue(o_id == id);
    }

    @Test
    public void testSetDate() {
        OrderClass o = new OrderClass(5, "2021-05-17", "453825801121", 4.3, 50, "ISSUED", 2);
        LocalDate d = LocalDate.of(2021, 05, 17);

        o.setDate(d);
        LocalDate ld = o.getDate();
        assertEquals(d, ld);

        o.setDate(null);
        ld = o.getDate();
        assertEquals(d, ld);
    }

    @Test
    public void testSetMoney() {
        OrderClass o = new OrderClass(5, "2021-05-17", "453825801121", 4.3, 50, "ISSUED", 2);
        double money = 120.40;

        o.setMoney(money);
        double m = o.getMoney();
        assertTrue(money == m);

        o.setMoney(-500);
        m = o.getMoney();
        assertTrue(money == m);
    }

    @Test
    public void testSetType() {
        OrderClass o = new OrderClass(5, "2021-05-17", "453825801121", 4.3, 50, "ISSUED", 2);
        String type = "Credit";

        o.setType(type);
        String pc = o.getType();
        assertTrue(pc.equals(type));

        o.setType(null);
        pc = o.getType();
        assertTrue(pc.equals(type));

        o.setType("");
        pc = o.getType();
        assertTrue(pc.equals(type));
    }
}