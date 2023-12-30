package it.polito.ezshop.utils;

import it.polito.ezshop.data.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.Map;

import static it.polito.ezshop.data.BalanceOperationClass.TYPE_DEBIT;


public class DatabaseQuery {
//    public synchronized static ArrayList<BalanceOperation> get_balance_operations(){
//        ResultSet r = DbManagerClass.FetchData("SELECT * FROM `balance_operations`;");
//        ArrayList<BalanceOperation> operations = new ArrayList<>();
//        try
//        {
//            while (r.next())
//            {
//                BalanceOperation operation = new BalanceOperationClass();
//                operation.setBalanceId(r.getInt("id"));
//                operation.setDate(LocalDate.parse(r.getString("date")));
//                operation.setMoney(r.getLong("money"));
//                operation.setType(r.getString("type"));
//            }
//        } catch (SQLException e)
//        {
//            System.out.println(e);
//        }
//        return operations;
//    }

    public synchronized static Map<Integer, UserClass> get_users() throws SQLException {

        Statement stmt = DbManagerClass.conn.createStatement();
        UserClass usr;
        Map<Integer, UserClass> users = new TreeMap<>();
        ResultSet rs = stmt.executeQuery("SELECT * FROM users");

        while (rs.next()) {
            usr = new UserClass(rs.getString(2), rs.getString(3), rs.getString(4));
            usr.setId(rs.getInt(1));
            users.put(usr.getId(), usr);
        }
        stmt.close();
        return users;
    }

    public synchronized static void createUser(UserClass user) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into users (username, password, role) values (?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1,user.getUsername());
        stmt.setString(2,user.getPassword());
        stmt.setString(3,user.getRole());

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();

        if (rs.next()) {
            user.setId(rs.getInt(1));
        }
    }

    public synchronized static boolean deleteUser(Integer id) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "delete from users where id = ?"
        );
        stmt.setInt(1, id);
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static boolean updateUserRole(Integer id, String role) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE users SET role = ? WHERE id = ?"
        );

        // set the corresponding param
        stmt.setString(1,role);
        stmt.setInt(2, id);

        // update
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static User getUserByUsername (String username) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "SELECT * FROM users WHERE username=?"
        );

        User usr = null;

        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            usr = new UserClass(rs.getString(2), rs.getString(3), rs.getString(4));
            usr.setId(rs.getInt(1));
            stmt.close();
            return usr;
        }
        return null;
    }

//    public static boolean deleteAllUsers() throws SQLException {
//        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
//                "delete from users"
//        );
//        int changes = stmt.executeUpdate();
//        return changes > 0;
//    }

    public synchronized static Map<Integer, ProductTypeClass> get_products() throws SQLException {
        Statement stmt = DbManagerClass.conn.createStatement();
        ProductTypeClass prd;
        Map<Integer, ProductTypeClass> products = new HashMap<>();
        ResultSet rs = stmt.executeQuery("SELECT * FROM products");

                while (rs.next()) {
                    prd = new ProductTypeClass(rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getString(5));
                    prd.setQuantity(rs.getInt(6));
                    prd.setLocation(rs.getString(7));
                    prd.setId(rs.getInt(1));
                    products.put(prd.getId(), prd);
                }


        stmt.close();
        return products;
    }

    public synchronized static void createProductType(ProductTypeClass productType) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into products (productDescription, barCode, pricePerUnit, note) values (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1,productType.getProductDescription());
        stmt.setString(2,productType.getBarCode());
        stmt.setDouble(3,productType.getPricePerUnit());
        stmt.setString(4,productType.getNote());

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            productType.setId(rs.getInt(1));
        }
    }
    public synchronized static boolean updateProductType(Integer id, ProductTypeClass product) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE products SET (productDescription, barCode, pricePerUnit, note) = (?,?,?,?) WHERE id = ?"
        );

        // set the corresponding param
        stmt.setString(1, product.getProductDescription());
        stmt.setString(2,product.getBarCode());
        stmt.setDouble(3,product.getPricePerUnit());
        stmt.setString(4,product.getNote());
        stmt.setInt(5, id);

        // update
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static boolean updateQuantity(Integer id, int quantity) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE products SET quantity = ? WHERE id = ?"
        );

        // set the corresponding param
        stmt.setInt(1, quantity);
        stmt.setInt(2, id);

        // update
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }
    public synchronized static boolean updatePosition(Integer id, String position) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE products SET location = ? WHERE id = ?"
        );

        // set the corresponding param
        stmt.setString(1, position);
        stmt.setInt(2, id);

        // update
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static boolean deleteProductType(Integer id) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "delete from products where id = ?"
        );
        stmt.setInt(1, id);
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

