package net.rroadvpn.services.rest;

import net.rroadvpn.model.rest.RESTResponse;

import java.util.HashMap;

public class RESTService implements RESTServiceI {

    private HashMap headers;
    private String serviceURL;


    @Override
    public RESTResponse get(String url, HashMap data, HashMap headers) {
        this.prepare(url, headers);
        RESTResponse response = new RESTResponse();
        return response;
    }

    @Override
    public RESTResponse put(String url, HashMap data, HashMap headers) {
        this.prepare(url, headers);
        RESTResponse response = new RESTResponse();
        return response;
    }

    @Override
    public RESTResponse post(String url, HashMap data, HashMap headers) {
        this.prepare(url, headers);
        RESTResponse response = new RESTResponse();
        return response;
    }

    @Override
    public RESTResponse delete(String url, HashMap data, HashMap headers) {
        this.prepare(url, headers);
        RESTResponse response = new RESTResponse();
        return response;
    }

    private void prepare(String url, HashMap headers) {

    }
}


