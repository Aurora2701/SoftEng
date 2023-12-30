package it.polito.ezshop.data;

import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.utils.DatabaseQuery;
import it.polito.ezshop.utils.DbManagerClass;
import it.polito.ezshop.utils.Validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static it.polito.ezshop.data.BalanceOperationClass.TYPE_CREDIT;
import static it.polito.ezshop.data.BalanceOperationClass.TYPE_DEBIT;
import static it.polito.ezshop.data.Product.*;
import static it.polito.ezshop.utils.DatabaseQuery.*;
import static it.polito.ezshop.utils.DbManagerClass.connect;
import static it.polito.ezshop.utils.DbManagerClass.initDbSchema;
import static it.polito.ezshop.utils.Validator.*;

public class EZShop implements EZShopInterface {
    private Map<Integer, OrderClass> orders;
    private Map<Integer, ProductTypeClass> products; //Map<ProductID, productType>
    private Map<Integer, SaleTransactionClass> sales;
    private Map<Integer, ReturnTransactionClass> returns;
    private Map<Integer, BalanceOperationClass> operations;
    private Map<Integer, UserClass> users;
    private Map<String, CreditCardClass> creditCards;
    private Map<String, Product> inventory; //Map<RFID, Product>
    public static final String ROLE_ADMINISTRATOR = "Administrator";
    public static final String ROLE_CASHIER = "Cashier";
    public static final String ROLE_MANAGER = "ShopManager";
    private static final String DB_ERROR_MESSAGE = "[SQL ERROR] DB is unreachable";
    private double currentBalance;
    private static final String SRC_CREDIT_CARDS_SYSTEM = "src/creditCards.txt";
    private static final String DATABASE_NAME = "ezshop.db";

    private User loggedUser;
    private Map<Integer, CustomerClass> customers;
    private Map<String, LoyaltyCard> loyaltyCards;

    public EZShop() {
        connect(DATABASE_NAME);
        initDbSchema();
        this.orders = new HashMap<>();
        this.products = new HashMap<>();
        this.sales = new HashMap<>();
        this.returns = new HashMap<>();
        this.operations = new HashMap<>();
        this.users = new HashMap<>();
        this.customers = new HashMap<>();
        this.loyaltyCards = new HashMap<>();
        this.creditCards = new HashMap<>();
        this.inventory = new HashMap<>();
        this.currentBalance = 0;

        try {
            getDataFromDb();
        } catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
        getCreditCards();
    }

    public static void connectToDb(){
        connect(DATABASE_NAME);
    }

    public void refresh(){
        try {
            getDataFromDb();
        } catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
        getCreditCards();
    }

    public void getDataFromDb() throws SQLException {

        users = DatabaseQuery.get_users();
        products = DatabaseQuery.get_products();
        sales = DatabaseQuery.getAllSaleTransactions();
        currentBalance = DatabaseQuery.getBalanceQuantity();
        customers = DatabaseQuery.getAllCustomers();
        returns = DatabaseQuery.getAllReturnTransactions();
        loyaltyCards = DatabaseQuery.getAllLoyaltyCards();
        inventory = DatabaseQuery.getAllRFIDBarcodeLinks();

        if(!sales.isEmpty()){
            SaleTransactionClass.setSaleCount(sales.keySet().stream().max(Integer::compareTo).get());
            for (SaleTransactionClass s : sales.values()){
                s.setEntries(DatabaseQuery.getAllTicketEntries(s.getBalanceId()));
            }
        }
        if(!returns.isEmpty()){
            for (ReturnTransactionClass ret : returns.values()){
                ret.setProducts(DatabaseQuery.getProductsOfTransaction(ret.getBalanceId()));
            }
        }
        orders = DatabaseQuery.getAllOrders();
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }


    private boolean isValidRole(String role){
        return role.equals(ROLE_ADMINISTRATOR) || role.equals(ROLE_CASHIER) || role.equals(ROLE_MANAGER);
    }

    private boolean doesProductExists(String productCode){
        return products.values().stream().anyMatch(p -> p.getBarCode().equals(productCode));
    }

    private Integer getProductIdFromBarCode(String productCode){
        for (ProductType product : products.values()){
            if(product.getBarCode().equals(productCode)){
                return product.getId();
            }
        }
        return null;
    }

    private String getBarCodeFromProductId(Integer productId){
        if(products.containsKey(productId))
            return products.get(productId).getBarCode();
        return null;
    }

    private void getCreditCards(){
        creditCards = new TreeMap<>();

        File f = new File(SRC_CREDIT_CARDS_SYSTEM);
        if(f!=null) {
            BufferedReader br;
            String line;
            Matcher m;
            CreditCardClass cc;
            Pattern p = Pattern.compile("([0-9]{16})\\;([0-9]+\\.[0-9]+)");
            try {
                br = new BufferedReader(new FileReader(f));
                while((line=br.readLine())!=null){
                    if(line.charAt(0)!='#') {//skip lines which start with '#'
                        m = p.matcher(line);
                        m.find();
                        cc = new CreditCardClass(m.group(1));
                        cc.setCardCredit(Double.parseDouble(m.group(2)));
                        creditCards.put(cc.getCardNumber(),cc);
//                        System.out.println("CARD READ: "+cc.getCardNumber()+", "+cc.getCardCredit());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void reset() {
        try{
            DbManagerClass.clearTables();
            DatabaseQuery.setBalanceQuantity(0);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return;
        }
        this.orders = new HashMap<>();
        this.products = new HashMap<>();
        this.sales = new HashMap<>();
        this.returns = new HashMap<>();
        this.operations = new HashMap<>();
        this.users = new HashMap<>();
        this.customers = new HashMap<>();
        this.loyaltyCards = new HashMap<>();
        this.creditCards = new HashMap<>();
        this.inventory = new HashMap<>();
        this.currentBalance = 0;
    }

    @Override
    public Integer createUser(String username, String password, String role) throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
        if  ( username == null || username.isEmpty())
            throw new InvalidUsernameException();

        if  (password == null || password.isEmpty() )
            throw new InvalidPasswordException();

        if  (role == null || !isValidRole(role))
            throw new InvalidRoleException();

        for(UserClass usr : users.values())
            if (username.equals(usr.getUsername()))
                return -1;

        UserClass user = new UserClass(username, password, role);
        try {
            DatabaseQuery.createUser(user);
            this.users.put(user.getId(), user);
            return user.getId();
        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE); }
        return 0;
    }



    @Override
    public boolean deleteUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        if(id == null || id <= 0  )
            throw new InvalidUserIdException();

        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR))
            throw new UnauthorizedException();


