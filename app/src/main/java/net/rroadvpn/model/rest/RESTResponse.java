package net.rroadvpn.model.rest;

import org.json.JSONArray;

import java.util.List;

public class RESTResponse {
    public Boolean isOk;
    public String status;
    public Integer code;
    public JSONArray headers;
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

    public JSONArray getHeaders() {
        return headers;
    }

    public void setHeaders(JSONArray headers) {
        this.headers = headers;
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
