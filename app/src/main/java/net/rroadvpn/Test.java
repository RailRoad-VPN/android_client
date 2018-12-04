package net.rroadvpn;

import net.rroadvpn.services.Utilities;

public class Test {

    public static void main(String[] args) {
        Utilities utils = new Utilities();
        for (int i = 0; i < 10000000; i++) {
            utils.generateAuthToken();
        }
    }
}
