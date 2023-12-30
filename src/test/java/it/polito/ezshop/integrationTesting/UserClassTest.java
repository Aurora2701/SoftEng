package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.utils.DbManagerClass;
import org.junit.*;

import static it.polito.ezshop.utils.DbManagerClass.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class UserClassTest {

    private static EZShop s;

    @AfterClass
    public static void disconnect() {
        log_out();
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

    @Test
    public void testLogin () {
        /* Test invalid username */
        // username is null
        Assert.assertThrows(InvalidUsernameException.class, () -> s.login(null, "m"));
        // username is empty
        Assert.assertThrows(InvalidUsernameException.class, () -> s.login("", "m"));
        // username not found
        try {
            assertNull(s.login("test", "test"));
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }

        /* Test invalid password */
        // password is null
        Assert.assertThrows(InvalidPasswordException.class, () -> s.login("testManager", null));

        //password is empty
        Assert.assertThrows(InvalidPasswordException.class, () -> s.login("testManager", ""));

        //password is wrong
        try {
            assertNull(s.login("testManager", "wrongPass"));
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }

        /* Test login successful */
        try {
            assertNotNull(s.login("testManager", "testPass"));
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }

        /* DB is unreachable */

        DbManagerClass.disconnect();
        try {
            assertNull(s.login("testManager", "testPass"));
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            fail();
        }
        EZShop.connectToDb();
    }

    @After
    public void restoreTables(){
        log_out();
        s.reset();
    }

    @Test
    public void testCreateUser () {

        loginAsAdministrator();

        /* Test invalid username */
        // username is empty
        Assert.assertThrows(InvalidUsernameException.class, () -> s.createUser("", "abc", "Cashier"));

        // username is null
        Assert.assertThrows(InvalidUsernameException.class, () ->   s.createUser(null, "abc", "Cashier"));

        /* Test invalid password */
        // password is empty
        Assert.assertThrows(InvalidPasswordException.class, () ->  s.createUser("newUser", "", "Cashier"));

        // password is null
        Assert.assertThrows(InvalidPasswordException.class, () ->    s.createUser("newUser", null, "Cashier"));

        /* Test invalid role */
        // role is null
        Assert.assertThrows(InvalidRoleException.class, () -> s.createUser("newUser", "abc", null));

        // role is empty
        Assert.assertThrows(InvalidRoleException.class, () -> s.createUser("newUser", "abc", ""));

        // role is not "Administrator" or "ShopManager" or "Cashier"
        Assert.assertThrows(InvalidRoleException.class, () -> s.createUser("newUser", "abc", "Order"));

        /* Test username not unique */
        try {
            assertEquals(-1, (int) s.createUser("testManager", "testPass", "ShopManager"));
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException e) {
            e.printStackTrace();
        }

        /* Test valid newUser */
        try {
            assertEquals(4, (int)s.createUser("testUser", "testPass", "Cashier"));
        } catch (InvalidUsernameException | InvalidPasswordException |InvalidRoleException e) {
            e.printStackTrace();
        }

        /* DB is unreachable */
        DbManagerClass.disconnect();
        try {
            assertEquals(0, (int) s.createUser("newUser", "newPassword", "ShopManager"));
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException e) {
            e.printStackTrace();
        }
        EZShop.connectToDb();

    }

    @Test
    public void testDeleteUser () {
        /*Test invalid roles*/
        assertThrows(UnauthorizedException.class , () -> s.deleteUser(1));

        loginAsAdministrator();

        /* Test invalid user id */
        // id is null
        Assert.assertThrows(InvalidUserIdException.class, () -> s.deleteUser(null));
        // id is 0
        Assert.assertThrows(InvalidUserIdException.class, () -> s.deleteUser(0));
        // id is < 0
        Assert.assertThrows(InvalidUserIdException.class, () -> s.deleteUser(-55));
        // id not found
        try {
            assertFalse(s.deleteUser(55555555));
        } catch (InvalidUserIdException | UnauthorizedException e) {
            e.printStackTrace();
        }

        /* Test valid id */
        try {
            assertTrue(s.deleteUser(1));
        } catch (InvalidUserIdException | UnauthorizedException e) {
            e.printStackTrace();
        }

        /* Test DB connection */
        DbManagerClass.disconnect();
        try {
            assertFalse(s.deleteUser(55555555));
        } catch (InvalidUserIdException | UnauthorizedException e) {
            fail();
        }
        EZShop.connectToDb();

    }

    @Test
    public void testGetAllUsers () {
        /*Test invalid roles*/
        // no logged user
        assertThrows(UnauthorizedException.class , () -> s.getAllUsers());

        try {
            s.login("testCashier", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }
        // not Administrator
        assertThrows(UnauthorizedException.class , () -> s.getAllUsers());

        s.logout();
        loginAsAdministrator();

        /*  */
        try {
            assertNotNull(s.getAllUsers());
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        try {
            s.deleteUser(1);
            s.deleteUser(2);
            s.deleteUser(3);
            assertEquals(0, s.getAllUsers().size());
        } catch (InvalidUserIdException | UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGetUser () {
        /*Test invalid roles*/
        // no logged user
        assertThrows(UnauthorizedException.class , () -> s.getUser(1));

        try {
            s.login("testCashier", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }
        // not Administrator
        assertThrows(UnauthorizedException.class , () -> s.getUser(1));

        loginAsAdministrator();

        /* Test invalid user id */
        // id is null
        Assert.assertThrows(InvalidUserIdException.class, () -> s.getUser(null));
        // id is 0
        Assert.assertThrows(InvalidUserIdException.class, () -> s.getUser(0));
        // id is < 0
        Assert.assertThrows(InvalidUserIdException.class, () -> s.getUser(-55));
        // id not found
        try {
            assertNull(s.getUser(55555555));
        } catch (InvalidUserIdException | UnauthorizedException e) {
            e.printStackTrace();
        }

        /* Test valid id */
        try {
            assertNotNull(s.getUser(1));
        } catch (InvalidUserIdException | UnauthorizedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateUserRights () {
        /*Test invalid roles*/
        // no logged user
        assertThrows(UnauthorizedException.class , () -> s.updateUserRights(1, "ShopManager"));

        try {
            s.login("testCashier", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }
        // not Administrator
        assertThrows(UnauthorizedException.class , () -> s.updateUserRights(1, "ShopManager"));

        loginAsAdministrator();

        /* Test invalid user id */
        // id is null
        Assert.assertThrows(InvalidUserIdException.class, () -> s.updateUserRights(null, "ShopManager"));
        // id is 0
        Assert.assertThrows(InvalidUserIdException.class, () -> s.updateUserRights(0, "ShopManager"));
        // id is < 0
        Assert.assertThrows(InvalidUserIdException.class, () -> s.updateUserRights(-55, "ShopManager"));
        // id not found
        try {
            assertFalse(s.updateUserRights(55555555, "ShopManager"));
        } catch (InvalidUserIdException | UnauthorizedException | InvalidRoleException e) {
            e.printStackTrace();
        }

        // newRole is not "Administrator" or "ShopManager" or "Cashier"
        Assert.assertThrows(InvalidRoleException.class, () -> s.updateUserRights(1, "abc"));

        /* Test valid id and role */
        try {
            assertTrue(s.updateUserRights(1, "ShopManager"));
        } catch (InvalidUserIdException | UnauthorizedException | InvalidRoleException e) {
            e.printStackTrace();
        }

        /* Test DB connection */

        try {
            s.createUser("Antonio", "55555555", "Cashier");
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException e) {
            fail();
        }
        DbManagerClass.disconnect();
        try {
            assertFalse(s.updateUserRights(1, "ShopManager"));
        } catch (InvalidUserIdException | UnauthorizedException | InvalidRoleException e) {
            fail();
        }
        EZShop.connectToDb();

    }

    @Test
    public void testLogout () {
        /* No logged user */
        assertFalse(s.logout());

        loginAsAdministrator();

        /* Test successful logout */
        assertTrue(s.logout());
    }


    private void loginAsAdministrator () {
        try {
            s.login("testAdministrator", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }
    }

    private static void log_out (){
        s.logout();
    }
}