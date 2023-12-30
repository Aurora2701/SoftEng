package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.Customer;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.utils.DbManagerClass;
import org.junit.*;

import static junit.framework.TestCase.*;

public class CustomerClassTest {

    private static EZShop s;

    @BeforeClass
    public static void createEZShop(){
        s = new EZShop();
    }
    public static void firstSetup(){

        EZShop.connectToDb();

    }

    @AfterClass
    public static void disconnect() {
        DbManagerClass.disconnect();
    }

    @Before
    public void setupDBandLogin(){
        try {
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
    public void testDefineCustomer_databaseDisconnected() {

        Integer dbUnavailable = -1;

        try {
            DbManagerClass.disconnect();
            assertEquals(dbUnavailable,s.defineCustomer("test"));
            EZShop.connectToDb();
        } catch (InvalidCustomerNameException | UnauthorizedException e) {
            fail();
        }

    }

    @Test
    public void testDefineCustomer_customerNameAlreadyExists() {

        try {
            Integer duplicateFlag = -1;
            s.defineCustomer("test");
            assertEquals(s.defineCustomer("test"), duplicateFlag);
        } catch (InvalidCustomerNameException | UnauthorizedException e) {
            fail();
        }

    }

    @Test
    public void testDefineCustomer_successfulDefine() {

        try {
            Integer correctValue = 2;
            s.defineCustomer("test");
            assertEquals(s.defineCustomer("test2"), correctValue);
        } catch (InvalidCustomerNameException | UnauthorizedException e) {
            fail();
        }

    }

    @Test
    public void testDefineCustomer_InvalidCustomerIdException() {

        // customerID is empty

        try {
            s.defineCustomer("");
            fail();
        } catch (InvalidCustomerNameException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            fail();
        }

        // customerID is null

        try {
            s.defineCustomer(null);
            fail();
        } catch (InvalidCustomerNameException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            fail();
        }


    }

    @Test
    public void defineCustomer_UnauthorisedException() {
        try {
            s.logout();
            s.defineCustomer("test");
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidCustomerNameException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testModifyCustomer_databaseDisconnected() {

        try {
            s.defineCustomer("test");
            s.createCard();
            DbManagerClass.disconnect();
            assertFalse(s.modifyCustomer(1, "test2", "0000000001"));
            EZShop.connectToDb();
        } catch (InvalidCustomerNameException | UnauthorizedException | InvalidCustomerIdException | InvalidCustomerCardException e) {
            fail();
        }

    }

    @Test
    public void testModifyCustomer_InvalidCustomerCardException() {

        // customerCard is alphanumeric

        try {
            s.defineCustomer("test");
            s.modifyCustomer(1, "test2", "00000000A");
            fail();
        } catch (InvalidCustomerNameException | UnauthorizedException invalidCustomerNameException) {
            invalidCustomerNameException.printStackTrace();
        } catch (InvalidCustomerCardException e) {
            assertTrue(true);
        } catch (InvalidCustomerIdException e) {
            fail();
        }

        // customerCard is not 10 characters long

        try {
            s.modifyCustomer(1, "test2", "00");
            fail();
        } catch (InvalidCustomerNameException | UnauthorizedException invalidCustomerNameException) {
            invalidCustomerNameException.printStackTrace();
        } catch (InvalidCustomerCardException e) {
            assertTrue(true);
        } catch (InvalidCustomerIdException e) {
            fail();
        }

    }

    @Test
    public void testModifyCustomer_InvalidCustomerNameException() {

        // customerName is empty

        try {
            s.defineCustomer("test");
            s.modifyCustomer(1, "", "0000000001");
            fail();
        } catch (InvalidCustomerNameException e) {
            assertTrue(true);
        } catch (UnauthorizedException | InvalidCustomerIdException | InvalidCustomerCardException invalidCustomerNameException) {
            invalidCustomerNameException.printStackTrace();
        }

        // customerName is null

        try {
            s.defineCustomer("test2");
            s.modifyCustomer(1, null, "0000000001");
            fail();
        } catch (InvalidCustomerNameException e) {
            assertTrue(true);
        } catch (UnauthorizedException | InvalidCustomerIdException | InvalidCustomerCardException invalidCustomerNameException) {
            invalidCustomerNameException.printStackTrace();
        }

    }

    @Test
    public void testModifyCustomer_SuccessfulRenaming() {

        try {
            s.defineCustomer("test");
            s.createCard();
            s.attachCardToCustomer("0000000001", 1);
            boolean isSuccessful = s.modifyCustomer(1, "test2", "");
            assertTrue(isSuccessful);
            assertEquals(s.getCustomer(1).getCustomerName(), "test2");
        } catch (InvalidCustomerIdException | InvalidCustomerCardException | UnauthorizedException | InvalidCustomerNameException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testModifyCustomer_SuccessfulRenamingAndCardUpdate() {

        try {
            s.defineCustomer("test");
            s.createCard();
            s.attachCardToCustomer("0000000001", 1);
            s.createCard();
            boolean isSuccessful = s.modifyCustomer(1, "test2", "0000000002");
            assertTrue(isSuccessful);
            assertEquals(s.getCustomer(1).getCustomerName(), "test2");
            assertEquals(s.getCustomer(1).getCustomerCard(), "0000000002");
        } catch (InvalidCustomerIdException | InvalidCustomerCardException | UnauthorizedException | InvalidCustomerNameException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testModifyCustomer_SuccessfulCardRemoval() {

        try {
            s.defineCustomer("test");
            s.createCard();
            s.attachCardToCustomer("0000000001", 1);
            boolean isSuccessful = s.modifyCustomer(1, "test", "");
            assertTrue(isSuccessful);
            assertNull(s.getCustomer(1).getCustomerCard());
        } catch (InvalidCustomerIdException | InvalidCustomerCardException | UnauthorizedException | InvalidCustomerNameException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testModifyCustomer_CardUnchanged() {

        try {
            s.defineCustomer("test");
            s.createCard();
            s.attachCardToCustomer("0000000001", 1);
            boolean isSuccessful = s.modifyCustomer(1, "test", null);
            assertTrue(isSuccessful);
            assertEquals(s.getCustomer(1).getCustomerCard(), "0000000001");
        } catch (InvalidCustomerIdException | InvalidCustomerCardException | UnauthorizedException | InvalidCustomerNameException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void modifyCustomer_UnauthorisedException() {
        try {
            s.logout();
            s.modifyCustomer(1, "test", null);
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidCustomerNameException | InvalidCustomerIdException | InvalidCustomerCardException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testDeleteCustomer_databaseDisconnected() {

        try {
            s.defineCustomer("test");
            DbManagerClass.disconnect();
            assertFalse(s.deleteCustomer(1));
            EZShop.connectToDb();
        } catch (InvalidCustomerNameException | UnauthorizedException | InvalidCustomerIdException e) {
            fail();
        }

    }

    @Test
    public void testDeleteCustomer_verifyUserRemoval() {

        boolean isDeleted;
        boolean cannotDelete;

        try {
            s.defineCustomer("test1");
            s.defineCustomer("test2");
            s.defineCustomer("test3");
            isDeleted = s.deleteCustomer(2);
            assertTrue(isDeleted);
            cannotDelete = s.deleteCustomer(6);
            assertFalse(cannotDelete);
        } catch (InvalidCustomerIdException | InvalidCustomerNameException | UnauthorizedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteCustomer_InvalidCustomerIdException() {

        // customerID is null

        try {
            s.deleteCustomer(null);
            fail();
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerID is 0

        try {
            s.deleteCustomer(0);
            fail();
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerID is < 0

        try {
            s.deleteCustomer(-3);
            fail();
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void deleteCustomer_UnauthorisedException() {
        try {
            s.logout();
            s.deleteCustomer(1);
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidCustomerIdException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getCustomer_InvalidCustomerIdException() {

        // customerID is null

        try {
            s.getCustomer(null);
            fail();
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerID is 0

        try {
            s.getCustomer(0);
            fail();
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        // customerID is < 0

        try {
            s.getCustomer(-3);
            fail();
        } catch (InvalidCustomerIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getCustomer_userDoesntExist() {

        try {
            s.defineCustomer("test1");
            s.defineCustomer("test2");
            s.defineCustomer("test3");

            Customer userFlag = s.getCustomer(7);
            assertNull(userFlag);

            Customer userFlag2 = s.getCustomer(3);
            assertEquals(userFlag2.getId(), Integer.valueOf(3));

        } catch (InvalidCustomerIdException | InvalidCustomerNameException | UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getCustomer_UnauthorisedException() {
        try {
            s.logout();
            s.getCustomer(1);
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidCustomerIdException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getCustomer_isSuccessful() {
        try {
            Integer expectedID = 1;
            Integer expectedPoints = 0;
            s.defineCustomer("test");
            assertEquals(s.getCustomer(1).getCustomerName(), "test");
            assertEquals(s.getCustomer(1).getId(), expectedID);
            assertEquals(s.getCustomer(1).getPoints(), expectedPoints);
            assertEquals(s.getCustomer(1).getCustomerCard(), null);
        } catch (InvalidCustomerIdException | InvalidCustomerNameException | UnauthorizedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAllCustomers_checkReturn() {

        // isEmpty

        try {
            assertTrue(s.getAllCustomers().isEmpty());
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        // !isEmpty

        try {
            s.defineCustomer("test1");
            s.defineCustomer("test2");
            s.defineCustomer("test3");
            assertFalse(s.getAllCustomers().isEmpty());
        } catch (InvalidCustomerNameException | UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getAllCustomers_UnauthorisedException() {
        try {
            s.logout();
            s.getAllCustomers();
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
    }

}