        try {
            if (DatabaseQuery.deleteUser(id)) {
                users.remove(id);
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE);
        }

        return false;
    }

    @Override
    public List<User> getAllUsers() throws UnauthorizedException {
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR))
            throw new UnauthorizedException();

        return new ArrayList<>(users.values());

    }

    @Override
    public User getUser(Integer id) throws InvalidUserIdException, UnauthorizedException {

        if(id == null || id <= 0)
            throw new InvalidUserIdException();

        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR))
            throw new UnauthorizedException();

        return users.get(id);

    }

    @Override
    public boolean updateUserRights(Integer id, String role) throws InvalidUserIdException, InvalidRoleException, UnauthorizedException {
        if (id == null || id <= 0)
            throw new InvalidUserIdException();

        if  (role == null || !isValidRole(role))
            throw new InvalidRoleException();

        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR))
            throw new UnauthorizedException();

        if (users.containsKey(id)){
            try {
                if (DatabaseQuery.updateUserRole(id, role)) {
                    users.get(id).setRole(role);

                    return true;
                }
            } catch (SQLException throwables) {
                System.out.println(DB_ERROR_MESSAGE); }
        }
        return false;
    }

    @Override
    public User login(String username, String password) throws InvalidUsernameException, InvalidPasswordException {

        if  ( username == null || username.isEmpty())
            throw new InvalidUsernameException();

        if  (password == null || password.isEmpty())
            throw new InvalidPasswordException();

        User usr = null;
        try{
            usr = DatabaseQuery.getUserByUsername(username);

        } catch (SQLException e){
            System.out.println(DB_ERROR_MESSAGE);
            return null;
        }
        if (usr != null) {
            if (usr.getPassword().equals(password)) {
                this.loggedUser = usr;
                return loggedUser;
            }
        }
        return null;
    }

    @Override
    public boolean logout() {
        if (loggedUser == null )
            return false;

        loggedUser = null;
        return true;
    }

    @Override
    public Integer createProductType(String description, String productCode, double pricePerUnit, String note) throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {

        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER))
            throw new UnauthorizedException();

        if  ( description == null || description.isEmpty())
            throw new InvalidProductDescriptionException();

        if (!checkBarCode(productCode))
            throw new InvalidProductCodeException();

        if  (pricePerUnit<=0)
            throw new InvalidPricePerUnitException();

        // check barcode unique
        for(ProductTypeClass prd : products.values())
            if (productCode.equals(prd.getBarCode()))
                return -1;

        ProductTypeClass product = new ProductTypeClass(description, productCode, pricePerUnit, note);
        try {
            DatabaseQuery.createProductType(product);
            this.products.put(product.getId(), product);
            return product.getId();
        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE);
        }
        return -1;
    }

    @Override
    public boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote) throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {

        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER))
            throw new UnauthorizedException();

        if(id == null || id <= 0)
            throw new InvalidProductIdException();

        if  ( newDescription == null || newDescription.isEmpty())
            throw new InvalidProductDescriptionException();

        if  (!checkBarCode(newCode))
            throw new InvalidProductCodeException();

        if  (newPrice<=0)
            throw new InvalidPricePerUnitException();

        ProductTypeClass product = new ProductTypeClass(newDescription, newCode, newPrice, newNote);
        product.setId(id);

        if (products.containsKey(id)){
            for(ProductTypeClass prd : products.values())
                if (!id.equals(prd.getId()) && newCode.equals(prd.getBarCode()))
                    return false;
            try {
                if (DatabaseQuery.updateProductType(id, product)) {
                    products.replace(id, product);
                    return true;
                }
            } catch (SQLException throwables) {
                System.out.println(DB_ERROR_MESSAGE); }
        }
        return false;
    }

    @Override
    public boolean deleteProductType(Integer id) throws InvalidProductIdException, UnauthorizedException {
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER))
            throw new UnauthorizedException();

        if(id == null || id <= 0  )
            throw new InvalidProductIdException();

        if (products.containsKey(id)) {
            try {
                if (DatabaseQuery.deleteProductType(id)) {
                    products.remove(id);
                    return true;
                }
            } catch (SQLException throwables) {
                System.out.println(DB_ERROR_MESSAGE); }
        }

        return false;
    }

    @Override
    public List<ProductType> getAllProductTypes() throws UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole()))
            throw new UnauthorizedException();

        return new ArrayList<>(products.values());
    }

    @Override
    public ProductType getProductTypeByBarCode(String barCode) throws InvalidProductCodeException, UnauthorizedException {

        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER))
            throw new UnauthorizedException();

        if (!checkBarCode(barCode))
            throw new InvalidProductCodeException();

        for(ProductTypeClass ptc : this.products.values()) {
            if (barCode.equals(ptc.getBarCode())){
                return ptc;
            }
        }
        return null;
    }

    @Override
    public List<ProductType> getProductTypesByDescription(String description) throws UnauthorizedException {

        List<ProductType> list = new ArrayList<>();

        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER))
            throw new UnauthorizedException();

        if (description == null) {
            description = "";
        }

        int max;
        String sub;
        for(ProductTypeClass ptc : this.products.values()) {
            max = description.length();
            if (max <= ptc.getProductDescription().length()) {
                sub = ptc.getProductDescription().substring(0, max);
                if (description.equals(sub)) {
                    list.add(ptc);
                }
            }
        }
        return list;
    }

    @Override
    public boolean updateQuantity(Integer productId, int toBeAdded) throws InvalidProductIdException, UnauthorizedException {
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER))
            throw new UnauthorizedException();

        if(productId == null || productId <= 0  )
            throw new InvalidProductIdException();

        int tot;

        if (products.containsKey(productId)) {
            if(!(products.get(productId).getLocation() == null) && !products.get(productId).getLocation().isEmpty() && !(products.get(productId).getLocation() == "")){
                tot = products.get(productId).getQuantity() + toBeAdded;
                if (tot >= 0){
                    try {
                        if (DatabaseQuery.updateQuantity(productId, tot)) {
                            products.get(productId).setQuantity(tot);
                            return true;
                        }
                    } catch (SQLException throwables) {
                        System.out.println(DB_ERROR_MESSAGE); }
                }
            }
        }
        return false;
    }

    @Override
    public boolean updatePosition(Integer productId, String newPos) throws InvalidProductIdException, InvalidLocationException, UnauthorizedException {
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER))
            throw new UnauthorizedException();

        if (productId == null || productId <= 0  )
            throw new InvalidProductIdException();

        if (!(newPos == null || newPos.isEmpty()) && !checkPositionFormat(newPos))
            throw new InvalidLocationException();

        if (products.containsKey(productId)) {
            if (newPos == null || newPos.isEmpty())
                newPos = null;
            else {
                for (ProductTypeClass prd : products.values())
                    if (newPos.equals(prd.getLocation()))
                        return false;
            }
            try {
                if (DatabaseQuery.updatePosition(productId, newPos)) {
                    products.get(productId).setLocation(newPos);
                    return true;
                }
            } catch (SQLException throwables) {
                System.out.println(DB_ERROR_MESSAGE); }
        }
        return false;
    }

    @Override
    public Integer issueOrder(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        if (!checkBarCode(productCode)){
            throw new InvalidProductCodeException();
        }
        if (quantity <= 0){
            throw new InvalidQuantityException();
        }
        if (pricePerUnit <= 0){
            throw new InvalidPricePerUnitException();
        }
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER)){
            throw new UnauthorizedException();
        }

        if (!doesProductExists(productCode)){
            return -1;
        }

        OrderClass ord = new OrderClass(productCode, pricePerUnit, quantity, "ISSUED");
        try{
            DatabaseQuery.createOrder(ord);
            orders.put(ord.getOrderId(), ord);
            return ord.getOrderId();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    @Override
    public Integer payOrderFor(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        if (productCode == null || !checkBarCode(productCode)){
            throw new InvalidProductCodeException();
        }
        if (quantity <= 0){
            throw new InvalidQuantityException();
        }
        if (pricePerUnit <= 0){
            throw new InvalidPricePerUnitException();
        }
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER)){
            throw new UnauthorizedException();
        }

        if (!doesProductExists(productCode) || currentBalance < pricePerUnit*quantity){
            return -1;
        }

        OrderClass ord = new OrderClass(productCode, pricePerUnit, quantity, "PAYED");
        try{
            DatabaseQuery.createOrder(ord);
            orders.put(ord.getOrderId(), ord);
            updateCurrentBalance( -1 * pricePerUnit * quantity);
            return ord.getOrderId();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    @Override
    public boolean payOrder(Integer orderId) throws InvalidOrderIdException, UnauthorizedException {
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER)){
            throw new UnauthorizedException();
        }

        if (orderId == null || orderId <= 0){
            throw new InvalidOrderIdException();
        }
        OrderClass order = orders.get(orderId);
        if (order == null || order.getStatus().equals("COMPLETED"))
            return false;

        if (currentBalance <  order.getMoney())
            return false;

        try {
            if (DatabaseQuery.updateOrderStatus(order.getBalanceId(), "PAYED")) {
                order.setStatus("PAYED");
                updateCurrentBalance( -1 * order.getPricePerUnit() * order.getQuantity());
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    @Override
    public boolean recordOrderArrival(Integer orderId) throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException {
        if (orderId == null || orderId <= 0){
            throw new InvalidOrderIdException();
        }
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER)){
            throw new UnauthorizedException();
        }
        OrderClass order = orders.get(orderId);
        if (order == null || order.getStatus().equals("ISSUED")){
            return false;
        }
        String barcode = order.getProductCode();
        ProductTypeClass prod = null;
        try {
            prod = (ProductTypeClass) this.getProductTypeByBarCode(barcode);
        } catch (InvalidProductCodeException e) {
            e.printStackTrace();
        }
        if (prod.getLocation() == null || prod.getLocation().equals("")){
            throw new InvalidLocationException();
        }

        try {
            if (DatabaseQuery.updateOrderStatus(order.getBalanceId(), "COMPLETED")) {
                order.setStatus("COMPLETED");
            }
            if (DatabaseQuery.updateQuantity(prod.getId(), prod.getQuantity() + order.getQuantity())){
                prod.setQuantity(prod.getQuantity() + order.getQuantity());
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    private String convertRFIDToString(long i){
        return String.format("%012d", i);
    }

    @Override
    public boolean recordOrderArrivalRFID(Integer orderId, String RFIDfrom) throws InvalidOrderIdException, UnauthorizedException, 
InvalidLocationException, InvalidRFIDException {
        if (orderId == null || orderId <= 0){
            throw new InvalidOrderIdException();
        }
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER)){
            throw new UnauthorizedException();
        }

        if (RFIDfrom == null || RFIDfrom.isEmpty() || !checkValidRFID(RFIDfrom))
            throw new InvalidRFIDException();

        if (orders.containsKey(orderId)) {

            OrderClass order = orders.get(orderId);
            if (order.getStatus().equals("ISSUED")) {
                return false;
            }
            long from = Long.parseLong(RFIDfrom);
            // check unique
            for (long i = from; i < order.getQuantity() + from; i++) {
                if (inventory.containsKey(convertRFIDToString(i))){
                    throw new InvalidRFIDException();
                }
            }


            String barcode = order.getProductCode();
            ProductTypeClass prod = null;
            try {
                prod = (ProductTypeClass) this.getProductTypeByBarCode(barcode);
            } catch (InvalidProductCodeException e) {
                e.printStackTrace();
            }
            if (prod.getLocation() == null || prod.getLocation().equals("")) {
                throw new InvalidLocationException();
            }

            try {
                if (updateOrderStatus(order.getBalanceId(), "COMPLETED")) {
                    order.setStatus("COMPLETED");
                }
                for (long i = from; i < order.getQuantity() + from; i++) {
                    Product p = new Product(convertRFIDToString(i), barcode, STAT_INV);

                    createRFIDBarcodeLink(p);
                    inventory.put(convertRFIDToString(i), p);
                }
                if (DatabaseQuery.updateQuantity(prod.getId(), prod.getQuantity() + order.getQuantity())) {
                    prod.setQuantity(prod.getQuantity() + order.getQuantity());
                    return true;
                }
            } catch (SQLException throwables) {
                System.out.println(throwables.getMessage());
            }
        }
        return false;
    }
    @Override
    public List<Order> getAllOrders() throws UnauthorizedException {
        if (loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER))
            throw new UnauthorizedException();
        return orders.values().stream().collect(Collectors.toList());
    }

    @Override
    public Integer defineCustomer(String customerName) throws InvalidCustomerNameException, UnauthorizedException {

        if (customerName == null || customerName.isEmpty()) {
            throw new InvalidCustomerNameException();
        }
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager") && !loggedUser.getRole().equals("Cashier"))
            throw new UnauthorizedException();

        for(CustomerClass current : this.customers.values()) {
            if (current.getCustomerName().equals(customerName)){
                return -1;
            }
        }

        CustomerClass customer = new CustomerClass(customerName);

        try {
            if(DatabaseQuery.defineCustomer(customer)) {
                this.customers.put(customer.getId(), customer);
                return customer.getId();
            }

        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE); }

        return -1;

    }

    @Override
    public boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard)
            throws InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException, UnauthorizedException {

        if(id == null || id < 1)
            throw new InvalidCustomerIdException();
        if (newCustomerName == null || newCustomerName.isEmpty())
            throw new InvalidCustomerNameException();
        if (!(newCustomerCard == null) && !newCustomerCard.isEmpty() && (newCustomerCard.length() != 10 || !newCustomerCard.matches("[0-9]+")))
            throw new InvalidCustomerCardException();
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager") && !loggedUser.getRole().equals("Cashier"))
            throw new UnauthorizedException();

        /* TODO === [19/05] conflitto con attuale versione della GUI ===
            click su 'generate card' nella GUI -> createCard()
            click su 'OK' nella GUI -> modifyCustomer()
            modifyCustomer richiede l'unicità della carta passata all'ingresso ma tale carta esiste già nel DB
            perché è appena stata creata da createCard()

        for(LoyaltyCard current : this.loyaltyCards.values()) {
            if (current.getID().equals(newCustomerCard)){
                return false;
            }
        } */

        if(newCustomerCard != null && !newCustomerCard.equals("") && loyaltyCards.get(newCustomerCard).getCardOwnerID()!=-1)
            return false;

        try {

            // newCustomerCard is null -> RENAME

            if(newCustomerCard == null){
                if(DatabaseQuery.modifyCustomer_null(id, newCustomerName)) {
                    customers.get(id).setCustomerName(newCustomerName);
                    return true;
                }
            }

            // newCustomerCard is empty -> REMOVE

            else if (newCustomerCard.isEmpty()) {
                if(customers.get(id).getCustomerCard() != null) {
                    if(DatabaseQuery.modifyCustomer_empty(id, newCustomerName, customers.get(id).getCustomerCard())) {
                        customers.get(id).setCustomerName(newCustomerName);
                        loyaltyCards.remove(customers.get(id).getCustomerCard());
                        customers.get(id).setCustomerCard(null);
                        return true;
                    }
                }
            }

            // newCustomerCard has a value != null -> RENAME + UPDATE CARD

            else if(newCustomerCard.matches("[0-9]+")) { // 'newCustomerCard.length() == 10' already verified
                if(DatabaseQuery.modifyCustomer_valid(id, newCustomerName, newCustomerCard)) {
                    customers.get(id).setCustomerCard(newCustomerCard);
                    customers.get(id).setCustomerName(newCustomerName);
                    return true;
                }
            }
        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE); }

        return false;
    }

    @Override
    public boolean deleteCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {

        if (id == null || id <= 0 )
            throw new InvalidCustomerIdException();
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager") && !loggedUser.getRole().equals("Cashier"))
            throw new UnauthorizedException();

        if (customers.get(id) == null)
            return false;

        try {
            if (DatabaseQuery.deleteCustomer(id)) {
                customers.remove(id);
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE);
        }

        return false;
    }

    @Override
    public Customer getCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {

        if (id == null || id <= 0 )
            throw new InvalidCustomerIdException();
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager") && !loggedUser.getRole().equals("Cashier"))
            throw new UnauthorizedException();

        if (customers.get(id) == null)
            return null;

        return customers.get(id);

    }

    @Override
    public List<Customer> getAllCustomers() throws UnauthorizedException {
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager") && !loggedUser.getRole().equals("Cashier"))
            throw new UnauthorizedException();

        return new ArrayList<>(customers.values());
    }

    @Override
    public String createCard() throws UnauthorizedException {

        if (loggedUser == null || !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager") && !loggedUser.getRole().equals("Cashier"))
            throw new UnauthorizedException();

        int myID = -1;

        try {
            myID = DatabaseQuery.getNextCardID();
        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE);
        }

        if(myID != -1)  {
            LoyaltyCard myCard = new LoyaltyCard(String.format("%010d", myID));
            loyaltyCards.put(myCard.getID(), myCard);

            try {
                DatabaseQuery.createCard(myCard);
                return myCard.getID();
            } catch (SQLException throwables) {
                System.out.println(DB_ERROR_MESSAGE); }

        }

        return null;

    }

    @Override
    public boolean attachCardToCustomer(String customerCard, Integer customerId) throws InvalidCustomerIdException, InvalidCustomerCardException, UnauthorizedException {

        if (customerId == null || customerId <= 0 )
            throw new InvalidCustomerIdException();
        if (customerCard == null || customerCard.length() != 10 || !customerCard.matches("[0-9]+"))
            throw new InvalidCustomerCardException();
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager") && !loggedUser.getRole().equals("Cashier"))
            throw new UnauthorizedException();

        if (!loyaltyCards.containsKey(customerCard))
            return false;

        if (loyaltyCards.get(customerCard).getCardOwnerID() != -1 || !customers.containsKey(customerId))  {
            return false; // card already owned or customer doesn't exists
        }

        try {
            if(DatabaseQuery.attachCardToCustomer(loyaltyCards.get(customerCard),customers.get(customerId))) {
                customers.get(customerId).setCustomerCard(customerCard);
                loyaltyCards.get(customerCard).setCardOwnerID(customerId);
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE); }

        return false;
    }

    @Override
    public boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded) throws InvalidCustomerCardException, UnauthorizedException {

        if (customerCard == null || customerCard.length() != 10 || !customerCard.matches("[0-9]+"))
            throw new InvalidCustomerCardException();
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager") && !loggedUser.getRole().equals("Cashier"))
            throw new UnauthorizedException();

        if (!loyaltyCards.containsKey(customerCard) || (loyaltyCards.get(customerCard).getPoints() + pointsToBeAdded < 0 ))
            return false;

        int newPoints = loyaltyCards.get(customerCard).getPoints() + pointsToBeAdded;

        try {
            if(DatabaseQuery.modifyPointsOnCard(newPoints, customerCard, loyaltyCards.get(customerCard).getCardOwnerID())) {

                loyaltyCards.get(customerCard).addPoints(pointsToBeAdded); // update points on LoyaltyCard
                int cardOwnerID = loyaltyCards.get(customerCard).getCardOwnerID();

                /* TODO === [19/05] conflitto con attuale versione della GUI ===
                    createCard non associa la carta al proprietario (attachCardToCustomer non viene richiamata)
                    impedendo in questo modo di eseguire l'operazione sottostante (cardOwnerID ha ancora il valore di default) */

                if (cardOwnerID!=-1) customers.get(cardOwnerID).setPoints(newPoints); // update points on Customer

                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(DB_ERROR_MESSAGE); }

        return false;
    }

    @Override
    public Integer startSaleTransaction() throws UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        SaleTransactionClass sale = new SaleTransactionClass();
        int tn = sale.getTicketNumber();

        try{
            DatabaseQuery.createSaleTransaction(sale);
            sales.put(tn, sale);
            return tn;
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean addProductToSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();
        if (productCode == null || productCode.equals("") || !checkBarCode(productCode))
            throw new InvalidProductCodeException();
        if (amount <= 0)
            throw new InvalidQuantityException();
        SaleTransactionClass sale = sales.get(transactionId);
        if (sale == null)
            return false;
        Integer pid = this.getProductIdFromBarCode(productCode);
        ProductType pt = products.get(pid);
        if (pt == null || pt.getQuantity() < amount)
            return false;
        TicketProduct toAdd = new TicketProduct(productCode, pt.getProductDescription(), amount, pt.getPricePerUnit(), 0.0);
        try{
            if (DatabaseQuery.addTicketEntry(sale.getBalanceId(), toAdd)){
                sale.addEntry(toAdd);
            }
        } catch (SQLException e){
            return false;
        }
        try{
            if (DatabaseQuery.updateQuantity(pt.getId(), pt.getQuantity() - amount)){
                pt.setQuantity(pt.getQuantity() - amount);
                return true;
            }
        } catch (SQLException throwables) {
            try{
                if (DatabaseQuery.deleteTicketEntry(sale.getBalanceId(), toAdd)){
                    sale.removeEntry(productCode);
                    return false;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    @Override
    public boolean addProductToSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException, UnauthorizedException{
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();
        if (RFID == null || RFID.equals("") || !checkValidRFID(RFID))
            throw new InvalidRFIDException();

        SaleTransactionClass sale = sales.get(transactionId);
        if (sale == null)
            return false;
        Product p = inventory.get(RFID);
        if (p == null)
            return false;
        String pid = p.getProductCode();
        ProductType pt = null;
        try {
            pt = this.getProductTypeByBarCode(pid);
        } catch (InvalidProductCodeException e) {
            e.printStackTrace();
        }
        TicketProduct toAdd = new TicketProduct(pid, RFID, pt.getProductDescription(), 1, pt.getPricePerUnit(), 0.0);
        try{
            if (DatabaseQuery.addTicketEntryRFID(sale.getBalanceId(), toAdd, RFID)){
                sale.addEntry(toAdd);
            }
            if (DatabaseQuery.updateRFIDBarcodeStatus(p, STAT_SOLD)){
                p.setStatus(STAT_SOLD);
            }
            if (DatabaseQuery.updateQuantity(pt.getId(), pt.getQuantity() - 1)){
                pt.setQuantity(pt.getQuantity() - 1);
                return true;
            }
        } catch (SQLException throwables) {
            try{
                if (DatabaseQuery.deleteTicketEntryRFID(sale.getBalanceId(), toAdd, RFID)){
                    sale.removeEntryRFID(pid, RFID);
                    p.setStatus(STAT_INV);
                    return false;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            System.out.println(throwables.getMessage());
        }
        return false;    }
    
    @Override
    public boolean deleteProductFromSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();
        if (productCode == null || productCode.equals("") || !checkBarCode(productCode))
            throw new InvalidProductCodeException();
        if (amount < 0)
            throw new InvalidQuantityException();
        SaleTransactionClass sale = sales.get(transactionId);
        if (sale == null || sale.isClosed())
            return false;


        ProductTypeClass pt = null;
        Integer pid = this.getProductIdFromBarCode(productCode);
        if (pid == null)
            return false;
        pt = products.get(pid);

        TicketProduct entry = (TicketProduct) sale.getEntry(productCode);
        if(entry == null){
            return false;
        }
        int oldAmount = entry.getAmount();
        if (oldAmount < amount)
            return false;

        //if oldAmount == amount --> cancella la Entry (sia da DB che da lista di Entry in SaleTransaction)
        if (oldAmount == amount){
            try{
                if (DatabaseQuery.deleteTicketEntry(sale.getBalanceId(), entry))
                    sale.removeEntry(productCode);
            } catch (SQLException throwables) {
                return false;
            }
        }
        else{
            try{
                if (DatabaseQuery.updateTicketEntryAmount(oldAmount - amount, sale.getBalanceId(), entry.getBarCode()))
                    entry.setAmount(oldAmount - amount);
            } catch (SQLException throwables) {
                return false;
            }
        }
        try{
            if (DatabaseQuery.updateQuantity(pt.getId(), pt.getQuantity() + amount)){
                pt.setQuantity(pt.getQuantity() + amount);
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteProductFromSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException, UnauthorizedException{
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();
        if (RFID == null || RFID.equals("") || !checkValidRFID(RFID))
            throw new InvalidRFIDException();

        SaleTransactionClass sale = sales.get(transactionId);
        if (sale == null || sale.isClosed())
            return false;

        Product p = inventory.get(RFID);
        if (p == null)
            return false;
        String pid = p.getProductCode();
        ProductType pt = null;
        try {
            pt = this.getProductTypeByBarCode(pid);
        } catch (InvalidProductCodeException e) {
            e.printStackTrace();
        }
        TicketProduct entry = (TicketProduct) sale.getEntry(pid);
        if(entry == null){
            return false;
        }

        //cancella la Entry (sia da DB che da lista di Entry in SaleTransaction)
        try{
            if (DatabaseQuery.deleteTicketEntryRFID(sale.getBalanceId(), entry, RFID) && DatabaseQuery.updateRFIDBarcodeStatus(p, STAT_INV)) {
                sale.removeEntryRFID(pid, RFID);
                p.setStatus(STAT_INV);
            }
        } catch (SQLException throwables) {
            return false;
        }

        try{
            if (DatabaseQuery.updateQuantity(pt.getId(), pt.getQuantity() + 1)){
                pt.setQuantity(pt.getQuantity() + 1);
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    @Override
    public boolean applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();
        if (productCode == null || productCode.equals("") || !checkBarCode(productCode))
            throw new InvalidProductCodeException();
        if (discountRate < 0 || discountRate >= 1)
            throw new InvalidDiscountRateException();

        SaleTransactionClass sale = sales.get(transactionId);
        if (sale == null || sale.isClosed())
            return false;

        TicketProduct tp = (TicketProduct) sale.getEntry(productCode);
        if (tp == null)
            return false;

        try{
            if (DatabaseQuery.updateTicketEntryDiscount(discountRate, sale.getBalanceId(), tp))
                tp.setDiscountRate(discountRate);
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean applyDiscountRateToSale(Integer transactionId, double discountRate) throws InvalidTransactionIdException, InvalidDiscountRateException, UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();
        if (discountRate < 0 || discountRate >= 1)
            throw new InvalidDiscountRateException();

        SaleTransactionClass sale = sales.get(transactionId);
        if (sale == null || sale.isPayed())
            return false;
        try{
            if (DatabaseQuery.updateSaleDiscountRate(sale.getBalanceId(), discountRate)){
                sale.setDiscountRate(discountRate);
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    @Override
    public int computePointsForSale(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();

        SaleTransactionClass t = sales.get(transactionId);
        if (t == null)
            return -1;
        return ((int) t.getPrice() / 10);
    }

    @Override
    public boolean endSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }

        if (!sales.containsKey(transactionId))
            return false;

        SaleTransactionClass s = sales.get(transactionId);

        if (s.isClosed()){
            return false;
        }

        try{
            if (DatabaseQuery.updateSaleClosed(s.getBalanceId())){
                s.setClosed(true);
                System.out.println(s.isClosed());
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteSaleTransaction(Integer saleNumber) throws InvalidTransactionIdException, UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (saleNumber == null || saleNumber <= 0)
            throw new InvalidTransactionIdException();

        if (sales.get(saleNumber) == null)
            return false;
        if (sales.get(saleNumber).isPayed())
            return false;

        try{
            if (DatabaseQuery.deleteSaleTransaction(sales.get(saleNumber).getBalanceId()))
                sales.remove(saleNumber);
            return true;
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    @Override
    public SaleTransaction getSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        if (loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (transactionId == null || transactionId <= 0)
            throw new InvalidTransactionIdException();

        SaleTransactionClass t = sales.get(transactionId);
        if (t == null || !t.isClosed())
            return null;
        return t;
    }

    @Override
    public Integer startReturnTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException("startReturnTransaction is being called by an incorrect user");
        }
        if(transactionId == null || transactionId <= 0){
            throw new InvalidTransactionIdException("The transaction id: " + transactionId + " is not a valid transactionId!");
        }
        if(!sales.containsKey(transactionId)){
            return -1;
        }

        /*create ReturnTransaction instance*/
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(transactionId);
        try {
            createReturnTransaction(returnTransactionClass);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        returns.put(returnTransactionClass.getBalanceId(), returnTransactionClass);
        return returnTransactionClass.getBalanceId();
    }

    @Override
    public boolean returnProduct(Integer returnId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException("returnProduct is being called by an incorrect user");
        }
        if(amount <= 0){
            throw new InvalidQuantityException(amount + " is not a valid quantity for the function returnProduct");
        }
        if(returnId == null || returnId <= 0){
            throw new InvalidTransactionIdException(returnId + " is not a valid id for a return transaction");
        }
        if(productCode == null || !checkBarCode(productCode)){
            throw new InvalidProductCodeException(productCode + " is not a valid product code");
        }
        if(!returns.containsKey(returnId)){
            return false;
        }
        if(!doesProductExists(productCode)){
            return false;
        }
        ReturnTransactionClass returnTransactionClass = returns.get(returnId);
        SaleTransactionClass associatedSale = sales.get(returnTransactionClass.getParentTransactionId());
        /* check if the product to be added in the ReturnTransactionClass is present in the associated
        SaleTransactionClass and has an higher amount there */
        Integer productId = getProductIdFromBarCode(productCode);
//        if(returnTransactionClass.getProducts().containsKey(productId)){
//            amount += returnTransactionClass.getProducts().get(productId);
//        }
        if(associatedSale.getEntry(productCode) == null || associatedSale.getEntry(productCode).getAmount() < amount){
            return false;
        }
        returnTransactionClass.addProduct(productId, amount);
        return true;
    }

    @Override
    public boolean returnProductRFID(Integer returnId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, UnauthorizedException 
    {
        if(loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException("returnProduct is being called by an incorrect user");
        }
        if(returnId == null || returnId <= 0){
            throw new InvalidTransactionIdException(returnId + " is not a valid id for a return transaction");
        }
        if(RFID == null || RFID.isEmpty() || !checkValidRFID(RFID)){
            throw new InvalidRFIDException();
        }
        if(!returns.containsKey(returnId)){
            return false;
        }
        if(!inventory.containsKey(RFID)){
            return false;
        }
        Product p = inventory.get(RFID);
        String productCode = p.getProductCode();
        ReturnTransactionClass returnTransactionClass = returns.get(returnId);
        SaleTransactionClass associatedSale = sales.get(returnTransactionClass.getParentTransactionId());
        if(associatedSale.getEntry(productCode) == null){
            return false;
        }
        returnTransactionClass.addRFIDProduct(RFID);
        return true;
    }


    @Override
    public boolean endReturnTransaction(Integer returnId, boolean commit) throws InvalidTransactionIdException, UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException("endReturnTransaction is being called by an incorrect user");
        }
        if(returnId == null || returnId <= 0){
            throw new InvalidTransactionIdException(returnId + " is not a valid id for a return transaction");
        }
        if(!returns.containsKey(returnId) || returns.get(returnId).isClosed()){
            return false;
        }

        //closing the transaction
        ReturnTransactionClass returnTransactionClass = returns.get(returnId);
        returnTransactionClass.setClosed(true);

        if(commit){
            /*This thing should change the product quantity on shelves, I don't like it tho */


            for(String RFID : returnTransactionClass.getRFIDReturns()){
                Integer productId = getProductIdFromBarCode(inventory.get(RFID).getProductCode());
                ProductType productOnShelf = products.get(productId);
                int newProductQuantity = productOnShelf.getQuantity() + 1;
                try{
                    DatabaseQuery.updateQuantity(productId, newProductQuantity);
                } catch (SQLException e){
                    e.printStackTrace();
                    return false;
                }
                productOnShelf.setQuantity(newProductQuantity);
            }

            /* FOR PRODUCTS W/O RFID */
            for(Integer productId : returnTransactionClass.getProducts().keySet()){
                ProductType productOnShelf = products.get(productId);
                int newProductQuantity = productOnShelf.getQuantity() + returnTransactionClass.getProducts().get(productId);
                try{
                    DatabaseQuery.updateQuantity(productId, newProductQuantity);
                } catch (SQLException e){
                    e.printStackTrace();
                    return false;
                }
                productOnShelf.setQuantity(newProductQuantity);
            }
            /*
             * This method updates the transaction status (decreasing the number of units sold by the number of returned one and
             * decreasing the final price).*/
            SaleTransactionClass associatedSale = sales.get(returnTransactionClass.getParentTransactionId());


            /* FOR PRODUCTS W/O RFID */
            for (Integer productId: returnTransactionClass.getProducts().keySet()) {
                try {
                    if (!updateReturnTransaction(returnTransactionClass)) {
                        return false;
                    }
                    addProductToReturnTransaction(returnId, productId, returnTransactionClass.getProducts().get(productId));

                    Integer amountInSale = associatedSale.getEntry(getBarCodeFromProductId(productId)).getAmount();
                    associatedSale.getEntry(getBarCodeFromProductId(productId)).setAmount(amountInSale - returnTransactionClass.getProducts().get(productId));
                    updateTicketEntryAmount(amountInSale - returnTransactionClass.getProducts().get(productId), associatedSale.getBalanceId(), getBarCodeFromProductId(productId));
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            for (String productRFID : returnTransactionClass.getRFIDReturns()) {
                try {
                    if(!updateReturnTransaction(returnTransactionClass)){
                        return false;
                    }
                    addProductToReturnTransactionRFID(returnId, productRFID);

                    TicketProduct productEntryInSale = associatedSale.getEntry(inventory.get(productRFID).getProductCode());
                    productEntryInSale.removeRFID(productRFID);//This also decreases the amount
                    updateTicketEntryAmount(productEntryInSale.getAmount(), associatedSale.getBalanceId(), productEntryInSale.getBarCode());
                    inventory.get(productRFID).setStatus(STAT_RET);
                }catch (SQLException e){
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean deleteReturnTransaction(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException("deleteReturnTransaction is being called by an incorrect user");
        }
        if(returnId == null || returnId <= 0){
            throw new InvalidTransactionIdException(returnId + " is not a valid id for a return transaction");
        }
        if(!returns.containsKey(returnId) || returns.get(returnId).isPayed()){
            System.out.println("Transaction with id " + returnId + " does not exist.");
            return false;
        }
        try{
            it.polito.ezshop.utils.DatabaseQuery.deleteReturnTransaction(returnId);
        } catch (SQLException e){
            System.out.println(e.getMessage());
            return false;
        }
        System.out.println("Transaction with id " + returnId + " has been deleted.");
        return true;
    }

    @Override
    public double receiveCashPayment(Integer ticketNumber, double cash) throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (ticketNumber == null || ticketNumber <= 0)
            throw new InvalidTransactionIdException();
        if (cash <= 0)
            throw new InvalidPaymentException();

        SaleTransactionClass sale;
        if (!sales.containsKey(ticketNumber) || (sale = sales.get(ticketNumber)).getPrice() > cash)
            return -1;

        try{
            if (DatabaseQuery.updateSalePaid(sale.getBalanceId())){
                updateCurrentBalance(sale.getPrice());
                sale.setPayed(true);
                return cash - sale.getPrice();
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return -1;
    }

    @Override
    public boolean receiveCreditCardPayment(Integer ticketNumber, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole()))
            throw new UnauthorizedException();

        if (ticketNumber == null || ticketNumber <= 0)
            throw new InvalidTransactionIdException();

        if (creditCard == null || creditCard.isEmpty() || !Validator.checkCreditCard(creditCard))
            throw new InvalidCreditCardException();

        SaleTransactionClass sale;
        CreditCardClass cc;

        if (!sales.containsKey(ticketNumber) || !creditCards.containsKey(creditCard) || (cc = creditCards.get(creditCard)).getCardCredit() < (sale = sales.get(ticketNumber)).getPrice())
            return false;

        try{
            if (DatabaseQuery.updateSalePaid(sale.getBalanceId())){
                this.recordBalanceUpdate(sale.getPrice());
                cc.setCardCredit(cc.getCardCredit() - sale.getPrice());
                updateCurrentBalance(sale.getPrice());
                sale.setPayed(true);
                return true;
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
        return false;
    }

    @Override
    public double returnCashPayment(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        if (returnId == null || returnId <= 0)
            throw new InvalidTransactionIdException();

        ReturnTransactionClass returnTr;
        if (!returns.containsKey(returnId) || !(returnTr = returns.get(returnId)).isClosed())
            return -1;

        try{
            if (DatabaseQuery.updateSalePaid(returnTr.getBalanceId())){
                returnTr.setMoney(computeReturnMoney(returnTr));
                this.recordBalanceUpdate(-1 * returnTr.getMoney());
                returnTr.setPayed(true);
                return returnTr.getMoney();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    @Override
    public double returnCreditCardPayment(Integer returnId, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole()))
            throw new UnauthorizedException();

        if (returnId == null || returnId <= 0)
            throw new InvalidTransactionIdException();

        if (creditCard == null || creditCard.isEmpty() || !Validator.checkCreditCard(creditCard))
            throw new InvalidCreditCardException();

        CreditCardClass cc;
        ReturnTransactionClass returnTr;

        if (!returns.containsKey(returnId) || !creditCards.containsKey(creditCard) || !(returnTr = returns.get(returnId)).isClosed())
            return -1;
        cc = creditCards.get(creditCard);

        try{
            if (DatabaseQuery.updateSalePaid(returnTr.getBalanceId())){
                returnTr.setMoney(computeReturnMoney(returnTr));
                this.recordBalanceUpdate(-1 * returnTr.getMoney());
                cc.setCardCredit(cc.getCardCredit() - returnTr.getMoney());
                returnTr.setPayed(true);
                return returnTr.getMoney();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean recordBalanceUpdate(double toBeAdded) throws UnauthorizedException {
        if(loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER)){
            throw new UnauthorizedException();
        }
        String transactionType = TYPE_CREDIT;
        if(toBeAdded <0){
            transactionType = TYPE_DEBIT;
        }
        if(toBeAdded + currentBalance < 0){
            return false;
        }
        BalanceOperationClass balanceOperationClass = new BalanceOperationClass();
        balanceOperationClass.setType(transactionType);
        balanceOperationClass.setMoney(toBeAdded);
        balanceOperationClass.setDate(LocalDate.now());
        try{
            createBalanceTransaction(balanceOperationClass);
            operations.put(balanceOperationClass.getBalanceId(), balanceOperationClass);
            updateCurrentBalance(toBeAdded);
        } catch (SQLException e){
            return false;
        }
        return true;
    }

    @Override
    public List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to) throws UnauthorizedException {
        if(loggedUser == null || !loggedUser.getRole().equals(ROLE_ADMINISTRATOR) && !loggedUser.getRole().equals(ROLE_MANAGER)){
            throw new UnauthorizedException();
        }
        List<BalanceOperation> operations;
        try{
            operations = getAllBalanceOperationsBetweenDates(from, to);
        } catch (SQLException e){
            return null;
        }

        return operations;
    }

    @Override
    public double computeBalance() throws UnauthorizedException {
        if(loggedUser == null || !isValidRole(loggedUser.getRole())){
            throw new UnauthorizedException();
        }
        return currentBalance;
    }

    private void updateCurrentBalance(double amount){
        this.currentBalance += amount;
        try{
            setBalanceQuantity(currentBalance);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private double computeReturnMoney(ReturnTransactionClass returnTr){
        if(returnTr.getProducts().isEmpty()){
            return 0;
        }
        final double[] money = {0};
        returnTr.getProducts().forEach((productId,price)-> {
            if(products.containsKey(productId)){
                money[0] += products.get(productId).getPricePerUnit() * price;
            }
        });
        return money[0];
    }

}
