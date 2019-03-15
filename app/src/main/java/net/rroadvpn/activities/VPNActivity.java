package net.rroadvpn.activities;


import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.exception.UserDeviceNotFoundException;
import net.rroadvpn.exception.UserPolicyException;
import net.rroadvpn.model.OnSwipeTouchListener;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.core.ConnectionStatus;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.OpenVPNControlService;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UserVPNPolicyI;
import net.rroadvpn.services.UserVPNPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.rroadvpn.openvpn.core.OpenVPNService.DISCONNECT_VPN;
import static net.rroadvpn.services.OpenVPNControlService.VPN_SERVICE_INTENT_PERMISSION;


public class VPNActivity extends BaseActivity {
    private OpenVPNControlService ovcs;
    private UserVPNPolicyI userVPNPolicyI;
    private Logger log = LoggerFactory.getLogger(VPNActivity.class);

    private boolean MENU_VISIBLE = false;

    private static final Integer DISCONNECT_VPN_REQUEST_CODE = 9090;
    private static final Integer LOGOUT_DISCONNECT_VPN_REQUEST_CODE = 9091;

    public static final int CONNECT_UNKNOWN_ERROR_CODE = 101;
    public static final int CONNECT_NO_NETWORK_ERROR_CODE = 102;
    public static final int CONNECT_PREPARE_ERROR_CODE = 103;
    public static final int CONNECT_NO_ERROR_CODE = 99;
    public static final int USER_DEVICE_NO_ERROR_CODE = 200;
    public static final int USER_DEVICE_NOT_ACTIVE_ERROR_CODE = 201;
    public static final int USER_DEVICE_DELETED_ERROR_CODE = 202;

    private AfterDisconnectVPNTask afterDisconnectVPNTask;
    private ConnectVPNTask connectVPNTask;
    private CheckUserDeviceTask checkUserDeviceTask;
    private LogoutTask logoutTask;

    private ImageButton connectToVPNBtn;
    private LinearLayout connectHintLayout;
    private LinearLayout disconnectHintLayout;

    private Handler checkUserDeviceHandler;
    private Runnable checkUserDeviceHandlerRunnableCode;

    private PreferencesService preferencesService;

    private TextView statusTextView;

    private Integer MENU_MARGIN_LEFT = -1;

    private boolean WAS_CONNECTED = false;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log.info("VPNActivity onCreate enter");
        this.ovcs = new OpenVPNControlService(this);

