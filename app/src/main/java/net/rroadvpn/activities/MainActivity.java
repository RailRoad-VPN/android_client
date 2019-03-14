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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity {

    private static final String APP_VERSION = "1.0a";

    public static int REQUIRE_PIN = 0;
    public static int START_VPN = 1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        ProgressBarTask progressBarTask = new ProgressBarTask(this);
        progressBarTask.execute();
    }

    public static class ProgressBarTask extends AsyncTask<Void, Integer, Intent> {
        private Logger log = LoggerFactory.getLogger(ProgressBarTask.class);

        private WeakReference<MainActivity> activityReference;

        private String logsDir;
        private int count = 0;

        public int INTENT_CODE;

        ProgressBarTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            log.debug("create progressbar");
            ProgressBar pb = activityReference.get().findViewById(R.id.pbLoading);
            pb.setVisibility(ProgressBar.VISIBLE);

            log.debug("get logs dir");
            this.logsDir = activityReference.get().getApplicationInfo().dataDir + "/logs";
            log.debug("logs dir: " + this.logsDir);
        }

        @Override
        protected Intent doInBackground(Void... params) {
            count = 10;
            publishProgress(count);

            log.debug("create date format");
            DateFormat df = new SimpleDateFormat("yyyyMMdd"); // Quoted "Z" to indicate UTC, no timezone offset
            String todayDate = df.format(new Date());

            count = 20;
            publishProgress(count);

            log.debug("get log files");
            File directory = new File(logsDir);
            File[] files = directory.listFiles();
            log.debug("log files count: " + files.length);

            log.debug("delete log files that no start with todayDate: " + todayDate);
            for (File file : files) {
                if (!file.getName().startsWith(todayDate)) {
                    file.delete();
                }
            }

            count = 30;
            publishProgress(count);

            log.debug("get preferences service");
            Context baseContext = activityReference.get().getBaseContext();
            PreferencesService preferencesService = new PreferencesService(baseContext, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
            String deviceToken = preferencesService.getString(VPNAppPreferences.DEVICE_TOKEN);
            preferencesService.save(VPNAppPreferences.APP_VERSION, APP_VERSION);

            count = 65;
            publishProgress(count);

            log.debug("check device token");
            Intent intent;
            if (deviceToken.equals("")) {
                log.debug("device token is empty - open input pin activity");
                intent = new Intent(baseContext, InputPinView.class);
                INTENT_CODE = REQUIRE_PIN;
            } else {
                log.debug("device token is empty - open VPN activity");
                intent = new Intent(baseContext, VPNActivity.class);
                INTENT_CODE = START_VPN;
            }

            count = 100;
            publishProgress(count);

            return intent;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ProgressBar pb = activityReference.get().findViewById(R.id.pbLoading);
            pb.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Intent intent) {
            super.onPostExecute(intent);

            log.debug("onPostExecute start activity");
            activityReference.get().startActivityForResult(intent, INTENT_CODE);
            activityReference.get().finish();
        }
    }
}
