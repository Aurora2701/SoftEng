package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.CustomerClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CustomerClassTest {

    @Test
    public void testSetCustomerName() {
        CustomerClass cc = new CustomerClass(1, 230, "Antonio", "0000000001");
        cc.setCustomerName("Piero");
        String t1 = cc.getCustomerName();
        cc.setCustomerName(null);
        String t2 = cc.getCustomerName();
        cc.setCustomerName("");
        String t3 = cc.getCustomerName();

        assertEquals("Piero", t1);
        assertEquals("Piero", t2);
        assertEquals("Piero", t3);
    }

    @Test
    public void testSetCustomerCard() {
        CustomerClass cc = new CustomerClass(1, 230, "Antonio", "0000000001");

        cc.setCustomerCard(null);
        String t1 = cc.getCustomerCard();
        cc.setCustomerCard("0000000002");
        String t2 = cc.getCustomerCard();
        cc.setCustomerCard("");
        String t3 = cc.getCustomerCard();
        cc.setCustomerCard("003");
        String t4 = cc.getCustomerCard();
        cc.setCustomerCard("aaaaaaa003");
        String t5 = cc.getCustomerCard();

        assertNull(t1);
        assertEquals("0000000002", t2);
        assertEquals("0000000002", t3);
        assertEquals("0000000002", t4);
        assertEquals("0000000002", t5);
    }

    @Test
    public void testSetId() {
        CustomerClass cc = new CustomerClass(1, 230, "Antonio", "0000000001");
        cc.setId(2);
        int t1 = cc.getId();
        cc.setId(-15);
        int t2 = cc.getId();

        assertEquals(2, t1);
        assertEquals(2, t2);
    }

    @Test
    public void testSetPoints() {
        CustomerClass cc = new CustomerClass(1, 230, "Antonio", "0000000001");
        cc.setPoints(235);
        int t = cc.getPoints();
        assertEquals(235, t);
    }

    @Test
    public void testPointsNotNegative() {
        CustomerClass cc = new CustomerClass(1, -20, "Antonio", "0000000001");
        int t = cc.getPoints();
        assertEquals(0, t);
    }

    @Test
    public void testCustomerClassConstructor_nameAndCustomerCard() {
        CustomerClass cc = new CustomerClass("Antonio", "0000000001");
        cc.setCustomerName("Piero");
        String t1 = cc.getCustomerName();
        cc.setCustomerName(null);
        String t2 = cc.getCustomerName();
        cc.setCustomerName("");
        String t3 = cc.getCustomerName();

        assertEquals("Piero", t1);
        assertEquals("Piero", t2);
        assertEquals("Piero", t3);

        cc.setCustomerCard(null);
        String t4 = cc.getCustomerCard();
        cc.setCustomerCard("0000000002");
        String t5 = cc.getCustomerCard();
        cc.setCustomerCard("");
        String t6 = cc.getCustomerCard();
        cc.setCustomerCard("003");
        String t7 = cc.getCustomerCard();
        cc.setCustomerCard("aaaaaaa003");
        String t8 = cc.getCustomerCard();

        assertNull(t4);
        assertEquals("0000000002", t5);
        assertEquals("0000000002", t6);
        assertEquals("0000000002", t7);
        assertEquals("0000000002", t8);

    }

    @Test
    public void testCustomerClassConstructor_nameAndID() {
        CustomerClass cc = new CustomerClass("Antonio", 1);
        cc.setCustomerName("Piero");
        String t1 = cc.getCustomerName();
        cc.setCustomerName(null);
        String t2 = cc.getCustomerName();
        cc.setCustomerName("");
        String t3 = cc.getCustomerName();

        assertEquals("Piero", t1);
        assertEquals("Piero", t2);
        assertEquals("Piero", t3);

        cc.setId(2);
        int t4 = cc.getId();
        cc.setId(-15);
        int t5 = cc.getId();

        assertEquals(2, t4);
        assertEquals(2, t5);

    }



}