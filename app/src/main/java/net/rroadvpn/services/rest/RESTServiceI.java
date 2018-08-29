package net.rroadvpn.services.rest;

import net.rroadvpn.model.rest.RESTResponse;

import java.util.HashMap;
import java.util.Map;

public interface RESTServiceI {
    RESTResponse get(String url, Map<String, Object> data, Map<String, String> headers);
    RESTResponse put(String url, Map<String, Object> data, Map<String, String> headers);
    RESTResponse post(String url, Map<String, Object> data, Map<String, String> headers);
    RESTResponse delete(String url, Map<String, Object> data, Map<String, String> headers);
}
