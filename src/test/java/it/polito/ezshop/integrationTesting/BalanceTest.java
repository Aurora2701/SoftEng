package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.BalanceOperation;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.InvalidPasswordException;
import it.polito.ezshop.exceptions.InvalidRoleException;
import it.polito.ezshop.exceptions.InvalidUsernameException;
import it.polito.ezshop.exceptions.UnauthorizedException;
import it.polito.ezshop.utils.DatabaseQuery;
import it.polito.ezshop.utils.DbManagerClass;
import org.junit.*;

import java.sql.SQLException;
import java.util.List;

import static it.polito.ezshop.utils.DbManagerClass.*;
import static org.junit.Assert.*;

public class BalanceTest {

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
    public void setUp () {
        try {
            s.createUser("testCashier", "testPass", s.ROLE_CASHIER);
            s.createUser("testManager", "testPass", s.ROLE_MANAGER);
            s.createUser("testAdministrator", "testPass", s.ROLE_ADMINISTRATOR);

        } catch (InvalidPasswordException | InvalidRoleException | InvalidUsernameException throwables) {
            throwables.printStackTrace();
        }
    }

    @After
    public void restoreTables () {
        s.reset();
        s.logout();
    }

    @Test
    public void testRecordBalanceUpdate () {
        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.recordBalanceUpdate(1));

        loginAsAdministrator();

        try {
            /*if the balance has been successfully updated*/
            assertTrue(s.recordBalanceUpdate(10));
            assertEquals(10, s.computeBalance(), 0.1);

            /*if toBeAdded + currentBalance < 0.*/
            assertFalse(s.recordBalanceUpdate(-110));
        } catch (UnauthorizedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetCreditsAndDebits () {
        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.getCreditsAndDebits(null, null));

        loginAsAdministrator();
        try {
            s.recordBalanceUpdate(10);
            s.recordBalanceUpdate(10);
            s.recordBalanceUpdate(10);

            List<BalanceOperation> operations;
            operations = DatabaseQuery.getAllBalanceOperationsBetweenDates(null, null);
            assertEquals(3, operations.size());
        } catch (UnauthorizedException | SQLException e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    public void testComputeBalance () {
        /*if there is no logged user or if it has not the rights to perform the operation*/
        assertThrows(UnauthorizedException.class , () -> s.computeBalance());

        loginAsAdministrator();

        try {
            assertEquals(0, s.computeBalance(), 0);

            s.recordBalanceUpdate(111);
            assertEquals(111, s.computeBalance(), 0);

        } catch (UnauthorizedException e) {
            e.printStackTrace();
            fail();
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
}