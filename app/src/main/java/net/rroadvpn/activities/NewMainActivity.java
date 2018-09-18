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
import net.rroadvpn.services.UsersService;

public class NewMainActivity extends BaseActivity {

    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_main_activity);

        PreferencesService preferencesService = new PreferencesService(this, Preferences.PREF_USER_GLOBAL_KEY);

        String device_token = preferencesService.getString(Preferences.DEVICE_TOKEN);

        if (device_token.equals("")) {
            Intent intent = new Intent(getBaseContext(), InputPinView.class);
            startActivityForResult(intent, REQUIRE_PIN);
        } else {
            Intent intent = new Intent(getBaseContext(), NewMainActivity2.class);
            startActivityForResult(intent, START_VPN);
        }



    }
}
