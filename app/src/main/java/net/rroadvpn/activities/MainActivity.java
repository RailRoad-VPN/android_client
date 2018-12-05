package net.rroadvpn.activities;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.ProgressBar;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.services.PreferencesService;

import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity {

    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    public ProgressBar pb;
    protected Context ctx;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = this;

        setContentView(R.layout.main_activity);

        // on some click or some loading we need to wait for...
        pb = findViewById(R.id.pbLoading);

        ProgressBarTask progressBarTask = new ProgressBarTask();
        progressBarTask.execute();
    }

    public class ProgressBarTask extends AsyncTask<Void, Integer, String> {

        int count = 0;

        @Override
        protected void onPreExecute() {
            pb.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            SystemClock.sleep(200);
            while (count < 100) {
                SystemClock.sleep(200);
                count += 10;
                publishProgress(count);
            }
            return "Complete";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pb.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);

            PreferencesService preferencesService = new PreferencesService(ctx, VPNAppPreferences.PREF_USER_GLOBAL_KEY);

            String device_token = preferencesService.getString(VPNAppPreferences.DEVICE_TOKEN);

            if (device_token.equals("")) {
                Intent intent = new Intent(getBaseContext(), InputPinView.class);
                startActivityForResult(intent, REQUIRE_PIN);
                finish();
            } else {
                Intent intent = new Intent(getBaseContext(), VPNActivity.class);
                startActivityForResult(intent, START_VPN);
                finish();
            }
        }
    }
}
