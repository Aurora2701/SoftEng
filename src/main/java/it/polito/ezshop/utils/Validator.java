package it.polito.ezshop.utils;

import java.nio.charset.Charset;

public class Validator {

    // Returns true if given bar code number is valid
    public static boolean checkBarCode (String barCode) {

        String pattern = "[0-9]{12,14}";

        if (barCode == null || !barCode.matches(pattern)) //check length & if it's a number
            return false;

        if (barCode.length() == 13)
            barCode = "0" + barCode;

        int evens = 0;
        int odds = 0;
        int checkDigit;

        for (int i = 0; i < barCode.length()-1; i++) {
            //check if position is odd or even
            if (i % 2 == 0) {
                evens += Integer.parseInt(String.valueOf(barCode.charAt(i)));
            }
            else {
                odds += Integer.parseInt(String.valueOf(barCode.charAt(i)));
            }
        }
        evens *= 3; //multiply evens by three
        int total = odds + evens;

        if (total % 10 == 0){ //if total is divisible by ten, special case
            checkDigit = 0;//checksum is zero
        }
        else {
            checkDigit = (10 - (total % 10)); //subtract the ones digit from 10 to find the checkDigit
        }
        int last = Integer.parseInt(String.valueOf(barCode.charAt(barCode.length()-1)));
        return (last == checkDigit);
    }

    // Returns true if the product location is in a valid format
    public static boolean checkPositionFormat (String position){
        int pos = position.indexOf('-');
        int pos1 = position.lastIndexOf('-');

        if (pos == -1 || pos1 == pos)
            return false;

        String una =position.substring(0, pos);
        String due = position.substring(pos+1, pos1);
        String tre = position.substring(pos1+1);

        return una.matches("[0-9]+") && due.matches("[a-zA-Z]+") && tre.matches("[0-9]+");
    }

    // Returns true if given card number is valid
    public static boolean checkCreditCard(String cardNo)
    {
        int nDigits = cardNo.length();

        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--)
        {

            int d = cardNo.charAt(i) - '0';

            if (isSecond)
                d = d * 2;

            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }

    public static boolean checkValidRFID(String rfid){
        String pattern = "[0-9]{12}";
        return rfid != null && rfid.matches(pattern); //check length & if it's a number
    }
}