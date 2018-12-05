package net.rroadvpn.activities;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;
import android.widget.ProgressBar;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.services.PreferencesService;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity {

    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        ProgressBarTask progressBarTask = new ProgressBarTask(this);
        progressBarTask.execute();
    }

    public static class ProgressBarTask extends AsyncTask<Void, Integer, String> {

        private WeakReference<MainActivity> activityReference;

        int count = 0;

        ProgressBarTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            ProgressBar pb = activityReference.get().findViewById(R.id.pbLoading);
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
            ProgressBar pb = activityReference.get().findViewById(R.id.pbLoading);
            pb.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);

            Context baseContext = activityReference.get().getBaseContext();

            PreferencesService preferencesService = new PreferencesService(baseContext, VPNAppPreferences.PREF_USER_GLOBAL_KEY);

            String device_token = preferencesService.getString(VPNAppPreferences.DEVICE_TOKEN);

            if (device_token.equals("")) {
                Intent intent = new Intent(baseContext, InputPinView.class);
                activityReference.get().startActivityForResult(intent, REQUIRE_PIN);
                activityReference.get().finish();
            } else {
                Intent intent = new Intent(baseContext, VPNActivity.class);
                activityReference.get().startActivityForResult(intent, START_VPN);
                activityReference.get().finish();
            }
        }
    }
}
