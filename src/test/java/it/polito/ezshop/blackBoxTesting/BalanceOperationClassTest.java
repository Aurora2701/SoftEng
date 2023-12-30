package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.BalanceOperationClass;
import junit.framework.TestCase;
import org.junit.Test;

import java.time.LocalDate;

public class BalanceOperationClassTest extends TestCase {

    @Test
    public void testSetBalanceId() {
        BalanceOperationClass b = new BalanceOperationClass();

        b.setBalanceId(12);
        assertTrue(b.getBalanceId() == 12);

        b.setBalanceId(-1);
        assertTrue(b.getBalanceId() == 12);
    }

    @Test
    public void testSetDate() {
        BalanceOperationClass b = new BalanceOperationClass();
        LocalDate d = LocalDate.of(2021, 05, 17);

        b.setDate(d);
        assertTrue(d.isEqual(b.getDate()));
        b.setDate(null);
        assertTrue(d.isEqual(b.getDate()));
    }

    @Test
    public void testSetMoney() {
        BalanceOperationClass b = new BalanceOperationClass();

        b.setMoney(12.27);
        assertTrue(b.getMoney() == 12.27);
        b.setMoney(-2.27);
        assertTrue(b.getMoney() == 12.27);
    }

    @Test
    public void testSetType() {
        BalanceOperationClass b = new BalanceOperationClass();

        String type = "Credit";

        b.setType(type);
        String pc = b.getType();
        assertTrue(pc.equals(type));

        b.setType(null);
        pc = b.getType();
        assertTrue(pc.equals(type));

        b.setType("");
        pc = b.getType();
        assertTrue(pc.equals(type));
    }
}