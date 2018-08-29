package net.rroadvpn.model.rest;

public class RESTError {
    public Integer code;
    public String message;
    public String developer_message;

    public RESTError(Integer code, String message, String developer_message) {
        this.code = code;
        this.message = message;
        this.developer_message = developer_message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeveloper_message() {
        return developer_message;
    }

    public void setDeveloper_message(String developer_message) {
        this.developer_message = developer_message;
    }
}
