package net.rroadvpn.activities;


import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.OpenVPNControlService;
import net.rroadvpn.services.UserVPNPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.rroadvpn.services.OpenVPNControlService.VPN_SERVICE_INTENT_PERMISSION;


public class NewMainActivity2 extends BaseActivity {
    private OpenVPNControlService ovcs;
    private UserVPNPolicy userVPNPolicy;
    private Logger log = LoggerFactory.getLogger(NewMainActivity2.class);


    public static String EXTRA_KEY = "net.rroadvpn.openvpn.shortcutProfileUUID";
    private boolean MENU_VISIBLE = false;


    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log.info("NewMainActivity2 onCreate enter");
        this.ovcs = new OpenVPNControlService(this);
        this.userVPNPolicy = new UserVPNPolicy(this);

        setContentView(R.layout.new_main_activity2);

        ImageButton menuBtn = findViewById(R.id.side_menu_btn);
        RelativeLayout mainLayout = findViewById(R.id.main_wrapper);

        log.debug("check build version");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            log.debug("layout transition enable changing transition type");
            mainLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

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
            log.debug("vpn is active - set background resource for connect to vpn button");
            connectToVPNBtn.setBackgroundResource(R.drawable.ic_green_semaphore);
        }


        connectToVPNBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("connectToVPNBtn button click");

                if (ovcs.isVPNActive()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewMainActivity2.this);
                    builder.setTitle("Turning off VPN");
                    builder.setMessage("Are you sure?");
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            log.info("dialogInterface negative button pressed");
                        }
                    });
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            log.info("dialogInterface positive button pressed. AsyncTask disconnectFromVPN enter.");
                            connectToVPNBtn.setBackgroundResource(R.drawable.black_yellow_semaphore_animation);
                            ((AnimationDrawable) connectToVPNBtn.getBackground()).start();
                            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        ovcs.disconnectFromVPN();
                                    } catch (RemoteException e) {
                                        log.error(e.getMessage());
                                    }
                                    userVPNPolicy.afterDisconnectVPN();
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void result) {
                                    findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.ic_red_semaphore);
                                    log.info("disconnectFromVPN AsyncTask onPostExecute exit");
                                }
                            }.execute();
                        }
                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            log.info("dialogInterface cancel button pressed");
                        }
                    });
                    builder.show();
                } else {
                    connectToVPNBtn.setBackgroundResource(R.drawable.black_yellow_semaphore_animation);
                    ((AnimationDrawable) connectToVPNBtn.getBackground()).start();
                    if (ovcs.vpnPreparePermissionIntent() != null) {
                        try {
                            startActivityForResult(ovcs.vpnPreparePermissionIntent(), VPN_SERVICE_INTENT_PERMISSION);
                        } catch (ActivityNotFoundException ane) {
                            // Shame on you Sony! At least one user reported that
                            // an official Sony Xperia Arc S image triggers this exception
                            VpnStatus.logError("ActivityNotFoundException PIZDEC!!!!!");
                            log.debug("ActivityNotFoundException PIZDEC!!!!!!!!!!!");
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
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        log.debug("check is vpn active");
                        if (ovcs.isVPNActive()) {
                            log.debug("vpn is active");
                            try {
                                ovcs.disconnectFromVPN();
                                userVPNPolicy.afterDisconnectVPN();
                            } catch (RemoteException e) {
                                log.error(e.getMessage());
                                return null;
                            }
                        }
                        userVPNPolicy.reInitUserServiceCrutch();
                        userVPNPolicy.deleteUserSettings();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.ic_red_semaphore);
                        Intent intent = new Intent(getBaseContext(), InputPinView.class);
                        startActivity(intent);
                        log.info("disconnectFromVPN AsyncTask onPostExecute exit");
                    }
                }.execute();
            }
        });

    }

    private void toggleMenu(RelativeLayout mainLayout) {
        ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) mainLayout.getLayoutParams();
        if (margins.leftMargin == -133) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels;
            margins.setMargins(Math.round(dpWidth / 2) - 133, 0, -Math.round(dpWidth / 2), 0);
            MENU_VISIBLE = true;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            margins.setMargins(-133, 0, 0, 0);
            MENU_VISIBLE = false;
        }

        mainLayout.setLayoutParams(margins);
    }

    private void connectToVPN() {
        log.info("connectToVPN enter.  AsyncTask connectToVPN enter.");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String vpnConfig = userVPNPolicy.getNewRandomVPNServer();
                boolean isReady = ovcs.prepareToConnectVPN(vpnConfig);
                if (!isReady) {
                    // TODO do some UI update
                    return null;
                }
                ovcs.connectToVPN();
                userVPNPolicy.afterConnectedToVPN();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.ic_green_semaphore);
                log.info("AsyncTask connectToVPN onPostExecute exit.");

            }
        }.execute();
        log.info("connectToVPN exit");
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
        } else {
            // TODO make error
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ovcs.bindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ovcs.unBindService();
    }
}
