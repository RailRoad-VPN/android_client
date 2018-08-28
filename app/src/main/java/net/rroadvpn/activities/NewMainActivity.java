package net.rroadvpn.activities;


import android.content.Intent;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.activities.BaseActivity;
import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;

public class NewMainActivity extends BaseActivity {

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_main_activity);


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

    private ProfileManager getPM() {
        return ProfileManager.getInstance(getBaseContext());
    }
}