//    public synchronized static boolean deleteAllProducts() throws SQLException {
//        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
//                "delete from products"
//        );
//        int changes = stmt.executeUpdate();
//        return changes > 0;
//    }

    public synchronized static void createOrder(OrderClass order) throws SQLException {
        PreparedStatement stmt1 = DbManagerClass.conn.prepareStatement(
                "insert into balance_operations (date, type, product_code_O, quantity_O, price_per_unit, order_status, transactionClass) values (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt1.setString(1, order.getDate().toString());
        stmt1.setString(2, TYPE_DEBIT);
        stmt1.setString(3,order.getProductCode());
        stmt1.setInt(4,order.getQuantity());
        stmt1.setDouble(5,order.getPricePerUnit());
        stmt1.setString(6,order.getStatus());
        stmt1.setString(7,"OrderClass");

        stmt1.executeUpdate();
        ResultSet rs = stmt1.getGeneratedKeys();
        if (rs.next()) {
            order.setBalanceId(rs.getInt(1));
        }

        PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
                "insert into orders (date, type, product_code, quantity, price_per_unit, status, balance_id) values (?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);

        stmt2.setString(1, order.getDate().toString());
        stmt2.setString(2, TYPE_DEBIT);
        stmt2.setString(3,order.getProductCode());
        stmt2.setInt(4,order.getQuantity());
        stmt2.setDouble(5,order.getPricePerUnit());
        stmt2.setString(6,order.getStatus());
        stmt2.setInt(7,order.getBalanceId());

        stmt2.executeUpdate();
        ResultSet rs2 = stmt2.getGeneratedKeys();
        if (rs2.next()){
            order.setOrderId(rs.getInt(1));
        }

    }

    public synchronized static boolean updateOrderStatus(Integer id, String status) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE orders SET status = ? WHERE balance_id = ?"
        );

        PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
                "UPDATE balance_operations SET order_status = ? WHERE balance_id = ? AND transactionClass = ?"
        );

        // set the corresponding param
        stmt.setString(1, status);
        stmt.setInt(2, id);

        stmt2.setString(1, status);
        stmt2.setInt(2, id);
        stmt2.setString(3, "OrderClass");

        // update
        int affectedRows = stmt.executeUpdate();
        int affectedRows2 = stmt2.executeUpdate();

        return (affectedRows != 0 && affectedRows2 != 0);
    }

