package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.CreditCardClass;
import junit.framework.TestCase;

public class CreditCardClassTest extends TestCase {

    public void testSetCardCredit () {
        CreditCardClass cc = new CreditCardClass("123456789012");

        cc.setCardCredit(999.99);
        assertEquals(999.99, cc.getCardCredit());

        cc.setCardCredit(-99.99);
        assertEquals(999.99, cc.getCardCredit());
    }
}