package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.utils.DbManagerClass;
import org.junit.*;

import static it.polito.ezshop.utils.DbManagerClass.*;
import static junit.framework.TestCase.*;

public class LoyaltyCardTest {

    private static EZShop s;

    @AfterClass
    public static void disconnect() {
        DbManagerClass.disconnect();
    }

    @BeforeClass
    public static void createEZShop(){
        s = new EZShop();
    }

    @Before
    public void setupDBandLogin(){
        try {
            s.logout();
            s.createUser("testUser", "testPass", "Cashier");
            s.login("testUser", "testPass");
        } catch (InvalidPasswordException | InvalidRoleException | InvalidUsernameException throwables) {
            throwables.printStackTrace();
        }

    }

    @After
    public void restoreTables(){
        s.reset();
    }

    @Test
    public void createCard_databaseDisconnected() {

        try {
            DbManagerClass.disconnect();
            assertNull(s.createCard());
            EZShop.connectToDb();
        } catch (UnauthorizedException e) {
            fail();
        }

    }

    @Test
    public void createCard_addingCard() {

        try {
            String cardTest = s.createCard();
            assertEquals(cardTest, "0000000001");
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void createCard_UnauthorizedException() {
        try {
            s.logout();
            s.createCard();
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
    }

    @Test
    public void attachCardToCustomer_databaseDisconnected() {

        try {
            s.defineCustomer("test1");
            s.defineCustomer("test2");
            s.createCard();
            DbManagerClass.disconnect();
            assertFalse(s.attachCardToCustomer("0000000001",2));
            EZShop.connectToDb();
        } catch (UnauthorizedException | InvalidCustomerNameException | InvalidCustomerIdException | InvalidCustomerCardException e) {
            fail();
        }

    }

    @Test
    public void attachCardToCustomer_successfulAttach() {

        try {
            s.defineCustomer("test1");
            s.defineCustomer("test2");
            s.createCard();
            assertTrue(s.attachCardToCustomer("0000000001",2));
        } catch (InvalidCustomerIdException | InvalidCustomerCardException | UnauthorizedException | InvalidCustomerNameException e) {
            fail();
        }

    }

    @Test
    public void attachCardToCustomer_InvalidCustomerIdException() {

        // customerId is null

        try {
            s.attachCardToCustomer("0000000001", null);
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (InvalidCustomerCardException | UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerId is 0

        try {
            s.attachCardToCustomer("0000000001", 0);
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (InvalidCustomerCardException | UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerId is 0

        try {
            s.attachCardToCustomer("0000000001", -3);
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (InvalidCustomerCardException | UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void attachCardToCustomer_InvalidCustomerCardException() {

        // customerCard is null

        try {
            s.attachCardToCustomer(null, 1);
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (InvalidCustomerCardException | UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerCard is not numeric

        try {
            s.attachCardToCustomer("a000000000", 1);
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (InvalidCustomerCardException | UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerCard is not 10 characters long

        try {
            s.attachCardToCustomer("00", 1);
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (InvalidCustomerCardException | UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void attachCardToCustomer_cardtoAnotherUser() {

        try {
            s.defineCustomer("test1");
            s.defineCustomer("test2");
            s.createCard();
            s.attachCardToCustomer("0000000001",1);
            assertFalse(s.attachCardToCustomer("0000000001",2));
        } catch (InvalidCustomerIdException | InvalidCustomerCardException | UnauthorizedException | InvalidCustomerNameException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void attachCardToCustomer_noUserWithGivenID() {

        try {

            s.createCard();
            assertFalse(s.attachCardToCustomer("0000000001",1));

        } catch (InvalidCustomerIdException | InvalidCustomerCardException | UnauthorizedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void modifyPointsOnCard_databaseDisconnected() {

        try {
            s.createCard();
            DbManagerClass.disconnect();
            assertFalse(s.modifyPointsOnCard("0000000001", 300));
            EZShop.connectToDb();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidCustomerCardException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void modifyPointsOnCard_UnauthorisedException() {

        try {
            s.logout();
            s.modifyPointsOnCard(null, 300);
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidCustomerCardException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void modifyPointsOnCard_InvalidCustomerCardException() {

        // customerCard is null

        try {
            s.modifyPointsOnCard(null, 300);
            fail();
        } catch (InvalidCustomerCardException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerCard is empty

        try {
            s.modifyPointsOnCard("", 300);
            fail();
        } catch (InvalidCustomerCardException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerCard has an invalid format

        try {
            s.modifyPointsOnCard("a000000001", 300);
            fail();
        } catch (InvalidCustomerCardException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void modifyPointsOnCard_noCardWithGivenCode() {

        try {
            s.createCard();
            assertTrue(s.modifyPointsOnCard("0000000001", 300));
            assertFalse(s.modifyPointsOnCard("0000000003", 300));
        } catch (UnauthorizedException | InvalidCustomerCardException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void modifyPointsOnCard_verifyBalance() {

        try {
            s.createCard();
            s.createCard();
            assertTrue(s.modifyPointsOnCard("0000000001", 300));
            assertFalse(s.modifyPointsOnCard("0000000002", -300));
        } catch (UnauthorizedException | InvalidCustomerCardException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void attachCardToCustomer_cardAlreadyAssigned() {
        try {
            s.defineCustomer("test");
            s.createCard();
            s.attachCardToCustomer("0000000001", 1);
            s.defineCustomer("test2");
            assertFalse(s.attachCardToCustomer("0000000001", 2));

        } catch (UnauthorizedException | InvalidCustomerIdException | InvalidCustomerCardException | InvalidCustomerNameException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void attachCardToCustomer_UnauthorizedException() {
        try {
            s.logout();
            s.attachCardToCustomer("0000000001",1);
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidCustomerIdException | InvalidCustomerCardException e) {
            e.printStackTrace();
        }
    }


}