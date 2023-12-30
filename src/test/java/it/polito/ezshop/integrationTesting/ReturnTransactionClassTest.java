package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.data.Product;
import it.polito.ezshop.data.ProductTypeClass;
import it.polito.ezshop.data.SaleTransactionClass;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.utils.DatabaseQuery;
import it.polito.ezshop.utils.DbManagerClass;
import org.junit.*;

import javax.print.DocFlavor;
import java.sql.SQLException;

import static it.polito.ezshop.utils.DbManagerClass.*;
import static org.junit.Assert.*;

public class ReturnTransactionClassTest {

    private final String VALID_PRODUCT_CODE = "1111111111116";
    private final String VALID_RFID = "111111111111";
    private final String CARD_150 = "4485370086510891";

    private static EZShop s;

    @BeforeClass
    public static void createEZShop(){
        s = new EZShop();
    }

    @AfterClass
    public static void disconnect() {
        DbManagerClass.disconnect();
    }


    @Before
    public void setupDBandLogin(){
        SaleTransactionClass.setSaleCount(0);
        createSellTransaction();
        s.refresh();

        try {
            s.createUser("testCashier", "testPass", EZShop.ROLE_CASHIER);
            s.createUser("testManager", "testPass", EZShop.ROLE_MANAGER);
            s.createUser("testAdministrator", "testPass", EZShop.ROLE_ADMINISTRATOR);

        } catch (InvalidPasswordException | InvalidRoleException | InvalidUsernameException throwables) {
            throwables.printStackTrace();
        }

    }


