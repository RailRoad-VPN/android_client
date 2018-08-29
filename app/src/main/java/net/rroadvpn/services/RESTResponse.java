package net.rroadvpn.services;

import java.util.HashMap;
import java.util.List;

public class RESTResponse {
    public Boolean isOk;
    public String status;
    public Integer code;
    public HashMap headers;
    public HashMap data;
    public List<RESTError> errors;
    public Integer limit;
    public Integer offset;

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

    public HashMap getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap headers) {
        this.headers = headers;
    }

    public HashMap getData() {
        return data;
    }

    public void setData(HashMap data) {
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
