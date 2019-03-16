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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserVPNPolicy implements UserVPNPolicyI {

    private UsersAPIService us;
    private PreferencesService preferencesService;
    private Utilities utilities;

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
        } catch (Exception e) {
            log.error("Exception: {}", e);
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
        } catch (Exception e) {
            log.error("Exception: {}", e);
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
        } catch (Exception e) {
            log.error("Exception: {}", e);
            throw new UserPolicyException(e);
        }

        this.preferencesService.save(VPNAppPreferences.CONNECTION_UUID, "");

        log.info("afterDisconnectVPN method exit");
    }

    @Override
    public String getVPNServerByUuid(String serverUuid) throws UserPolicyException {
        log.info("getVPNServerByUuid method enter");
        try {
            String userUuid = this.preferencesService.getString(VPNAppPreferences.USER_UUID);

            String vpnConfigurationByUserAndServer = us.getVPNConfigurationByUserAndServer(userUuid, serverUuid);

            log.info("getVPNServerByUuid method exit");

            return vpnConfigurationByUserAndServer;
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        } catch (Exception e) {
            log.error("Exception: {}", e);
            throw new UserPolicyException(e);
        }
    }

    @Override
    public String getRandomVPNServerUuid() throws UserPolicyException {
        log.info("getRandomVPNServerUuid method enter");
        try {
            String serverUuid = us.getRandomServerUuid(user.getUuid());

            this.preferencesService.save(VPNAppPreferences.SERVER_UUID, serverUuid);

            log.info("getRandomVPNServerUuid method exit");

            return serverUuid;
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        } catch (Exception e) {
            log.error("Exception: {}", e);
            throw new UserPolicyException(e);
        }
    }

    @Override
    public void afterConnectedToVPN(String virtualIP, String deviceIp)
            throws UserPolicyException {
        log.info("afterConnectedToVPN method enter");

        String userDeviceUuid = this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID);
        String userUuid = this.preferencesService.getString(VPNAppPreferences.USER_UUID);
        String serverUuid = this.preferencesService.getString(VPNAppPreferences.SERVER_UUID);

        try {
            String connectionUuid = this.us.createConnection(userUuid, serverUuid,
                    userDeviceUuid, deviceIp, virtualIP, null, null);
            this.preferencesService.save(VPNAppPreferences.CONNECTION_UUID, connectionUuid);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        } catch (Exception e) {
            log.error("Exception: {}", e);
            throw new UserPolicyException(e);
        }
        log.info("afterConnectedToVPN method exit");
    }

    @Override
    public void updateConnection(Long bytesI, Long bytesO, Boolean isConnected, String modifyReason)
            throws UserPolicyException {

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
        } catch (Exception e) {
            log.error("Exception: {}", e);
            throw new UserPolicyException(e);
        }
    }

    @Override
    public UserDevice getUserDevice(String userUuid, String uuid) throws UserPolicyException, UserDeviceNotFoundException {
        UserDevice userDevice;
        try {
            userDevice = this.us.getUserDevice(userUuid, uuid);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        } catch (Exception e) {
            log.error("Exception: {}", e);
            throw new UserPolicyException(e);
        }

        return userDevice;
    }

    @Override
    public boolean isUserDeviceActive() throws UserDeviceNotFoundException {
        try {
            String userDeviceUuid = preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID);
            String userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);

            UserDevice userDevice = this.getUserDevice(userUuid, userDeviceUuid);
            return userDevice.isActive();
        } catch (UserPolicyException e) {
            log.error("UserPolicyException when get user device: {}", e);
            return false;
        } catch (Exception e) {
            log.error("Exception when get user device: {}", e);
            return false;
        }
    }

    @Override
    public void deleteUserSettings() throws UserPolicyException {
        log.info("deleteUserSettings method enter");

        try {
            this.us.deleteUserDevice(this.user.getUuid(), this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID));
        } catch (UserServiceException e) {
            log.error("UserServiceException when delete user device: {}", e);
            throw new UserPolicyException(e);
        } catch (Exception e) {
            log.error("Exception when delete user device: {}", e);
            throw new UserPolicyException(e);
        } finally {
            this.preferencesService.clear();
        }
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
        } catch (Exception e) {
            log.error("Exception. can't create zip with log files for support ticket", e);
        }

        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("app_version", this.preferencesService.getString(VPNAppPreferences.APP_VERSION));
        extraInfo.put("sdk", android.os.Build.VERSION.SDK);
        extraInfo.put("device", android.os.Build.DEVICE);
        extraInfo.put("model", android.os.Build.MODEL);
        extraInfo.put("product", android.os.Build.PRODUCT);
        extraInfo.put("version_release", android.os.Build.VERSION.RELEASE);

        String os_version = System.getProperty("os.version");
        if (os_version != null) {
            extraInfo.put("os_version", os_version);
        }

        if (contactEmail == null || contactEmail.equals("")) {
            contactEmail = this.preferencesService.getString(VPNAppPreferences.USER_EMAIL);
        }

        if (userUuid != null && !userUuid.equals("")) {
            extraInfo.put("user_device_uuid", this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID));
            extraInfo.put("user_email", this.preferencesService.getString(VPNAppPreferences.USER_EMAIL));
            extraInfo.put("user_uuid", this.preferencesService.getString(VPNAppPreferences.USER_UUID));
            extraInfo.put("server_uuid", this.preferencesService.getString(VPNAppPreferences.SERVER_UUID));
            extraInfo.put("device_id", this.preferencesService.getString(VPNAppPreferences.DEVICE_ID));
            extraInfo.put("device_token", this.preferencesService.getString(VPNAppPreferences.DEVICE_TOKEN));
        }

        try {
            log.debug("create ticket");
            int supportTicket = us.createSupportTicket(userUuid, contactEmail, description,
                    extraInfo, zipWithFiles);
            log.info("sendSupportTicket method exit");
            return supportTicket;
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        } catch (Exception e) {
            log.error("Exception: {}", e);
            throw new UserPolicyException(e);
        }
    }
}
