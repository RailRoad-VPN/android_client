package net.rroadvpn.activities;


import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.rroadvpn.activities.pin.InputPinView;
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

    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String apiURL = "http://internal.novicorp.com:61885";
        String apiVer = "v1";

        String usersAPIResourceName = "users";
        String userServiceURL = apiURL + "/api/" + apiVer + "/" + usersAPIResourceName;

        setContentView(R.layout.new_main_activity2);

        PreferencesService preferencesService = new PreferencesService(this, Preferences.PREF_USER_GLOBAL_KEY);
        String userUuid = preferencesService.getString(Preferences.USER_UUID);


        UsersService us = new UsersService(preferencesService, userServiceURL);

//        us.generateAuthToken();

        Button button = (Button) findViewById(R.id.fasd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "to another view", Toast.LENGTH_SHORT).show();
                try {
                    String serverUuid = us.getRandomServerUuid(userUuid);
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
                startActivity(intent);
            }
        } catch (OpenVPNProfileException e) {
            e.printStackTrace();
        }
    }
}