//    public synchronized static boolean deleteAllOrders() throws SQLException {
//        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
//                "delete from orders"
//        );
//        PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
//                "delete from balance_operations where transactionClass = 'OrderClass'"
//        );
//        int changes = stmt.executeUpdate();
//        int ch2 = stmt2.executeUpdate();
//        return changes > 0 && ch2 > 0;
//    }

    public synchronized static void createSaleTransaction(SaleTransactionClass sale) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into balance_operations (date, money, type, ticket_number, discount_rate, isClosed, isPayed, transactionClass) values (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1, String.valueOf(sale.getDate()));
        stmt.setDouble(2,sale.getMoney());
        stmt.setString(3,sale.getType());
        stmt.setInt(4,sale.getTicketNumber());
        stmt.setDouble(5,sale.getDiscountRate());
        stmt.setBoolean(6,sale.isClosed());
        stmt.setBoolean(7,sale.isPayed());
        stmt.setString(8, "SaleTransactionClass");

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();

        if (rs.next()) {
            sale.setBalanceId(rs.getInt(1));
        }

    }

    public synchronized static boolean addTicketEntry(Integer balance_id, TicketProduct t) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into ticketEntries (sale_id, barcode, amount, price, discount, productDescription) values (?,?,?,?,?,?)");

        // set the corresponding param
        stmt.setInt(1, balance_id);
        stmt.setString(2, t.getBarCode());
        stmt.setInt(3, t.getAmount());
        stmt.setDouble(4, t.getPricePerUnit());
        stmt.setDouble(5, t.getDiscountRate());
        stmt.setString(6, t.getProductDescription());

        //add entry
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static boolean addTicketEntryRFID(Integer balance_id, TicketProduct tp, String RFID) throws SQLException {

        boolean tmp;
        if (tp.getRfid().size() == 1) {//se ce n'è uno solo vuol dire che devo anche aggiungerlo a ticketEntries
            tmp = addTicketEntry(balance_id, tp);
        }
        else {  //devo aggiornare l'amount
            tmp = updateTicketEntryAmount(tp.getAmount() + 1, balance_id, tp.getBarCode());
        }

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into saleRFID (sale_id, barcode, RFID) values (?,?,?)");
        stmt.setInt(1, balance_id);
        stmt.setString(2, tp.getBarCode());
        stmt.setString(3, RFID);

        int affectedRows = stmt.executeUpdate();

        return affectedRows > 0 && tmp;
    }


    public synchronized static boolean deleteTicketEntry(Integer balance_id, TicketProduct t) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "delete from ticketEntries where sale_id = ? and barcode = ?"
        );

        PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
                "delete from saleRFID where sale_id = ? and barcode = ?"
        );

        stmt.setInt(1, balance_id);
        stmt.setString(2, t.getBarCode());

        int affectedRows = stmt.executeUpdate();
        stmt2.executeUpdate();
        return affectedRows > 0;
    }

    public synchronized static boolean deleteTicketEntryRFID(Integer balance_id, TicketProduct t, String RFID) throws SQLException {

        boolean tmp = updateTicketEntryAmount(t.getAmount() - 1, balance_id, t.getBarCode());
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "delete from saleRFID where sale_id = ? and RFID = ?"
        );

        stmt.setInt(1, balance_id);
        stmt.setString(2, RFID);

        int affectedRows = stmt.executeUpdate();
        return affectedRows > 0 && tmp;
    }

    public synchronized static boolean updateTicketEntryAmount(Integer newAmount, Integer balance_id, String barcode) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "update ticketEntries set amount = ? where sale_id = ? and barcode = ?"
        );

        stmt.setInt(1, newAmount);
        stmt.setInt(2, balance_id);
        stmt.setString(3, barcode);

        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static boolean updateTicketEntryDiscount(Double discount, Integer balance_id, TicketProduct t) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "update ticketEntries set discount = ? where sale_id = ? and barcode = ?"
        );

        stmt.setDouble(1, discount);
        stmt.setInt(2, balance_id);
        stmt.setString(3, t.getBarCode());

        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static boolean updateSaleDiscountRate(Integer id, double discount) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE balance_operations SET discount_rate = ? WHERE balance_id = ?"
        );
        // set the corresponding param
        stmt.setDouble(1, discount);
        stmt.setInt(2, id);
        // update
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static boolean updateSaleClosed(Integer id) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE balance_operations SET isClosed = ? WHERE balance_id = ?"
        );
        // set the corresponding param
        stmt.setBoolean(1, true);
        stmt.setInt(2, id);
        // update
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static boolean updateSalePaid(Integer id) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE balance_operations SET isPayed = ? WHERE balance_id = ?"
        );
        // set the corresponding param
        stmt.setBoolean(1, true);
        stmt.setInt(2, id);
        // update
        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

