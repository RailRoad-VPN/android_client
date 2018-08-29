package net.rroadvpn.services;

import java.util.HashMap;

public interface RESTServiceI {
    RESTResponse get(String url, HashMap data, HashMap headers);
    RESTResponse put(String url, HashMap data, HashMap headers);
    RESTResponse post(String url, HashMap data, HashMap headers);
}
