package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.utils.DbManagerClass;
import org.junit.*;
import static org.junit.Assert.*;

public class ProductTypeTest {

    private static EZShop s;

    @AfterClass
    public static void disconnect() {
        DbManagerClass.disconnect();
        log_out();
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

        loginAsAdministrator();
        try {
            s.createProductType("Beer", "123456789012", 7.0, "keep cool");
            s.createProductType("Soda", "3214569870128", 5.5, "keep cool");
            s.updatePosition(1,"1-a-1");
        } catch (InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException | InvalidLocationException | InvalidProductIdException e) {
            e.printStackTrace();
        }
        s.setLoggedUser(null);
    }

    @After
    public void restoreTables(){
        log_out();
        s.reset();
    }

    @Test
    public void testCreateProductType () {
        /*Test invalid roles*/
        assertThrows(UnauthorizedException.class, () -> s.createProductType("Gin", "123456789012", 14.0, "keep cool"));

        loginAsAdministrator();

        /* Test invalid description */
        // description is empty
        Assert.assertThrows(InvalidProductDescriptionException.class, () -> s.createProductType("", "123456789012", 7.0, "keep cool"));

        // description is null
        Assert.assertThrows(InvalidProductDescriptionException.class, () -> s.createProductType(null, "123456789012", 7.0, "keep cool"));

        /* Test invalid price per unit */
        // price per unit is 0
        Assert.assertThrows(InvalidPricePerUnitException.class, () -> s.createProductType("Beer", "123456789012", 0.0, "keep cool"));
        // price per unit is < 0
        Assert.assertThrows(InvalidPricePerUnitException.class, () -> s.createProductType("Beer", "123456789012", -777.222, "keep cool"));

        /* Test invalid bar code */
        // bar code is null
        Assert.assertThrows(InvalidProductCodeException.class, () -> s.createProductType("Beer", null, 7.0, "keep cool"));
        //bar code is not in GTIN-12/13/14 format
        Assert.assertThrows(InvalidProductCodeException.class, () -> s.createProductType("Beer", "555", 7.0, "keep cool"));

        /* Test bar code not unique */
        try {
            assertEquals(-1, (int) s.createProductType("Gin", "123456789012", 14.0, "keep cool"));
        } catch (UnauthorizedException | InvalidProductDescriptionException | InvalidPricePerUnitException | InvalidProductCodeException e) {
            e.printStackTrace();
        }

        /* Test valid newProductType -> update location as in Scenario 1-1 */
        try {
            assertEquals(3, (int)s.createProductType("Gin", "1234567890128", 14.0, "keep cool"));
            assertTrue(s.updatePosition(3,"3-c-3"));
        } catch (UnauthorizedException | InvalidProductDescriptionException | InvalidPricePerUnitException | InvalidProductCodeException | InvalidLocationException | InvalidProductIdException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateProduct () {
        /*Test invalid roles*/
        assertThrows(UnauthorizedException.class, () -> s.updateProduct(1,"Gin", "123456789012", 14.0, "keep cool"));

        loginAsAdministrator();

        /* Test invalid product id */
        // id is null
        Assert.assertThrows(InvalidProductIdException.class, () -> s.updateProduct(null, "Soda", "3214569870128", 2.5, "keep cool"));
        // id is 0
        Assert.assertThrows(InvalidProductIdException.class, () -> s.updateProduct(0, "Soda", "3214569870128", 2.5, "keep cool"));
        // id is < 0
        Assert.assertThrows(InvalidProductIdException.class, () -> s.updateProduct(-5551, "Soda", "3214569870128", 2.5, "keep cool"));
        // id not found
        try {
            assertFalse(s.updateProduct(55555555, "Soda", "3214569870128", 2.5, "keep cool"));
        } catch (InvalidProductIdException | UnauthorizedException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException e) {
            e.printStackTrace();
        }

        /* Test invalid description */
        // description is empty
        Assert.assertThrows(InvalidProductDescriptionException.class, () -> s.updateProduct(1, "", "3214569870128", 2.5, "keep cool"));
        // description is null
        Assert.assertThrows(InvalidProductDescriptionException.class, () -> s.updateProduct(1, null, "3214569870128", 2.5, "keep cool"));

        /* Test invalid price per unit */
        // price per unit is 0
        Assert.assertThrows(InvalidPricePerUnitException.class, () -> s.updateProduct(1, "Soda", "3214569870128", 0, "keep cool"));
        // price per unit is < 0
        Assert.assertThrows(InvalidPricePerUnitException.class, () -> s.updateProduct(1, "Soda", "3214569870128", -77.23, "keep cool"));

        /* Test invalid bar code */
        // bar code is null
        Assert.assertThrows(InvalidProductCodeException.class, () -> s.updateProduct(1, "Soda", null, 2.5, "keep cool"));
        //bar code is not in GTIN-12/13/14 format
        Assert.assertThrows(InvalidProductCodeException.class, () -> s.updateProduct(1, "Soda", "321128", 2.5, "keep cool"));

        /* Test bar code not unique */
        try {
            assertFalse(s.updateProduct(2, "Soda", "123456789012", 2.5, "keep cool"));
        } catch (UnauthorizedException | InvalidProductDescriptionException | InvalidPricePerUnitException | InvalidProductCodeException | InvalidProductIdException e) {
            e.printStackTrace();
        }

        /* Test valid update -> search product via bar code and modify product type price per unit as in scenario 1.3  */
        try {
            Integer id = s.getProductTypeByBarCode("3214569870128").getId();
            assertTrue(s.updateProduct(id, "Soda", "3214569870128", 2.5, "keep cool"));
        } catch (UnauthorizedException | InvalidProductDescriptionException | InvalidPricePerUnitException | InvalidProductCodeException | InvalidProductIdException e) {
            e.printStackTrace();
        }

        // DB is unreachable

        Integer id = null;
        try {
            id = s.getProductTypeByBarCode("3214569870128").getId();
        } catch (InvalidProductCodeException | UnauthorizedException e) {
            fail();
        }
        DbManagerClass.disconnect();
        try {
            assertFalse(s.updateProduct(id, "Soda", "3214569870128", 2.5, "keep cool"));
        } catch (InvalidProductIdException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }
        EZShop.connectToDb();

    }

    @Test
    public void testDeleteProductType () {
        /*Test invalid roles*/
        assertThrows(UnauthorizedException.class, () -> s.deleteProductType(1));

        loginAsAdministrator();

        /* Test invalid product id */
        // id is null
        Assert.assertThrows(InvalidProductIdException.class, () -> s.deleteProductType(null));
        // id is 0
        Assert.assertThrows(InvalidProductIdException.class, () -> s.deleteProductType(0));
        // id is < 0
        Assert.assertThrows(InvalidProductIdException.class, () -> s.deleteProductType(-441));
        // id not found
        try {
            assertFalse(s.deleteProductType(5555));
        } catch (InvalidProductIdException | UnauthorizedException e) {
            e.printStackTrace();
        }

        /* DB is unreachable */

        Integer id = null;
        try {
            id = s.getProductTypeByBarCode("3214569870128").getId();
        } catch (InvalidProductCodeException | UnauthorizedException e) {
            fail();
        }
        /* Test successful delete -> search product via bar code as in scenario 1.4  */
        try {
            id = s.getProductTypeByBarCode("3214569870128").getId();
            assertTrue(s.deleteProductType(id));
        } catch (InvalidProductIdException | UnauthorizedException | InvalidProductCodeException e) {
            e.printStackTrace();
        }

        DbManagerClass.disconnect();
        try {
            assertFalse(s.deleteProductType(id));
        } catch (InvalidProductIdException | UnauthorizedException e) {
            fail();
        }
        EZShop.connectToDb();
    }

    @Test
    public void testGetAllProductTypes () {
        /*Test invalid roles*/
        // no logged user
        assertThrows(UnauthorizedException.class , () -> s.getAllProductTypes());

        loginAsAdministrator();

        /* ProductType list */
        try {
            assertNotNull(s.getAllProductTypes());
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        try {
            s.deleteProductType(1);
            s.deleteProductType(2);
            assertEquals(0, s.getAllProductTypes().size());
        } catch (UnauthorizedException | InvalidProductIdException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetProductTypeByBarCode () {
        /*Test invalid roles*/
        // no logged user
        assertThrows(UnauthorizedException.class , () -> s.getProductTypeByBarCode("123456789012"));

        try {
            s.login("testCashier", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }
        // not Administrator
        assertThrows(UnauthorizedException.class , () -> s.getProductTypeByBarCode("123456789012"));

        loginAsAdministrator();

        /* Test invalid bar code */
        // bar code is null
        Assert.assertThrows(InvalidProductCodeException.class, () -> s.getProductTypeByBarCode(null));
        //bar code is not in GTIN-12/13/14 format
        Assert.assertThrows(InvalidProductCodeException.class, () -> s.getProductTypeByBarCode("12345"));

        /* valid bar code */
        // barcode exists
        try {
            assertNotNull(s.getProductTypeByBarCode("123456789012"));
        } catch (InvalidProductCodeException | UnauthorizedException e) {
            e.printStackTrace();
        }
        // barcode doesn't exist
        try {
            assertNull(s.getProductTypeByBarCode("4638435435485"));
        } catch (InvalidProductCodeException | UnauthorizedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetProductTypesByDescription () {
        /*Test invalid roles*/
        // no logged user
        assertThrows(UnauthorizedException.class , () -> s.getProductTypesByDescription("Beer"));

        try {
            s.login("testCashier", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }
        // not Administrator
        assertThrows(UnauthorizedException.class , () -> s.getProductTypesByDescription("Beer"));

        loginAsAdministrator();

        /* Test valid description */
        // existent description
        try {
            assertNotNull(s.getProductTypesByDescription("Beer"));
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }
        // description is null -> empty description
        try {
            assertNotNull(s.getProductTypesByDescription(null));
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }
        // product description doesn't exist
        try {
            assertEquals(0, s.getProductTypesByDescription("Vodka").size());
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testUpdateQuantity () {
        /*Test invalid roles*/
        // no logged user
        assertThrows(UnauthorizedException.class , () -> s.updateQuantity(1, 100));

        try {
            s.login("testCashier", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }
        // not Administrator
        assertThrows(UnauthorizedException.class , () -> s.updateQuantity(1, 100));

        loginAsAdministrator();

        /* Test invalid product id */
        // id is null
        Assert.assertThrows(InvalidProductIdException.class, () ->s.updateQuantity(null, 100));
        // id is 0
        Assert.assertThrows(InvalidProductIdException.class, () -> s.updateQuantity(0, 100));
        // id is < 0
        Assert.assertThrows(InvalidProductIdException.class, () -> s.updateQuantity(-551, 100));
        // id not found
        try {
            assertFalse(s.updateQuantity(555, 100));
        } catch (InvalidProductIdException | UnauthorizedException e) {
            e.printStackTrace();
        }

        // invalid location -> null
        try {
            assertFalse(s.updateQuantity(2, 100));
        } catch (InvalidProductIdException | UnauthorizedException e) {
            e.printStackTrace();
        }
        // invalid quantity ->  resulting amount would be negative
        try {
            assertFalse(s.updateQuantity(1, -100000));
        } catch (InvalidProductIdException | UnauthorizedException  e) {
            e.printStackTrace();
        }

        /* Test valid update -> search product via bar code as in scenario 1.8 */
        try {
            Integer id = s.getProductTypeByBarCode("123456789012").getId();
            assertTrue(s.updateQuantity(id, 100));
        } catch (InvalidProductIdException | UnauthorizedException | InvalidProductCodeException e) {
            e.printStackTrace();
        }

        // DB is unreachable
        DbManagerClass.disconnect();
        try {
            Integer id = s.getProductTypeByBarCode("123456789012").getId();
            assertFalse(s.updateQuantity(id, 100));
        } catch (UnauthorizedException | InvalidProductIdException | InvalidProductCodeException e) {
            fail();
        }
        EZShop.connectToDb();
    }

    @Test
    public void testUpdatePosition () {
        /*Test invalid roles*/
        // no logged user
        assertThrows(UnauthorizedException.class , () -> s.updatePosition(1, "1-a-1"));

        try {
            s.login("testCashier", "testPass");
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            e.printStackTrace();
        }
        // not Administrator
        assertThrows(UnauthorizedException.class , () -> s.updatePosition(1, "1-a-1"));

        loginAsAdministrator();

        /* Test invalid product id */
        // id is null
        Assert.assertThrows(InvalidProductIdException.class, () -> s.updatePosition(null, "1-a-1"));

        Assert.assertThrows(InvalidProductIdException.class, () -> s.updatePosition(0, "1-a-1"));
        // id is < 0
        Assert.assertThrows(InvalidProductIdException.class, () -> s.updatePosition(-100, "1-a-1"));
        // id not found
        try {
            assertFalse(s.updatePosition(55555, "1-a-1"));
        } catch (InvalidProductIdException | UnauthorizedException | InvalidLocationException e) {
            e.printStackTrace();
        }

        /* Test invalid position format*/
        // invalid position format ->  valid: <aisleNumber>-<rackAlphabeticIdentifier>-<levelNumber>
        Assert.assertThrows(InvalidLocationException.class, () -> s.updatePosition(1, "a1b2c3"));

        /* Test position not unique */
        try {
            assertFalse( s.updatePosition(2,"1-a-1"));
        } catch (UnauthorizedException | InvalidLocationException | InvalidProductIdException e) {
            e.printStackTrace();
        }

        // DB is unreachable

        DbManagerClass.disconnect();
        try {
            Integer id = s.getProductTypeByBarCode("3214569870128").getId();
            assertFalse(s.updatePosition(id, "2-b-2"));
        } catch (UnauthorizedException | InvalidProductIdException | InvalidProductCodeException | InvalidLocationException e) {
            fail();
        }
        EZShop.connectToDb();

        /* Test valid position -> search product via bar code as in scenario 1.2 */
        try {
            Integer id = s.getProductTypeByBarCode("3214569870128").getId();
            assertTrue(s.updatePosition(id, "2-b-2"));
        } catch (InvalidProductIdException | UnauthorizedException | InvalidLocationException | InvalidProductCodeException e) {
            e.printStackTrace();
        }
        // reset position
        try {
            assertTrue(s.updatePosition(2, null));
        } catch (InvalidProductIdException | UnauthorizedException | InvalidLocationException e) {
            e.printStackTrace();
        }

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