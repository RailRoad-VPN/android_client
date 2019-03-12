package net.rroadvpn.exception;

public class UserDeviceNotFoundException extends Exception {
    public UserDeviceNotFoundException(String s) {
        super(s);
    }

    public UserDeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDeviceNotFoundException(Throwable cause) {
        super(cause);
    }
}