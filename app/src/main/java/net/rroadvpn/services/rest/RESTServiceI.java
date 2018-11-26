package net.rroadvpn.services.rest;

import net.rroadvpn.exception.RESTException;
import net.rroadvpn.model.rest.RESTResponse;

import java.util.HashMap;
import java.util.Map;

public interface RESTServiceI {
    RESTResponse get(String url, Map<String, String> headers) throws RESTException;
    RESTResponse put(String url, Map<String, Object> data, Map<String, String> headers) throws RESTException;
    RESTResponse post(String url, Map<String, Object> data, Map<String, String> headers) throws RESTException;
    RESTResponse delete(String url, Map<String, Object> data, Map<String, String> headers) throws RESTException;
}
