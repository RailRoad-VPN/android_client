package net.rroadvpn.activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.exception.UserPolicyException;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.core.OpenVPNManagement;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.OpenVPNControlService;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UserVPNPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Text;

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

    private ImageButton connectToVPNBtn;
    private LinearLayout connectHintLayout;
    private LinearLayout disconnectHintLayout;

    private PreferencesService preferencesService;

    private Integer MENU_MARGIN_LEFT = -1;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log.info("VPNActivity onCreate enter");
        this.ovcs = new OpenVPNControlService(this);

        this.preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);

        this.userVPNPolicy = new UserVPNPolicy(preferencesService);

        setContentView(R.layout.vpn_activity);

        MENU_MARGIN_LEFT = convertDpToPx(21);

        this.connectHintLayout = findViewById(R.id.connect_hint_layout);
        this.disconnectHintLayout = findViewById(R.id.disconnect_hint_layout);

        ImageButton menuBtn = findViewById(R.id.side_menu_btn);
        RelativeLayout mainLayout = findViewById(R.id.main_wrapper);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            log.debug("layout transition enable changing transition type");
            mainLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

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

        this.connectToVPNBtn = findViewById(R.id.connect_to_vpn);
        calcSemaphoreState();

        this.connectToVPNBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("connectToVPNBtn button click");

                if (ovcs.isVPNActive()) {
                    showDisconnectDialogVPN(DISCONNECT_VPN_REQUEST_CODE);
                } else {
                    connectToVPNBtn.setBackgroundResource(R.drawable.blink_semaphore_animation);
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

        Button logOut = findViewById(R.id.side_menu_btn_log_out);
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

        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null && action.equals(DISCONNECT_VPN)) {
            showDisconnectDialogVPN(DISCONNECT_VPN_REQUEST_CODE);
        }

        TextView statusTextView = findViewById(R.id.vpn_connect_status);

        VpnStatus.onChangeStatusListener = new VpnStatus.OnChangeStatusListener() {
            @Override
            public void onConnect(int statusTextResourceId) {
                String status = VpnStatus.getLastCleanLogMessage(getApplicationContext());
                String virtualIP = status.split(",")[1];

                log.debug("virtualIP: {}", virtualIP);
                log.debug("do after connect staff");
                try {
                    userVPNPolicy.afterConnectedToVPN(virtualIP);
                } catch (UserPolicyException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.semaphore_green);
                        statusTextView.setText(getResources().getString(statusTextResourceId));

                        hideConnectHint();
                    }
                });
            }

            @Override
            public void onDisconnect(int statusTextResourceId) {
                afterDisconnectVPNTask = new AfterDisconnectVPNTask(userVPNPolicy);
                afterDisconnectVPNTask.execute();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.semaphore_red);
                        statusTextView.setText(getResources().getString(statusTextResourceId));

                        showConnectHint();
                    }
                });
            }

            @Override
            public void onChangeStatus(int statusTextResourceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusTextView.setText(getResources().getString(statusTextResourceId));
                    }
                });
            }

            @Override
            public void onStartConnecting() {
                Toast.makeText(getApplicationContext(), "onStartConnecting", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void showConnectHint() {
        connectHintLayout.setVisibility(View.VISIBLE);
        disconnectHintLayout.setVisibility(View.GONE);
    }

    private void hideConnectHint() {
        connectHintLayout.setVisibility(View.GONE);
        disconnectHintLayout.setVisibility(View.VISIBLE);
    }

    private void connectToVPN() {
        this.log.info("connectToVPN enter. AsyncTask connectToVPN enter.");

        if (this.connectVPNTask != null) {
            this.connectVPNTask.cancel(true);
            if (!this.connectVPNTask.isCancelled()) {
                AsyncTask.Status status = this.connectVPNTask.getStatus();
                if (status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING) {
                    this.connectVPNTask.cancel(true);
                }
            }
        }

        this.log.debug("create new connect vpn task");
        this.connectVPNTask = new ConnectVPNTask(this.userVPNPolicy, this.ovcs);
        this.connectVPNTask.execute();
        this.log.info("connectToVPN exit");
    }

    private void showDisconnectDialogVPN(int requestCode) {
        this.log.info("showDisconnectDialogVPN enter");

        if (this.connectVPNTask != null) {
            AsyncTask.Status status = this.connectVPNTask.getStatus();
            if (status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING) {
                this.connectVPNTask.cancel(true);
            }
        }

        Intent disconnectVPN = new Intent(getBaseContext(), DisconnectVPN.class);
        disconnectVPN.setAction(DISCONNECT_VPN);
        this.log.info("showDisconnectDialogVPN exit");
        startActivityForResult(disconnectVPN, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.log.info("onActivityResult enter");

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VPN_SERVICE_INTENT_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                connectToVPN();
            }
        } else if (requestCode == LOGOUT_DISCONNECT_VPN_REQUEST_CODE) {
            logoutUser();
        }

        this.log.info("onActivityResult exit");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.ovcs != null) {
            this.ovcs.bindService();
        }

        calcSemaphoreState();

        String userUUid = this.preferencesService.getString(VPNAppPreferences.USER_UUID);
        String userEmail = this.preferencesService.getString(VPNAppPreferences.USER_EMAIL);

        if (userUUid == null || userEmail == null) {
            // TODO logout
            goToPin();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // prevent memory leak
        if (this.connectVPNTask != null) {
            this.connectVPNTask.setListener(null);
        }
        if (this.logoutTask != null) {
            this.logoutTask.setListener(null);
        }
        if (this.ovcs != null) {
            this.ovcs.unBindService();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            calcSemaphoreState();
        }
    }

    private void calcSemaphoreState() {
        this.log.info("calcSemaphoreState enter");

        if (this.ovcs.isVPNActive() || (this.connectVPNTask != null && this.connectVPNTask.getStatus().equals(AsyncTask.Status.RUNNING))) {
            if (this.ovcs.isVPNConnected()) {
                this.connectToVPNBtn.setBackgroundResource(R.drawable.semaphore_green);
                hideConnectHint();
            } else {
                this.connectToVPNBtn.setBackgroundResource(R.drawable.blink_semaphore_animation);
                ((AnimationDrawable) this.connectToVPNBtn.getBackground()).start();
                showConnectHint();
            }
        } else {
            this.connectToVPNBtn.setBackgroundResource(R.drawable.semaphore_red);
            showConnectHint();
        }

        this.log.info("calcSemaphoreState exit");
    }

    private void logoutUser() {
        this.log.info("logoutUser enter");


        if (this.logoutTask != null) {
            AsyncTask.Status status = this.logoutTask.getStatus();
            if (status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING) {
                this.logoutTask.cancel(true);
            }
        }

        this.logoutTask = new LogoutTask(this.userVPNPolicy);
        this.logoutTask.setListener(new LogoutTask.LogoutTaskListener() {
            @Override
            public void onLogoutTask(Boolean value) {
                goToPin();
            }
        });
        this.logoutTask.execute();

        this.log.info("logoutUser exit");
    }

    private void goToPin() {
        findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.semaphore_red);
        Intent intent = new Intent(getBaseContext(), InputPinView.class);
        startActivity(intent);
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
            this.log.debug("clean user settings");
            try {
                this.userVPNPolicy.deleteUserSettings();
            } catch (UserPolicyException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);
            if (this.listener != null) {
                this.listener.onLogoutTask(isOk);
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
        }
    }

    private static class ConnectVPNTask extends AsyncTask<Void, Void, Boolean> {
        private Logger log = LoggerFactory.getLogger(ConnectVPNTask.class);

        private ConnectVPNTaskListener listener;

        private WeakReference<VPNActivity> activityReference;

        private UserVPNPolicy userVPNPolicy;
        private OpenVPNControlService ovcs;

        ConnectVPNTask(UserVPNPolicy userVPNPolicy, OpenVPNControlService ovcs) {
            this.userVPNPolicy = userVPNPolicy;
            this.ovcs = ovcs;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            log.debug("get new random server");
            String vpnConfig = null;
            try {
                vpnConfig = userVPNPolicy.getNewRandomVPNServer();
            } catch (UserPolicyException e) {
                log.debug("UserPolicyException: {}", e);
                VpnStatus.updateStatePause(OpenVPNManagement.pauseReason.noNetwork);
                return false;
            }
            log.debug("random server: {}", vpnConfig);

            log.debug("prepare to connect to VPN");
            boolean isOk = ovcs.prepareToConnectVPN(vpnConfig);

            if (!isOk) {
                return false;
            }

            log.debug("connect to VPN");
            ovcs.connectToVPN();

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

    private int convertDpToPx(int dp) {
        return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    private int convertPxToDp(int px) {
        return Math.round(px / (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
