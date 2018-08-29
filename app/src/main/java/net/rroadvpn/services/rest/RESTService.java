package net.rroadvpn.services.rest;

import net.rroadvpn.model.rest.RESTResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Map;

public class RESTService implements RESTServiceI {

    private Map<String, String> headers = new HashMap<>();
    private String serviceURL;

    public RESTService(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    @Override
    public RESTResponse get(String url, Map<String, Object> data, Map<String, String> headers) {
        if (url == null) {
            url = this.serviceURL;
        }
        if (headers != null) {
            this.headers.putAll(headers);
        }
        RESTResponse response = new RESTResponse();
        return response;
    }

    @Override
    public RESTResponse put(String url, Map<String, Object> data, Map<String, String> headers) {
        if (url == null) {
            url = this.serviceURL;
        }
        if (headers != null) {
            this.headers.putAll(headers);
        }
        RESTResponse response = new RESTResponse();
        return response;
    }

    @Override
    public RESTResponse post(String url, Map<String, Object> data, Map<String, String> headers) {
        if (url == null) {
            url = this.serviceURL;
        }
        if (headers != null) {
            this.headers.putAll(headers);
        }
        RESTResponse response = new RESTResponse();
        return response;
    }

    @Override
    public RESTResponse delete(String url, Map<String, Object> data, Map<String, String> headers) {
        if (url == null) {
            url = this.serviceURL;
        }
        if (headers != null) {
            this.headers.putAll(headers);
        }
        RESTResponse response = new RESTResponse();
        return response;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }
}


