package net.rroadvpn.exception;

public class OpenVPNControlServiceException extends Exception {
    public OpenVPNControlServiceException(String s) {
        super(s);
    }

    public OpenVPNControlServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenVPNControlServiceException(Throwable cause) {
        super(cause);
    }
}