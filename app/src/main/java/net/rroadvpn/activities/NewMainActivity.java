package net.rroadvpn.activities;


import android.content.Intent;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.services.PreferencesService;

public class NewMainActivity extends BaseActivity {

    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_main_activity);

        PreferencesService preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);

        String device_token = preferencesService.getString(VPNAppPreferences.DEVICE_TOKEN);

        if (device_token.equals("")) {
            Intent intent = new Intent(getBaseContext(), InputPinView.class);
            startActivityForResult(intent, REQUIRE_PIN);
            finish();
        } else {
            Intent intent = new Intent(getBaseContext(), NewMainActivity2.class);
            startActivityForResult(intent, START_VPN);
            finish();
        }



    }
}
