package net.rroadvpn.exception;

public class RESTNotFoundException extends RESTException {
    public RESTNotFoundException(String s) {
        super(s);
    }

    public RESTNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RESTNotFoundException(Throwable cause) {
        super(cause);
    }
}