package it.polito.ezshop.utils;

import java.sql.*;

public class DbManagerClass {

    static Statement stmt = null;

    public static Connection getConn() {
        return conn;
    }

    static Connection conn = null;

    public static void connect(String fileName) {

        String url = "jdbc:sqlite:sqlite/db/" + fileName;

        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                stmt = conn.createStatement();
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("Database is connected");
            }

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void disconnect() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                System.out.println("Database is disconnected");
            }
        }
    }

    public static void initDbSchema() {
        // SQL statement for creating a new table
        String users = "CREATE TABLE IF NOT EXISTS `users` ("
                + "`id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
                + "`username` VARCHAR NOT NULL UNIQUE , "
                + "`password` VARCHAR NOT NULL, "
                + "`role` VARCHAR NOT NULL "
                + ");";

        String products = "CREATE TABLE IF NOT EXISTS `products` ("
                + "`id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
                + "`productDescription` VARCHAR NOT NULL, "
                + "`barCode` VARCHAR NOT NULL UNIQUE, "
                + "`pricePerUnit` NUMERIC NOT NULL, "
                + "`note` VARCHAR DEFAULT NULL, "
                + "`quantity` INTEGER DEFAULT NULL, "
                + "`location` VARCHAR DEFAULT NULL "
                + ");";

        String balanceOperation = "create table if not exists balance_operations" +
                "(" +
                "balance_id integer not null primary key autoincrement unique," +
                "    date          VARCHAR   not null," +
                "    money         real default 0," +
                "    type          VARCHAR    not null," +
                "    ticket_number  int," +
                "    order_id       int," +
                "    product_code_O VARCHAR," +
                "    quantity_O     int," +
                "    order_status   VARCHAR," +
                "    discount_rate  real," +
                "    price_per_unit real," +
                "    isClosed      boolean default false," +
                "    isPayed       boolean default false," +
                "    transactionClass VARCHAR," +
                "    parentTransactionId integer" +
                ")";

        String orders = "create table if not exists orders" +
                "(" +
                "order_id           integer not null primary key autoincrement unique," +
                "date               VARCHAR not null," +
                "money              real," +
                "type               VARCHAR not null," +
                "product_code       VARCHAR not null," +
                "quantity           int default 0," +
                "price_per_unit     real," +
                "status             VARCHAR," +
                "balance_id         integer not null," +
                "FOREIGN KEY(balance_id) REFERENCES balance_operations(balance_id)" +
                ");";

        String ticketEntries = "create table if not exists ticketEntries" +
                "(" +
                "sale_id    integer not null," +
                "barcode    VARCHAR not null," +
                "amount     integer," +
                "price      double," +
                "discount   double," +
                "productDescription VARCHAR not null," +
                "FOREIGN KEY(sale_id) REFERENCES balance_operations(balance_id)," +
                "FOREIGN KEY(barcode) REFERENCES products(barCode)" +
                ");";

        String loyaltyCards = "create table if not exists loyalty_cards" +
                "(" +
                "    id            VARCHAR not null primary key unique," +
                "    points        int," +
                "    cardOwnerID   integer" +
                ")";

        String customers = "create table if not exists customers" +
                "(" +
                "    id             integer not null primary key autoincrement unique," +
                "    points         int," +
                "    name           VARCHAR," +
                "    customerCard   VARCHAR" +
                ")";

        String returns_products = "CREATE TABLE IF NOT EXISTS returns_products (" +
                "balance_id INTEGER," +
                "product_id INTEGER," +
                "product_amount integer," +
                "FOREIGN KEY(balance_id) REFERENCES balance_operations(balance_id)," +
                "FOREIGN KEY(product_id) REFERENCES products(id)" +
                ");";

        String returns_productsRFID = "CREATE TABLE IF NOT EXISTS returns_productsRFID (" +
                "balance_id INTEGER," +
                "product_rfid varchar(12)," +
                "FOREIGN KEY(balance_id) REFERENCES balance_operations(balance_id)," +
                "FOREIGN KEY(product_rfid) REFERENCES RFIDBarcodeLink(RFID)" +
                ");";

        String variables = "CREATE TABLE IF NOT EXISTS quantity_variables (" +
                "variable_name varchar unique not null, " +
                "quantity double default 0" +
                ");";

        String addCurrentBalance = "insert into quantity_variables (variable_name) " +
                "values ('currentBalance');";

        String RFIDBarcodeLinks = "create table if not exists RFIDBarcodeLink (" +
                "RFID                   varchar(12) not null PRIMARY KEY unique," +
                "barcode                VARCHAR," +
                "status                 VARCHAR" +
                ");";

        String saleRFID = "create table if not exists SaleRfid (" +
                "sale_id    integer not null," +
                "barcode    VARCHAR not null," +
                "RFID       varchar(12) not null," +
                "FOREIGN KEY(sale_id, barcode) REFERENCES ticketEntries(sale_id, barcode)," +
                "FOREIGN KEY(RFID) REFERENCES RFIDBarcodeLink(RFID)" +
 //               "FOREIGN KEY() REFERENCES ticketEntries(barcode)" +
                ");";
        try {
            stmt.execute(users);
            stmt.execute(balanceOperation);
            stmt.execute(orders);
            stmt.execute(products);
            stmt.execute(ticketEntries);
            stmt.execute(loyaltyCards);
            stmt.execute(customers);
            stmt.execute(returns_productsRFID);
            stmt.execute(returns_products);
            stmt.execute(RFIDBarcodeLinks);
            stmt.execute(saleRFID);
        } catch (Exception e) {
            /* System.out.println("ERRORE BRUTTO"); */
            System.out.println(e);
        }
        try{
            stmt.execute(variables);
            stmt.execute(addCurrentBalance);
        } catch (Exception e){
//            System.out.println(e);
        }
    }

    public static void clearTables() {
        try {
            stmt.execute("DELETE from balance_operations");
            stmt.execute("DELETE from customers");
            stmt.execute("DELETE from loyalty_cards");
            stmt.execute("delete from returns_products");
            stmt.execute("delete from orders");
            stmt.execute("delete from products");
            stmt.execute("delete from quantity_variables");
            stmt.execute("delete from ticketEntries");
            stmt.execute("delete from users");
            stmt.execute("delete from RFIDBarcodeLink");
            stmt.execute("delete from SaleRfid");
            stmt.execute("delete from returns_productsRFID");
            stmt.execute("delete from sqlite_sequence");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
