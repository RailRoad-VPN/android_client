package net.rroadvpn.services;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.User;

interface UsersAPIServiceI {
    User getUserByPinCode(Integer pincode) throws UserServiceException;
    User getUserByUuid(String uuid) throws UserServiceException;
    void createUserDevice(String userUuid) throws UserServiceException;
    String getRandomServerUuid(String userUuid) throws UserServiceException;
    String getVPNConfigurationByUserAndServer(String userUuid, String serverUuid) throws UserServiceException;
    void updateUserDevice(String userUuid, String userDeviceUuid, String virtualIp, String deviceIp) throws UserServiceException;
    void createConnection(String serverUuid, String virtualIp, String deviceIp, String email) throws UserServiceException;
    void deleteConnection(String serverUuid, String email) throws UserServiceException;
    void deleteUserDevice(String userUuid, String userDeviceUuid) throws UserServiceException;
}
