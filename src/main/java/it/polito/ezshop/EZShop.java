package it.polito.ezshop;

import it.polito.ezshop.data.EZShopInterface;
import it.polito.ezshop.data.OrderClass;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.view.EZShopGUI;

import java.sql.SQLException;
import java.util.Map;

import static it.polito.ezshop.utils.DatabaseQuery.*;
import static it.polito.ezshop.utils.DbManagerClass.connect;
import static it.polito.ezshop.utils.DbManagerClass.initDbSchema;


public class EZShop {

    public static void main(String[] args){
//        connect("ezshop.db");
//        initDbSchema();

        it.polito.ezshop.data.EZShop ezShop = new it.polito.ezshop.data.EZShop();
        EZShopGUI gui = new EZShopGUI(ezShop);
    }
}
