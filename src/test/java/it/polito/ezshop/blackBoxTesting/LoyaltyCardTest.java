package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.LoyaltyCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoyaltyCardTest {

    /* @Test
    public void testLoyaltyCardConstructor() {
        LoyaltyCard lc = new LoyaltyCard("00000001", 100, 1);

        lc.setID("00000003");
        lc.setPoints(300);
        lc.setCardOwnerID(3);

        String t1 = lc.getID();
        int t2 = lc.getPoints();
        int t3 = lc.getCardOwnerID();

        // Check defaulting negative 'points' value to 0

        lc.setPoints(-100);
        int t4 = lc.getPoints();

        assertEquals("00000003", t1);
        assertEquals(300, t2);
        assertEquals(3, t3);
        assertEquals(0, t4);
    } */

    @Test
    public void testAddPoints() {
        LoyaltyCard lc = new LoyaltyCard("0000000001", 100, 1);
        lc.addPoints(100);
        int t = lc.getPoints();
        assertEquals(200, t);
    }

    @Test
    public void testSetID() {

        LoyaltyCard lc = new LoyaltyCard("0000000001", 100, 1);
        lc.setID("0000000003");
        String t1 = lc.getID();
        lc.setID(null);
        String t2 = lc.getID();
        lc.setID("");
        String t3 = lc.getID();
        lc.setID("003");
        String t4 = lc.getID();
        lc.setID("aaaaaaa003");
        String t5 = lc.getID();

        assertEquals("0000000003", t1);
        assertEquals("0000000003", t2);
        assertEquals("0000000003", t3);
        assertEquals("0000000003", t4);
        assertEquals("0000000003", t5);

    }

    @Test
    public void testSetPoints() {
        LoyaltyCard lc = new LoyaltyCard("0000000001", 100, 1);
        lc.setPoints(300);
        int t = lc.getPoints();
        assertEquals(300, t);
    }

    @Test
    public void testSetCardOwnerID() {
        LoyaltyCard lc = new LoyaltyCard("0000000001", 100, 1);
        lc.setCardOwnerID(3);
        int t1 = lc.getCardOwnerID();

        assertEquals(3, t1);
    }

    @Test
    public void testPointsNotNegative() {
        LoyaltyCard lc = new LoyaltyCard("0000000001", -100, 1);
        int t = lc.getPoints();
        assertEquals(0, t);
    }

}