package net.rroadvpn.activities;


import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.model.User;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.OpenVPNControlService;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UserVPNPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

import static net.rroadvpn.openvpn.core.OpenVPNService.DISCONNECT_VPN;
import static net.rroadvpn.services.OpenVPNControlService.VPN_SERVICE_INTENT_PERMISSION;


public class VPNActivity extends BaseActivity {
    private OpenVPNControlService ovcs;
    private UserVPNPolicy userVPNPolicy;
    private Logger log = LoggerFactory.getLogger(VPNActivity.class);

    private boolean MENU_VISIBLE = false;

    private static final Integer DISCONNECT_VPN_REQUEST_CODE = 9090;
    private static final Integer LOGOUT_DISCONNECT_VPN_REQUEST_CODE = 9091;

    private AfterDisconnectVPNTask afterDisconnectVPNTask;
    private ConnectVPNTask connectVPNTask;
    private LogoutTask logoutTask;

    private Integer MENU_MARGIN_LEFT = 133;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log.info("VPNActivity onCreate enter");
        this.ovcs = new OpenVPNControlService(this);

        PreferencesService preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);

        this.userVPNPolicy = new UserVPNPolicy(preferencesService);

        setContentView(R.layout.vpn_activity);

        ImageButton menuBtn = findViewById(R.id.side_menu_btn);
        RelativeLayout mainLayout = findViewById(R.id.main_wrapper);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            log.debug("layout transition enable changing transition type");
            mainLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

//        switch (getResources().getDisplayMetrics().densityDpi) {
//            case DisplayMetrics.DENSITY_LOW:
//                MENU_MARGIN_LEFT = pixelToDP(MENU_MARGIN_LEFT /= 4);
//                break;
//            case DisplayMetrics.DENSITY_MEDIUM:
//                MENU_MARGIN_LEFT = pixelToDP(MENU_MARGIN_LEFT /= 2);
//                break;
//            case DisplayMetrics.DENSITY_HIGH:
//                MENU_MARGIN_LEFT = pixelToDP(MENU_MARGIN_LEFT);
//                break;
//            case DisplayMetrics.DENSITY_XHIGH:
//                MENU_MARGIN_LEFT = pixelToDP(MENU_MARGIN_LEFT);
//                break;
//        }

        ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) mainLayout.getLayoutParams();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        margins.setMargins(-MENU_MARGIN_LEFT, 0, 0, 0);
        mainLayout.setLayoutParams(margins);

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu(mainLayout);
            }
        });

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MENU_VISIBLE) {
                    toggleMenu(mainLayout);
                }
            }

        });

        Button profileButton = findViewById(R.id.side_menu_btn_profile);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.debug("click profile button");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://rroadvpn.net/en/profile"));
                startActivity(browserIntent);
            }
        });

        ImageButton connectToVPNBtn = (ImageButton) findViewById(R.id.connect_to_vpn);
        if (ovcs.isVPNActive()) {
            if (ovcs.isVPNConnected()) {
                connectToVPNBtn.setBackgroundResource(R.drawable.ic_green_semaphore);
            } else {
                connectToVPNBtn.setBackgroundResource(R.drawable.black_yellow_semaphore_animation);
                ((AnimationDrawable) connectToVPNBtn.getBackground()).start();
            }
        }

        connectToVPNBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("connectToVPNBtn button click");

                if (ovcs.isVPNActive()) {
                    showDisconnectDialogVPN(DISCONNECT_VPN_REQUEST_CODE);
                } else {
                    connectToVPNBtn.setBackgroundResource(R.drawable.black_yellow_semaphore_animation);
                    ((AnimationDrawable) connectToVPNBtn.getBackground()).start();
                    if (ovcs.vpnPreparePermissionIntent() != null) {
                        try {
                            startActivityForResult(ovcs.vpnPreparePermissionIntent(), VPN_SERVICE_INTENT_PERMISSION);
                        } catch (ActivityNotFoundException ane) {
                            // Shame on you Sony! At least one user reported that
                            // an official Sony Xperia Arc S image triggers this exception
                        }
                    } else {
                        connectToVPN();
                    }
                }
            }
        });