    public void createSellTransaction(){
        SaleTransactionClass sale = new SaleTransactionClass();
        ProductTypeClass product = new ProductTypeClass("Mela", VALID_PRODUCT_CODE, 69, "Cibo per umani e non solo");
        Product product1 = new Product(VALID_RFID, VALID_PRODUCT_CODE, Product.STAT_INV);
        try {
            DatabaseQuery.createSaleTransaction(sale);
            DatabaseQuery.createProductType(product);
            DatabaseQuery.updateQuantity(product.getId(),100);
            DatabaseQuery.createRFIDBarcodeLink(product1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @After
    public void restoreTables(){
        s.reset();
        s.logout();
    }

    @Test
    public void testStartReturnTransaction(){

        /*Test invalid roles*/
        assertThrows(UnauthorizedException.class , () -> s.startReturnTransaction(1));

        loginAsAdministrator();


        assertThrows(InvalidTransactionIdException.class , () -> s.startReturnTransaction(null));
        assertThrows(InvalidTransactionIdException.class , () -> s.startReturnTransaction(-666));

        try {
            assertEquals(-1, (int) s.startReturnTransaction(69));
            assertEquals(2, (int) s.startReturnTransaction(1));
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testReturnProduct(){
        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.returnProduct(1,VALID_PRODUCT_CODE, 1));

        loginAsAdministrator();

        startReturnTransactionWrapper();

        int correctReturnId = 2;

        /*if the quantity is less than or equal to 0*/
        assertThrows(InvalidQuantityException.class, ()-> s.returnProduct(correctReturnId, VALID_PRODUCT_CODE, -100));
        assertThrows(InvalidQuantityException.class, ()-> s.returnProduct(correctReturnId, VALID_PRODUCT_CODE, 0));

        /*if the return id is less ther or equal to 0 or if it is null*/
        assertThrows(InvalidTransactionIdException.class, ()-> s.returnProduct(null, VALID_PRODUCT_CODE, 1));
        assertThrows(InvalidTransactionIdException.class, ()-> s.returnProduct(0, VALID_PRODUCT_CODE, 1));
        assertThrows(InvalidTransactionIdException.class, ()-> s.returnProduct(-666, VALID_PRODUCT_CODE, 1));

        /*if the product code is empty, null or invalid*/
        assertThrows(InvalidProductCodeException.class, ()-> s.returnProduct(correctReturnId, null, 1));
        assertThrows(InvalidProductCodeException.class, ()-> s.returnProduct(correctReturnId, "111111116", 1));
        assertThrows(InvalidProductCodeException.class, ()-> s.returnProduct(correctReturnId, "1111111111115", 1));
        assertThrows(InvalidProductCodeException.class, ()-> s.returnProduct(correctReturnId, "", 1));

        try {
            /*if the transaction does not exist*/
            assertFalse(s.returnProduct(4,VALID_PRODUCT_CODE, 1));

            /*if the the product to be returned does not exists*/
            assertFalse(s.returnProduct(correctReturnId,"2222222222222", 1));

            /*if it was not in the transaction*/
            assertFalse(s.returnProduct(correctReturnId,VALID_PRODUCT_CODE, 1));

            s.addProductToSale(1, VALID_PRODUCT_CODE, 3);

            /*if the amount is higher than the one in the sale transaction*/
            assertFalse(s.returnProduct(correctReturnId,VALID_PRODUCT_CODE, 4));

            /*if the operation is successful*/
            assertTrue(s.returnProduct(correctReturnId,VALID_PRODUCT_CODE, 2));

        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testReturnProductRFID(){
        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.returnProductRFID(1,VALID_RFID));

        loginAsAdministrator();

        startReturnTransactionWrapper();

        int correctReturnId = 2;

        /*if the return id is less ther or equal to 0 or if it is null*/
        assertThrows(InvalidTransactionIdException.class, ()-> s.returnProductRFID(null, VALID_RFID));
        assertThrows(InvalidTransactionIdException.class, ()-> s.returnProductRFID(0, VALID_RFID));
        assertThrows(InvalidTransactionIdException.class, ()-> s.returnProductRFID(-666, VALID_RFID));

        /*if the product code is empty, null or invalid*/
        assertThrows(InvalidRFIDException.class, ()-> s.returnProductRFID(correctReturnId, null));
        assertThrows(InvalidRFIDException.class, ()-> s.returnProductRFID(correctReturnId, "111111116"));
        assertThrows(InvalidRFIDException.class, ()-> s.returnProductRFID(correctReturnId, "1111111111115"));
        assertThrows(InvalidRFIDException.class, ()-> s.returnProductRFID(correctReturnId, ""));

        try {
            /*if the transaction does not exist*/
            assertFalse(s.returnProductRFID(4,VALID_RFID));

            /*if the the product to be returned does not exists*/
            assertFalse(s.returnProductRFID(correctReturnId,"121212121212"));

            /*if it was not in the transaction*/
            assertFalse(s.returnProductRFID(correctReturnId,VALID_RFID));

            s.addProductToSaleRFID(1, VALID_RFID);

            /*if the operation is successful*/
            assertTrue(s.returnProductRFID(correctReturnId,VALID_RFID));

        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException | InvalidRFIDException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEndReturnTransaction(){
        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.endReturnTransaction(2,false));

        loginAsAdministrator();

        /*if returnId is less than or equal to 0 or if it is null*/
        assertThrows(InvalidTransactionIdException.class , () -> s.endReturnTransaction(null,false));
        assertThrows(InvalidTransactionIdException.class , () -> s.endReturnTransaction(0,false));
        assertThrows(InvalidTransactionIdException.class , () -> s.endReturnTransaction(-123,false));

        int returnId = startReturnTransactionWrapper();

        try {
            /*if the returnId does not correspond to an active return transaction*/
            assertFalse(s.endReturnTransaction(returnId+1, false));
            assertFalse(s.endReturnTransaction(returnId+1, true));

            /*if the operation is successful*/
            assertTrue(s.endReturnTransaction(returnId, false));

            /*if the returnId does not correspond to an active return transaction*/
            assertFalse(s.endReturnTransaction(returnId, false));

            returnId = startReturnTransactionWrapper();

            /*if the operation is successful*/
            assertTrue(s.endReturnTransaction(returnId, true));

            returnId = startReturnTransactionWrapper();

            returnProductWrapper(returnId, VALID_PRODUCT_CODE, 3);

//            disconnect(); TODO
//            /*if there is a problem with the db*/
//            assertFalse(s.endReturnTransaction(returnId, true));
//
//            EZShop.connectToDb();

            /*if the operation is successful*/
            assertTrue(s.endReturnTransaction(returnId, true));


        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            e.printStackTrace();
            fail();
        }


    }

    @Test
    public void testDeleteReturnTransaction(){

        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.deleteReturnTransaction(1));

        loginAsAdministrator();
        int returnId = startReturnTransactionWrapper();

        /*if the transaction id is less than or equal to 0 or if it is null*/
        assertThrows(InvalidTransactionIdException.class , () -> s.deleteReturnTransaction(-111));
        assertThrows(InvalidTransactionIdException.class , () -> s.deleteReturnTransaction(null));
        assertThrows(InvalidTransactionIdException.class , () -> s.deleteReturnTransaction(0));

        try {
            /*if it doesn't exist*/
            assertFalse(s.deleteReturnTransaction(3));
            assertFalse(s.deleteReturnTransaction(Integer.MAX_VALUE));

            /*if it has been payed*/
//            TODO:

            disconnect();
            /*if there is a problem with the db*/
            assertFalse(s.deleteReturnTransaction(returnId));

            EZShop.connectToDb();

            assertTrue(s.deleteReturnTransaction(returnId));

        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testReturnCashPayment(){
        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.deleteReturnTransaction(1));

        loginAsAdministrator();
        /*if the return id is less than or equal to 0*/
        assertThrows(InvalidTransactionIdException.class , () -> s.deleteReturnTransaction(-111));
        assertThrows(InvalidTransactionIdException.class , () -> s.deleteReturnTransaction(0));

        try {
            /*if it does not exist*/
            assertEquals(-1,(int) s.returnCashPayment(1));

            int returnId = startReturnTransactionWrapper();

            /*if the return transaction is not ended*/
            assertEquals(-1,(int) s.returnCashPayment(returnId));


            s.setCurrentBalance(5000);
            s.addProductToSale(1, VALID_PRODUCT_CODE, 10);
            s.endSaleTransaction(1);
            s.receiveCashPayment(1, 5000);

            returnProductWrapper(returnId, VALID_PRODUCT_CODE, 5);
            s.endReturnTransaction(returnId, true);

            disconnect();
            /*if there is a problem with the db*/
            assertEquals(-1, s.returnCashPayment(returnId), 0.1);

            EZShop.connectToDb();

            /*the money returned to the customer*/
            assertEquals(345,(double) s.returnCashPayment(returnId), 0.5);
            assertEquals(5345, s.computeBalance(), 0.1);

        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            e.printStackTrace();
            fail();
        } catch (InvalidQuantityException | InvalidProductCodeException | InvalidPaymentException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testReturnCreditCardPayment(){
        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.returnCreditCardPayment(1,  CARD_150));

        loginAsAdministrator();
        /*if the return id is less than or equal to 0*/
        assertThrows(InvalidTransactionIdException.class , () -> s.returnCreditCardPayment(-111, CARD_150));
        assertThrows(InvalidTransactionIdException.class , () -> s.returnCreditCardPayment(0, CARD_150));


        /*if the credit card number is empty, null or if luhn algorithm does not
         *                                      validate the credit card*/
        int returnId = startReturnTransactionWrapper();

        assertThrows(InvalidCreditCardException.class , () -> s.returnCreditCardPayment(returnId, ""));
        assertThrows(InvalidCreditCardException.class , () -> s.returnCreditCardPayment(returnId, null));
        assertThrows(InvalidCreditCardException.class , () -> s.returnCreditCardPayment(returnId, "TUTTURU"));
        assertThrows(InvalidCreditCardException.class , () -> s.returnCreditCardPayment(returnId, "4445370086510891"));

        try {
            /*if the return transaction is not ended*/
            assertEquals(-1,s.returnCreditCardPayment(returnId, CARD_150), 0.1);
            /*if it does not exist*/
            assertEquals(-1,s.returnCreditCardPayment(returnId+1, CARD_150), 0.1);
            /*if the card is not registered*/
            assertEquals(-1,s.returnCreditCardPayment(returnId, "6468394942"), 0.1);

            s.setCurrentBalance(5000);
            s.addProductToSale(1, VALID_PRODUCT_CODE, 10);
            s.endSaleTransaction(1);
            s.receiveCashPayment(1, 5000);

            returnProductWrapper(returnId, VALID_PRODUCT_CODE, 2);
            s.endReturnTransaction(returnId, true);

            disconnect();
            /*if there is a problem with the db*/
            assertEquals(-1, s.returnCreditCardPayment(returnId, CARD_150), 0.1);

            EZShop.connectToDb();

            /*the money returned to the customer*/
            assertEquals(138,(double) s.returnCreditCardPayment(returnId, CARD_150), 0.5);
            assertEquals(5552, s.computeBalance(), 0.1);


        } catch (InvalidTransactionIdException | InvalidCreditCardException | UnauthorizedException e) {
            e.printStackTrace();
            fail();
        } catch (InvalidQuantityException | InvalidPaymentException | InvalidProductCodeException e) {
            e.printStackTrace();
        }
    }

    private void loginAsAdministrator(){
        try {
            s.login("testAdministrator", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
            fail();
        }
    }

    private int startReturnTransactionWrapper(){
        try {
            return s.startReturnTransaction(1);
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            e.printStackTrace();
            fail();
        }
        return 0;
    }

    private boolean returnProductWrapper(Integer returnId,String productCode,int amount){
        try {
            return s.returnProduct(returnId, productCode, amount);
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidQuantityException | InvalidProductCodeException e) {
            e.printStackTrace();
            fail();
        }
        return false;
    }
}
