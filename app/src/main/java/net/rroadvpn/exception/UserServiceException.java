package net.rroadvpn.exception;

public class UserServiceException extends Exception {
    public UserServiceException(String s) {
        super(s);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserServiceException(Throwable cause) {
        super(cause);
    }
}