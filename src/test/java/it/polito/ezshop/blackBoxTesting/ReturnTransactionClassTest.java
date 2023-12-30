package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.ReturnTransactionClass;
import junit.framework.TestCase;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static it.polito.ezshop.data.BalanceOperationClass.TYPE_DEBIT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;

public class ReturnTransactionClassTest extends TestCase {

    public void testConstructor() {
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(69);

        assertEquals(69, (int)returnTransactionClass.getParentTransactionId());
        assertEquals(LocalDate.now(), returnTransactionClass.getDate());
        assertEquals(TYPE_DEBIT, returnTransactionClass.getType());
        assertThrows(NullPointerException.class, returnTransactionClass::getBalanceId);
        assertNotNull(returnTransactionClass.getProducts());
        assertTrue(returnTransactionClass.getProducts().isEmpty());

    }

    public void testConstructor1() {
        LocalDate date = LocalDate.now();
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(1, date, TYPE_DEBIT, 69);

        assertEquals(69, (int)returnTransactionClass.getParentTransactionId());
        assertEquals(date, returnTransactionClass.getDate());
        assertEquals(TYPE_DEBIT, returnTransactionClass.getType());
        assertEquals(1, returnTransactionClass.getBalanceId());
        assertNotNull(returnTransactionClass.getProducts());
        assertTrue(returnTransactionClass.getProducts().isEmpty());

    }
    public void testAddProduct() {
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(666);
        assertNotNull(returnTransactionClass.getProducts());
        assertTrue(returnTransactionClass.getProducts().isEmpty());

        returnTransactionClass.addProduct(10099,1);
        assertFalse(returnTransactionClass.getProducts().isEmpty());
        assertThat(returnTransactionClass.getProducts().size(), is(1));
        returnTransactionClass.addProduct(10099,1);
        assertThat(returnTransactionClass.getProducts().size(), is(1));
        assertEquals(2, (int)returnTransactionClass.getProducts().get(10099));

    }

    public void testSetClosed() {
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(11);

        assertFalse(returnTransactionClass.isClosed());

        returnTransactionClass.setClosed(true);
        assertTrue(returnTransactionClass.isClosed());

        returnTransactionClass.setClosed(false);
        assertFalse(returnTransactionClass.isClosed());
    }

    public void testSetPayed() {
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(11);

        assertFalse(returnTransactionClass.isPayed());

        returnTransactionClass.setPayed(true);
        assertTrue(returnTransactionClass.isPayed());

        returnTransactionClass.setPayed(false);
        assertFalse(returnTransactionClass.isPayed());
    }

    public void testSetProduct() {
        ReturnTransactionClass returnTransactionClass = new ReturnTransactionClass(666);
        assertNotNull(returnTransactionClass.getProducts());
        assertTrue(returnTransactionClass.getProducts().isEmpty());

        Map<Integer, Integer> products = new HashMap<>();
        products.put(1,5);
        products.put(3,5);
        returnTransactionClass.setProducts(products);
        assertFalse(returnTransactionClass.getProducts().isEmpty());
        assertThat(returnTransactionClass.getProducts().size(), is(2));
        assertEquals(5, (int)returnTransactionClass.getProducts().get(3));
    }
}