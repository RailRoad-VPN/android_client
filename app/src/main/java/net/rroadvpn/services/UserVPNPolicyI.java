package net.rroadvpn.services;

import net.rroadvpn.exception.UserPolicyException;

public interface UserVPNPolicyI {
    public void checkPinCode(Integer pinCode) throws UserPolicyException;

    public void createUserDevice(String location) throws UserPolicyException;

    public void afterDisconnectVPN(Long bytesI, Long bytesO) throws UserPolicyException;

    public String getNewRandomVPNServer() throws UserPolicyException;

    public void afterConnectedToVPN(String virtualIP, String deviceIp)
            throws UserPolicyException;

    public void deleteUserSettings() throws UserPolicyException;

    public void sendSupportTicket(String userUuid, String contactEmail, String description,
                                  String extraInfo, byte[] zipFileBytesArr)
            throws UserPolicyException;

    public void sendAnonymousSupportTicket(String contactEmail, String description,
                                           String extraInfo, byte[] zipFileBytesArr)
            throws UserPolicyException;

    public void updateConnection(Long bytesI, Long bytesO, Boolean isConnected, String modifyReason)
            throws UserPolicyException;
}
