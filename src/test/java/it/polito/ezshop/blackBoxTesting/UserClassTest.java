package it.polito.ezshop.blackBoxTesting;

import it.polito.ezshop.data.UserClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserClassTest {

    @Test
    public void testSetId () {
        UserClass uc = new UserClass("teresa", "0000", "Administrator");
        uc.setId(1);
        int id = uc.getId();
        assertEquals(1, id);

        uc.setId(-10);
        id = uc.getId();
        assertEquals(1, id);
    }

    @Test
    public void testSetUsername () {
        UserClass uc = new UserClass("teresa", "0000", "Administrator");

        uc.setUsername("Sauro");
        assertEquals("Sauro", uc.getUsername());

        uc.setUsername(null);
        assertEquals("Sauro", uc.getUsername());

        uc.setUsername("");
        assertEquals("Sauro", uc.getUsername());
    }

    @Test
    public void testSetPassword () {
        UserClass uc = new UserClass("teresa", "0000", "Administrator");

        uc.setPassword("0100");
        assertEquals("0100", uc.getPassword());

        uc.setPassword("");
        assertEquals("0100", uc.getPassword());

        uc.setPassword(null);
        assertEquals("0100", uc.getPassword());

    }

    @Test
    public void testSetRole () {
        UserClass uc = new UserClass("teresa", "0000", "Administrator");

        uc.setRole("Cashier");
        assertEquals("Cashier", uc.getRole());

        uc.setRole("");
        assertEquals("Cashier", uc.getRole());

        uc.setRole(null);
        assertEquals("Cashier", uc.getRole());
    }
}
