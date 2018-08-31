package net.rroadvpn.activities;


import android.content.Intent;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.User;
import net.rroadvpn.openvpn.LaunchVPN;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.VpnProfile;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.openvpn.activities.DisconnectVPN;
import net.rroadvpn.openvpn.core.ProfileManager;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.UsersService;

public class NewMainActivity extends BaseActivity {

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_main_activity);

        String apiURL = "http://internal.novicorp.com:61885";
        String apiVer = "v1";

        String usersAPIResourceName = "users";
        String userServiceURL = apiURL + "/api/" + apiVer + "/" + usersAPIResourceName;

        UsersService usersService = new UsersService(userServiceURL);
        try {
            User userByPinCode = usersService.getUserByPinCode(3261);
            System.out.println(userByPinCode.getEmail());
        } catch (UserServiceException e) {
            e.printStackTrace();
        }
    }

    private ProfileManager getPM() {
        return ProfileManager.getInstance(getBaseContext());
    }

    private void connectToVpn() {
        String configBase64 = TestConfig.conf_base64;

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
