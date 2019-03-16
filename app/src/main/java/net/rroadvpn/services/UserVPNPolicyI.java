package net.rroadvpn.services;

import net.rroadvpn.exception.UserDeviceNotFoundException;
import net.rroadvpn.exception.UserPolicyException;
import net.rroadvpn.model.UserDevice;

public interface UserVPNPolicyI {
    void checkPinCode(Integer pinCode) throws UserPolicyException;

    void createUserDevice(String location) throws UserPolicyException;

    void afterDisconnectVPN(Long bytesI, Long bytesO) throws UserPolicyException;

    String getRandomVPNServerUuid() throws UserPolicyException;
    String getVPNServerByUuid(String serverUuid) throws UserPolicyException;

    void afterConnectedToVPN(String virtualIP, String deviceIp)
            throws UserPolicyException;

    void deleteUserSettings() throws UserPolicyException;

    int sendSupportTicket(String contactEmail, String description, String logsDir) throws UserPolicyException;

    void updateConnection(Long bytesI, Long bytesO, Boolean isConnected, String modifyReason)
            throws UserPolicyException;

    UserDevice getUserDevice(String userUuid, String uuid) throws UserPolicyException, UserDeviceNotFoundException;

    boolean isUserDeviceActive() throws UserDeviceNotFoundException;
}
