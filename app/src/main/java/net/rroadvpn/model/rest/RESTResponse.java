package net.rroadvpn.model.rest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class RESTResponse {
    public Boolean isOk;
    public String status;
    public Integer code;
    public JSONArray headers;
    public JSONObject data;
    public List<RESTError> errors;
    public Integer limit;
    public Integer offset;

    public RESTResponse(String status, Integer code, JSONArray headers) {
        this.status = status;
        this.code = code;
        this.headers = headers;
    }

    public RESTResponse(String status, Integer code, JSONObject data) {
        this.status = status;
        this.code = code;
        this.data = data;
    }

    public RESTResponse(String status, Integer code, List<RESTError> errors) {
        this.status = status;
        this.code = code;
        this.errors = errors;
    }

    public RESTResponse(String status, Integer code, JSONArray headers, JSONObject data) {
        this.status = status;
        this.code = code;
        this.headers = headers;
        this.data = data;
    }

    public RESTResponse(String status, Integer code, JSONArray headers, JSONObject data,
                        Integer limit, Integer offset) {
        this.status = status;
        this.code = code;
        this.headers = headers;
        this.data = data;
        this.limit = limit;
        this.offset = offset;
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

    public JSONArray getHeaders() {
        return headers;
    }

    public void setHeaders(JSONArray headers) {
        this.headers = headers;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
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