//    public synchronized static boolean deleteAllBalanceOperations() throws SQLException {
//        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
//                "delete from balance_operations"
//        );
//        int changes = stmt.executeUpdate();
//        return changes > 0;
//    }

    public synchronized static void createReturnTransaction(ReturnTransactionClass returnTransactionClass) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into balance_operations (date, type, transactionClass, parentTransactionId) values (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1, String.valueOf(returnTransactionClass.getDate()));
        stmt.setString(2, returnTransactionClass.getType());
        stmt.setString(3, "ReturnTransactionClass");
        stmt.setInt(4, returnTransactionClass.getParentTransactionId());
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();

        if (rs.next()) {
            returnTransactionClass.setBalanceId(rs.getInt(1));
        }

    }

    public synchronized static boolean updateReturnTransaction(ReturnTransactionClass returnTransactionClass) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "update balance_operations SET date = ?, type = ?, transactionClass = ?, parentTransactionId = ?, isPayed = ?, isClosed = ?  where balance_id=?");

        stmt.setString(1, String.valueOf(returnTransactionClass.getDate()));
        stmt.setString(2, returnTransactionClass.getType());
        stmt.setString(3, "ReturnTransactionClass");
        stmt.setInt(4, returnTransactionClass.getParentTransactionId());
        stmt.setBoolean(5, returnTransactionClass.isPayed());
        stmt.setBoolean(6, returnTransactionClass.isClosed());
        stmt.setInt(7, returnTransactionClass.getBalanceId());

        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static void createBalanceTransaction(BalanceOperationClass balanceOperationClass) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into balance_operations (date, type, transactionClass, money) values (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1, String.valueOf(balanceOperationClass.getDate()));
        stmt.setString(2, balanceOperationClass.getType());
        stmt.setString(3, "BalanceOperationClass");
        stmt.setDouble(4, balanceOperationClass.getMoney());
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();

        if (rs.next()) {
            balanceOperationClass.setBalanceId(rs.getInt(1));
        }
    }
    public synchronized static boolean addProductToReturnTransaction(Integer transactionId, Integer productId, Integer amount) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into returns_products (balance_id, product_id, product_amount) values (?,?,?)");

        stmt.setInt(1, transactionId);
        stmt.setInt(2, productId);
        stmt.setInt(3, amount);

        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }


    public synchronized static boolean addProductToReturnTransactionRFID(Integer transactionId, String productRFID) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into returns_productsRFID (balance_id, product_rfid) values (?,?)");

        stmt.setInt(1, transactionId);
        stmt.setString(2, productRFID);

        int affectedRows = stmt.executeUpdate();
        return affectedRows != 0;
    }

    public synchronized static Map<Integer, Integer> getProductsOfTransaction(Integer transactionId) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "select * from returns_products where balance_id=?");

        stmt.setInt(1, transactionId);

        ResultSet rs = stmt.executeQuery();

        Map<Integer, Integer> toReturn = new HashMap<>();
        while (rs.next()){
            toReturn.put(rs.getInt("product_id"), rs.getInt("product_amount"));
        }
        return toReturn;
    }
    public synchronized static void deleteReturnTransaction(Integer returnId) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("DELETE from balance_operations where balance_id=? and transactionClass=?");

        stmt.setInt(1, returnId);
        stmt.setString(2, "ReturnTransactionClass");

        stmt.executeUpdate();
    }

    public synchronized static boolean deleteSaleTransaction(Integer balanceId) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("DELETE from balance_operations where balance_id=? and transactionClass=?");

        stmt.setInt(1, balanceId);
        stmt.setString(2, "SaleTransactionClass");

        stmt.executeUpdate();
        return true;
    }

    public synchronized static Map<Integer, ReturnTransactionClass> getAllReturnTransactions() throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM balance_operations where transactionClass is 'ReturnTransactionClass'");

        ResultSet rs = stmt.executeQuery();

        Map<Integer, ReturnTransactionClass> returns = new HashMap<>();
        while ( rs.next() ) {
            ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(
                    rs.getInt("balance_id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("type"),
                    rs.getInt("parentTransactionId")
            );
            returnTransactionClass.setClosed(rs.getBoolean("isClosed"));
            returnTransactionClass.setPayed(rs.getBoolean("isPayed"));
            returns.put(returnTransactionClass.getBalanceId(), returnTransactionClass);
        }

        return returns;
    }

    public synchronized static Map<Integer, CustomerClass> getAllCustomers() throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM customers");

        ResultSet rs = stmt.executeQuery();

        Map<Integer, CustomerClass> customers = new HashMap<>();
        while ( rs.next() ) {
            CustomerClass customer = new CustomerClass(
                    rs.getInt("id"),
                    rs.getInt("points"),
                    rs.getString("name"),
                    rs.getString("customerCard")
            );
            customers.put(customer.getId(), customer);
        }

        return customers;
    }

    public static Map<String, LoyaltyCard> getAllLoyaltyCards() throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM loyalty_cards");

        ResultSet rs = stmt.executeQuery();

        Map<String, LoyaltyCard> loyaltyCards = new HashMap<>();
        while ( rs.next() ) {
            LoyaltyCard loyaltyCard = new LoyaltyCard(
                    rs.getString("ID"),
                    rs.getInt("points"),
                    rs.getInt("cardOwnerID")
            );
            loyaltyCards.put(loyaltyCard.getID(), loyaltyCard);
        }

        return loyaltyCards;
    }

    // CUSTOMERS, CARDS

    public synchronized static boolean defineCustomer(CustomerClass customer) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into customers (name, points, customerCard) values (?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1,customer.getCustomerName());
        stmt.setInt(2,customer.getPoints());
        stmt.setString(3,customer.getCustomerCard());

        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();

        if (rs.next()) {
            customer.setId(rs.getInt(1));
        }
        return true;

    }

    public synchronized static boolean deleteCustomer(Integer id) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "delete from customers where id = ?"
        );
        stmt.setInt(1, id);
        stmt.executeUpdate();
        return true;
    }

    public synchronized static void createCard(LoyaltyCard loyaltyCard) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into loyalty_cards (id, points, cardOwnerID) values (?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1,loyaltyCard.getID());
        stmt.setInt(2,loyaltyCard.getPoints());
        stmt.setInt(3,loyaltyCard.getCardOwnerID());

        stmt.executeUpdate();

    }

    public synchronized static boolean attachCardToCustomer(LoyaltyCard loyaltyCard, Customer customer) throws SQLException {

        PreparedStatement stmt1 = DbManagerClass.conn.prepareStatement(
                "UPDATE customers SET customerCard = ? WHERE id = ?"
        );
        stmt1.setString(1, loyaltyCard.getID());
        stmt1.setInt(2, customer.getId());
        stmt1.executeUpdate();

        PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
                "UPDATE loyalty_cards SET cardOwnerID = ? WHERE id = ?"
        );
        stmt2.setInt(1, customer.getId());
        stmt2.setString(2, loyaltyCard.getID());
        stmt2.executeUpdate();

        return true;

    }

    public synchronized static boolean modifyCustomer_valid(Integer id, String newCustomerName, String newCustomerCard) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE customers SET customerCard = ?, name = ? WHERE id = ?"
        );
        stmt.setString(1, newCustomerCard);
        stmt.setString(2, newCustomerName);
        stmt.setInt(3, id);
        stmt.executeUpdate();

        PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
                "UPDATE loyalty_cards SET cardOwnerID = ? WHERE id = ?"
        );
        stmt2.setInt(1, id);
        stmt2.setString(2, newCustomerCard);
        stmt2.executeUpdate();

        return true;

    }

    public synchronized static boolean modifyCustomer_null(Integer id, String newCustomerName) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE customers SET name = ? WHERE id = ?"
        );
        stmt.setString(1, newCustomerName);
        stmt.setInt(2, id);
        stmt.executeUpdate();

        return true;

    }

    public synchronized static boolean modifyCustomer_empty(Integer id, String newCustomerName, String oldCard) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "UPDATE customers SET customerCard = ?, name = ? WHERE id = ?"
        );
        stmt.setString(1, null);
        stmt.setString(2, newCustomerName);
        stmt.setInt(3, id);
        stmt.executeUpdate();

        PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
                "delete from loyalty_cards where id = ?"
        );
        stmt2.setString(1, oldCard);
        stmt2.executeUpdate();

        return true;

    }

    public synchronized static boolean modifyPointsOnCard(int newPoints, String customerCard, Integer ownerID) throws SQLException {

        if(ownerID != null) {
            PreparedStatement stmt1 = DbManagerClass.conn.prepareStatement(
                    "UPDATE customers SET points = ? WHERE id = ?"
            );
            stmt1.setInt(1, newPoints);
            stmt1.setInt(2, ownerID);
            stmt1.executeUpdate();

        }


        PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
                "UPDATE loyalty_cards SET points = ? WHERE id = ?"
        );
        stmt2.setInt(1, newPoints);
        stmt2.setString(2, customerCard);

        stmt2.executeUpdate();

        return true;

    }

    public synchronized static int getNextCardID() throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("SELECT max(id) FROM loyalty_cards");
        ResultSet rs = stmt.executeQuery();

        if(rs.getString(1)!=null) {
            return Integer.parseInt(rs.getString(1))+1;
        }

        else return 1;

    }

    public synchronized static Map<Integer, SaleTransactionClass> getAllSaleTransactions() throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM balance_operations where transactionClass is 'SaleTransactionClass'");

        ResultSet rs = stmt.executeQuery();

        Map<Integer, SaleTransactionClass> sales = new HashMap<>();
        while ( rs.next() ) {
            SaleTransactionClass sale = new SaleTransactionClass(
                    rs.getInt("balance_id"),
                    rs.getString("type"),
                    rs.getDouble("money"),
                    rs.getString("date"),
                    rs.getInt("ticket_number"),
                    rs.getBoolean("isClosed"),
                    rs.getBoolean("isPayed"),
                    rs.getDouble("discount_rate")
            );
            sales.put(sale.getTicketNumber(), sale);
        }

        return sales;
    }

    public synchronized static List<TicketEntry> getAllTicketEntries(Integer balance_id) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "SELECT * FROM ticketEntries where sale_id is ?");

        PreparedStatement s = DbManagerClass.conn.prepareStatement(
                "SELECT * FROM saleRFID where sale_id is ? and barcode is ?");

        stmt.setInt(1, balance_id);
        s.setInt(1, balance_id);

        ResultSet rs = stmt.executeQuery();

        List<TicketEntry> sales = new ArrayList<>();
        while ( rs.next() ) {
            TicketProduct product = new TicketProduct(
                    rs.getString("barcode"),
                    rs.getString("productDescription"),
                    rs.getInt("amount"),
                    rs.getDouble("price"),
                    rs.getDouble("discount")
            );
            s.setString(2, product.getBarCode());
            ResultSet res = s.executeQuery();
            while ( res.next() ){
                product.addRFID(res.getString("RFID"));
            }
            sales.add(product);
        }

        return sales;
    }

    public synchronized static List<BalanceOperation> getAllBalanceOperationsBetweenDates(LocalDate from, LocalDate to) throws SQLException {
        PreparedStatement stmt = null;
        if(from == null && to == null){
            stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM balance_operations");

        }
        else if(from != null && to != null) {
            stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM balance_operations " +
                    "where date >= ? and date <= ?");

            stmt.setString(1, String.valueOf(from));
            stmt.setString(2, String.valueOf(to));
        }
        else if(from != null && to == null){
            stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM balance_operations " +
                    "where date >= ?");

            stmt.setString(1, String.valueOf(from));
        }else if(from == null && to != null){
            stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM balance_operations " +
                    "where date <= ?");

            stmt.setString(1, String.valueOf(to));
        }
        ResultSet rs = stmt.executeQuery();

        List<BalanceOperation> operations = new ArrayList<BalanceOperation>();
        while ( rs.next() ) {
            BalanceOperationClass operation = new BalanceOperationClass();
            operation.setBalanceId(rs.getInt("balance_id"));
            operation.setMoney(rs.getDouble("money"));
            operation.setType(rs.getString("type"));
            operation.setDate(LocalDate.parse(rs.getString("date")));
            operations.add(operation);
        }

        return operations;
    }

    public synchronized static Map<Integer, OrderClass> getAllOrders() throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM orders");

        ResultSet rs = stmt.executeQuery();

        Map<Integer, OrderClass> orders = new HashMap<>();
        while ( rs.next() ) {
            OrderClass order = new OrderClass(
                    rs.getInt("balance_id"),
                    rs.getString("date"),
                    rs.getString("product_code"),
                    rs.getDouble("price_per_unit"),
                    rs.getInt("quantity"),
                    rs.getString("status"),
                    rs.getInt("order_id")

            );
            orders.put(order.getOrderId(), order);
        }

        return orders;
    }

    public synchronized static Map<String, Product> getAllRFIDBarcodeLinks() throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM RFIDBarcodeLink");
        ResultSet rs = stmt.executeQuery();

        Map<String, Product> RFIDBarcodeLinks = new HashMap<>();
        while ( rs.next() ) {
            Product link = new Product(
                    rs.getString("RFID"),
                    rs.getString("barcode"),
                    rs.getString("status")
            );

            RFIDBarcodeLinks.put(link.getRfid(), link);
        }

        return RFIDBarcodeLinks;
    }

    public synchronized static boolean createRFIDBarcodeLink(Product link) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "insert into RFIDBarcodeLink (RFID, barcode, status) values (?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1, link.getRfid());
        stmt.setString(2, link.getProductCode());
        stmt.setString(3, link.getStatus());

        int affected = stmt.executeUpdate();

        return affected > 0;
    }

    public synchronized static boolean updateRFIDBarcodeStatus(Product link, String status) throws SQLException {

        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "update RFIDBarcodeLink set status = ? where RFID is ?");

        stmt.setString(1, status);
        stmt.setString(2, link.getRfid());

        int affectedRows = stmt.executeUpdate();

        return affectedRows > 0;
    }




    public synchronized static double getBalanceQuantity() throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement("SELECT * FROM quantity_variables where variable_name is 'currentBalance'");

        ResultSet rs = stmt.executeQuery();

        double currentBalance = 0;
        if ( rs.next() ) {
            currentBalance = rs.getDouble("quantity");
        }

        return currentBalance;
    }

    public synchronized static void setBalanceQuantity(double quantity) throws SQLException {
        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
                "update quantity_variables set quantity = ? where variable_name is 'currentBalance'");

        stmt.setDouble(1, quantity);
        stmt.executeUpdate();

        return;
    }




