package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.ProductTypeClass;
import junit.framework.TestCase;

public class ProductTypeClassTest extends TestCase {

    public void testSetId () {
        ProductTypeClass pc = new ProductTypeClass("jazz", "123456789012", 33.3,"new");

        pc.setId(1);
        int id = pc.getId();
        assertEquals(1, id);

        pc.setId(-10);
        id = pc.getId();
        assertEquals(1, id);
    }

    public void testSetProductDescription () {
        ProductTypeClass pc = new ProductTypeClass("jazz", "123456789012", 33.3,"new");

        pc.setProductDescription("blues");
        assertEquals("blues", pc.getProductDescription());

        pc.setProductDescription("");
        assertEquals("blues", pc.getProductDescription());

        pc.setProductDescription(null);
        assertEquals("blues", pc.getProductDescription());
    }

    public void testSetBarCode () {
        ProductTypeClass pc = new ProductTypeClass("jazz", "123456789012", 33.3,"new");

        pc.setBarCode("2109876543210");
        assertEquals("2109876543210", pc.getBarCode());

        pc.setBarCode("");
        assertEquals("2109876543210", pc.getBarCode());

        pc.setBarCode(null);
        assertEquals("2109876543210", pc.getBarCode());
    }

    public void testSetPricePerUnit () {
        ProductTypeClass pc = new ProductTypeClass("jazz", "123456789012", 33.3,"new");

        pc.setPricePerUnit(2.33);
        assertEquals(2.33, pc.getPricePerUnit());

        pc.setPricePerUnit(-66.2);
        assertEquals(2.33, pc.getPricePerUnit());

        pc.setPricePerUnit(null);
        assertEquals(2.33, pc.getPricePerUnit());
    }

    public void testSetNote () {
        ProductTypeClass pc = new ProductTypeClass("jazz", "123456789012", 33.3,"new");
        pc.setNote("old");
        assertEquals("old", pc.getNote());
    }

    public void testSetQuantity () {
        ProductTypeClass pc = new ProductTypeClass("jazz", "123456789012", 33.3,"new");

        pc.setQuantity(77);
        assertEquals(77, (int)pc.getQuantity());

        pc.setQuantity(-99);
        assertEquals(77, (int)pc.getQuantity());

        pc.setQuantity(null);
        assertNull(pc.getQuantity());
    }

    public void testSetLocation () {
        ProductTypeClass pc = new ProductTypeClass("jazz", "123456789012", 33.3,"new");

        pc.setLocation("12-aa-21");
        assertEquals("12-aa-21", pc.getLocation());
    }
}