package net.rroadvpn.exception;

public class UserPolicyException extends Exception {
    public UserPolicyException(String s) {
        super(s);
    }

    public UserPolicyException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserPolicyException(Throwable cause) {
        super(cause);
    }
}