package net.rroadvpn.services;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.widget.Toast;

import net.rroadvpn.activities.NewMainActivity2;
import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.User;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.core.VpnStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


//import java.util.concurrent.ExecutionException;

public class UserVPNPolicy {
    private UsersAPIService us;

    private String serverUuid;
    private String userUuid;
    private Context ctx;
    private PreferencesService preferencesService;
    private Logger log = LoggerFactory.getLogger(UserVPNPolicy.class);


    public UserVPNPolicy(Context ctx) {
        this.ctx = ctx;
        String userServiceURL = VPNAppPreferences.getUserServiceURL("users");

        this.preferencesService = new PreferencesService(ctx, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
        this.userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);

        this.us = new UsersAPIService(preferencesService, userServiceURL);
    }

    public void checkPinCode(Integer pinCode) throws UserServiceException {
        log.info("checkPinCode method enter. Pin:" + String.valueOf(pinCode));
        User user = us.getUserByPinCode(pinCode);
        log.debug("email:" + user.getEmail() +
                ", userUuid:" + user.getUuid() +
                ", createdDate" + user.getCreatedDate()
        );

        this.userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);
    }

    public void createUserDevice() throws UserServiceException {
        log.info("createUserDevice method enter");
        us.createUserDevice(userUuid);
        log.info("createUserDevice method exit");
    }

    public void afterDisconnectVPN() {
        log.info("afterDisconnectVPN method enter");
        reInitUserServiceCrutch();
        us.deleteConnection();
        log.info("afterDisconnectVPN method exit");
    }


    public String getNewRandomVPNServer() {
        log.info("getNewRandomVPNServer method enter");
        try {
            this.serverUuid = us.getRandomServerUuid(userUuid);
            // TODO проверить наличие профиля в ПрофильМенеджере и если нет то получать через API
            String vpnConfig = us.getVPNConfigurationByUserAndServer(userUuid, serverUuid);
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

    public void afterConnectedToVPN() {
        log.info("afterConnectedToVPN method enter");

        String status = VpnStatus.getLastCleanLogMessage(this.ctx);

        while (!status.contains("Connected: SUCCESS")) {
//               System.out.println("openVPN log:" + VpnStatus.getLastCleanLogMessage(this.ctx));
            status = VpnStatus.getLastCleanLogMessage(this.ctx);
        }

        log.info("afterConnectedToVPN while ended. Status contains SUCCESS");
        String virtualIP = status.split(",")[1];
//        Toast.makeText(this.ctx, "YOUR VIRTUAL IP IS: " + virtualIP, Toast.LENGTH_LONG).show();

        try {
            //TODO cut second UsersService init
            reInitUserServiceCrutch();
            //todo device_ip
            this.us.updateUserDevice(this.userUuid, this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID), virtualIP, "1.1.1.1");

            String email = this.preferencesService.getString(VPNAppPreferences.USER_EMAIL);
            System.out.println("Create connection begin");
            this.us.createConnection(this.serverUuid, virtualIP, "1.1.1.1", email);
        } catch (UserServiceException e) {
            log.error(String.format("Message: %s\nStackTrace: %s"
                    , e.getMessage()
                    , Arrays.toString(e.getStackTrace())
            ));
        }
        log.info("afterConnectedToVPN method exit");
    }

    private void reInitUserServiceCrutch() {
        log.info("reInitUserServiceCrutch method enter");
        String userServiceURL = VPNAppPreferences.getUserServiceURL("users");

        this.preferencesService = new PreferencesService(this.ctx, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
        this.userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);

        this.us = new UsersAPIService(preferencesService, userServiceURL);
        log.info("reInitUserServiceCrutch method exit");
    }
}
