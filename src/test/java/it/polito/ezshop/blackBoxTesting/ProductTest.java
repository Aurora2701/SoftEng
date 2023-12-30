package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.Product;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProductTest {
    private Product pc;
    private final String RFID = "000000010000";
    private final String productCode = "1234567890128";

    @Before
    public void setup (){
        pc = new Product(RFID, productCode, Product.STAT_INV);
    }


    @Test
    public void testSetRfid () {
        /*Invalid RFID (< 0) */
        pc.setRfid("-1");
        assertEquals(RFID,pc.getRfid());
        /*Invalid RFID null*/
        pc.setRfid(null);
        assertEquals(RFID,pc.getRfid());
        /* Valid RFID */
        pc.setRfid("111111111111");
        assertEquals("111111111111", pc.getRfid());
    }

    @Test
    public void testCheckStatus () {
        assertFalse(RFID, pc.checkStatus("123"));
        assertFalse(RFID, pc.checkStatus(""));
        assertTrue(RFID, pc.checkStatus("Inventory"));
        assertTrue(RFID, pc.checkStatus("Sold"));
        assertTrue(RFID, pc.checkStatus("Returned"));
    }

    @Test
    public void testSetStatus () {
        /*Invalid Status */
        pc.setStatus("Status");
        assertEquals(pc.getStatus(), Product.STAT_INV);
        /*Invalid Status -> null */
        pc.setStatus(null);
        assertEquals(pc.getStatus(), Product.STAT_INV);
        /*Invalid Status -> empty */
        pc.setStatus("");
        assertEquals(pc.getStatus(), Product.STAT_INV);

        /* Valid Status */
        pc.setStatus("Sold");
        assertEquals(pc.getStatus(), Product.STAT_SOLD);
    }

    @Test
    public void testSetProductCode () {
        /* Invalid ProductCode -> empty */
        pc.setProductCode("");
        assertEquals(productCode, pc.getProductCode());

        /* Invalid ProductCode -> null */
        pc.setProductCode(null);
        assertEquals(productCode, pc.getProductCode());

        /* Valid ProductCode */
        pc.setProductCode("2109876543210");
        assertEquals("2109876543210", pc.getProductCode());

    }
}