package net.rroadvpn.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.UUID;

public class Utilities {

    private Random randomGenerator;

    public Utilities() {
        this.randomGenerator = new Random();
    }

    public String generateAuthToken() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        int r = getRandomInt(1, uuid.length());
        //System.out.println("Random number: " + r);
        Long unixTime = System.currentTimeMillis() / 1000L;
        //System.out.println("Unixtime: " + unixTime);
        //System.out.println(unixTime.doubleValue());
        Double unixTime1 = (unixTime.doubleValue()) / r;
        //System.out.println("Unixtime devided float: " + unixTime1);

        DecimalFormat df = new DecimalFormat("#.##########");
        df.setRoundingMode(RoundingMode.CEILING);

        String unixtimeRounded = df.format(unixTime1);
        //System.out.println("Unixtime Rounded double value: " + unixtimeRounded);

        // TODO fix it some other way
        if (unixtimeRounded.contains(",")) {
            System.out.println("ROUNDED CONTAINS COMMA INSTEAD OF DOT");
            unixtimeRounded = unixtimeRounded.replace(",", ".");
        }

        int unixtimeRoundedLength = unixtimeRounded.length();
        //System.out.println("Unixtime Rounded double value len: " + String.valueOf(unixtimeRoundedLength));

        String unixtimeRoundedLengthStr;
        if (unixtimeRoundedLength < 10) {
            unixtimeRoundedLengthStr = '0' + String.valueOf(unixtimeRoundedLength);
        } else {
            unixtimeRoundedLengthStr = String.valueOf(unixtimeRoundedLength);
        }
        //System.out.println("Unixtime Rounded double value string len: " + String.valueOf(unixtimeRoundedLengthStr));

        String rStr;
        if (r < 10) {
            rStr = '0' + String.valueOf(r);
        } else {
            rStr = String.valueOf(r);
        }
        //System.out.println("Random number string len: " + String.valueOf(rStr));

        String leftToken = uuid.substring(0, r);
        String rightToken = uuid.substring(r, uuid.length());
        String token = String.format("%s%s%s%s%s", rStr, unixtimeRoundedLengthStr, leftToken, unixtimeRounded, rightToken);
        System.out.println("TOKEN: " + String.valueOf(token));

        return "7d@qjf-hK:qwQuQqH]Pq+xJNseU<Gh]:A0A=AY\\PJKjNnQOP#YA'lXADW[k7FzGE";
    }

    public int getRandomInt(int min, int max) {
        return randomGenerator.nextInt((max - min) + 1) + min;
    }
}
