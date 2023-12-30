package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.*;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.utils.DatabaseQuery;
import it.polito.ezshop.utils.DbManagerClass;
import org.junit.*;

import java.sql.SQLException;

import static it.polito.ezshop.data.Product.STAT_INV;
import static it.polito.ezshop.utils.DbManagerClass.*;
import static org.junit.Assert.*;

public class OrdersSaleTransactionTest {

    private static EZShop s;
    private UserClass u;
    private UserClass c;
    private int issuedOrder = 0;
    private int paidOrder = 0;
    private String productCode = "088388484247";
    private String RFID1 = "000000000001";
    private String RFID2 = "000001000001";
    private String RFID3 = "000001200001";

    public static void firstSetup(){
        EZShop.connectToDb();
    }

    @AfterClass
    public static void disconnect() {
        DbManagerClass.disconnect();
    }

    private String convertRFIDToString(long i){
        return String.format("%012d", i);
    }

    public void initProdOrd(){
        int p = 0;
        try {
            s.createProductType("prodotto senza location", "1597534562580", 2.3, "");
            p = s.createProductType("prodotto con location", productCode, 2.1, "");
            s.updatePosition(p, "001-a-003");
            s.updateQuantity(p, 600);

        } catch (InvalidProductIdException | InvalidLocationException | UnauthorizedException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException e) {
            e.printStackTrace();
        }

        for (long i = Long.parseLong(RFID1); i < Long.parseLong(RFID1)+10 ; i++) {
            Product product = new Product(convertRFIDToString(i), productCode, STAT_INV);
            try {
                DatabaseQuery.createRFIDBarcodeLink(product);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        s.setCurrentBalance(1000);
        try {
            issuedOrder = s.issueOrder(productCode, 10, 1.5);
            paidOrder = s.payOrderFor(productCode, 10, 1.5);
        } catch (InvalidProductCodeException | InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void createEZShop(){
        s = new EZShop();
    }

    @Before
    public void setupDBandLogin(){
        u = new UserClass("testing", "testing", "Administrator");
        c = new UserClass("cashier", "testing", "Cashier");
        s.setLoggedUser(u);
        initProdOrd();
        s.setLoggedUser(null);
        s.refresh();
    }

    @After
    public void restoreTables(){
        s.reset();
    }

    private void initAllTables() {
        //1 user
        UserClass u1 = new UserClass("Name1", "Pass", "Administrator");
        //1 product
        ProductTypeClass p2 = new ProductTypeClass("Salad", "1234567890128",1.2,"Expiring");
        //1 order
        OrderClass o = new OrderClass("088388484261", 1.5, 10, "ISSUED");
        //1 sale transaction
        SaleTransactionClass s2 = new SaleTransactionClass();
        //1 customer
        CustomerClass cc = new CustomerClass("Antonio");

        try {
            DatabaseQuery.createUser(u1);
            DatabaseQuery.createProductType(p2);
            DatabaseQuery.createOrder(o);
            DatabaseQuery.createSaleTransaction(s2);
            DatabaseQuery.defineCustomer(cc);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //balance
        s.setCurrentBalance(1000);
    }

    @Test
    public void testGetDataFromDb() {
        clearTables();
        disconnect();
        try {
            s.getDataFromDb();
            fail();
        } catch (SQLException throwables) {
            assertTrue(true);
            firstSetup();
            initAllTables();
        }
        try {
            s.getDataFromDb();
        } catch (SQLException throwables) {
            fail();
        }
        s.setLoggedUser(u);
        try {
            assertEquals(1, s.getAllUsers().size());
            assertEquals(1, s.getAllProductTypes().size());
            assertEquals(1, s.getAllOrders().size());
            assertEquals(2, s.getCreditsAndDebits(null, null).size());
            assertEquals(1, s.getAllCustomers().size());
        } catch (UnauthorizedException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testReset() {
        s.setLoggedUser(u);
        disconnect();
        s.reset();
        firstSetup();
        s.reset();
        try {
            assertEquals(0.0, s.computeBalance(), 0.001);
            assertTrue(s.getAllProductTypes().isEmpty());
            assertTrue(s.getAllUsers().isEmpty());
            assertTrue(s.getAllCustomers().isEmpty());
            assertTrue(s.getCreditsAndDebits(null, null).isEmpty());
        } catch (UnauthorizedException e) {
            fail();
        }
        //ripristino prodotti e bilancio, utili per gli altri test
        initProdOrd();
    }

    @Test
    public void testIssueOrder() {
        int res;
        try {
            s.issueOrder("987654321012", 20, 1.2);
            fail();
        } catch (InvalidProductCodeException | InvalidQuantityException | InvalidPricePerUnitException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(c);
        try {
            s.issueOrder("987654321012", 20, 1.2);
            fail();
        } catch (InvalidProductCodeException | InvalidQuantityException | InvalidPricePerUnitException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.issueOrder("98765321012", 20, 1.2);   //invalid productCode
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        } catch (InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }
        try {
            s.issueOrder("", 20, 1.2);  //empty productCode
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        } catch (InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }
        try {
            s.issueOrder(null, 20, 1.2); //null productCode
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        } catch (InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }

        try {
            s.issueOrder("987654321012", -20, 1.2);
            fail();
        } catch (InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        } catch (InvalidQuantityException e) {
            assertTrue(true);
        }

        try {
            s.issueOrder("987654321012", 0, 1.2);
            fail();
        } catch (InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        } catch (InvalidQuantityException e) {
            assertTrue(true);
        }

        try {
            s.issueOrder("987654321012", 20, -1.2);
            fail();
        } catch (InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidPricePerUnitException e) {
            assertTrue(true);
        }

        try {
            s.issueOrder("987654321012", 20, 0);
            fail();
        } catch (InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidPricePerUnitException e) {
            assertTrue(true);
        }

        try{
            res = s.issueOrder("1597534562580", 20, 1.2); //ordine senza location - OK
            assertTrue(res>0);
        } catch (InvalidQuantityException | InvalidProductCodeException | UnauthorizedException | InvalidPricePerUnitException e) {
            fail();
        }

        try{
            int actual = s.issueOrder("1478520369140", 20, 1.2); //product does not exist
            assertEquals(-1, actual);
        } catch (InvalidQuantityException | InvalidProductCodeException | UnauthorizedException | InvalidPricePerUnitException e) {
            fail();
        }
        disconnect();
        try{
            res = s.issueOrder("1597534562580", 20, 1.2); //problems with db
            assertTrue(res == -1);
        } catch (InvalidQuantityException | InvalidProductCodeException | UnauthorizedException | InvalidPricePerUnitException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testPayOrderFor() {

        try {
            s.payOrderFor("987654321012", 20, 1.2); //no logged user
            fail();
        } catch (InvalidProductCodeException | InvalidQuantityException | InvalidPricePerUnitException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(c);
        try {
            s.payOrderFor("987654321012", 20, 1.2); //user with the wrong role
            fail();
        } catch (InvalidProductCodeException | InvalidQuantityException | InvalidPricePerUnitException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.payOrderFor("98765321012", 20, 1.2);  //invalid productCode
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        } catch (InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }
        try {
            s.payOrderFor("", 20, 1.2); //empty productCode
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        } catch (InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }
        try {
            s.payOrderFor(null, 20, 1.2); //productCode is null
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        } catch (InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }

        try {
            s.payOrderFor("987654321012", -20, 1.2);
            fail();
        } catch (InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        } catch (InvalidQuantityException e) {
            assertTrue(true);
        }

        try {
            s.payOrderFor("987654321012", 0, 1.2);
            fail();
        } catch (InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        } catch (InvalidQuantityException e) {
            assertTrue(true);
        }

        try {
            s.payOrderFor("987654321012", 20, -1.2);
            fail();
        } catch (InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidPricePerUnitException e) {
            assertTrue(true);
        }

        try {
            s.payOrderFor("987654321012", 20, 0);
            fail();
        } catch (InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidPricePerUnitException e) {
            assertTrue(true);
        }
        s.setCurrentBalance(1000);
        try{
            int res = s.payOrderFor("1597534562580", 20, 1.2);
            assertTrue(res>0);  //caso bilancio sufficiente
        } catch (InvalidQuantityException | InvalidProductCodeException | UnauthorizedException | InvalidPricePerUnitException e) {
            fail();
        }
        s.setCurrentBalance(0.5);
        try{
            int res = s.payOrderFor("1597534562580", 20, 1.2);
            assertEquals(-1, res); //caso bilancio insufficiente
        } catch (InvalidQuantityException | InvalidProductCodeException | UnauthorizedException | InvalidPricePerUnitException e) {
            fail();
        }

        try{
            int res = s.payOrderFor("1478520369140", 20, 1.2);
            assertEquals(-1, res);  //caso prodotto inesistente
        } catch (InvalidQuantityException | InvalidProductCodeException | UnauthorizedException | InvalidPricePerUnitException e) {
            fail();
        }
        s.setCurrentBalance(1000);
        disconnect();
        try{
            int res = s.payOrderFor("1597534562580", 20, 1.2);
            assertTrue(res == -1);  //caso DB unreachable
        } catch (InvalidQuantityException | InvalidProductCodeException | UnauthorizedException | InvalidPricePerUnitException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testPayOrder() {
        int id = issuedOrder;

        s.setLoggedUser(null);
        try {
            s.payOrder(id);
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidOrderIdException e) {
            fail();
        }
        s.setLoggedUser(c);
        try {
            s.payOrder(id);
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidOrderIdException e) {
            fail();
        }
        s.setLoggedUser(u);
        try {
            s.payOrder(-1);
            fail();
        } catch (UnauthorizedException e) {
            fail();
        } catch (InvalidOrderIdException e) {
            assertTrue(true);
        }
        s.setCurrentBalance(0.5);
        try {
            assertFalse(s.payOrder(id));    //bilancio insufficiente
        } catch (UnauthorizedException | InvalidOrderIdException e) {
            fail();
        }
        s.setCurrentBalance(2000);
        try {
            assertTrue(s.payOrder(id));    //caso esecuzione corretta
        } catch (UnauthorizedException | InvalidOrderIdException e) {
            fail();
        }
        try {
            assertTrue(s.payOrder(id));    //caso ordine già pagato
        } catch (UnauthorizedException | InvalidOrderIdException e) {
            fail();
        }
        try {
            s.recordOrderArrival(id);
            assertFalse(s.payOrder(id));    //caso ordine già arrivato
        } catch (UnauthorizedException | InvalidOrderIdException | InvalidLocationException e) {
            fail();
        }

        try {
            assertFalse(s.payOrder(20000));    //caso ordine inesistente
        } catch (UnauthorizedException | InvalidOrderIdException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.payOrder(id));    //problems with db
        } catch (UnauthorizedException | InvalidOrderIdException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testRecordOrderArrival() {
        int id = 0, id2 = issuedOrder;
        s.setLoggedUser(u);
        s.setCurrentBalance(1000);

        s.setLoggedUser(null);
        try {
            s.recordOrderArrival(id2);   //no logged user
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidOrderIdException | InvalidLocationException e) {
            fail();
        }
        s.setLoggedUser(c);
        try {
            s.recordOrderArrival(id2);   //user does not have the right role
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidOrderIdException | InvalidLocationException e) {
            fail();
        }
        s.setLoggedUser(u);
        try {
            s.recordOrderArrival(-1);   //invalid orderId
            fail();
        } catch (InvalidOrderIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        try {
            s.recordOrderArrival(null);   //null orderId
            fail();
        } catch (InvalidOrderIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        try {
            id = s.payOrderFor("1597534562580", 30, 3.2); //crea e paga ordine di prodotto senza location
        } catch (InvalidProductCodeException | InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }
        try {
            s.recordOrderArrival(id);  //prodotto senza location
            fail();
        } catch (InvalidOrderIdException | UnauthorizedException e) {
            fail();
        } catch (InvalidLocationException e) {
            assertTrue(true);
        }

        id = paidOrder;
        //record order arrival
        try {
            assertTrue(s.recordOrderArrival(id));   //ordine da completare
        } catch (InvalidOrderIdException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        try {
            assertTrue(s.recordOrderArrival(id));  //ordine già completato
        } catch (InvalidOrderIdException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        try {
            assertFalse(s.recordOrderArrival(id2));  //ordine ancora issued
        } catch (InvalidOrderIdException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        try {
            assertFalse(s.recordOrderArrival(40000));  //ordine inesistente
        } catch (InvalidOrderIdException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.recordOrderArrival(id));   //DB unreachable
        } catch (InvalidOrderIdException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testRecordOrderArrivalRFID() {
        int id = 0, id2 = issuedOrder;
        s.setLoggedUser(u);
        s.setCurrentBalance(1000);

        s.setLoggedUser(null);
        try {
            s.recordOrderArrivalRFID(id2, RFID2);   //no logged user
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidOrderIdException | InvalidRFIDException | InvalidLocationException e) {
            fail();
        }
        s.setLoggedUser(c);
        try {
            s.recordOrderArrivalRFID(id2, RFID2);   //user does not have the right role
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        } catch (InvalidOrderIdException | InvalidRFIDException | InvalidLocationException e) {
            fail();
        }
        s.setLoggedUser(u);
        try {
            s.recordOrderArrivalRFID(-1, RFID2);   //invalid orderId
            fail();
        } catch (InvalidOrderIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException | InvalidRFIDException | InvalidLocationException e) {
            fail();
        }
        try {
            s.recordOrderArrivalRFID(null, RFID2);  //null orderId
            fail();
        } catch (InvalidOrderIdException e) {
            assertTrue(true);
        } catch (UnauthorizedException | InvalidRFIDException | InvalidLocationException e) {
            fail();
        }
        try {
            id = s.payOrderFor("1597534562580", 30, 3.2); //crea e paga ordine di prodotto senza location
        } catch (InvalidProductCodeException | InvalidQuantityException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail();
        }
        try {
            s.recordOrderArrivalRFID(id, RFID2);  //prodotto senza location
            fail();
        } catch (InvalidOrderIdException | InvalidRFIDException | UnauthorizedException e) {
            fail();
        } catch (InvalidLocationException e) {
            assertTrue(true);
        }

        id = paidOrder;
        try {
            s.recordOrderArrivalRFID(id, "invalid rfid");  //RFID non valido
            fail();
        } catch (InvalidOrderIdException | InvalidLocationException | UnauthorizedException e) {
            fail();
        } catch (InvalidRFIDException e) {
            assertTrue(true);
        }
        try {
            s.recordOrderArrivalRFID(id, null);  //RFID null
            fail();
        } catch (InvalidOrderIdException | InvalidLocationException | UnauthorizedException e) {
            fail();
        } catch (InvalidRFIDException e) {
            assertTrue(true);
        }

        //record order arrival
        try {
            assertTrue(s.recordOrderArrivalRFID(id, RFID2));   //ordine da completare
        } catch (InvalidOrderIdException | InvalidRFIDException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        try {
            assertFalse(s.recordOrderArrivalRFID(id2, RFID3));  //ordine ancora issued
        } catch (InvalidOrderIdException | InvalidRFIDException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        try {
            assertFalse(s.recordOrderArrivalRFID(4000, RFID3));  //ordine inesistente
        } catch (InvalidOrderIdException | InvalidRFIDException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.recordOrderArrivalRFID(id, RFID3));   //DB unreachable
        } catch (InvalidOrderIdException | InvalidRFIDException | UnauthorizedException | InvalidLocationException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testGetAllOrders() {

        try {
            s.getAllOrders();
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(c);
        try {
            s.getAllOrders();
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            assertNotNull(s.getAllOrders());
        } catch (UnauthorizedException e) {
            fail();
        }
    }

    @Test
    public void testStartSaleTransaction() {

        try {
            s.startSaleTransaction();
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            assertTrue(s.startSaleTransaction() > 0);
        } catch (UnauthorizedException e) {
            fail();
        }
        disconnect();
        try {
            assertTrue(s.startSaleTransaction() < 0); //case DB unreachable
        } catch (UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testAddProductToSale() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
        } catch (UnauthorizedException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.addProductToSale(st, productCode, 1);
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | InvalidProductCodeException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.addProductToSale(st, productCode, -1);
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidQuantityException e) {
            assertTrue(true);
        }
        try {
            s.addProductToSale(st, "98754321012", 1);
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        }
        try {
            s.addProductToSale(0, productCode, 1);
            fail();
        } catch ( InvalidQuantityException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.addProductToSale(null, productCode, 1);
            fail();
        } catch ( InvalidQuantityException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }

        try {   //per testare quantità insufficiente setto quantità del prodotto a zero
            s.updateQuantity(s.getProductTypeByBarCode(productCode).getId(), -s.getProductTypeByBarCode(productCode).getQuantity());
        } catch (InvalidProductIdException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        }

        try {
            assertFalse(s.addProductToSale(st, productCode, 1)); //quantity is not enough
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertFalse(s.addProductToSale(90000, "987654321012", 1)); //not an existing and open transaction
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertFalse(s.addProductToSale(st, "1478520369140", 1)); //not an existing product
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }

        try {
            s.updateQuantity(s.getProductTypeByBarCode(productCode).getId(), 200);
        } catch (InvalidProductIdException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        }

        try {
            assertTrue(s.addProductToSale(st, productCode, 1));
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.addProductToSale(st, productCode, 1));
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testAddProductToSaleRFID() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
        } catch (UnauthorizedException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.addProductToSaleRFID(st, RFID1);
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | InvalidRFIDException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);

        try {
            s.addProductToSaleRFID(st, "98754321012");
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidRFIDException e) {
            assertTrue(true);
        }
        try {
            s.addProductToSaleRFID(0, RFID1);
            fail();
        } catch ( InvalidQuantityException | UnauthorizedException | InvalidRFIDException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.addProductToSaleRFID(null, productCode);
            fail();
        } catch ( InvalidQuantityException | UnauthorizedException | InvalidRFIDException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }

        try {   //per testare quantità insufficiente setto quantità del prodotto a zero
            s.updateQuantity(s.getProductTypeByBarCode(productCode).getId(), -s.getProductTypeByBarCode(productCode).getQuantity());
        } catch (InvalidProductIdException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        }

        try {
            assertFalse(s.addProductToSaleRFID(90000, RFID1)); //not an existing and open transaction
        } catch (InvalidTransactionIdException | InvalidRFIDException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertFalse(s.addProductToSaleRFID(st, RFID2)); //not an existing product
        } catch (InvalidTransactionIdException | InvalidRFIDException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }

        try {
            s.updateQuantity(s.getProductTypeByBarCode(productCode).getId(), 200);
        } catch (InvalidProductIdException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        }

        try {
            assertTrue(s.addProductToSaleRFID(st, RFID1));
        } catch (InvalidTransactionIdException | InvalidRFIDException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.addProductToSaleRFID(st, "000000000002"));
        } catch (InvalidTransactionIdException | InvalidRFIDException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testDeleteProductFromSale() {

        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 2);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.deleteProductFromSale(st, productCode, 1);
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | InvalidProductCodeException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.deleteProductFromSale(st, productCode, -1);
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidQuantityException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSale(st, "98754321012", 1);  //invalid productCode
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSale(st, "", 1); //empty productCode
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSale(st, null, 1); //null productCode
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSale(0, productCode, 1);
            fail();
        } catch ( InvalidQuantityException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSale(null, productCode, 1);
            fail();
        } catch ( InvalidQuantityException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }

        try {
            assertFalse(s.deleteProductFromSale(st, productCode, 10)); //quantity is not enough
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertFalse(s.deleteProductFromSale(90000, productCode, 1)); //not an existing and open transaction
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertFalse(s.deleteProductFromSale(st, "1478520369140", 1)); //not an existing product
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertTrue(s.deleteProductFromSale(st, productCode, 1));
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertTrue(s.deleteProductFromSale(st, productCode, 1));    //case oldAmount == amount to remove
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.deleteProductFromSale(st, productCode, 1));    //DB unreachable
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }


    @Test
    public void testDeleteProductFromSaleRFID() {

        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSaleRFID(st, RFID1);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidRFIDException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.deleteProductFromSaleRFID(st, RFID1);
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException  | InvalidRFIDException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);

        try {
            s.deleteProductFromSaleRFID(st, "98754321012");  //invalid RFID
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidRFIDException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSaleRFID(st, ""); //empty rfid
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidRFIDException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSaleRFID(st, null); //null rfid
            fail();
        } catch (InvalidTransactionIdException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        } catch (InvalidRFIDException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSaleRFID(0, RFID1);
            fail();
        } catch ( InvalidQuantityException | UnauthorizedException | InvalidRFIDException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.deleteProductFromSaleRFID(null, RFID1);
            fail();
        } catch ( InvalidQuantityException | UnauthorizedException | InvalidRFIDException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }


        try {
            assertFalse(s.deleteProductFromSaleRFID(90000, RFID1)); //not an existing and open transaction
        } catch (InvalidTransactionIdException | InvalidRFIDException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertFalse(s.deleteProductFromSaleRFID(st, "000000000111")); //not an existing product
        } catch (InvalidTransactionIdException | InvalidRFIDException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        try {
            assertTrue(s.deleteProductFromSaleRFID(st, RFID1));
        } catch (InvalidTransactionIdException | InvalidRFIDException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.deleteProductFromSaleRFID(st, RFID1));    //DB unreachable
        } catch (InvalidTransactionIdException | InvalidRFIDException | InvalidQuantityException | UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testApplyDiscountRateToProduct() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 2);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.applyDiscountRateToProduct(st, productCode, 0.1);
            fail();
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | InvalidProductCodeException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.applyDiscountRateToProduct(st, productCode, -1); //negative discount rate
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidDiscountRateException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToProduct(st, productCode, 1.2); //discount rate > 1
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidDiscountRateException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToProduct(st, "98754321012", 0.1);  //invalid productCode
            fail();
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToProduct(st, "", 0.1); //empty productCode
            fail();
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToProduct(st, null, 0.1); //null productCode
            fail();
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        } catch (InvalidProductCodeException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToProduct(0, productCode, 0.1);
            fail();
        } catch ( InvalidDiscountRateException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToProduct(null, productCode, 0.1);
            fail();
        } catch ( InvalidDiscountRateException | UnauthorizedException | InvalidProductCodeException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }


        try {
            assertFalse(s.applyDiscountRateToProduct(9000, productCode, 0.1)); //not an existing and open transaction
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        }
        try {
            assertFalse(s.applyDiscountRateToProduct(st, "1478520369140", 0.1)); //not an existing product
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        }
        try {
            assertTrue(s.applyDiscountRateToProduct(st, productCode, 0.1));
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.applyDiscountRateToProduct(st, productCode, 0.1)); //DB unreachable
        } catch (InvalidTransactionIdException | InvalidProductCodeException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testApplyDiscountRateToSale() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 2);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.applyDiscountRateToSale(st, 0.1);
            fail();
        } catch (InvalidTransactionIdException | InvalidDiscountRateException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.applyDiscountRateToSale(st, -1); //negative discount rate
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        } catch (InvalidDiscountRateException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToSale(st, 1.2); //discount rate > 1
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        } catch (InvalidDiscountRateException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToSale(0, 0.1);
            fail();
        } catch ( InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.applyDiscountRateToSale(null, 0.1);
            fail();
        } catch ( InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }


        try {
            assertFalse(s.applyDiscountRateToSale(900, 0.1)); //not an existing and open transaction
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        }
        try {
            assertTrue(s.applyDiscountRateToSale(st, 0.1));
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        }
        try {
            s.endSaleTransaction(st);
            assertTrue(s.applyDiscountRateToSale(st,0.2)); //works with a closed (but not paid) transaction
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        }
        try {
            s.receiveCashPayment(s.getSaleTransaction(st).getTicketNumber(), 100);
            assertFalse(s.applyDiscountRateToSale(st, 0.1)); //cannot apply discount to an already paid transaction
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | UnauthorizedException | InvalidPaymentException e) {
            fail();
        }

        disconnect();
        try {
            s.endSaleTransaction(st);
            assertFalse(s.applyDiscountRateToSale(st,0.2)); //DB unreachable
        } catch (InvalidTransactionIdException | InvalidDiscountRateException | UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testComputePointsForSale() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 10);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.computePointsForSale(st);
            fail();
        } catch (InvalidTransactionIdException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.computePointsForSale(0);
            fail();
        } catch ( UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.computePointsForSale(null);
            fail();
        } catch ( UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }

        try {
            assertEquals(2, s.computePointsForSale(st));    //price == 2.1*10 = 21
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
        try {
            assertEquals(-1, s.computePointsForSale(10000));  //transaction does not exist
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
    }

    @Test
    public void testEndSaleTransaction() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 3);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.endSaleTransaction(st);
            fail();
        } catch (InvalidTransactionIdException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.endSaleTransaction(0);
            fail();
        } catch ( UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.endSaleTransaction(null);
            fail();
        } catch ( UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }

        try {
            assertFalse(s.endSaleTransaction(10000));   //transaction does not exist
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
        try {
            assertTrue(s.endSaleTransaction(st));
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
        try {
            assertFalse(s.endSaleTransaction(st));   //transaction already closed
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.endSaleTransaction(st));   //DB unreachable
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testDeleteSaleTransaction() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 1);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.deleteSaleTransaction(st);
            fail();
        } catch (InvalidTransactionIdException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.deleteSaleTransaction(0);
            fail();
        } catch ( UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.deleteSaleTransaction(null);
            fail();
        } catch ( UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }

        try {
            assertFalse(s.deleteSaleTransaction(10000));   //transaction does not exist
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
        try {
            assertTrue(s.deleteSaleTransaction(st));
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
        try {
            s.receiveCashPayment(st,5);
            assertFalse(s.deleteSaleTransaction(st));   //transaction already paid
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidPaymentException e) {
            fail();
        }
    }

    @Test
    public void testGetSaleTransaction() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 1);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.deleteSaleTransaction(st);
            fail();
        } catch (InvalidTransactionIdException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.deleteSaleTransaction(0);
            fail();
        } catch ( UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.deleteSaleTransaction(null);
            fail();
        } catch ( UnauthorizedException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }

        try {
            assertNull(s.getSaleTransaction(st));   //transaction has not been closed yet
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
        try {
            s.endSaleTransaction(st);
            assertNotNull(s.getSaleTransaction(st));
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        }
    }

    @Test
    public void testReceiveCashPayment() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 1);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.receiveCashPayment(st, 5);
            fail();
        } catch (InvalidTransactionIdException | InvalidPaymentException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.receiveCashPayment(0, 5);
            fail();
        } catch ( UnauthorizedException | InvalidPaymentException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.receiveCashPayment(null, 5);
            fail();
        } catch ( UnauthorizedException | InvalidPaymentException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.receiveCashPayment(st, 0);
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        } catch (InvalidPaymentException e) {
            assertTrue(true);
        }

        try {
            assertEquals(-1.0, s.receiveCashPayment(st, 1.0), 0.005);  //cash not enough
        } catch (InvalidTransactionIdException | InvalidPaymentException | UnauthorizedException e) {
            fail();
        }
        try {
            assertEquals(-1.0, s.receiveCashPayment(10000, 10), 0.005);  //transaction does not exist
        } catch (InvalidTransactionIdException | InvalidPaymentException | UnauthorizedException e) {
            fail();
        }
        try {
            assertTrue(s.receiveCashPayment(st, 10) >= 0);  //return the change
        } catch (InvalidTransactionIdException | InvalidPaymentException | UnauthorizedException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.receiveCashPayment(st, 10) >= 0);  //DB unreachable
        } catch (InvalidTransactionIdException | InvalidPaymentException | UnauthorizedException e) {
            fail();
        }
        firstSetup();
    }

    @Test
    public void testReceiveCreditCardPayment() {
        int st = -1;

        s.setLoggedUser(u);
        try {
            st = s.startSaleTransaction();
            s.addProductToSale(st, productCode, 1);
        } catch (UnauthorizedException | InvalidQuantityException | InvalidTransactionIdException | InvalidProductCodeException e) {
            fail();
        }
        s.setLoggedUser(null);
        try {
            s.receiveCreditCardPayment(st, "4485370086510891");
            fail();
        } catch (InvalidTransactionIdException | InvalidCreditCardException e) {
            fail();
        } catch (UnauthorizedException e) {
            assertTrue(true);
        }
        s.setLoggedUser(u);
        try {
            s.receiveCreditCardPayment(0, "4485370086510891");
            fail();
        } catch ( UnauthorizedException | InvalidCreditCardException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.receiveCreditCardPayment(null, "4485370086510891");
            fail();
        } catch ( UnauthorizedException | InvalidCreditCardException e) {
            fail();
        } catch (InvalidTransactionIdException e) {
            assertTrue(true);
        }
        try {
            s.receiveCreditCardPayment(st, null);
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        } catch (InvalidCreditCardException e) {
            assertTrue(true);
        }
        try {
            s.receiveCreditCardPayment(st, "");
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        } catch (InvalidCreditCardException e) {
            assertTrue(true);
        }
        try {
            s.receiveCreditCardPayment(st, "123123123123123");
            fail();
        } catch (InvalidTransactionIdException | UnauthorizedException e) {
            fail();
        } catch (InvalidCreditCardException e) {
            assertTrue(true);
        }

        try {
            assertFalse(s.receiveCreditCardPayment(st, "4716258050958645"));    //card has not enough money
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidCreditCardException e) {
            fail();
        }
        try {
            assertFalse(s.receiveCreditCardPayment(10000, "4485370086510891"));  //sale does not exist
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidCreditCardException e) {
            fail();
        }
        try {
            assertFalse(s.receiveCreditCardPayment(st, "4023610017085680"));  //card not registered
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidCreditCardException e) {
            fail();
        }
        try {
            assertTrue(s.receiveCreditCardPayment(st, "4485370086510891"));
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidCreditCardException e) {
            fail();
        }
        disconnect();
        try {
            assertFalse(s.receiveCreditCardPayment(st, "4485370086510891")); //DB unreachable
        } catch (InvalidTransactionIdException | UnauthorizedException | InvalidCreditCardException e) {
            fail();
        }
        firstSetup();
    }
}