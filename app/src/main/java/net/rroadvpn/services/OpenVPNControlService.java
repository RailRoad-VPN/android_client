package net.rroadvpn.services;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.IBinder;
import android.os.RemoteException;

import net.rroadvpn.activities.NewMainActivity2;
import net.rroadvpn.activities.OpenVPNProfileException;
import net.rroadvpn.activities.OpenVPNProfileManager;
import net.rroadvpn.exception.OpenVPNControlServiceException;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.VpnProfile;
import net.rroadvpn.openvpn.core.ConnectionStatus;
import net.rroadvpn.openvpn.core.IOpenVPNServiceInternal;
import net.rroadvpn.openvpn.core.OpenVPNService;
import net.rroadvpn.openvpn.core.Preferences;
import net.rroadvpn.openvpn.core.ProfileManager;
import net.rroadvpn.openvpn.core.VPNLaunchHelper;
import net.rroadvpn.openvpn.core.VpnStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OpenVPNControlService {
    //TODO CUT Context
    private Context ctx;
    private VpnProfile mSelectedProfile;
    private boolean mCmfixed = false;
    public static String EXTRA_KEY = "net.rroadvpn.openvpn.shortcutProfileUUID";
    public static final String EXTRA_NAME = "net.rroadvpn.openvpn.shortcutProfileName";
    public static final Boolean EXTRA_HIDELOG = false;
    public static final String CLEARLOG = "clearlogconnect";
    public static final int VPN_SERVICE_INTENT_PERMISSION = 70;

    private Logger log = LoggerFactory.getLogger(OpenVPNControlService.class);
    private IOpenVPNServiceInternal mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }

    };


    public OpenVPNControlService(Context ctx) {
        this.ctx = ctx;
    }

    private ProfileManager getPM() {
        return ProfileManager.getInstance(this.ctx);
    }

    public void bindService() {
        log.debug("bindService method entered");
        Intent intent = new Intent(ctx, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        ctx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unBindService() {
        log.debug("unBindService method entered");
        ctx.unbindService(mConnection);
    }

    public void prepareToConnectVPN(String configBase64) {
        log.info("prepareToConnectVPN method entered");

        byte[] decoded = android.util.Base64.decode(configBase64, android.util.Base64.DEFAULT);

        OpenVPNProfileManager openVPNProfileManager = new OpenVPNProfileManager(decoded);

        VpnProfile profile;
        try {
            log.debug("Working with profile");
            profile = openVPNProfileManager.getVPNProfile();
            ProfileManager pm = getPM();
            pm.addProfile(profile);
            pm.saveProfileList(this.ctx);
            pm.saveProfile(this.ctx, profile);

            this.mSelectedProfile = profile;

            EXTRA_KEY = profile.getUUID().toString();

        } catch (OpenVPNProfileException e) {
            e.printStackTrace();
            return;
        }

        // Check if we need to clear the log
        if (Preferences.getDefaultSharedPreferences(this.ctx).getBoolean(CLEARLOG, true))
            VpnStatus.clearLog();

        int vpnok = profile.checkProfile(this.ctx);
        if (vpnok != R.string.no_error_found) {
            return;
        }


        // Check if we want to fix /dev/tun
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(this.ctx);
        boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
        boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

        if (loadTunModule) {
            String command = "insmod /system/lib/modules/tun.ko";
            try {
                ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
                Process p = pb.start();
                int ret = p.waitFor();
                if (ret == 0)
                    mCmfixed = true;
            } catch (InterruptedException | IOException e) {
                VpnStatus.logException("SU command", e);
            }
        }
        if (usecm9fix && !mCmfixed) {
            String command = "chown system /dev/tun";
            try {
                ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
                Process p = pb.start();
                int ret = p.waitFor();
                if (ret == 0)
                    mCmfixed = true;
            } catch (InterruptedException | IOException e) {
                VpnStatus.logException("SU command", e);
            }
        }
    }

    public Intent vpnPreparePermissionIntent() {
        log.info("vpnPreparePermissionIntent method entered");

        VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);

        return VpnService.prepare(this.ctx);
    }

    public void connectToVPN() {
        log.info("connectToVPN method entered");

        SharedPreferences prefs2 = Preferences.getDefaultSharedPreferences(this.ctx);
        boolean showLogWindow = prefs2.getBoolean("showlogwindow", true);

        ProfileManager.updateLRU(this.ctx, mSelectedProfile);
        VPNLaunchHelper.startOpenVpn(mSelectedProfile, this.ctx);
    }

    public void disconnectFromVPN() throws RemoteException {
        log.info("connectToVPN method entered");
        mService.stopVPN(false);
    }

    public boolean isVPNActive() {
        return VpnStatus.isVPNActive();
    }
}
