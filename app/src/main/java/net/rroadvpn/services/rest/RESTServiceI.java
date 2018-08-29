package net.rroadvpn.services.rest;

import net.rroadvpn.model.rest.RESTResponse;

import java.util.HashMap;

public interface RESTServiceI {
    RESTResponse get(String url, HashMap data, HashMap headers);
    RESTResponse put(String url, HashMap data, HashMap headers);
    RESTResponse post(String url, HashMap data, HashMap headers);
    RESTResponse delete(String url, HashMap data, HashMap headers);
}