//   public synchronized static boolean modifyPointsOnCard(int newPoints, String customerCard, int ownerID) throws SQLException {

//       PreparedStatement stmt1 = DbManagerClass.conn.prepareStatement(
//               "UPDATE customers SET points = ? WHERE id = ?"
//       );
//       stmt1.setInt(1, newPoints);
//       stmt1.setInt(2, ownerID);

//       PreparedStatement stmt2 = DbManagerClass.conn.prepareStatement(
//               "UPDATE loyalty_cards SET points = ? WHERE id = ?"
//       );
//       stmt2.setInt(1, newPoints);
//       stmt2.setString(2, customerCard);

//       stmt1.executeUpdate();
//       stmt2.executeUpdate();

//       return true;

//   }


//    public synchronized static void createReturnTransaction(SaleTransactionClass sale) throws SQLException {
//        PreparedStatement stmt = DbManagerClass.conn.prepareStatement(
//                "insert into sale (product_code, price, quantity, status) values (?,?,?,?)",
//                Statement.RETURN_GENERATED_KEYS);
//
//        stmt.setString(1,order.getProductCode());
//        stmt.setDouble(2,order.getPricePerUnit());
//        stmt.setInt(3,order.getQuantity());
//        stmt.setString(4,order.getStatus());
//
//        stmt.executeUpdate();
//
//        ResultSet rs = stmt.getGeneratedKeys();
//
//        if (rs.next()) {
//            order.setOrderId(rs.getInt(1));
//        }
//
//    }

}
