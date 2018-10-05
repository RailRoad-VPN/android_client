package net.rroadvpn.activities;


import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.Preferences;
import net.rroadvpn.openvpn.LaunchVPN;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.VpnProfile;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.openvpn.activities.DisconnectVPN;
import net.rroadvpn.openvpn.core.ProfileManager;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UsersService;

public class NewMainActivity2 extends BaseActivity {
    private UsersService us;
    private String userUuid;
    private String serverUuid;
    private PreferencesService preferencesService;
    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String apiURL = "http://internal.novicorp.com:61885";
        String apiVer = "v1";

        String usersAPIResourceName = "users";
        String userServiceURL = apiURL + "/api/" + apiVer + "/" + usersAPIResourceName;

        setContentView(R.layout.new_main_activity2);

        this.preferencesService = new PreferencesService(this, Preferences.PREF_USER_GLOBAL_KEY);
        this.userUuid = preferencesService.getString(Preferences.USER_UUID);


        this.us = new UsersService(preferencesService, userServiceURL);

//        us.generateAuthToken();

        Button button = (Button) findViewById(R.id.fasd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "to another view", Toast.LENGTH_SHORT).show();
                try {
                    serverUuid = us.getRandomServerUuid(userUuid);
                    String vpnConfig = us.getVpnConfigByUuid(userUuid, serverUuid);
                    connectToVpn(vpnConfig);
                } catch (UserServiceException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    private ProfileManager getPM() {
        return ProfileManager.getInstance(getBaseContext());
    }

    private void connectToVpn(String configBase64) {

        byte[] decoded = android.util.Base64.decode(configBase64, android.util.Base64.DEFAULT);

        OpenVPNProfileManager openVPNProfileManager = new OpenVPNProfileManager(decoded);

        try {
            VpnProfile profile = openVPNProfileManager.getVPNProfile();
            if (VpnStatus.isVPNActive() && profile.getUUIDString().equals(VpnStatus.getLastConnectedVPNProfile())) {
                Intent disconnectVPN = new Intent(getBaseContext(), DisconnectVPN.class);
                startActivity(disconnectVPN);
            } else {

                getPM().addProfile(profile);
                getPM().saveProfileList(getBaseContext());
                getPM().saveProfile(getBaseContext(), profile);

                Intent intent = new Intent(getBaseContext(), LaunchVPN.class);
                intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
                intent.setAction(Intent.ACTION_MAIN);
                startActivityForResult(intent, 1000);
            }
        } catch (OpenVPNProfileException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            System.out.println("#####################################################  MAIN!!!!" + VpnStatus.getLastCleanLogMessage(getBaseContext()));
            String status = VpnStatus.getLastCleanLogMessage(getBaseContext());
            while (!status.contains("Connected: SUCCESS")) {
                status = VpnStatus.getLastCleanLogMessage(getBaseContext());
            }

            String virtualIP = status.split(",")[1];
            Toast.makeText(getBaseContext(), "YOUR VIRTUAL IP IS: " + virtualIP, Toast.LENGTH_LONG).show();


            try {
                //todo email, device_ip
                System.out.println("Update user device begin");
                this.us.updateUserDevice(this.userUuid, this.preferencesService.getString(Preferences.USER_DEVICE_UUID), virtualIP, "1.1.1.1");
                System.out.println("Create connection begin");
                this.us.createConnection(this.serverUuid,virtualIP,"1.1.1.1","t@t.t");
            } catch (UserServiceException e) {
                e.printStackTrace();
            }

            Toast.makeText(getBaseContext(), "PUT COMPLETED SUCCESSFULLY!", Toast.LENGTH_SHORT).show();
        }
    }
}
