package net.rroadvpn.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.OpenVPNControlService;
import net.rroadvpn.services.RroadLogger;
import net.rroadvpn.services.UserVPNPolicy;

import java.util.logging.Logger;

import static net.rroadvpn.services.OpenVPNControlService.VPN_SERVICE_INTENT_PERMISSION;


public class NewMainActivity2 extends BaseActivity {
    private OpenVPNControlService ovcs;
    private UserVPNPolicy userVPNPolicy;


//    private UsersAPIService us;
//    private String userUuid;
//    private String serverUuid;
//    private PreferencesService preferencesService;
//    public static int REQUIRE_PIN = 0;
//    public static int START_VPN = 1;

    //    private boolean mCmfixed = false;
    public static String EXTRA_KEY = "net.rroadvpn.openvpn.shortcutProfileUUID";
//    public static final String EXTRA_NAME = "net.rroadvpn.openvpn.shortcutProfileName";
//    public static final Boolean EXTRA_HIDELOG = false;
//    public static final String CLEARLOG = "clearlogconnect";
//
//    // TODO
//    private VpnProfile mSelectedProfile;
//
//    private IOpenVPNServiceInternal mService;
//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mService = null;
//        }
//
//    };

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.ovcs = new OpenVPNControlService(this);
        this.userVPNPolicy = new UserVPNPolicy(this);

        setContentView(R.layout.new_main_activity2);

        ImageButton connectToVPNBtn = (ImageButton) findViewById(R.id.connect_to_vpn);

        connectToVPNBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToVPNBtn.setBackgroundResource(R.drawable.black_yellow_semaphore_animation);
                ((AnimationDrawable) connectToVPNBtn.getBackground()).start();

                System.out.println(VpnStatus.isVPNActive());
                if (ovcs.isVPNActive()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewMainActivity2.this);
                    builder.setTitle("А ТЫ УВЕРЕН?");
                    builder.setMessage("ОТКЛЮЧЕНИЕ ВОДЫ");
                    builder.setNegativeButton("Я ССЫЛКЛО", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.out.println("#####################################################  NEGATIVE BUTTON!!!!");
                        }
                    });
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.out.println("#####################################################  POSITIVE BUTTON!!!!");
//                            ProfileManager.setConntectedVpnProfileDisconnected(getBaseContext());
                            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        ovcs.disonnectFromVPN();
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                    userVPNPolicy.afterDisconnectVPN();
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void result) {
                                    findViewById(R.id.connect_to_vpn).setBackgroundResource(R.drawable.ic_red_semaphore);
                                }
                            }.execute();

                        }
                    });
                    builder.setNeutralButton("RECONNECT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.out.println("#####################################################  RECONNECT BUTTON!!!!");
                        }
                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            System.out.println("#####################################################  CANCEL BUTTON!!!!");
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

                RroadLogger.writeLog("test log file");
//                try {
//                    String randomServerUuid = us.getRandomServerUuid(userUuid);
//                } catch (UserServiceException e) {
//                    e.printStackTrace();
//                }
//
//                try {
//                    String randomServerUuid = us.getRandomServerUuid(userUuid);
//                } catch (UserServiceException e) {
//                    e.printStackTrace();
//                }

//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            String randomServerUuid = us.getRandomServerUuid(userUuid);
//                            us.createUserDevice(userUuid);
//                        } catch (UserServiceException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, 20000);
            }
        });

    }

    private void connectToVPN(){
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
            }
        }.execute();

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
