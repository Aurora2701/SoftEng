package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.TicketProduct;
import junit.framework.TestCase;
import org.junit.Test;

public class TicketProductTest extends TestCase {

    private final String rfid = "123456789012";

    @Test
    public void testSetBarCode() {
        TicketProduct tp = new TicketProduct("453825801121", rfid, "milk", 2, 0.99, 0.0);
        String newBarCode = "453825801152";

        tp.setBarCode(newBarCode);
        String pc = tp.getBarCode();
        assertTrue(pc.equals(newBarCode));

        tp.setBarCode(null);
        pc = tp.getBarCode();
        assertTrue(pc.equals(newBarCode));

        tp.setBarCode("");
        pc = tp.getBarCode();
        assertTrue(pc.equals(newBarCode));
    }

    @Test
    public void testSetProductDescription() {
        TicketProduct tp = new TicketProduct("453825801121", rfid, "milk", 2, 0.99, 0.0);
        String newDescr = "biscuits";

        tp.setProductDescription(newDescr);
        assertTrue(newDescr.equals(tp.getProductDescription()));

        tp.setProductDescription(null);
        assertTrue(newDescr.equals(tp.getProductDescription()));

        tp.setProductDescription("");
        assertTrue(newDescr.equals(tp.getProductDescription()));

    }

    @Test
    public void testSetAmount() {
        TicketProduct tp = new TicketProduct("453825801121", rfid, "milk", 2, 0.99, 0.0);

        tp.setAmount(4);
        assertTrue(tp.getAmount() == 4);

        tp.setAmount(-3);
        assertTrue(tp.getAmount() == 4);
    }

    @Test
    public void testSetPricePerUnit() {
        TicketProduct tp = new TicketProduct("453825801121", rfid, "milk", 2, 0.99, 0.0);

        tp.setPricePerUnit(1.19);
        assertTrue(tp.getPricePerUnit() == 1.19);

        tp.setPricePerUnit(-0.2);
        assertTrue(tp.getPricePerUnit() == 1.19);
    }

    @Test
    public void testSetDiscountRate() {
        TicketProduct tp = new TicketProduct("453825801121", rfid, "milk", 2, 0.99, 0.0);

        tp.setDiscountRate(0.15);
        assertTrue(tp.getDiscountRate() == 0.15);

        tp.setDiscountRate(-0.2);
        assertTrue(tp.getDiscountRate() == 0.15);

        tp.setDiscountRate(1.15);
        assertTrue(tp.getDiscountRate() == 0.15);
    }
}