package net.rroadvpn.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.IBinder;
import android.os.RemoteException;

import net.rroadvpn.activities.vpn.OpenVPNProfileException;
import net.rroadvpn.activities.vpn.OpenVPNProfileManager;
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
    public static final String CLEARLOG = "clearlogconnect";
    public static final int VPN_SERVICE_INTENT_PERMISSION = 70;

    public Integer CONNECTION_RETRIES_COUNT = 0;

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
        log.debug("bindService method enter");
        Intent intent = new Intent(ctx, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        ctx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        log.debug("bindService method exit");
    }

    public void unBindService() {
        log.debug("unBindService method enter");
        ctx.unbindService(mConnection);
        log.debug("unBindService method exit");
    }

    public void createNewProfileForServer(String serverUUid, String configBase64) throws OpenVPNProfileException {
        log.debug("createNewProfileForServer method enter");

        log.debug("create new profile through profile manager");
        byte[] decoded = android.util.Base64.decode(configBase64, android.util.Base64.DEFAULT);
        OpenVPNProfileManager openVPNProfileManager = new OpenVPNProfileManager(decoded, serverUUid);

        log.debug("work with profile");
        this.mSelectedProfile = openVPNProfileManager.getVPNProfile();
        ProfileManager pm = getPM();
        pm.addProfile(this.mSelectedProfile);
        pm.saveProfileList(this.ctx);
        pm.saveProfile(this.ctx, this.mSelectedProfile);

        log.debug("createNewProfileForServer method exit");
    }

    public boolean isProfileReady(String serverUUid) {
        log.debug("isProfileReady method enter");

        log.debug("get profile by name");
        VpnProfile profileByName = getPM().getProfileByName(serverUUid);

        log.debug("isProfileReady method exit");
        return profileByName != null;
    }

    public boolean prepareToConnectVPN(String vpnServerUUid) {
        log.info("prepareToConnectVPN method enter");

        this.mSelectedProfile = getPM().getProfileByName(vpnServerUUid);
        if (this.mSelectedProfile == null) {
            return false;
        }

        // Check if we need to clear the log
        if (Preferences.getDefaultSharedPreferences(this.ctx).getBoolean(CLEARLOG, true))
            VpnStatus.clearLog();

        int vpnok = this.mSelectedProfile.checkProfile(this.ctx);
        if (vpnok != R.string.no_error_found) {
            return false;
        }


        // Check if we want to fix /dev/tun
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(this.ctx);
        boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
        boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

        if (loadTunModule) {
            String command = "insmod /system/lib/modules/tun.ko";
            // TODO handle exception
            executeSuCommand(command);
        }

        if (usecm9fix && !mCmfixed) {
            String command = "chown system /dev/tun";
            // TODO handle exception
            executeSuCommand(command);
        }

        log.info("prepareToConnectVPN method exit");
        return true;
    }

    private void executeSuCommand(String command) {
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

    public Intent vpnPreparePermissionIntent() {
        log.info("vpnPreparePermissionIntent method enter");

        VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);

        log.info("vpnPreparePermissionIntent method exit");
        return VpnService.prepare(this.ctx);
    }

    public void connectToVPN() {
        log.info("connectToVPN method enter");

        ProfileManager.updateLRU(this.ctx, mSelectedProfile);
        VPNLaunchHelper.startOpenVpn(mSelectedProfile, this.ctx);
        log.info("connectToVPN method exit");
    }

    public void disconnectVPN() {
        try {
            mService.stopVPN(false);
        } catch (RemoteException e) {
            VpnStatus.logException(e);
        }
    }

    public boolean ismCmfixed() {
        return mCmfixed;
    }

    public boolean isVPNActive() {
        return VpnStatus.isVPNActive();
    }

    public boolean isVPNConnected() {
        return VpnStatus.isVPNConnected();
    }
}
