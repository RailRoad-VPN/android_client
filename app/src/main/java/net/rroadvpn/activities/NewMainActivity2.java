package net.rroadvpn.activities;


import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.rroadvpn.activities.pin.InputPinView;
import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.openvpn.core.LogItem;
import net.rroadvpn.openvpn.core.OpenVPNStatusService;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.OpenVPNControlService;
import net.rroadvpn.services.UserVPNPolicy;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static net.rroadvpn.services.OpenVPNControlService.VPN_SERVICE_INTENT_PERMISSION;


public class NewMainActivity2 extends BaseActivity {
    private OpenVPNControlService ovcs;
    private UserVPNPolicy userVPNPolicy;
    private Logger log = LoggerFactory.getLogger(NewMainActivity2.class);


    public static String EXTRA_KEY = "net.rroadvpn.openvpn.shortcutProfileUUID";
    private boolean MENU_VISIBLE = false;

    ImageButton menuBtn;
    LinearLayout menuLayout;

    private void toggleMenu() {
        ViewGroup.LayoutParams menuLP = menuLayout.getLayoutParams();

        if (menuLP.width == 0) {
            DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels;

            menuLP.width = Math.round(dpWidth/2);
            MENU_VISIBLE = true;
        } else {
            menuLP.width = 0;
            MENU_VISIBLE = false;
        }

        menuLayout.setLayoutParams(menuLP);
    }


    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log.info("NewMainActivity2 onCreate enter");
        this.ovcs = new OpenVPNControlService(this);
        this.userVPNPolicy = new UserVPNPolicy(this);

        setContentView(R.layout.new_main_activity2);

        this.menuBtn = findViewById(R.id.main_menu_btn);
        this.menuLayout = findViewById(R.id.main_menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            menuLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

        this.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });

        RelativeLayout viewById = findViewById(R.id.main_wrapper);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MENU_VISIBLE) {
                    toggleMenu();
                }
            }
        });

        Button testMenuItem = findViewById(R.id.test_menu_item);

        testMenuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "PIZEDC", Toast.LENGTH_LONG).show();

            }
        });

        /////


        ImageButton connectToVPNBtn = (ImageButton) findViewById(R.id.connect_to_vpn);
        if (ovcs.isVPNActive()) {
            connectToVPNBtn.setBackgroundResource(R.drawable.ic_green_semaphore);
        }


        connectToVPNBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("connectToVPNBtn button pressed");
                connectToVPNBtn.setBackgroundResource(R.drawable.black_yellow_semaphore_animation);
                ((AnimationDrawable) connectToVPNBtn.getBackground()).start();

                if (ovcs.isVPNActive()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewMainActivity2.this);
                    builder.setTitle("А ТЫ УВЕРЕН?");
                    builder.setMessage("ОТКЛЮЧЕНИЕ ВОДЫ");
                    builder.setNegativeButton("Я ССЫЛКЛО", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            log.info("dialogInterface negative button pressed");
                        }
                    });
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            log.info("dialogInterface positive button pressed. AsyncTask disconnectFromVPN enter.");
//                            ProfileManager.setConntectedVpnProfileDisconnected(getBaseContext());
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
                    builder.setNeutralButton("RECONNECT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            log.info("dialogInterface reconnect button pressed");
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
                    if (ovcs.vpnPreparePermissionIntent() != null) {
                        try {
                            startActivityForResult(ovcs.vpnPreparePermissionIntent(), VPN_SERVICE_INTENT_PERMISSION);
                        } catch (ActivityNotFoundException ane) {
                            // Shame on you Sony! At least one user reported that
                            // an official Sony Xperia Arc S image triggers this exception
                            VpnStatus.logError("ActivityNotFoundException PIZDEC!!!!!");
                            System.out.println("ActivityNotFoundException PIZDEC!!!!!!!!!!!");
                        }
                    } else {
                        connectToVPN();
                    }
                }
            }
        });

        Button testAPIBtn = (Button) findViewById(R.id.test_api);
        testAPIBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("1");

                System.out.println();


////TODO read from file (DO NOT HARDCODE /sdcard/)
//                StringBuilder text = new StringBuilder();
//                try {
////                    File sdcard = Environment.getExternalStorageDirectory();
//                    File sdcard = new File("/sdcard/Android/data/files");
//                    File file = new File(sdcard, "rroadVPN_openVPN_log.2018_10_25.log");
//
//                    BufferedReader br = new BufferedReader(new FileReader(file));
//                    String line;
//                    while ((line = br.readLine()) != null) {
//                        text.append(line);
//                        text.append('\n');
//                    }
//                    br.close();
////                    log.info(String.valueOf(text));
//                    System.out.println(text);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
////<<<<<<<<<<<<<<<<<<<<<
            }
        });


        Button logOut = (Button) findViewById(R.id.log_out);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("Log out button pressed");
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (ovcs.isVPNActive()) {
                            try {
                                ovcs.disconnectFromVPN();
//                                userVPNPolicy.afterDisconnectVPN();
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

    private void connectToVPN() {
        log.info("connectToVPN enter.  AsyncTask connectToVPN enter.");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String vpnConfig = userVPNPolicy.getNewRandomVPNServer();
                ovcs.prepareToConnectVPN(vpnConfig);
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
