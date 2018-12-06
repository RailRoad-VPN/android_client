package net.rroadvpn.services;

import android.content.Context;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.User;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.core.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class UserVPNPolicy {

    private UsersAPIService us;
    private PreferencesService preferencesService;

    private String serverUuid;
    private User user;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public UserVPNPolicy(PreferencesService preferencesService) {
        String userServiceURL = VPNAppPreferences.getUserServiceURL("users");

        this.preferencesService = preferencesService;

        this.user = new User(
                preferencesService.getString(VPNAppPreferences.USER_UUID)
                , preferencesService.getString(VPNAppPreferences.USER_EMAIL)
        );

        this.us = new UsersAPIService(preferencesService, userServiceURL);
    }

    public void checkPinCode(Integer pinCode) throws UserServiceException {
        log.debug("checkPinCode method enter. Pin:" + String.valueOf(pinCode));
        user = us.getUserByPinCode(pinCode);
        log.debug("email:" + user.getEmail() +
                ", userUuid:" + user.getUuid()
        );

    }

    public void createUserDevice() throws UserServiceException {
        log.info("createUserDevice method enter");
        us.createUserDevice(user.getUuid());
        log.info("createUserDevice method exit");
    }

    public void afterDisconnectVPN() {
        log.info("afterDisconnectVPN method enter");
//        us.deleteConnection(serverUuid, user.getEmail());
        log.info("afterDisconnectVPN method exit");
    }


    public String getNewRandomVPNServer() {
        log.info("getNewRandomVPNServer method enter");
        try {
            this.serverUuid = us.getRandomServerUuid(user.getUuid());
            // TODO проверить наличие профиля в ПрофильМенеджере и если нет то получать через API
            String vpnConfig = us.getVPNConfigurationByUserAndServer(user.getUuid(), serverUuid);
            log.debug("MY CONFIG" + vpnConfig);
            log.info("getNewRandomVPNServer method exit");
            return vpnConfig;
        } catch (UserServiceException e) {
            log.error(String.format("Message: %s\nStackTrace: %s"
                    , e.getMessage()
                    , Arrays.toString(e.getStackTrace())
            ));
        }
        return null;
    }

    public void afterConnectedToVPN(String virtualIP) {
        log.info("afterConnectedToVPN method enter");

        try {
            // TODO device_ip
            this.us.updateUserDevice(this.user.getUuid(), this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID), virtualIP, "1.1.1.1");

            String email = this.preferencesService.getString(VPNAppPreferences.USER_EMAIL);

            this.us.createConnection(this.serverUuid, virtualIP, "1.1.1.1", email);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
        }
        log.info("afterConnectedToVPN method exit");
    }

//    private void reInitUserServiceCrutch() {
//        log.info("reInitUserServiceCrutch method enter");
//        String userServiceURL = VPNAppPreferences.getUserServiceURL("users");
//
//        this.us = new UsersAPIService(preferencesService, userServiceURL);
//        log.info("reInitUserServiceCrutch method exit");
//    }

    public void deleteUserSettings() {
        log.info("deleteUserSettings method enter");
        this.us.deleteUserDevice(user.getUuid(), this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID));
        this.preferencesService.clear();
        log.info("deleteUserSettings method exit");
    }
}
