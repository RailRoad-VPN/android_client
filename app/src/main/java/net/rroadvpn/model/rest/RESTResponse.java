package net.rroadvpn.model.rest;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RESTResponse {
    public Boolean isOk;
    public String status;
    public Integer code;
    public Map<String, List<String>> headers;
    public Object data;
    public List<RESTError> errors;
    public Integer limit;
    public Integer offset;

    public RESTResponse(String status, int responseCode) {
        this.status = status;
        this.code = responseCode;
    }

    public Boolean getOk() {
        return isOk;
    }

    public void setOk(Boolean ok) {
        isOk = ok;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }

        List<String> values;
        if (this.headers.containsKey(key)) {
            values = this.headers.get(key);
            values.add(value);
        } else {
            values = new ArrayList<>();
            values.add(value);
        }

        this.headers.put(key, values);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<RESTError> getErrors() {
        return errors;
    }

    public void setErrors(List<RESTError> errors) {
        this.errors = errors;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
