package net.rroadvpn.activities;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.VpnProfile;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.openvpn.activities.DisconnectVPN;
import net.rroadvpn.openvpn.core.ConnectionStatus;
import net.rroadvpn.openvpn.core.Preferences;
import net.rroadvpn.openvpn.core.ProfileManager;
import net.rroadvpn.openvpn.core.VPNLaunchHelper;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UsersService;

import java.io.IOException;

public class NewMainActivity2 extends BaseActivity {
    private UsersService us;
    private String userUuid;
    private String serverUuid;
    private PreferencesService preferencesService;
    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    private boolean mCmfixed = false;
    public static String EXTRA_KEY = "net.rroadvpn.openvpn.shortcutProfileUUID";
    public static final String EXTRA_NAME = "net.rroadvpn.openvpn.shortcutProfileName";
    public static final Boolean EXTRA_HIDELOG = false;
    public static final String CLEARLOG = "clearlogconnect";

    // TODO
    private VpnProfile mSelectedProfile;


    private static final int VPN_SERVICE_INTENT_PERMISSION = 70;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String apiURL = "http://rroadvpn.net:61885";
        String apiVer = "v1";

        String usersAPIResourceName = "users";
        String userServiceURL = apiURL + "/api/" + apiVer + "/" + usersAPIResourceName;

        setContentView(R.layout.new_main_activity2);

        this.preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
        this.userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);


        this.us = new UsersService(preferencesService, userServiceURL);

        Button button = (Button) findViewById(R.id.fasd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "to another view", Toast.LENGTH_SHORT).show();
                try {
                    serverUuid = us.getRandomServerUuid(userUuid);
                    String vpnConfig = us.getVpnConfigByUuid(userUuid, serverUuid);
                    System.out.println("MY CONFIG" + vpnConfig);
                    prepareToConnectVPN(vpnConfig);
                } catch (UserServiceException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    private ProfileManager getPM() {
        return ProfileManager.getInstance(getBaseContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VPN_SERVICE_INTENT_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                this.connectToVPN();
            } else {
                // TODO make error
            }
        } else {
            // TODO make error
        }
    }

    private void connectToVPN() {
        SharedPreferences prefs2 = Preferences.getDefaultSharedPreferences(getBaseContext());
        boolean showLogWindow = prefs2.getBoolean("showlogwindow", true);

        ProfileManager.updateLRU(getBaseContext(), mSelectedProfile);
        VPNLaunchHelper.startOpenVpn(mSelectedProfile, getBaseContext());
        setResult(RESULT_OK, null);
        this.workWithAPIDeviceConnectionAndSoOn();
    }

    private void workWithAPIDeviceConnectionAndSoOn() {
        System.out.println("#####################################################  MAIN!!!!" + VpnStatus.getLastCleanLogMessage(getBaseContext()));
        String status = VpnStatus.getLastCleanLogMessage(getBaseContext());

        while (!status.contains("Connected: SUCCESS")) {
//                System.out.println("openVPN log:" + VpnStatus.getLastCleanLogMessage(getBaseContext()));
            status = VpnStatus.getLastCleanLogMessage(getBaseContext());
        }

        System.out.println("WHILE ENDED");
        String virtualIP = status.split(",")[1];
        Toast.makeText(getBaseContext(), "YOUR VIRTUAL IP IS: " + virtualIP, Toast.LENGTH_LONG).show();

        try {
            String apiURL = "http://rroadvpn.net:61885";
            String apiVer = "v1";

            String usersAPIResourceName = "users";
            String userServiceURL = apiURL + "/api/" + apiVer + "/" + usersAPIResourceName;

            setContentView(R.layout.new_main_activity2);

            this.preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
            this.userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);


            this.us = new UsersService(preferencesService, userServiceURL);

            //todo email, device_ip
            System.out.println("Update user device begin");
            this.us.updateUserDevice(this.userUuid, this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID), virtualIP, "1.1.1.1");
            System.out.println("Create connection begin");
            this.us.createConnection(this.serverUuid, virtualIP, "1.1.1.1", "t@t.t");
        } catch (UserServiceException e) {
            e.printStackTrace();
        }

        Toast.makeText(getBaseContext(), "PUT COMPLETED SUCCESSFULLY!", Toast.LENGTH_SHORT).show();
    }

    public void prepareToConnectVPN(String configBase64) {
        byte[] decoded = android.util.Base64.decode(configBase64, android.util.Base64.DEFAULT);

        OpenVPNProfileManager openVPNProfileManager = new OpenVPNProfileManager(decoded);

        VpnProfile profile;
        try {
            profile = openVPNProfileManager.getVPNProfile();
            ProfileManager pm = getPM();
            pm.addProfile(profile);
            pm.saveProfileList(getBaseContext());
            pm.saveProfile(getBaseContext(), profile);

            this.mSelectedProfile = profile;

            EXTRA_KEY = profile.getUUID().toString();
                
        } catch (OpenVPNProfileException e) {
            e.printStackTrace();
            return;
        }

        // Check if we need to clear the log
        if (Preferences.getDefaultSharedPreferences(this).getBoolean(CLEARLOG, true))
            VpnStatus.clearLog();

        int vpnok = profile.checkProfile(getBaseContext());
        if (vpnok != R.string.no_error_found) {
            return;
        }

        Intent vpnPrepareIntent = VpnService.prepare(getBaseContext());
        // Check if we want to fix /dev/tun
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(getBaseContext());
        boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
        boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

        if (loadTunModule) {
            String command = "insmod /system/lib/modules/tun.ko";
            try {
                ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
                Process p = pb.start();
                int ret = p.waitFor();
                if (ret == 0)
                    mCmfixed = true;
            } catch (InterruptedException | IOException e) {
                VpnStatus.logException("SU command", e);
            }
        }
        if (usecm9fix && !mCmfixed) {
            String command = "chown system /dev/tun";
            try {
                ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
                Process p = pb.start();
                int ret = p.waitFor();
                if (ret == 0)
                    mCmfixed = true;
            } catch (InterruptedException | IOException e) {
                VpnStatus.logException("SU command", e);
            }
        }

        VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);

        if (vpnPrepareIntent == null) {
            this.connectToVPN();
        } else {
            try {
                startActivityForResult(vpnPrepareIntent, VPN_SERVICE_INTENT_PERMISSION);
            } catch (ActivityNotFoundException ane) {
                // Shame on you Sony! At least one user reported that
                // an official Sony Xperia Arc S image triggers this exception
                VpnStatus.logError("ActivityNotFoundException PIZDEC!!!!!");
                System.out.println("ActivityNotFoundException PIZDEC!!!!!!!!!!!");
            }
        }
    }
}