        this.preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);

        this.userVPNPolicyI = new UserVPNPolicy(preferencesService);

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

        OnSwipeTouchListener menuSwipeListener = new OnSwipeTouchListener(this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (v instanceof ImageButton) {
                    toggleMenu(mainLayout);
                } else if (v instanceof RelativeLayout) {
                    if (MENU_VISIBLE) {
                        toggleMenu(mainLayout);
                    }
                }
            }

            @Override
            public void onDoubleClick(View v) {
                super.onDoubleClick(v);
            }

            @Override
            public void onLongClick(View v) {
                super.onLongClick(v);
            }

            public void onSwipeTop() {
                super.onSwipeTop();
            }

            public void onSwipeRight() {
                super.onSwipeRight();
                if (!MENU_VISIBLE) {
                    toggleMenu(mainLayout);
                }
            }

            public void onSwipeLeft() {
                super.onSwipeLeft();
                if (MENU_VISIBLE) {
                    toggleMenu(mainLayout);
                }
            }

            public void onSwipeBottom() {
                super.onSwipeBottom();
            }
        };

        menuBtn.setOnTouchListener(menuSwipeListener);

        mainLayout.setOnTouchListener(menuSwipeListener);

        Button profileButton = findViewById(R.id.side_menu_btn_profile);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.debug("click profile button");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rroadvpn.net/profile"));
                startActivity(browserIntent);
            }
        });

        this.connectToVPNBtn = findViewById(R.id.connect_to_vpn);
        calcSemaphoreState();

        this.connectToVPNBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("connectToVPNBtn button click");

                log.debug("check is vpn active");
                if (ovcs.isVPNActive()) {
                    log.debug("vpn is active. show disconnect dialog");
                    showDisconnectDialogVPN(DISCONNECT_VPN_REQUEST_CODE, false);
                } else {
                    log.debug("vpn is NOT active");
                    connectToVPNBtn.setBackgroundResource(R.drawable.blink_semaphore_animation);
                    ((AnimationDrawable) connectToVPNBtn.getBackground()).start();

                    log.debug("check prepare permission intent");
                    if (ovcs.vpnPreparePermissionIntent() != null) {
                        log.debug("not null");
                        try {
                            log.debug("start activity for result to get permissions");
                            startActivityForResult(ovcs.vpnPreparePermissionIntent(), VPN_SERVICE_INTENT_PERMISSION);
                        } catch (ActivityNotFoundException ane) {
                            log.debug("ActivityNotFoundException: {}", ane);
                            // Shame on you Sony! At least one user reported that
                            // an official Sony Xperia Arc S image triggers this exception
                        }
                    } else {
                        log.debug("connect to VPN");
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
                    showDisconnectDialogVPN(LOGOUT_DISCONNECT_VPN_REQUEST_CODE, false);
                } else {
                    createDialog(null,
                            R.string.logout_confirm,
                            R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    logoutUser();
                                }
                            },
                            R.string.no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }, true).show();
                }
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null && action.equals(DISCONNECT_VPN)) {
            showDisconnectDialogVPN(DISCONNECT_VPN_REQUEST_CODE, false);
        }

        statusTextView = findViewById(R.id.vpn_connect_status);

        VpnStatus.onChangeStatusListener = new VpnStatus.OnChangeStatusListener() {
            @Override
            public void onConnect(String state, String msg, int resid, ConnectionStatus level) {
                log.debug("VPN connected");
                boolean was_WAS_CONNECTED = WAS_CONNECTED;
                ovcs.CONNECTION_RETRIES_COUNT = 0;

                if (!was_WAS_CONNECTED) {
                    log.debug("VPN was connected, but reconnected, don't create new connection");
                    String status = VpnStatus.getLastCleanLogMessage(getApplicationContext());
                    String virtualIP = status.split(",")[1];

                    log.debug("virtualIP: {}", virtualIP);
                    log.debug("do after connect staff");
                    try {
                        userVPNPolicyI.afterConnectedToVPN(virtualIP, null);
                    } catch (UserPolicyException e) {
                        log.error("UserPolicyException when do after disconnect vpn staff: {}", e);
                    } catch (Exception e) {
                        log.error("Exception when do after disconnect vpn staff: {}", e);
                    }
                }

                WAS_CONNECTED = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.semaphore_green);
                        String text = getResources().getString(resid);
                        statusTextView.setText(text);
                        log.debug("onConnect: setStatus to: " + text);

                        hideConnectHint();
                    }
                });
            }

            @Override
            public void onDisconnect(String state, String msg, int resid, ConnectionStatus level) {
                log.debug("VPN was disconnected");
                if (WAS_CONNECTED) {
                    afterDisconnectVPNTask = new AfterDisconnectVPNTask(userVPNPolicyI);
                    afterDisconnectVPNTask.execute();
                }
                WAS_CONNECTED = false;
                ovcs.CONNECTION_RETRIES_COUNT = 0;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.semaphore_red);
                        String text = getResources().getString(resid);
                        statusTextView.setText(text);
                        log.debug("onDisconnect: setStatus to: " + text);

                        showConnectHint();
                    }
                });
            }

            @Override
            public void onChangeStatus(String state, String msg, int resid, ConnectionStatus level) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = getResources().getString(resid);

                        if (state.equals("CONNECTRETRY")) {
                            ovcs.CONNECTION_RETRIES_COUNT += 1;
                            int seconds = 4;
                            try {
                                seconds = Integer.parseInt(msg);
                            } catch (NumberFormatException ignored) {
                            }
                            text = String.format(text, seconds);

                            if (ovcs.CONNECTION_RETRIES_COUNT > 3) {
                                ovcs.CONNECTION_RETRIES_COUNT = 0;
                                ovcs.disconnectVPN();
                                connectToVPN();
                            }
                        }

                        statusTextView.setText(text);
                        log.debug("onChangeStatus: setStatus to: " + text + ", state: " + state +
                                ", msg: " + msg + ", level: " + level.name());
                        calcSemaphoreState();
                    }
                });
            }
        };

        VpnStatus.addByteCountListener(new VpnStatus.ByteCountListener() {
            @Override
            public void updateByteCount(long in, long out, long diffIn, long diffOut) {
                if (diffIn >= 1000000 || diffOut >= 1000000) { // update traffic every 1 MB
                    try {
                        userVPNPolicyI.updateConnection(in, out, null, "update traffic");
                    } catch (UserPolicyException e) {
                        log.error("UserPolicyException when update connection: {}", e);
                    } catch (Exception e) {
                        log.error("Exception when update connection: {}", e);
                    }
                }
            }
        });

        VPNActivity that = this;
        Button supportBtn = findViewById(R.id.side_menu_btn_support);
        supportBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(VPNActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.support_dialog);
                dialog.setCancelable(true);

                final EditText emailET = dialog.findViewById(R.id.help_form_email_input);
                final EditText descriptonET = dialog.findViewById(R.id.help_form_description_input);
                final Button sendBtn = dialog.findViewById(R.id.help_form_send_btn);
                final Button cancelBtn = dialog.findViewById(R.id.help_form_cancel_btn);

                emailET.setText(preferencesService.getString(VPNAppPreferences.USER_EMAIL));

                cancelBtn.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                sendBtn.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = emailET.getText().toString();
                        String description = descriptonET.getText().toString();

                        if (description.trim().equals("")) {
                            Toast.makeText(that, R.string.help_form_description_required, Toast.LENGTH_LONG).show();
                            return;
                        }

                        SupportDialogSendTask supportDialogSendTask = new SupportDialogSendTask(userVPNPolicyI, email, description, that);
                        supportDialogSendTask.setListener(new SupportDialogSendTask.SupportDialogSendTaskListener() {
                            @Override
                            public void onSupportDialogSendTaskListener(Integer value) {
                                dialog.dismiss();

                                if (value != null) {
                                    createDialog(null,
                                            "Ticket #" + String.valueOf(value),
                                            "OK",
                                            null,
                                            null,
                                            null, true).show();
                                } else {
                                    createErrorDialog(R.string.help_form_error, null, null).show();
                                }
                            }
                        });
                        supportDialogSendTask.execute();
                    }
                });
                dialog.show();
            }
        });
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
        this.connectVPNTask = new ConnectVPNTask(this.userVPNPolicyI, this.ovcs);
        this.connectVPNTask.execute();

        VPNActivity that = this;

        this.connectVPNTask.setOnPostExecuteListener(new ConnectVPNTask.ConnectVPNTaskPostListener() {
            @Override
            public void onConnectVPNTaskPostListener(Integer code) {
                if (code == CONNECT_NO_ERROR_CODE) {
                    // Create the Handler object (on the main thread by default)
                    checkUserDeviceHandler = new Handler();
                    // Define the code block to be executed
                    checkUserDeviceHandlerRunnableCode = new Runnable() {
                        @Override
                        public void run() {
                            // Do something here on the main thread
                            int delay = 120000;
                            CheckUserDeviceTask checkUserDeviceTask = new CheckUserDeviceTask(userVPNPolicyI, ovcs);
                            checkUserDeviceTask.setOnPostExecuteListener(new CheckUserDeviceTask.CheckUserDeviceTaskPostListener() {
                                @Override
                                public void onCheckUserDeviceTaskPostListener(Integer code) {
                                    switch (code) {
                                        case USER_DEVICE_NOT_ACTIVE_ERROR_CODE:
                                            statusTextView.setText(R.string.connect_device_deactivated_error);
                                            createErrorDialog(R.string.connect_device_deactivated_error_message,
                                                    null, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            ovcs.disconnectVPN();
                                                        }
                                                    }).show();
                                            break;
                                        case USER_DEVICE_DELETED_ERROR_CODE:
                                            statusTextView.setText(R.string.connect_device_deleted_error);
                                            createErrorDialog(R.string.connect_device_deleted_error_message, R.string.side_menu_btn_log_out, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    logoutUser();
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                            break;
                                        case USER_DEVICE_NO_ERROR_CODE:
                                            break;
                                    }
                                }
                            });
                            checkUserDeviceTask.execute();
                            checkUserDeviceHandler.postDelayed(this, delay);
                        }
                    };
                    // Start the initial runnable task by posting through the handler
                    checkUserDeviceHandler.post(checkUserDeviceHandlerRunnableCode);
                } else {
                    connectVPNTask.cancel(true);
                    connectVPNTask = null;

                    findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.semaphore_red);

                    DialogInterface.OnClickListener onPositiveClickListener = null;
                    switch (code) {
                        case USER_DEVICE_NOT_ACTIVE_ERROR_CODE:
                            statusTextView.setText(R.string.connect_device_deactivated_error);
                            createErrorDialog(R.string.connect_device_deactivated_error_message,
                                    null, onPositiveClickListener).show();
                            break;
                        case CONNECT_NO_NETWORK_ERROR_CODE:
                            statusTextView.setText(R.string.vpn_connect_network_error);
                            break;
                        case USER_DEVICE_DELETED_ERROR_CODE:
                            statusTextView.setText(R.string.connect_device_deleted_error);
                            onPositiveClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    logoutUser();
                                    dialog.dismiss();
                                }
                            };
                            createErrorDialog(R.string.connect_device_deleted_error_message,
                                    R.string.side_menu_btn_log_out, onPositiveClickListener).show();
                            break;
                        default:
                            statusTextView.setText(R.string.connect_unknown_error);
                            onPositiveClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    findViewById(R.id.side_menu_btn_support).performClick();
                                    String helpText = getResources().getString(R.string.connect_unknown_error) + " " + getResources().getString(R.string.connect_unknown_error_help_text) + "\n" + String.valueOf(code);
                                    ((EditText) findViewById(R.id.help_form_description_input)).setText(helpText);
                                }
                            };
                            createErrorDialog(R.string.connect_device_deactivated_error_message, R.string.connect_unknown_error_create_ticket_btn_text, onPositiveClickListener).show();
                            break;
                    }

                    calcSemaphoreState();
                }
            }
        });

        this.log.info("connectToVPN exit");
    }


    private AlertDialog createDialog(Integer titleId, Integer messageId,
                                     Integer positiveButtonTextId,
                                     DialogInterface.OnClickListener onPositiveClickListener,
                                     Integer negativeButtonTextId,
                                     DialogInterface.OnClickListener onNegativeClickListener,
                                     boolean isCanceable) {
        String titleText = null;
        if (titleId != null) {
            titleText = getResources().getString(titleId);
        }
        String messageText = null;
        if (messageId != null) {
            messageText = getResources().getString(messageId);
        }

        String positiveButtonText = null;
        if (positiveButtonTextId != null) {
            positiveButtonText = getResources().getString(positiveButtonTextId);
        }

        String negativeButtonText = null;
        if (negativeButtonTextId != null) {
            negativeButtonText = getResources().getString(negativeButtonTextId);
        }

        return createDialog(titleText, messageText, positiveButtonText, onPositiveClickListener,
                negativeButtonText, onNegativeClickListener, isCanceable);
    }


    private AlertDialog createErrorDialog(Integer messageId, Integer positiveButtonTextId,
                                          DialogInterface.OnClickListener onPositiveClickListener) {

        String titleText = getResources().getString(R.string.connect_error_dialog_title);
        boolean isCancelable = false;

        String message = null;
        if (messageId != null) {
            message = getResources().getString(messageId);
        }

        String positiveButtonText = "OK";
        if (positiveButtonTextId != null) {
            positiveButtonText = getResources().getString(positiveButtonTextId);
        }

        return this.createDialog(titleText, message, positiveButtonText, onPositiveClickListener,
                null, null, isCancelable);
    }

    private AlertDialog createDialog(String title, String message,
                                     String positiveButtonText,
                                     DialogInterface.OnClickListener onPositiveClickListener,
                                     String negativeButtonText,
                                     DialogInterface.OnClickListener onNegativeClickListener,
                                     boolean isCanceable) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (message == null) {
            message = "";
        }

        // positive button
        if (positiveButtonText == null) {
            positiveButtonText = "OK";
        }
        if (onPositiveClickListener == null) {
            onPositiveClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
        }

        // negative button
        if (onNegativeClickListener != null) {
            if (negativeButtonText == null) {
                negativeButtonText = getResources().getString(R.string.cancel);
            }
            builder.setNegativeButton(negativeButtonText, onNegativeClickListener);
        }

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(isCanceable);
        builder.setPositiveButton(positiveButtonText, onPositiveClickListener);

        AlertDialog alertDialog = builder.create();
        if (title == null) {
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return alertDialog;
    }

    private void showDisconnectDialogVPN(int requestCode, boolean force) {
        this.log.info("showDisconnectDialogVPN enter");

        if (this.connectVPNTask != null) {
            AsyncTask.Status status = this.connectVPNTask.getStatus();
            if (status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING) {
                this.connectVPNTask.cancel(true);
            }
        }

        Intent disconnectVPN = new Intent(getBaseContext(), DisconnectVPN.class);
        disconnectVPN.setAction(DISCONNECT_VPN);
        disconnectVPN.putExtra("force", force);
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

        if (userUUid == null || userUUid.equals("") || userEmail == null || userEmail.equals("")) {
            logoutUser();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // prevent memory leak
        if (this.connectVPNTask != null) {
            this.connectVPNTask.setOnPostExecuteListener(null);
        }
        if (this.checkUserDeviceTask != null) {
            this.checkUserDeviceTask.setOnPostExecuteListener(null);
        }
        if (this.checkUserDeviceHandler != null) {
            this.checkUserDeviceHandler = null;
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
    }

    private void logoutUser() {
        this.log.info("logoutUser enter");

        ovcs.disconnectVPN();

        if (this.logoutTask != null) {
            AsyncTask.Status status = this.logoutTask.getStatus();
            if (status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING) {
                this.logoutTask.cancel(true);
            }
        }

        this.logoutTask = new LogoutTask(this.userVPNPolicyI, this);
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
        private ProgressDialog dialog;
        private String dialogMessage;

        private UserVPNPolicyI userVPNPolicyI;

        LogoutTask(UserVPNPolicyI userVPNPolicyI, VPNActivity activity) {
            this.userVPNPolicyI = userVPNPolicyI;

            dialog = new ProgressDialog(activity);
            dialog.setCancelable(false);

            dialogMessage = activity.getResources().getString(R.string.logout_dialog);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(dialogMessage);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            this.log.debug("logout task start");
            try {
                this.userVPNPolicyI.deleteUserSettings();
            } catch (UserPolicyException e) {
                log.error("UserPolicyException when delete user settings: {}", e);
                return false;
            } catch (Exception e) {
                log.error("Exception when send support ticket: {}", e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

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

        private UserVPNPolicyI userVPNPolicyI;

        AfterDisconnectVPNTask(UserVPNPolicyI userVPNPolicyI) {
            this.userVPNPolicyI = userVPNPolicyI;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            log.debug("do after disconnect vpn staff");
            try {
                userVPNPolicyI.afterDisconnectVPN(null, null);
            } catch (UserPolicyException e) {
                log.error("UserPolicyException when do after disconnect vpn staff: {}", e);
                return false;
            } catch (Exception e) {
                log.error("Exception when do after disconnect vpn staff: {}", e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);
        }
    }

    private static class ConnectVPNTask extends AsyncTask<Void, Void, Integer> {
        private Logger log = LoggerFactory.getLogger(ConnectVPNTask.class);

        private ConnectVPNTaskPreListener preListener;
        private ConnectVPNTaskPostListener postListener;

        private UserVPNPolicyI userVPNPolicyI;
        private OpenVPNControlService ovcs;

        ConnectVPNTask(UserVPNPolicyI userVPNPolicyI, OpenVPNControlService ovcs) {
            this.userVPNPolicyI = userVPNPolicyI;
            this.ovcs = ovcs;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (preListener != null) {
                preListener.onConnectVPNTaskPreListener();
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                log.debug("check user device is active");
                boolean isActive = userVPNPolicyI.isUserDeviceActive();
                if (!isActive) {
                    log.debug("user device is NOT active");
                    return USER_DEVICE_NOT_ACTIVE_ERROR_CODE;
                }
            } catch (UserDeviceNotFoundException e) {
                log.error("UserDeviceNotFoundException when get user device: {}", e);
                return USER_DEVICE_DELETED_ERROR_CODE;
            }

            log.debug("get new random server uuid");
            String vpnServerUuid = null;
            try {
                vpnServerUuid = userVPNPolicyI.getRandomVPNServerUuid();
                log.debug("random server uuid: " + vpnServerUuid);

                log.debug("check profile ready");
                boolean isProfileReady = ovcs.isProfileReady(vpnServerUuid);
                if (!isProfileReady) {
                    log.debug("profile is not ready. get vpn server config");
                    String vpnServerConfig = userVPNPolicyI.getVPNServerByUuid(vpnServerUuid);

                    log.debug("create new profile for server");
                    ovcs.createNewProfileForServer(vpnServerUuid, vpnServerConfig);
                }
            } catch (UserPolicyException e) {
                log.debug("UserPolicyException: {}", e);
                VpnStatus.updateStateString("NOPROCESS", "No process running.", R.string.state_noprocess, ConnectionStatus.LEVEL_NOTCONNECTED);
                return CONNECT_UNKNOWN_ERROR_CODE;
            } catch (Exception e) {
                log.debug("Exception: {}", e);
                VpnStatus.updateStateString("NOPROCESS", "No process running.", R.string.state_noprocess, ConnectionStatus.LEVEL_NOTCONNECTED);
                return CONNECT_NO_NETWORK_ERROR_CODE;
            }

            log.debug("prepare to connect to VPN");
            boolean isOk = ovcs.prepareToConnectVPN(vpnServerUuid);

            if (!isOk) {
                log.debug("prepare failed");
                return CONNECT_PREPARE_ERROR_CODE;
            }

            log.debug("connect to VPN");
            ovcs.connectToVPN();

            return CONNECT_NO_ERROR_CODE;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (postListener != null) {
                postListener.onConnectVPNTaskPostListener(code);
            }
        }

        void setOnPostExecuteListener(ConnectVPNTaskPostListener listener) {
            this.postListener = listener;
        }

        void setOnPreExecuteListener(ConnectVPNTaskPreListener listener) {
            this.preListener = listener;
        }

        public interface ConnectVPNTaskPreListener {
            void onConnectVPNTaskPreListener();
        }

        public interface ConnectVPNTaskPostListener {
            void onConnectVPNTaskPostListener(Integer value);
        }
    }

    private static class CheckUserDeviceTask extends AsyncTask<Void, Void, Integer> {
        private Logger log = LoggerFactory.getLogger(ConnectVPNTask.class);

        private CheckUserDeviceTaskPostListener postListener;
        private UserVPNPolicyI userVPNPolicyI;
        private OpenVPNControlService ovcs;

        CheckUserDeviceTask(UserVPNPolicyI userVPNPolicyI, OpenVPNControlService ovcs) {
            this.userVPNPolicyI = userVPNPolicyI;
            this.ovcs = ovcs;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (!this.ovcs.isVPNConnected()) {
                return USER_DEVICE_NO_ERROR_CODE;
            }

            try {
                if (userVPNPolicyI.isUserDeviceActive()) {
                    return USER_DEVICE_NO_ERROR_CODE;
                } else {
                    return USER_DEVICE_NOT_ACTIVE_ERROR_CODE;
                }
            } catch (UserDeviceNotFoundException e) {
                log.error("UserDeviceNotFoundException when check is user device active in checkuserdevicetask");
                return USER_DEVICE_DELETED_ERROR_CODE;
            }
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (postListener != null) {
                postListener.onCheckUserDeviceTaskPostListener(code);
            }
        }

        void setOnPostExecuteListener(CheckUserDeviceTaskPostListener listener) {
            this.postListener = listener;
        }

        public interface CheckUserDeviceTaskPostListener {
            void onCheckUserDeviceTaskPostListener(Integer value);
        }
    }

    private static class SupportDialogSendTask extends AsyncTask<Void, Void, Integer> {
        private Logger log = LoggerFactory.getLogger(SupportDialogSendTask.class);

        private SupportDialogSendTaskListener listener;
        private ProgressDialog dialog;
        private String dialogMessage;

        private UserVPNPolicyI userVPNPolicyI;
        private String email;
        private String description;
        private String logsDir;

        SupportDialogSendTask(UserVPNPolicyI userVPNPolicyI, String email, String description,
                              VPNActivity activity) {
            this.userVPNPolicyI = userVPNPolicyI;
            this.email = email;
            this.description = description;
            this.logsDir = activity.getApplicationInfo().dataDir + "/logs";

            dialog = new ProgressDialog(activity);
            dialog.setCancelable(false);

            dialogMessage = activity.getResources().getString(R.string.sending_dialog);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(dialogMessage);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            log.debug("support dialog send ticket");

            try {
                return this.userVPNPolicyI.sendSupportTicket(email, description, logsDir);
            } catch (UserPolicyException e) {
                log.error("UserPolicyException when send support ticket: {}", e);
                return null;
            } catch (Exception e) {
                log.error("Exception when send support ticket: {}", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (listener != null) {
                listener.onSupportDialogSendTaskListener(result);
            }
        }

        void setListener(SupportDialogSendTask.SupportDialogSendTaskListener listener) {
            this.listener = listener;
        }

        public interface SupportDialogSendTaskListener {
            void onSupportDialogSendTaskListener(Integer value);
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
        return Math.round(dp * (getResources().getDisplayMetrics().ydpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    private int convertPxToDp(int px) {
        return Math.round(px / (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
