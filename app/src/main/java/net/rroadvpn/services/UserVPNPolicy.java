package net.rroadvpn.services;

import net.rroadvpn.exception.UserDeviceNotFoundException;
import net.rroadvpn.exception.UserPolicyException;
import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.User;
import net.rroadvpn.model.UserDevice;
import net.rroadvpn.model.VPNAppPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class UserVPNPolicy implements UserVPNPolicyI {

    private UsersAPIService us;
    private PreferencesService preferencesService;
    private Utilities utilities;

    private String serverUuid;
    private User user;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public UserVPNPolicy(PreferencesService preferencesService) {
        String userServiceURL = VPNAppPreferences.getUserServiceURL("users");

        this.preferencesService = preferencesService;
        this.utilities = new Utilities();

        this.user = new User(
                preferencesService.getString(VPNAppPreferences.USER_UUID)
                , preferencesService.getString(VPNAppPreferences.USER_EMAIL)
        );

        this.us = new UsersAPIService(preferencesService, userServiceURL);
    }

    @Override
    public void checkPinCode(Integer pinCode) throws UserPolicyException {
        log.debug("checkPinCode method enter. Pin:" + String.valueOf(pinCode));
        try {
            user = us.getUserByPinCode(pinCode);
        } catch (UserServiceException e) {
            throw new UserPolicyException(e);
        }
        log.debug("email: {}, userUuid: {}", user.getEmail(), user.getUuid());
        log.debug("checkPinCode method exit");

    }

    @Override
    public void createUserDevice(String location) throws UserPolicyException {
        log.info("createUserDevice method enter");
        try {
            String deviceId = String.valueOf(utilities.getRandomInt(100000, 999999));
            log.debug("Save to preference generated device id: {}", deviceId);
            this.preferencesService.save(VPNAppPreferences.DEVICE_ID, deviceId);

            us.createUserDevice(user.getUuid(), deviceId, location, true);
        } catch (UserServiceException e) {
            throw new UserPolicyException(e);
        }
        log.info("createUserDevice method exit");
    }

    @Override
    public void afterDisconnectVPN(Long bytesI, Long bytesO) throws UserPolicyException {
        log.info("afterDisconnectVPN method enter");

        String connectionUuid = this.preferencesService.getString(VPNAppPreferences.CONNECTION_UUID);
        String userUuid = this.preferencesService.getString(VPNAppPreferences.USER_UUID);
        String serverUuid = this.preferencesService.getString(VPNAppPreferences.SERVER_UUID);
        String userDeviceUuid = this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID);

        String modifyReason = "update traffic";

        try {
            us.updateConnection(connectionUuid, userUuid, serverUuid, userDeviceUuid, bytesI,
                    bytesO, false, modifyReason);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }

        this.preferencesService.save(VPNAppPreferences.CONNECTION_UUID, "");

        log.info("afterDisconnectVPN method exit");
    }

    @Override
    public String getNewRandomVPNServer() throws UserPolicyException {
        log.info("getNewRandomVPNServer method enter");
        String vpnConfig;
        try {
            this.serverUuid = us.getRandomServerUuid(user.getUuid());
            this.preferencesService.save(VPNAppPreferences.SERVER_UUID, serverUuid);
            vpnConfig = us.getVPNConfigurationByUserAndServer(user.getUuid(), serverUuid);
            log.debug("MY CONFIG" + vpnConfig);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }
        log.info("getNewRandomVPNServer method exit");
        return vpnConfig;
    }

    @Override
    public void afterConnectedToVPN(String virtualIP, String deviceIp)
            throws UserPolicyException {
        log.info("afterConnectedToVPN method enter");

        String userDeviceUuid = this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID);
        String userUuid = this.preferencesService.getString(VPNAppPreferences.USER_UUID);

        try {
            String connectionUuid = this.us.createConnection(userUuid, this.serverUuid,
                    userDeviceUuid, deviceIp, virtualIP, null, null);
            this.preferencesService.save(VPNAppPreferences.CONNECTION_UUID, connectionUuid);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }
        log.info("afterConnectedToVPN method exit");
    }

    @Override
    public void updateConnection(Long bytesI, Long bytesO, Boolean isConnected, String modifyReason)
            throws UserPolicyException {
        log.info("updateConnection method enter");

        String connectionUuid = this.preferencesService.getString(VPNAppPreferences.CONNECTION_UUID);
        String userUuid = this.preferencesService.getString(VPNAppPreferences.USER_UUID);
        String serverUuid = this.preferencesService.getString(VPNAppPreferences.SERVER_UUID);
        String userDeviceUuid = this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID);

        if (connectionUuid.equals("") || userUuid.equals("") || serverUuid.equals("") || userDeviceUuid.equals("")) {
            return;
        }

        try {
            this.us.updateConnection(connectionUuid, userUuid, serverUuid, userDeviceUuid, bytesI,
                    bytesO, isConnected, modifyReason);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }
        log.info("updateConnection method exit");
    }

    @Override
    public UserDevice getUserDevice(String userUuid, String uuid) throws UserPolicyException, UserDeviceNotFoundException {
        log.info("getUserDevice method enter");

        UserDevice userDevice;
        try {
            userDevice = this.us.getUserDevice(userUuid, uuid);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }

        log.info("getUserDevice method exit");

        return userDevice;
    }

    @Override
    public boolean isUserDeviceActive() throws UserDeviceNotFoundException {
        try {
            String userDeviceUuid = preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID);
            String userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);

            UserDevice userDevice = this.getUserDevice(userUuid, userDeviceUuid);
            log.error("check user device is active");
            return userDevice.isActive();
        } catch (UserPolicyException e) {
            log.error("UserPolicyException when get user device: {}", e);
            return false;
        }
    }

    @Override
    public void deleteUserSettings() throws UserPolicyException {
        log.info("deleteUserSettings method enter");
        try {
            this.us.deleteUserDevice(this.user.getUuid(), this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID));
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }
        this.preferencesService.clear();
        log.info("deleteUserSettings method exit");
    }

    @Override
    public int sendSupportTicket(String contactEmail, String description, String logsDir) throws UserPolicyException {
        log.info("sendSupportTicket method enter");

        String userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);

        byte[] zipWithFiles = null;
        try {
            log.debug("log dir list files");
            File directory = new File(logsDir);
            File[] files = directory.listFiles();
            log.debug("create zip with log files. files count: " + files.length);
            zipWithFiles = utilities.createZipWithFiles(files);
        } catch (IOException e) {
            log.error("can't create zip with log files for support ticket");
        }

        try {
            log.debug("create ticket");
            int supportTicket = us.createSupportTicket(userUuid, contactEmail, description,
                    null, zipWithFiles);
            log.info("sendSupportTicket method exit");
            return supportTicket;
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }
    }

    @Override
    public int sendAnonymousSupportTicket(String contactEmail, String description, String logsDir)
            throws UserPolicyException {
        return 0;
    }
}
