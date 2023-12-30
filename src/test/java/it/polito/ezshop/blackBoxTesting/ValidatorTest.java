package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.utils.Validator;
import org.junit.Assert;
import org.junit.Test;


public class ValidatorTest {

    @Test
    public void testCheckBarCode () {
        String barCode= "123456789012";
        Assert.assertTrue(Validator.checkBarCode(barCode));

        barCode= "1234567890127";
        Assert.assertFalse(Validator.checkBarCode(barCode));
    }

    @Test
    public void testCheckPositionFormat () {
        String pos = "2-tt-9";
        Assert.assertTrue(Validator.checkPositionFormat(pos));

        pos = "2-t-2-q";
        Assert.assertFalse(Validator.checkPositionFormat(pos));
    }

    @Test
    public void testCheckCreditCard () {
        String cardNo = "4716258050958645";
        Assert.assertTrue(Validator.checkCreditCard(cardNo));

        cardNo = "12345678912345";
        Assert.assertFalse(Validator.checkCreditCard(cardNo));
    }

    @Test
    public void testCheckValidRFID () {
        /* Invalid RFID format */
        // RFID < 0
        String rfid = "-12345678901";
        Assert.assertFalse(Validator.checkValidRFID(rfid));
        // RFID is NaN
        rfid = "abcdefghijkl";
        Assert.assertFalse(Validator.checkValidRFID(rfid));
        // RFID is empty
        Assert.assertFalse(Validator.checkValidRFID(""));
        // RFID is null
        Assert.assertFalse(Validator.checkValidRFID(null));

        /* Valid RFID format */
        rfid = "123456789012";
        Assert.assertTrue(Validator.checkValidRFID(rfid));

    }
}