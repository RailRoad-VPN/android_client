package net.rroadvpn.activities;


import android.content.Intent;
import android.widget.Toast;

import net.rroadvpn.exception.UserServiceException;
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

//test get user by pin
//        try {
//            User user = usersService.getUserByPinCode(7087);
//            System.out.println(user.getEmail());
//        } catch (UserServiceException e) {
//            Toast.makeText(getBaseContext(), "wrong pin?", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//test get user by uuid
//        try {
//            User user = usersService.getUserByUuid("cf402144-0c02-4b97-98f2-73f7b56160cf");
//            System.out.println(user.getEmail());
//        } catch (UserServiceException e) {
//            Toast.makeText(getBaseContext(), "wrong uuid", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//test post userDevice
        try {
            usersService.postUserDevice("cf402144-0c02-4b97-98f2-73f7b56160cf");
        } catch (UserServiceException e) {
            Toast.makeText(getBaseContext(), "bad post", Toast.LENGTH_SHORT).show();
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
