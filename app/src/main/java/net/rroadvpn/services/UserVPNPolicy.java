package net.rroadvpn.services;

import android.content.Context;

import net.rroadvpn.exception.UserPolicyException;
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

    public void createUserDevice() throws UserPolicyException {
        log.info("createUserDevice method enter");
        try {
            us.createUserDevice(user.getUuid());
        } catch (UserServiceException e) {
            throw new UserPolicyException(e);
        }
        log.info("createUserDevice method exit");
    }

    public void afterDisconnectVPN() {
        log.info("afterDisconnectVPN method enter");
        // TODO
//        us.deleteConnection(serverUuid, user.getEmail());
        log.info("afterDisconnectVPN method exit");
    }


    public String getNewRandomVPNServer() throws UserPolicyException {
        log.info("getNewRandomVPNServer method enter");
        String vpnConfig;
        try {
            this.serverUuid = us.getRandomServerUuid(user.getUuid());
            // TODO проверить наличие профиля в ПрофильМенеджере и если нет то получать через API
            vpnConfig = us.getVPNConfigurationByUserAndServer(user.getUuid(), serverUuid);
            log.debug("MY CONFIG" + vpnConfig);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }
        log.info("getNewRandomVPNServer method exit");
        return vpnConfig;
    }

    public void afterConnectedToVPN(String virtualIP) throws UserPolicyException {
        log.info("afterConnectedToVPN method enter");

        try {
            // TODO device_ip
            this.us.updateUserDevice(this.user.getUuid(), this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID), virtualIP, "1.1.1.1");

            String email = this.preferencesService.getString(VPNAppPreferences.USER_EMAIL);

            this.us.createConnection(this.serverUuid, virtualIP, "1.1.1.1", email);
        } catch (UserServiceException e) {
            log.error("UserServiceException: {}", e);
            throw new UserPolicyException(e);
        }
        log.info("afterConnectedToVPN method exit");
    }

    public void deleteUserSettings() throws UserPolicyException {
        log.info("deleteUserSettings method enter");
        try {
            this.us.deleteUserDevice(this.user.getUuid(), this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID));
        } catch (UserServiceException e) {
            throw new UserPolicyException(e);
        }
        this.preferencesService.clear();
        log.info("deleteUserSettings method exit");
    }
}
