package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.SaleTransactionClass;
import it.polito.ezshop.data.TicketEntry;
import it.polito.ezshop.data.TicketProduct;
import it.polito.ezshop.utils.DbManagerClass;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;

public class SaleTransactionClassTest extends TestCase {

    String rfid = "test";

    @AfterClass
    public static void disconnect() {
        DbManagerClass.disconnect();
    }

    @Test
    public void testSetTicketNumber() {
        SaleTransactionClass s = new SaleTransactionClass(12,
                "Credit", 63.27, "2021-05-17", 3, false, false, 0.0);

        s.setTicketNumber(13);
        assertTrue(s.getTicketNumber() == 13);

        s.setTicketNumber(null);
        assertTrue(s.getTicketNumber() == 13);

        s.setTicketNumber(-2);
        assertTrue(s.getTicketNumber() == 13);
    }

    @Test
    public void testSetEntries() {
        SaleTransactionClass s = new SaleTransactionClass(12,
                "Credit", 63.27, "2021-05-17", 3, false, false, 0.0);
        TicketProduct p1 = new TicketProduct("453825801121", rfid, "milk", 3, 0.99, 0.0);
        TicketProduct p2 = new TicketProduct("147258036907", rfid, "pasta", 2, 1.39, 0.0);
        TicketProduct p3 = new TicketProduct("1472580369089", rfid, "sugar", 1, 0.79, 0.0);
        TicketProduct p4 = new TicketProduct("123456789098", rfid, "chicken", 2, 3.29, 0.2);
        TicketProduct p5 = new TicketProduct("987654321012", rfid, "cheese", 2, 3.99, 0.2);

        ArrayList<TicketEntry> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);
        list.add(p3);
        list.add(p4);
        list.add(p5);

        s.setEntries(list);
        for (int i = 0; i< 5; i++){
            assertNotNull(s.getEntry(list.get(i).getBarCode()));
        }
        s.setEntries(null);
        assertFalse(s.getEntries().isEmpty());
        s.setEntries(new ArrayList<>());
        assertFalse(s.getEntries().isEmpty());
    }

    @Test
    public void testAddEntry() {
        SaleTransactionClass s = new SaleTransactionClass(12,
                "Credit", 63.27, "2021-05-17", 3, false, false, 0.0);
        TicketProduct p1 = new TicketProduct("453825801121", rfid, "milk", 3, 0.99, 0.0);

        s.addEntry(p1);
        assertTrue(p1.equals(s.getEntry(p1.getBarCode())));
    }

    @Test
    public void testRemoveEntry() {
        SaleTransactionClass s = new SaleTransactionClass(12,
                "Credit", 63.27, "2021-05-17", 3, false, false, 0.0);
        TicketProduct p1 = new TicketProduct("453825801121", rfid, "milk", 3, 0.99, 0.0);
        TicketProduct p2 = new TicketProduct("147258036907", rfid, "pasta", 2, 1.39, 0.0);
        TicketProduct p3 = new TicketProduct("1472580369089", rfid, "sugar", 1, 0.79, 0.0);

        ArrayList<TicketEntry> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);
        list.add(p3);

        s.setEntries(list);
        s.removeEntry(p1.getBarCode());
        assertTrue(s.getEntry(p1.getBarCode()) == null);
    }

    @Test
    public void testSetDiscountRate() {
        SaleTransactionClass s = new SaleTransactionClass(12,
                "Credit", 63.27, "2021-05-17", 3, false, false, 0.0);

        s.setDiscountRate(0.15);
        assertTrue(s.getDiscountRate() == 0.15);

        s.setDiscountRate(-0.2);
        assertTrue(s.getDiscountRate() == 0.15);

        s.setDiscountRate(1.15);
        assertTrue(s.getDiscountRate() == 0.15);
    }

    @Test
    public void testSetPrice() {
        SaleTransactionClass s = new SaleTransactionClass(12,
                "Credit", 63.27, "2021-05-17", 3, false, false, 0.0);

        TicketProduct p1 = new TicketProduct("453825801121", rfid, "milk", 3, 0.99, 0.0);

        s.addEntry(p1);
        assertTrue(s.getPrice() == 3*0.99);

        s.setPrice(-5.91);
        assertTrue(s.getPrice() == 3*0.99);
    }

    @Test
    public void testSetClosed() {
        SaleTransactionClass s = new SaleTransactionClass(12,
                "Credit", 63.27, "2021-05-17", 3, false, false, 0.0);

        s.setClosed(true);
        assertTrue(s.isClosed());
        s.setClosed(false);
        assertTrue(!s.isClosed());
    }

    @Test
    public void testSetPayed() {
        SaleTransactionClass s = new SaleTransactionClass(12,
                "Credit", 63.27, "2021-05-17", 3, false, false, 0.0);

        s.setPayed(true);
        assertTrue(s.isPayed());
        s.setPayed(false);
        assertTrue(!s.isPayed());
    }
}