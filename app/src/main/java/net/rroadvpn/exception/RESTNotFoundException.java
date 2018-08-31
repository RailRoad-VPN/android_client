package net.rroadvpn.exception;

public class RESTNotFoundException extends Exception {
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