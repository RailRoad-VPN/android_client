package net.rroadvpn.services;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.User;

interface UsersAPIServiceI {
    User getUserByPinCode(Integer pincode) throws UserServiceException;

    User getUserByUuid(String uuid) throws UserServiceException;

    void createUserDevice(String userUuid, String deviceId, String location, boolean isActive) throws UserServiceException;

    String getRandomServerUuid(String userUuid) throws UserServiceException;

    String getVPNConfigurationByUserAndServer(String userUuid, String serverUuid) throws UserServiceException;

    void updateUserDevice(String deviceUuid, String userUuid, String location, boolean isActive,
                          String modifyReason) throws UserServiceException;

    String createConnection(String userUuid, String serverUuid, String userDeviceUuid,
                            String deviceIp, String virtualIp, Long bytesI, Long bytesO)
            throws UserServiceException;

    void updateConnection(String connectionUuid, String userUuid, String serverUuid,
                          String userDeviceUuid, Long bytesI, Long bytesO, Boolean isConnected,
                          String modifyReason) throws UserServiceException;

    void deleteUserDevice(String userUuid, String userDeviceUuid) throws UserServiceException;
}
