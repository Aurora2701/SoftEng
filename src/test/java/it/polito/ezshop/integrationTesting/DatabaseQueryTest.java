package it.polito.ezshop.integrationTesting;

import it.polito.ezshop.data.*;
import it.polito.ezshop.utils.DatabaseQuery;
import it.polito.ezshop.utils.DbManagerClass;
import org.junit.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class DatabaseQueryTest{

    String rfid = "test";


    private static EZShop s;

    @BeforeClass
    public static void createEZShop(){
        s = new EZShop();
    }

    @Before
    public void setupDBandLogin(){
        DbManagerClass.initDbSchema();
        SaleTransactionClass.setSaleCount(0);
    }

    @After
    public void restoreTables(){
        s.reset();
    }

    @AfterClass
    public static void disconnect() {
        DbManagerClass.disconnect();
    }

    @Test
    public void testGet_users() {
        UserClass u1 = new UserClass("Name1", "Pass", "Administrator");
        UserClass u2 = new UserClass("Name2", "Pass", "ShopManager");
        UserClass u3 = new UserClass("Name3", "Pass", "Cashier");

        try {
            // Empty map
            assertEquals(0, DatabaseQuery.get_users().size());

            DatabaseQuery.createUser(u1);
            DatabaseQuery.createUser(u2);
            DatabaseQuery.createUser(u3);

            assertEquals(3, DatabaseQuery.get_users().size());
            assertTrue(DatabaseQuery.get_users().containsKey(u1.getId()));
            assertTrue(DatabaseQuery.get_users().containsKey(u2.getId()));
            assertTrue(DatabaseQuery.get_users().containsKey(u3.getId()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testCreateUser() {
        UserClass u1 = new UserClass("Name1", "Pass", "Administrator");

        UserClass fromDB = null;
        try {
            DatabaseQuery.createUser(u1);
            fromDB = DatabaseQuery.get_users().get(u1.getId());

            assertEquals(u1.getId(),fromDB.getId());
            assertEquals(u1.getUsername(),fromDB.getUsername());
            assertEquals(u1.getPassword(),fromDB.getPassword());
            assertEquals(u1.getRole(),fromDB.getRole());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testDeleteUser() {
        UserClass u1 = new UserClass("Name1", "Pass", "Administrator");
        UserClass u2 = new UserClass("Name2", "Pass", "ShopManager");
        UserClass u3 = new UserClass("Name3", "Pass", "Cashier");

        try {

            DatabaseQuery.createUser(u1);
            DatabaseQuery.createUser(u2);
            DatabaseQuery.createUser(u3);

            DatabaseQuery.deleteUser(u2.getId());
            assertEquals(2, DatabaseQuery.get_users().size());
            assertFalse(DatabaseQuery.get_users().containsKey(u2.getId()));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testUpdateUserRole() {
        UserClass u1 = new UserClass("Name1", "Pass", "Administrator");
        UserClass u2 = new UserClass("Name2", "Pass", "ShopManager");

        try {

            DatabaseQuery.createUser(u1);
            DatabaseQuery.createUser(u2);

            assertTrue(DatabaseQuery.updateUserRole(u2.getId(),"Cashier"));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testGetUserByUsername() {
        UserClass u1 = new UserClass("Name1", "Pass", "Administrator");
        UserClass u2 = new UserClass("Name2", "Pass", "ShopManager");

        try {

            DatabaseQuery.createUser(u1);
            DatabaseQuery.createUser(u2);

            assertEquals(1,(int)DatabaseQuery.getUserByUsername("Name1").getId());
            assertEquals(2,(int)DatabaseQuery.getUserByUsername("Name2").getId());
            assertNull(DatabaseQuery.getUserByUsername("Gianna"));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testGet_products() {
        ProductTypeClass p1 = new ProductTypeClass("Beer", "123456789012",7.5,"");
        ProductTypeClass p2 = new ProductTypeClass("Salad", "1234567890128",1.2,"Expiring");
        ProductTypeClass p3 = new ProductTypeClass("Ferrari Brut", "4638435435485",14.5,"");

        try {
            // Empty map
            assertEquals(0, DatabaseQuery.get_products().size());

            DatabaseQuery.createProductType(p1);
            DatabaseQuery.createProductType(p2);
            DatabaseQuery.createProductType(p3);

            assertEquals(3, DatabaseQuery.get_products().size());
            assertTrue(DatabaseQuery.get_products().containsKey(p1.getId()));
            assertTrue(DatabaseQuery.get_products().containsKey(p2.getId()));
            assertTrue(DatabaseQuery.get_products().containsKey(p3.getId()));
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    @Test
    public void testCreateProductType() {
        ProductTypeClass p2 = new ProductTypeClass("Salad", "1234567890128",1.2,"Expiring");

        ProductTypeClass fromDB = null;
        try {
            DatabaseQuery.createProductType(p2);
            fromDB = DatabaseQuery.get_products().get(p2.getId());

            assertEquals(p2.getId(),fromDB.getId());
            assertEquals(p2.getProductDescription(),fromDB.getProductDescription());
            assertEquals(p2.getBarCode(),fromDB.getBarCode());
            assertEquals(p2.getNote(),fromDB.getNote());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testUpdateProductType() {
        ProductTypeClass p1 = new ProductTypeClass("Beer", "123456789012",7.5,"");
        ProductTypeClass p2 = new ProductTypeClass("Salad", "1234567890128",1.2,"Expiring");
        try {
            DatabaseQuery.createProductType(p1);
            DatabaseQuery.createProductType(p2);

            assertEquals(1.2, DatabaseQuery.get_products().get(p2.getId()).getPricePerUnit(), 0.0);

            p2.setPricePerUnit(55.1);
            assertTrue(DatabaseQuery.updateProductType(2, p2));
            assertEquals(55.1, DatabaseQuery.get_products().get(p2.getId()).getPricePerUnit(), 0.0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateQuantity() {
        ProductTypeClass p1 = new ProductTypeClass("Beer", "123456789012",7.5,"");
        ProductTypeClass p2 = new ProductTypeClass("Salad", "1234567890128",1.2,"Expiring");
        try {
            DatabaseQuery.createProductType(p1);
            DatabaseQuery.createProductType(p2);

            assertEquals(0, (int)DatabaseQuery.get_products().get(p2.getId()).getQuantity());
            assertTrue(DatabaseQuery.updateQuantity(2, 100));
            assertEquals(100, (int)DatabaseQuery.get_products().get(p2.getId()).getQuantity());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdatePosition() {
        ProductTypeClass p1 = new ProductTypeClass("Beer", "123456789012",7.5,"");
        ProductTypeClass p2 = new ProductTypeClass("Salad", "1234567890128",1.2,"Expiring");
        try {
            DatabaseQuery.createProductType(p1);
            DatabaseQuery.createProductType(p2);

            assertNull(DatabaseQuery.get_products().get(p2.getId()).getLocation());
            assertTrue(DatabaseQuery.updatePosition(2, "1-a-1"));
            assertEquals("1-a-1", DatabaseQuery.get_products().get(p2.getId()).getLocation());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteProductType() {
        ProductTypeClass p1 = new ProductTypeClass("Beer", "123456789012",7.5,"");
        ProductTypeClass p2 = new ProductTypeClass("Salad", "1234567890128",1.2,"Expiring");
        try {
            DatabaseQuery.createProductType(p1);
            DatabaseQuery.createProductType(p2);

            DatabaseQuery.deleteProductType(p2.getId());
            assertEquals(1, DatabaseQuery.get_products().size());
            assertFalse(DatabaseQuery.get_products().containsKey(p2.getId()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    @Test
//    public void testDeleteAllProducts() {
//        ProductTypeClass p1 = new ProductTypeClass("Beer", "123456789012",7.5,"");
//        ProductTypeClass p2 = new ProductTypeClass("Salad", "1234567890128",1.2,"Expiring");
//        ProductTypeClass p3 = new ProductTypeClass("Ferrari Brut", "4638435435485",14.5,"");
//
//        try {
//            DatabaseQuery.createProductType(p1);
//            DatabaseQuery.createProductType(p2);
//            DatabaseQuery.createProductType(p3);
//
//            assertEquals(3, DatabaseQuery.get_products().size());
//            assertTrue(DatabaseQuery.deleteAllProducts());
//            assertEquals(0, DatabaseQuery.get_products().size());
//
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    public void testCreateOrder() {
        OrderClass o = new OrderClass("088388484261", 1.5, 10, "ISSUED");
        OrderClass fromDB = null;
        try {
            DatabaseQuery.createOrder(o);
            fromDB = DatabaseQuery.getAllOrders().get(o.getOrderId());
        } catch (SQLException throwables) {
            fail();
        }

        assertEquals(o.getOrderId(), fromDB.getOrderId());
    }

    @Test
    public void testUpdateOrderStatus() {
        OrderClass o = new OrderClass("088388484261", 1.5, 10, "ISSUED");
        OrderClass fromDB = null;
        try {
            DatabaseQuery.createOrder(o);
            DatabaseQuery.updateOrderStatus(o.getBalanceId(), "PAYED");
            o.setStatus("PAYED");
            fromDB = DatabaseQuery.getAllOrders().get(o.getOrderId());
        } catch (SQLException throwables) {
            fail();
        }

        assertEquals(o.getStatus(), fromDB.getStatus());

    }

//    @Test
//    public void testDeleteAllOrders() {
//        OrderClass o = new OrderClass("088388484261", 1.5, 10, "ISSUED");
//        OrderClass o2 = new OrderClass("088388484261", 1.5, 20, "COMPLETED");
//        OrderClass o3 = new OrderClass("088388484261", 1.5, 5, "PAYED");
//
//        try {
//            DatabaseQuery.createOrder(o);
//            DatabaseQuery.createOrder(o2);
//            DatabaseQuery.createOrder(o3);
//            DatabaseQuery.deleteAllOrders();
//            assertTrue(DatabaseQuery.getAllOrders().isEmpty());
//        } catch (SQLException throwables) {
//            fail();
//        }
//    }

    @Test
    public void testCreateSaleTransaction() {
        //SaleTransactionClass.setSaleCount(0);
        SaleTransactionClass s = new SaleTransactionClass();
        SaleTransactionClass fromDB = null;

        try {
            DatabaseQuery.createSaleTransaction(s);
            int actual = s.getTicketNumber();
            assertEquals(1, actual);
            fromDB = DatabaseQuery.getAllSaleTransactions().get(s.getTicketNumber());
            assertEquals(s.getTicketNumber(), fromDB.getTicketNumber());
            assertEquals(s.getBalanceId(), fromDB.getBalanceId());
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testAddTicketEntry() {
        SaleTransactionClass s = new SaleTransactionClass();
        ProductTypeClass p = new ProductTypeClass("test", "088388484261", 3.5, "");
        TicketProduct tp = new TicketProduct("088388484261", rfid, "test", 2, 3.5, 0.0);
        s.addEntry(tp);
        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createProductType(p);
            assertTrue(DatabaseQuery.addTicketEntry(s.getBalanceId(), tp));
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testDeleteTicketEntry() {
        SaleTransactionClass s = new SaleTransactionClass();
        ProductTypeClass p = new ProductTypeClass("test", "088388484261", 3.5, "");
        TicketProduct tp = new TicketProduct("088388484261", rfid, "test", 3, 3.5, 0.0);
        s.addEntry(tp);
        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createProductType(p);
            DatabaseQuery.addTicketEntry(s.getBalanceId(), tp);
            DatabaseQuery.deleteTicketEntry(s.getBalanceId(), tp);
            assertTrue(DatabaseQuery.getAllTicketEntries(s.getBalanceId()).isEmpty());
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testUpdateTicketEntryAmount() {
        SaleTransactionClass s = new SaleTransactionClass();
        ProductTypeClass p = new ProductTypeClass("test", "088388484261", 3.5, "");
        TicketProduct tp = new TicketProduct("088388484261", rfid, "test", 3, 3.5, 0.0);
        s.addEntry(tp);
        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createProductType(p);
            DatabaseQuery.addTicketEntry(s.getBalanceId(), tp);
            DatabaseQuery.updateTicketEntryAmount(1, s.getBalanceId(), tp.getBarCode());
            assertEquals(1, DatabaseQuery.getAllTicketEntries(s.getBalanceId()).get(0).getAmount());
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testUpdateTicketEntryDiscount() {
        SaleTransactionClass s = new SaleTransactionClass();
        ProductTypeClass p = new ProductTypeClass("test", "088388484261", 3.5, "");
        TicketProduct tp = new TicketProduct("088388484261", rfid, "test", 2, 3.5, 0.0);
        s.addEntry(tp);
        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createProductType(p);
            DatabaseQuery.addTicketEntry(s.getBalanceId(), tp);
            DatabaseQuery.updateTicketEntryDiscount(0.2, s.getBalanceId(), tp);
            tp.setDiscountRate(0.2);
            assertEquals(tp.getDiscountRate(), DatabaseQuery.getAllTicketEntries(s.getBalanceId()).get(0).getDiscountRate(), 0.005);
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testUpdateSaleDiscountRate() {
        //SaleTransactionClass.setSaleCount(0);
        SaleTransactionClass s = new SaleTransactionClass();
        ProductTypeClass p = new ProductTypeClass("test", "088388484261", 3.5, "");
        TicketProduct tp = new TicketProduct("088388484261", rfid, "test", 2, 3.5, 0.0);
        s.addEntry(tp);
        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createProductType(p);
            DatabaseQuery.addTicketEntry(s.getBalanceId(), tp);
            DatabaseQuery.updateSaleDiscountRate(s.getBalanceId(), 0.2);
            s.setDiscountRate(0.2);
            assertEquals(s.getDiscountRate(), DatabaseQuery.getAllSaleTransactions().get(s.getBalanceId()).getDiscountRate(), 0.005);
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testUpdateSaleClosed() {
        SaleTransactionClass s = new SaleTransactionClass();
        ProductTypeClass p = new ProductTypeClass("test", "088388484261", 3.5, "");
        TicketProduct tp = new TicketProduct("088388484261", rfid, "test", 2, 3.5, 0.0);
        s.addEntry(tp);
        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createProductType(p);
            DatabaseQuery.addTicketEntry(s.getBalanceId(), tp);
            DatabaseQuery.updateSaleClosed(s.getBalanceId());
            assertTrue(DatabaseQuery.getAllSaleTransactions().get(s.getBalanceId()).isClosed());
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testUpdateSalePaid() {
        //SaleTransactionClass.setSaleCount(0);
        SaleTransactionClass s = new SaleTransactionClass();
        ProductTypeClass p = new ProductTypeClass("test", "088388484261", 3.5, "");
        TicketProduct tp = new TicketProduct("088388484261", rfid, "test", 2, 3.5, 0.0);
        s.addEntry(tp);
        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createProductType(p);
            DatabaseQuery.addTicketEntry(s.getBalanceId(), tp);
            DatabaseQuery.updateSaleClosed(s.getBalanceId());
            DatabaseQuery.updateSalePaid(s.getBalanceId());
            assertTrue(DatabaseQuery.getAllSaleTransactions().get(s.getBalanceId()).isPayed());
        } catch (SQLException throwables) {
            fail();
        }
    }

//    @Test
//    public void testDeleteAllBalanceOperations() {
//        SaleTransactionClass s = new SaleTransactionClass();
//        SaleTransactionClass s1 = new SaleTransactionClass();
//        SaleTransactionClass s2 = new SaleTransactionClass();
//
//        try {
//            DatabaseQuery.createSaleTransaction(s);
//            DatabaseQuery.createSaleTransaction(s1);
//            DatabaseQuery.createSaleTransaction(s2);
//            assertTrue(DatabaseQuery.deleteAllBalanceOperations());
//            assertTrue(DatabaseQuery.getAllBalanceOperationsBetweenDates(null, null).isEmpty());
//        } catch (SQLException throwables) {
//            fail();
//        }
//    }

    @Test
    public void testCreateReturnTransaction() {
        //SaleTransactionClass.setSaleCount(0);
        SaleTransactionClass s = new SaleTransactionClass();
        ReturnTransactionClass ret = new ReturnTransactionClass(s.getTicketNumber());
        ReturnTransactionClass fromDB = null;

        try {
            DatabaseQuery.createReturnTransaction(ret);
            assertEquals(1, ret.getBalanceId());
            fromDB = DatabaseQuery.getAllReturnTransactions().get(ret.getBalanceId());
            assertEquals(ret.getBalanceId(), fromDB.getBalanceId());
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testUpdateReturnTransaction() {
        SaleTransactionClass s = new SaleTransactionClass();
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(s.getTicketNumber());
        try {
            DatabaseQuery.createReturnTransaction(returnTransactionClass);
            returnTransactionClass.setClosed(true);
            DatabaseQuery.updateReturnTransaction(returnTransactionClass);
            ReturnTransactionClass fromDb = DatabaseQuery.getAllReturnTransactions().get(returnTransactionClass.getBalanceId());
            assertEquals(fromDb.isClosed(), returnTransactionClass.isClosed());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCreateBalanceTransaction() {
        BalanceOperationClass balanceOperationClass = new BalanceOperationClass();
        balanceOperationClass.setType(BalanceOperationClass.TYPE_DEBIT);
        balanceOperationClass.setMoney(222);

        try {
            DatabaseQuery.createBalanceTransaction(balanceOperationClass);
            Optional<BalanceOperation> fromDb = DatabaseQuery.getAllBalanceOperationsBetweenDates(null, null).stream().filter((b)-> b.getBalanceId() == balanceOperationClass.getBalanceId()).findFirst();

            assertTrue(fromDb.isPresent());
            assertEquals(fromDb.get().getBalanceId(), balanceOperationClass.getBalanceId());
            assertEquals(fromDb.get().getDate(), balanceOperationClass.getDate());
            assertEquals(fromDb.get().getMoney(), balanceOperationClass.getMoney(), 0.1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            fail();
        }
    }

    @Test
    public void testAddProductToTransaction() {
        SaleTransactionClass s = new SaleTransactionClass();
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(s.getTicketNumber());
        try {
            DatabaseQuery.createReturnTransaction(returnTransactionClass);

            DatabaseQuery.addProductToReturnTransaction(returnTransactionClass.getBalanceId(), 3, 10);
            Map<Integer, Integer> products;
            products = DatabaseQuery.getProductsOfTransaction(returnTransactionClass.getBalanceId());

            assertFalse(products.isEmpty());
            assertTrue(products.containsKey(3));
            assertEquals(10, (int) products.get(3));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetProductsOfTransaction() {
    }

    @Test
    public void testDeleteReturnTransaction() {
        SaleTransactionClass s = new SaleTransactionClass();
        ReturnTransactionClass ret = new ReturnTransactionClass(s.getTicketNumber());
        ReturnTransactionClass fromDB = null;

        try {
            DatabaseQuery.createReturnTransaction(ret);
            assertEquals(1, ret.getBalanceId());
            fromDB = DatabaseQuery.getAllReturnTransactions().get(ret.getBalanceId());
            assertEquals(ret.getBalanceId(), fromDB.getBalanceId());

            DatabaseQuery.deleteReturnTransaction(ret.getBalanceId());
            assertFalse(DatabaseQuery.getAllReturnTransactions().containsKey(ret.getBalanceId()));
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testDeleteSaleTransaction() {
        SaleTransactionClass.setSaleCount(0);
        SaleTransactionClass s = new SaleTransactionClass();

        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.deleteSaleTransaction(s.getBalanceId());
            assertFalse(DatabaseQuery.getAllSaleTransactions().containsKey(s.getTicketNumber()));
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testGetAllReturnTransactions() {
    }

    @Test
    public void testGetAllCustomers() {
        Integer expectedID1 = 1;
        Integer expectedID2 = 2;
        CustomerClass cc = new CustomerClass("Antonio");
        CustomerClass cc2 = new CustomerClass("Piero");
        try {
            assertTrue(DatabaseQuery.getAllCustomers().isEmpty());
            DatabaseQuery.defineCustomer(cc);
            CustomerClass test = DatabaseQuery.getAllCustomers().get(1);
            assertEquals(test.getId(), expectedID1);
            assertEquals(test.getCustomerName(), "Antonio");
            DatabaseQuery.defineCustomer(cc2);
            CustomerClass test2 = DatabaseQuery.getAllCustomers().get(2);
            assertEquals(test2.getId(), expectedID2);
            assertEquals(test2.getCustomerName(), "Piero");
            assertEquals(2, DatabaseQuery.getAllCustomers().size());
        } catch (SQLException throwables) {
            fail();
        }

    }

    @Test
    public void testGetAllLoyaltyCards() {
        String expectedID1 = "0000000001";
        String expectedID2 = "0000000002";
        LoyaltyCard lc1 = new LoyaltyCard(expectedID1);
        LoyaltyCard lc2 = new LoyaltyCard(expectedID2);
        try {
            assertTrue(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            DatabaseQuery.createCard(lc1);
            LoyaltyCard test = DatabaseQuery.getAllLoyaltyCards().get(expectedID1);
            assertEquals(test.getID(), expectedID1);
            DatabaseQuery.createCard(lc2);
            LoyaltyCard test2 = DatabaseQuery.getAllLoyaltyCards().get(expectedID2);
            assertEquals(test2.getID(), expectedID2);
            assertEquals(2, DatabaseQuery.getAllLoyaltyCards().size());
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testDefineCustomer() {
        Integer expectedID = 1;
        CustomerClass cc = new CustomerClass("Antonio");
        try {
            assertTrue(DatabaseQuery.defineCustomer(cc));
            CustomerClass test = DatabaseQuery.getAllCustomers().get(1);
            assertEquals(test.getId(), expectedID);
            assertEquals(test.getCustomerName(), "Antonio");
            assertNull(test.getCustomerCard());
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testDeleteCustomer() {
        CustomerClass cc1 = new CustomerClass("Antonio");
        try {
            DatabaseQuery.defineCustomer(cc1);
            assertFalse(DatabaseQuery.getAllCustomers().isEmpty());
            DatabaseQuery.deleteCustomer(1);
            assertTrue(DatabaseQuery.getAllCustomers().isEmpty());
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testCreateCard() {
        Integer expectedCardOwnerID = -1;
        Integer expectedPoints = 0;
        String testID = "0000000001";
        LoyaltyCard lc = new LoyaltyCard(testID);
        try {
            assertTrue(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            DatabaseQuery.createCard(lc);
            assertFalse(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            assertEquals(DatabaseQuery.getAllLoyaltyCards().get(testID).getCardOwnerID(), expectedCardOwnerID);
            assertEquals(DatabaseQuery.getAllLoyaltyCards().get(testID).getID(), testID);
            assertEquals(DatabaseQuery.getAllLoyaltyCards().get(testID).getPoints(), expectedPoints);
        } catch (SQLException throwables) {
            fail();
        }

    }

    @Test
    public void testAttachCardToCustomer() {
        String testID = "0000000001";
        String testCustomer = "Piero";
        Integer testCustomerID = 1;
        LoyaltyCard lc = new LoyaltyCard(testID);
        CustomerClass cc = new CustomerClass(testCustomer);

        try {
            assertTrue(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            DatabaseQuery.createCard(lc);
            DatabaseQuery.defineCustomer(cc);
            DatabaseQuery.attachCardToCustomer(lc, cc);
            LoyaltyCard myCard = DatabaseQuery.getAllLoyaltyCards().get(testID);
            CustomerClass myCustomer = DatabaseQuery.getAllCustomers().get(testCustomerID);
            assertEquals(myCard.getID(), myCustomer.getCustomerCard());
        } catch (SQLException throwables) {
            fail();
        }

    }

    @Test
    public void testModifyCustomer_empty() {

        String oldCustomerName = "Piero";
        String cardID = "0000000001";
        String newCustomerName = "Antonio";
        Integer testCustomerID = 1;
        CustomerClass cc = new CustomerClass(oldCustomerName);
        LoyaltyCard lc = new LoyaltyCard(cardID);

        try {
            DatabaseQuery.defineCustomer(cc);
            DatabaseQuery.createCard(lc);
            assertFalse(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            DatabaseQuery.attachCardToCustomer(lc,cc);
            DatabaseQuery.modifyCustomer_empty(testCustomerID,newCustomerName,cardID);
            assertTrue(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            CustomerClass myCustomer = DatabaseQuery.getAllCustomers().get(testCustomerID);
            assertEquals(myCustomer.getCustomerName(), newCustomerName);
            assertNull(myCustomer.getCustomerCard());
        } catch (SQLException throwables) {
            fail();
        }

    }

    @Test
    public void testModifyCustomer_null() {

        String oldCustomerName = "Piero";
        String cardID = "0000000001";
        String newCustomerName = "Antonio";
        Integer testCustomerID = 1;
        CustomerClass cc = new CustomerClass(oldCustomerName);
        LoyaltyCard lc = new LoyaltyCard(cardID);

        try {
            DatabaseQuery.defineCustomer(cc);
            DatabaseQuery.createCard(lc);
            assertFalse(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            DatabaseQuery.attachCardToCustomer(lc,cc);
            DatabaseQuery.modifyCustomer_null(testCustomerID,newCustomerName);
            assertFalse(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            CustomerClass myCustomer = DatabaseQuery.getAllCustomers().get(testCustomerID);
            assertEquals(myCustomer.getCustomerName(), newCustomerName);
            assertEquals(myCustomer.getCustomerCard(), cardID);
        } catch (SQLException throwables) {
            fail();
        }

    }

    @Test
    public void testModifyCustomer_valid() {

        String oldCustomerName = "Piero";
        String oldCardID = "0000000001";
        String newCardID = "0000000003";
        String newCustomerName = "Antonio";
        Integer testCustomerID = 1;
        CustomerClass cc = new CustomerClass(oldCustomerName);
        LoyaltyCard lc = new LoyaltyCard(oldCardID);
        LoyaltyCard lc2 = new LoyaltyCard(newCardID);

        try {
            DatabaseQuery.defineCustomer(cc);
            DatabaseQuery.createCard(lc);
            DatabaseQuery.createCard(lc2);
            assertFalse(DatabaseQuery.getAllLoyaltyCards().isEmpty());
            DatabaseQuery.attachCardToCustomer(lc,cc);
            DatabaseQuery.modifyCustomer_valid(testCustomerID,newCustomerName,newCardID);
            CustomerClass myCustomer = DatabaseQuery.getAllCustomers().get(testCustomerID);
            LoyaltyCard myCard = DatabaseQuery.getAllLoyaltyCards().get(newCardID);
            assertEquals(myCustomer.getCustomerName(), newCustomerName);
            assertEquals(myCustomer.getCustomerCard(), newCardID);
            assertEquals(myCustomer.getCustomerCard(), newCardID);
            assertEquals(myCard.getCardOwnerID(),testCustomerID);
        } catch (SQLException throwables) {
            fail();
        }

    }

    @Test
    public void testModifyPointsOnCard_cardNotAssociated() {
        String cardID = "0000000001";
        Integer customerID = 1;
        String customerName = "Antonio";
        Integer firstBalance = 0;
        Integer lastBalance = 200;
        LoyaltyCard lc = new LoyaltyCard(cardID);
        try {
            DatabaseQuery.createCard(lc);
            LoyaltyCard myCard = DatabaseQuery.getAllLoyaltyCards().get(cardID);
            assertEquals(firstBalance, myCard.getPoints());
            DatabaseQuery.modifyPointsOnCard(lastBalance, myCard.getID(), null);
            LoyaltyCard myCard2 = DatabaseQuery.getAllLoyaltyCards().get(cardID);
            assertEquals(lastBalance, myCard2.getPoints());

        } catch (SQLException throwables) {
            fail();
        }

    }

    @Test
    public void testModifyPointsOnCard_cardAssociated() {
        String cardID = "0000000001";
        Integer customerID = 1;
        String customerName = "Antonio";
        Integer firstBalance = 0;
        Integer lastBalance = 200;
        CustomerClass cc = new CustomerClass(customerName);
        LoyaltyCard lc = new LoyaltyCard(cardID);
        try {
            DatabaseQuery.createCard(lc);
            LoyaltyCard myCard = DatabaseQuery.getAllLoyaltyCards().get(cardID);
            assertEquals(firstBalance, myCard.getPoints());

            DatabaseQuery.defineCustomer(cc);
            DatabaseQuery.attachCardToCustomer(lc,cc);
            DatabaseQuery.modifyPointsOnCard(lastBalance, cardID, customerID);

            LoyaltyCard myCard2 = DatabaseQuery.getAllLoyaltyCards().get(cardID);
            CustomerClass myCustomer = DatabaseQuery.getAllCustomers().get(customerID);
            assertEquals(lastBalance, myCard2.getPoints());
            assertEquals(myCustomer.getPoints(), myCard2.getPoints());

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testGetNextCardID() {
        try {
            assertEquals(DatabaseQuery.getNextCardID(), 1);
            LoyaltyCard myCard = new LoyaltyCard("0000000001");
            DatabaseQuery.createCard(myCard);
            assertEquals(DatabaseQuery.getNextCardID(), 2);
            LoyaltyCard myCard2 = new LoyaltyCard("0000000005");
            DatabaseQuery.createCard(myCard2);
            assertEquals(DatabaseQuery.getNextCardID(), 6);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testGetAllSaleTransactions() {
        SaleTransactionClass s = new SaleTransactionClass();
        SaleTransactionClass s1 = new SaleTransactionClass();
        SaleTransactionClass s2 = new SaleTransactionClass();

        TicketProduct tp = new TicketProduct("088388484261", rfid, "test", 2, 3.5, 0.0);
        s.addEntry(tp);
        s.setClosed(true);
        s.setPayed(true);

        try {
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createSaleTransaction(s1);
            DatabaseQuery.createSaleTransaction(s2);
            DatabaseQuery.addTicketEntry(s.getBalanceId(), tp);
            DatabaseQuery.updateSaleClosed(s.getBalanceId());
            DatabaseQuery.updateSalePaid(s.getBalanceId());
            assertTrue(DatabaseQuery.getAllSaleTransactions().size() == 3);
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testGetAllTicketEntries() {
        String rfid1 = "000000000001";
        String rfid2 = "000000000002";
        String rfid3 = "000000000003";
        ProductTypeClass p = new ProductTypeClass("test", "188388484268", 3.5, "");
        ProductTypeClass p1 = new ProductTypeClass("test1", "1883884842695", 5, "");
        ProductTypeClass p2 = new ProductTypeClass("test2", "198765432102", 12.29, "");
        SaleTransactionClass s = new SaleTransactionClass();
        SaleTransactionClass s1 = new SaleTransactionClass();
        SaleTransactionClass s2 = new SaleTransactionClass();
        //products and transactions needed for foreign keys
        TicketProduct tp = new TicketProduct("188388484268", rfid1, "test", 2, 3.5, 0.0);
        TicketProduct tp2 = new TicketProduct("1883884842695", "test1", 2, 5, 0.0);
        TicketProduct tp3 = new TicketProduct("198765432102", "test2", 1, 12.29, 0.0);

        tp2.addRFID(rfid2);
        try {
            DatabaseQuery.createProductType(p);
            DatabaseQuery.createProductType(p1);
            DatabaseQuery.createProductType(p2);
            DatabaseQuery.createSaleTransaction(s);
            DatabaseQuery.createSaleTransaction(s1);
            DatabaseQuery.createSaleTransaction(s2);
            DatabaseQuery.addTicketEntryRFID(s.getBalanceId(), tp, rfid1);
            DatabaseQuery.addTicketEntry(s1.getBalanceId(), tp3);
            DatabaseQuery.addTicketEntryRFID(s2.getBalanceId(), tp2, rfid2);
            tp2.addRFID(rfid3);
            DatabaseQuery.addTicketEntryRFID(s2.getBalanceId(), tp2, rfid3);
            List<TicketEntry> sale = DatabaseQuery.getAllTicketEntries(s.getBalanceId());
            assertEquals(tp.getBarCode(), sale.get(0).getBarCode());
            assertEquals(1, sale.size());
            assertEquals(1, ((TicketProduct) sale.get(0)).getRfid().size());
            assertTrue(((TicketProduct) sale.get(0)).getRfid().get(0).equals(rfid1));
            List<TicketEntry> sale1 = DatabaseQuery.getAllTicketEntries(s1.getBalanceId());
            assertEquals(tp3.getBarCode(), sale1.get(0).getBarCode());
            List<TicketEntry> sale2 = DatabaseQuery.getAllTicketEntries(s2.getBalanceId());
            assertEquals(tp2.getBarCode(), sale2.get(0).getBarCode());
            assertEquals(1, sale2.size());
            assertEquals(2, ((TicketProduct) sale2.get(0)).getRfid().size());
            String one, two;
            one = ((TicketProduct) sale2.get(0)).getRfid().get(0);
            two = ((TicketProduct) sale2.get(0)).getRfid().get(1);
            assertTrue(one.equals(rfid2));
            assertTrue(two.equals(rfid3));
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testGetAllBalanceOperationsBetweenDates() {
        BalanceOperationClass balanceOperationClass = new BalanceOperationClass();
        balanceOperationClass.setType(BalanceOperationClass.TYPE_DEBIT);
        balanceOperationClass.setMoney(222);
        BalanceOperationClass balanceOperationClass1 = new BalanceOperationClass();
        balanceOperationClass1.setType(BalanceOperationClass.TYPE_DEBIT);
        balanceOperationClass1.setMoney(222);
        BalanceOperationClass balanceOperationClass2 = new BalanceOperationClass();
        balanceOperationClass2.setType(BalanceOperationClass.TYPE_DEBIT);
        balanceOperationClass2.setMoney(222);

        try {
            DatabaseQuery.createBalanceTransaction(balanceOperationClass);
            DatabaseQuery.createBalanceTransaction(balanceOperationClass1);
            DatabaseQuery.createBalanceTransaction(balanceOperationClass2);
            List<BalanceOperation> fromDb = DatabaseQuery.getAllBalanceOperationsBetweenDates(null, null);

            assertFalse(fromDb.isEmpty());
            assertEquals(3, fromDb.size());
            fromDb = DatabaseQuery.getAllBalanceOperationsBetweenDates(LocalDate.MAX, LocalDate.MAX);
            assertTrue(fromDb.isEmpty());


            fromDb = DatabaseQuery.getAllBalanceOperationsBetweenDates(null, LocalDate.MIN);
            assertTrue(fromDb.isEmpty());


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetAllOrders() {
        OrderClass o = new OrderClass("088388484261", 1.5, 10, "ISSUED");
        OrderClass o1 = new OrderClass("109876543212", 2.5, 10, "PAYED");
        OrderClass o2 = new OrderClass("123456789104", 15.99, 10, "COMPLETED");

        try {
            DatabaseQuery.createOrder(o);
            DatabaseQuery.createOrder(o1);
            DatabaseQuery.createOrder(o2);
            assertEquals(3, DatabaseQuery.getAllOrders().size());
            assertTrue(DatabaseQuery.getAllOrders().containsKey(o.getOrderId()));
            assertTrue(DatabaseQuery.getAllOrders().containsKey(o1.getOrderId()));
            assertTrue(DatabaseQuery.getAllOrders().containsKey(o2.getOrderId()));
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testGetBalanceQuantity() {
        try {
            double quantity = DatabaseQuery.getBalanceQuantity();
            assertEquals(0 , quantity, 0.1);
            DatabaseQuery.setBalanceQuantity(666);

            quantity = DatabaseQuery.getBalanceQuantity();
            assertEquals(666 , quantity, 0.1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSetBalanceQuantity() {
        try {
            double quantity = DatabaseQuery.getBalanceQuantity();
            assertEquals(0 , quantity, 0.1);
            DatabaseQuery.setBalanceQuantity(666);

            quantity = DatabaseQuery.getBalanceQuantity();
            assertEquals(666 , quantity, 0.1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            fail();
        }
    }

    @Test
    public void testAddTicketEntryRFID() {

        TicketProduct tp = new TicketProduct("088388484261", "test", 2, 3.5, 0.0);

        try {
            // if (tmp == false) -> return FALSE
            assertFalse(DatabaseQuery.addTicketEntryRFID(23, tp, "000000001000"));
            // if (tmp == true && affectedRows > 0) -> return TRUE
            tp.addRFID("000000003000");
            assertTrue(DatabaseQuery.addTicketEntryRFID(24, tp, "000000002000"));
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testdeleteTicketEntryRFID() {

        TicketProduct tp = new TicketProduct("088388484261", "test", 2, 3.5, 0.0);

        try {
            // if (affectedRows == 0) -> return FALSE
            assertFalse(DatabaseQuery.deleteTicketEntryRFID(30, tp, "000000001000"));
            // if (tmp == false) -> return FALSE
            DatabaseQuery.addTicketEntryRFID(30, tp, "000000001000");
            assertFalse(DatabaseQuery.deleteTicketEntryRFID(30, tp, "000000001000"));
            // if (tmp == true && affectedRows > 0) -> return TRUE
            DatabaseQuery.addTicketEntry(30, tp);
            DatabaseQuery.addTicketEntryRFID(30, tp, "000000001000");
            assertTrue(DatabaseQuery.deleteTicketEntryRFID(30, tp, "000000001000"));
        } catch (SQLException throwables) {
            fail();
        }
    }

    @Test
    public void testUpdateRFIDBarcodeStatus() {

        Product p =  new Product("000000001000", "123456789012", "Inventory");
        TicketProduct tp = new TicketProduct("088388484261", "test", 2, 3.5, 0.0);
        tp.addRFID("000000001000");

        try {
            // if (affectedRows == 0) -> return FALSE
            assertFalse(DatabaseQuery.updateRFIDBarcodeStatus(p, "Sold"));
            // if (affectedRows > 0) -> return TRUE
            assertTrue(DatabaseQuery.createRFIDBarcodeLink(p));
            assertTrue(DatabaseQuery.updateRFIDBarcodeStatus(p, "Sold"));
        } catch (SQLException throwables) {
            fail();
        }
    }

}