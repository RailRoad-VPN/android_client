package net.rroadvpn.activities;


import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.model.Preferences;
import net.rroadvpn.openvpn.LaunchVPN;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.VpnProfile;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.openvpn.activities.DisconnectVPN;
import net.rroadvpn.openvpn.core.ProfileManager;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.PreferencesService;

public class NewMainActivity2 extends BaseActivity {

    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_main_activity2);

        PreferencesService preferencesService = new PreferencesService(this, Preferences.PREF_USER_GLOBAL_KEY);

        String device_token = preferencesService.getString(Preferences.DEVICE_TOKEN);

        Button button = (Button) findViewById(R.id.fasd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "to another view", Toast.LENGTH_SHORT).show();
                connectToVpn();
            }
        });

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
