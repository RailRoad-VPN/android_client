/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package net.rroadvpn.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Button;

import net.rroadvpn.openvpn.core.OpenVPNService;
import net.rroadvpn.openvpn.core.ProfileManager;
import net.rroadvpn.openvpn.core.VpnStatus;

import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.core.IOpenVPNServiceInternal;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static net.rroadvpn.openvpn.core.OpenVPNService.DISCONNECT_VPN;

/**
 * Created by arne on 13.10.13.
 */
public class DisconnectVPN extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private IOpenVPNServiceInternal mService;
    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        showDisconnectDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private void showDisconnectDialog() {

        boolean immediate = getIntent().getBooleanExtra("immediate", false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_cancel);
        builder.setMessage(R.string.cancel_connection_query);
        if (!immediate) {
            builder.setNegativeButton(android.R.string.cancel, this);
        }
        builder.setPositiveButton(R.string.cancel_connection, this);
        builder.setOnCancelListener(this);

        AlertDialog dialog = builder.create();

        if (immediate) {
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                private static final int AUTO_DISMISS_MILLIS = 6000;
                @Override
                public void onShow(final DialogInterface dialog) {
                    final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    final CharSequence positiveButtonText = defaultButton.getText();
                    new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            defaultButton.setText(String.format(
                                    Locale.getDefault(), "%s (%d)",
                                    positiveButtonText,
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                            ));
                        }
                        @Override
                        public void onFinish() {
                            if (((AlertDialog) dialog).isShowing()) {
                                dialog.dismiss();
                            }
                            defaultButton.performClick();
                        }
                    }.start();
                }
            });
        }

        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Intent returnIntent = new Intent();
        if (which == DialogInterface.BUTTON_POSITIVE) {
            ProfileManager.setConntectedVpnProfileDisconnected(this);
            if (mService != null) {
                try {
                    mService.stopVPN(false);
                    returnIntent.setAction(DISCONNECT_VPN);
                } catch (RemoteException e) {
                    VpnStatus.logException(e);
                }
            }
        }

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
