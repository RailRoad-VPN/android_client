package net.rroadvpn.services;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.UUID;

public class Utilities {
    private Random randomGenerator;

    public Utilities(){
        this.randomGenerator = new Random();
    }

    public String generateAuthToken() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        int r = getRandomInt(1, uuid.length());
        RroadLogger.writeLog("Random number: " + r);
        Long unixTime = System.currentTimeMillis() / 1000;
        RroadLogger.writeLog("Unixtime: " + unixTime);
        RroadLogger.writeLog(unixTime.doubleValue());
        Double unixTime1 =  (unixTime.doubleValue()) / r;
        RroadLogger.writeLog("Unixtime devided float: " + unixTime1);

        DecimalFormat df = new DecimalFormat("#.##########");
        df.setRoundingMode(RoundingMode.CEILING);

        String unixtimeRounded = df.format(unixTime1);
        RroadLogger.writeLog("Unixtime Rounded double value: " + String.valueOf(unixtimeRounded));
        int unixtimeRoundedLength = unixtimeRounded.length();
        RroadLogger.writeLog("Unixtime Rounded double value len: " + String.valueOf(unixtimeRoundedLength));

        String unixtimeRoundedLengthStr;
        if (unixtimeRoundedLength < 10) {
            unixtimeRoundedLengthStr = '0' + String.valueOf(unixtimeRoundedLength);
        } else {
            unixtimeRoundedLengthStr = String.valueOf(unixtimeRoundedLength);
        }
        RroadLogger.writeLog("Unixtime Rounded double value string len: " + String.valueOf(unixtimeRoundedLengthStr));

        String rStr;
        if (r < 10) {
            rStr = '0' + String.valueOf(r);
        } else {
            rStr = String.valueOf(r);
        }
        RroadLogger.writeLog("Random number string len: " + String.valueOf(rStr));

        String leftToken = uuid.substring(0, r);
        String rightToken = uuid.substring(r, uuid.length());
        String token = String.format("%s%s%s%s%s", rStr, unixtimeRoundedLengthStr, leftToken, unixtimeRounded, rightToken);
        RroadLogger.writeLog("TOKEN: " + String.valueOf(token));

        return token;
    }

    public int getRandomInt(int min, int max) {
        return randomGenerator.nextInt((max - min) + 1) + min;
    }
}