//        Button testAPIBtn = (Button) findViewById(R.id.test_api);
//        testAPIBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                log.info("1");
//
//                System.out.println();
//
//
//////TODO read from file (DO NOT HARDCODE /sdcard/)
////                StringBuilder text = new StringBuilder();
////                try {
//////                    File sdcard = Environment.getExternalStorageDirectory();
////                    File sdcard = new File("/sdcard/Android/data/files");
////                    File file = new File(sdcard, "rroadVPN_openVPN_log.2018_10_25.log");
////
////                    BufferedReader br = new BufferedReader(new FileReader(file));
////                    String line;
////                    while ((line = br.readLine()) != null) {
////                        text.append(line);
////                        text.append('\n');
////                    }
////                    br.close();
//////                    log.info(String.valueOf(text));
////                    System.out.println(text);
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//////<<<<<<<<<<<<<<<<<<<<<
//            }
//        });


        Button logOut = (Button) findViewById(R.id.side_menu_btn_log_out);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("Log out button click");
                log.debug("check is vpn active");
                if (ovcs.isVPNActive()) {
                    showDisconnectDialogVPN(LOGOUT_DISCONNECT_VPN_REQUEST_CODE);
                } else {
                    logoutUser();
                }
            }
        });
    }

    private void logoutUser() {
        logoutTask = new LogoutTask(userVPNPolicy);
        logoutTask.setListener(new LogoutTask.LogoutTaskListener() {
            @Override
            public void onLogoutTask(Boolean value) {
                findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.ic_red_semaphore);
                Intent intent = new Intent(getBaseContext(), InputPinView.class);
                startActivity(intent);
            }
        });
        logoutTask.execute();
    }

    private void connectToVPN() {
        log.info("connectToVPN enter.  AsyncTask connectToVPN enter.");
        if (connectVPNTask != null) {
            connectVPNTask.cancel(true);

        }
        connectVPNTask = new ConnectVPNTask(this, userVPNPolicy, ovcs);
        connectVPNTask.setListener(new ConnectVPNTask.ConnectVPNTaskListener() {
            @Override
            public void onConnectVPNTaskListener(Boolean value) {
                findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.ic_green_semaphore);
            }
        });
        connectVPNTask.execute();
        log.info("connectToVPN exit");
    }

    private void showDisconnectDialogVPN(int requestCode) {
        if (connectVPNTask != null) {
            connectVPNTask.cancel(true);
            if (!connectVPNTask.isCancelled()) {
                AsyncTask.Status status = connectVPNTask.getStatus();
                if (status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING) {
                    connectVPNTask.cancel(true);
                }
            }
        }
        Intent disconnectVPN = new Intent(getBaseContext(), DisconnectVPN.class);
        disconnectVPN.setAction(DISCONNECT_VPN);
        startActivityForResult(disconnectVPN, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VPN_SERVICE_INTENT_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                connectToVPN();
            } else {
                // TODO make error
            }
        } else if (requestCode == DISCONNECT_VPN_REQUEST_CODE) {
            if (data.getExtras() != null && data.getExtras().getBoolean("toDisconnect")) {
                afterDisconnectVPNTask = new AfterDisconnectVPNTask(userVPNPolicy);
                afterDisconnectVPNTask.setListener(new AfterDisconnectVPNTask.AfterDisconnectVPNTaskListener() {
                    @Override
                    public void onAfterDisconnectVPNTaskListener(Boolean value) {
                        findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.ic_red_semaphore);
                    }
                });
                afterDisconnectVPNTask.execute();
            }
        } else if (requestCode == LOGOUT_DISCONNECT_VPN_REQUEST_CODE) {
            afterDisconnectVPNTask = new AfterDisconnectVPNTask(userVPNPolicy);
            afterDisconnectVPNTask.setListener(new AfterDisconnectVPNTask.AfterDisconnectVPNTaskListener() {
                @Override
                public void onAfterDisconnectVPNTaskListener(Boolean value) {
                    findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.ic_red_semaphore);
                    logoutUser();
                }
            });
            afterDisconnectVPNTask.execute();
        } else {
            // TODO make error
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ovcs != null) {
            ovcs.bindService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // prevent memory leak
        if (afterDisconnectVPNTask != null) {
            afterDisconnectVPNTask.setListener(null);
        }
        if (connectVPNTask != null) {
            connectVPNTask.setListener(null);
        }
        if (logoutTask != null) {
            logoutTask.setListener(null);
        }
        if (ovcs != null) {
            ovcs.unBindService();
        }
    }

    private static class LogoutTask extends AsyncTask<Void, Void, Boolean> {
        private Logger log = LoggerFactory.getLogger(LogoutTask.class);

        private LogoutTaskListener listener;

        private UserVPNPolicy userVPNPolicy;

        LogoutTask(UserVPNPolicy userVPNPolicy) {
            this.userVPNPolicy = userVPNPolicy;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            log.debug("clean user settings");
            userVPNPolicy.deleteUserSettings();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);
            if (listener != null) {
                listener.onLogoutTask(isOk);
            }
        }

        void setListener(LogoutTaskListener listener) {
            this.listener = listener;
        }

        public interface LogoutTaskListener {
            void onLogoutTask(Boolean value);
        }
    }

    private static class AfterDisconnectVPNTask extends AsyncTask<Void, Void, Boolean> {
        private Logger log = LoggerFactory.getLogger(AfterDisconnectVPNTask.class);

        private AfterDisconnectVPNTaskListener listener;

        private UserVPNPolicy userVPNPolicy;

        AfterDisconnectVPNTask(UserVPNPolicy userVPNPolicy) {
            this.userVPNPolicy = userVPNPolicy;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            log.debug("do after disconnect vpn staff");
            userVPNPolicy.afterDisconnectVPN();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);
            if (listener != null) {
                listener.onAfterDisconnectVPNTaskListener(isOk);
            }
        }

        void setListener(AfterDisconnectVPNTaskListener listener) {
            this.listener = listener;
        }

        public interface AfterDisconnectVPNTaskListener {
            void onAfterDisconnectVPNTaskListener(Boolean value);
        }
    }

    private static class ConnectVPNTask extends AsyncTask<Void, Void, Boolean> {
        private Logger log = LoggerFactory.getLogger(ConnectVPNTask.class);

        private ConnectVPNTaskListener listener;

        private WeakReference<VPNActivity> activityReference;

        private UserVPNPolicy userVPNPolicy;
        private OpenVPNControlService ovcs;

        ConnectVPNTask(VPNActivity context, UserVPNPolicy userVPNPolicy, OpenVPNControlService ovcs) {
            this.activityReference = new WeakReference<>(context);
            this.userVPNPolicy = userVPNPolicy;
            this.ovcs = ovcs;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            log.debug("get new random server");
            String vpnConfig = userVPNPolicy.getNewRandomVPNServer();
            log.debug("random server: {}", vpnConfig);

            log.debug("prepare to connect to VPN");
            boolean isOk = ovcs.prepareToConnectVPN(vpnConfig);

            if (!isOk) {
                return false;
            }

            log.debug("connect to VPN");
            ovcs.connectToVPN();

            String status = VpnStatus.getLastCleanLogMessage(this.activityReference.get());
            boolean gotVirtualIP = false;
            while (!gotVirtualIP) {
                status = VpnStatus.getLastCleanLogMessage(activityReference.get());
                gotVirtualIP = status.contains("Connected: SUCCESS");
                if (isCancelled()) {
                    break;
                }
            }

            if (gotVirtualIP) {
                log.info("Status contains SUCCESS");
                String virtualIP = status.split(",")[1];

                log.debug("virtualIP: {}", virtualIP);
                log.debug("do after connect staff");
                userVPNPolicy.afterConnectedToVPN(virtualIP);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);
            if (listener != null) {
                listener.onConnectVPNTaskListener(isOk);
            }
        }

        void setListener(ConnectVPNTaskListener listener) {
            this.listener = listener;
        }

        public interface ConnectVPNTaskListener {
            void onConnectVPNTaskListener(Boolean value);
        }
    }

    private void toggleMenu(RelativeLayout mainLayout) {
        ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) mainLayout.getLayoutParams();
        if (margins.leftMargin == -MENU_MARGIN_LEFT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels;
            margins.setMargins(Math.round(dpWidth / 2) - MENU_MARGIN_LEFT, 0, -Math.round(dpWidth / 2), 0);
            MENU_VISIBLE = true;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            margins.setMargins(-MENU_MARGIN_LEFT, 0, 0, 0);
            MENU_VISIBLE = false;
        }

        mainLayout.setLayoutParams(margins);
    }

    int pixelToDP(int pixel) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) ((pixel * scale) + 0.5f);
    }
}
