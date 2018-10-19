package net.rroadvpn.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.openvpn.core.VpnStatus;
import net.rroadvpn.services.OpenVPNControlService;
import net.rroadvpn.services.UserVPNPolicy;


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

    private static final int VPN_SERVICE_INTENT_PERMISSION = 70;

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
                            try {
                                ovcs.disonnectFromVPN();
                                userVPNPolicy.afterDisconnectVPN();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
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
        String vpnConfig = userVPNPolicy.getNewRandomVPNServer();
        ovcs.prepareToConnectVPN(vpnConfig);
        ovcs.connectToVPN();
        userVPNPolicy.afterConnectedToVPN();
    }


//    private void getNewRandomVPNServer() {
//        System.out.println("getNewRandomVPNServer method");
//        try {
//            serverUuid = us.getRandomServerUuid(userUuid);
//            // TODO проверить наличие профиля в ПрофильМенеджере и если нет то получать через API
//            String vpnConfig = us.getVPNConfigurationByUserAndServer(userUuid, serverUuid);
//            System.out.println("MY CONFIG" + vpnConfig);
//            prepareToConnectVPN(vpnConfig);
//        } catch (UserServiceException e) {
//            e.printStackTrace();
//        }
//    }

//    private boolean checkProfileExisting(String serverUuid) {
//        // TODO
//        return false;
//    }
//
//    private ProfileManager getPM() {
//        return ProfileManager.getInstance(getBaseContext());
//    }

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

//    private void connectToVPN() {
//        SharedPreferences prefs2 = Preferences.getDefaultSharedPreferences(getBaseContext());
//        boolean showLogWindow = prefs2.getBoolean("showlogwindow", true);
//
//        ProfileManager.updateLRU(getBaseContext(), mSelectedProfile);
//        VPNLaunchHelper.startOpenVpn(mSelectedProfile, getBaseContext());
//        setResult(RESULT_OK, null);
//        this.workWithAPIDeviceConnectionAndSoOn();
//    }

//    private void workWithAPIDeviceConnectionAndSoOn() {
//        System.out.println("#####################################################  MAIN!!!!" + VpnStatus.getLastCleanLogMessage(getBaseContext()));
//        String status = VpnStatus.getLastCleanLogMessage(getBaseContext());
//
//        while (!status.contains("Connected: SUCCESS")) {
////                System.out.println("openVPN log:" + VpnStatus.getLastCleanLogMessage(getBaseContext()));
//            status = VpnStatus.getLastCleanLogMessage(getBaseContext());
//        }
//
//        System.out.println("WHILE ENDED");
//        String virtualIP = status.split(",")[1];
//        Toast.makeText(getBaseContext(), "YOUR VIRTUAL IP IS: " + virtualIP, Toast.LENGTH_LONG).show();
//
//        try {
//            //TODO cut second UsersService init
//            reInitUserServiceCrutch();
//            //todo device_ip
//            System.out.println("Update user device begin");
//            this.us.updateUserDevice(this.userUuid, this.preferencesService.getString(VPNAppPreferences.USER_DEVICE_UUID), virtualIP, "1.1.1.1");
//
//            String email = this.preferencesService.getString(VPNAppPreferences.USER_EMAIL);
//            System.out.println("Create connection begin");
//            this.us.createConnection(this.serverUuid, virtualIP, "1.1.1.1", email);
//        } catch (UserServiceException e) {
//            e.printStackTrace();
//        }
//    }

//    public void prepareToConnectVPN(String configBase64) {
//        System.out.println("prepareToConnectVPN");
//
//        System.out.println("decode config from bas64");
//        byte[] decoded = android.util.Base64.decode(configBase64, android.util.Base64.DEFAULT);
//
//        OpenVPNProfileManager openVPNProfileManager = new OpenVPNProfileManager(decoded);
//
//        VpnProfile profile;
//        try {
//            System.out.println("work with profile");
//            profile = openVPNProfileManager.getVPNProfile();
//            ProfileManager pm = getPM();
//            pm.addProfile(profile);
//            pm.saveProfileList(getBaseContext());
//            pm.saveProfile(getBaseContext(), profile);
//
//            this.mSelectedProfile = profile;
//
//            EXTRA_KEY = profile.getUUID().toString();
//
//        } catch (OpenVPNProfileException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        // Check if we need to clear the log
//        if (Preferences.getDefaultSharedPreferences(this).getBoolean(CLEARLOG, true))
//            VpnStatus.clearLog();
//
//        int vpnok = profile.checkProfile(getBaseContext());
//        if (vpnok != R.string.no_error_found) {
//            return;
//        }
//
//        Intent vpnPrepareIntent = VpnService.prepare(getBaseContext());
//        // Check if we want to fix /dev/tun
//        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(getBaseContext());
//        boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
//        boolean loadTunModule = prefs.getBoolean("loadTunModule", false);
//
//        if (loadTunModule) {
//            String command = "insmod /system/lib/modules/tun.ko";
//            try {
//                ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
//                Process p = pb.start();
//                int ret = p.waitFor();
//                if (ret == 0)
//                    mCmfixed = true;
//            } catch (InterruptedException | IOException e) {
//                VpnStatus.logException("SU command", e);
//            }
//        }
//        if (usecm9fix && !mCmfixed) {
//            String command = "chown system /dev/tun";
//            try {
//                ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
//                Process p = pb.start();
//                int ret = p.waitFor();
//                if (ret == 0)
//                    mCmfixed = true;
//            } catch (InterruptedException | IOException e) {
//                VpnStatus.logException("SU command", e);
//            }
//        }
//
//        VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
//                ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
//
//        if (vpnPrepareIntent == null) {
//            this.connectToVPN();
//        } else {
//            try {
//                startActivityForResult(vpnPrepareIntent, VPN_SERVICE_INTENT_PERMISSION);
//            } catch (ActivityNotFoundException ane) {
//                // Shame on you Sony! At least one user reported that
//                // an official Sony Xperia Arc S image triggers this exception
//                VpnStatus.logError("ActivityNotFoundException PIZDEC!!!!!");
//                System.out.println("ActivityNotFoundException PIZDEC!!!!!!!!!!!");
//            }
//        }
//    }

//    protected void reInitUserServiceCrutch() {
//        //TODO cut second UsersService init
//
//        String userServiceURL = VPNAppPreferences.getUserServiceURL("users");
//
//        this.preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
//        this.userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);
//
//        this.us = new UsersAPIService(preferencesService, userServiceURL);
//    }

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
