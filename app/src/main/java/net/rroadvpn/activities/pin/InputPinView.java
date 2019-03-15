package net.rroadvpn.activities.pin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.rroadvpn.activities.VPNActivity;
import net.rroadvpn.exception.UserPolicyException;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.activities.BaseActivity;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UserVPNPolicyI;
import net.rroadvpn.services.UserVPNPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputPinView extends BaseActivity {
    private Logger log = LoggerFactory.getLogger(InputPinView.class);

    private PinView pinView;

    private ProcessUserPincodeTask processUserPincodeTaskTask;

    private static String location;

    public static final Integer PROCESS_PINCODE_NO_ERROR = 100;
    public static final Integer PROCESS_PINCODE_WRONG_ERROR_CODE = 101;
    public static final Integer PROCESS_PINCODE_NO_NETWORK_ERROR_CODE = 102;
    public static final Integer PROCESS_PINCODE_UNKNOWN_ERROR_CODE = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferencesService preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
        UserVPNPolicyI userVPNPolicyI = new UserVPNPolicy(preferencesService);

        setContentView(R.layout.input_pin_view);

        pinView = (PinView) findViewById(R.id.pinView);
        pinView.setTextColor(
                ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme()));
        pinView.setTextColor(
                ResourcesCompat.getColorStateList(getResources(), R.color.text_colors, getTheme()));
        pinView.setTextSize(40);
        pinView.setItemCount(4);
        pinView.setItemHeight(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_size));
        pinView.setItemWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_size));
        pinView.setItemSpacing(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_spacing));
        pinView.setAnimationEnable(true);// start animation when adding text
        pinView.setCursorVisible(false);
        pinView.setCursorColor(
                ResourcesCompat.getColor(getResources(), R.color.line_selected, getTheme()));
        pinView.setCursorWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_cursor_width));

        InputPinView that = this;

        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 3 && before == 1) {
                    pinView.setItemBackgroundColor(getResources().getColor(R.color.line_selected));
                }
            }

            @Override
            public void afterTextChanged(Editable userPincodeValue) {
                if (userPincodeValue.length() == 4) {
                    log.info(String.format("Pin typed: %s. AsyncTask check pin enter", userPincodeValue.toString()));
                    processUserPincodeTaskTask = new ProcessUserPincodeTask(userVPNPolicyI, userPincodeValue);
                    processUserPincodeTaskTask.setListener(new ProcessUserPincodeTask.ProcessUserPincodeListener() {
                        @Override
                        public void onProcessUserPincodeListener(Integer code) {
                            if (PROCESS_PINCODE_NO_ERROR.equals(code)) {
                                Intent intent = new Intent(getBaseContext(), VPNActivity.class);
                                startActivity(intent);
                                finish();

                            } else if (PROCESS_PINCODE_NO_NETWORK_ERROR_CODE.equals(code) || PROCESS_PINCODE_WRONG_ERROR_CODE.equals(code)) {
                                int messageId;
                                if (PROCESS_PINCODE_NO_NETWORK_ERROR_CODE.equals(code)) {
                                    messageId = R.string.network_error;
                                } else {
                                    messageId = R.string.pin_error;
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(that);
                                builder.setMessage(messageId);
                                builder.setCancelable(true);
                                builder.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pinView.setText(pinView.getText());
                                        dialog.dismiss();
                                    }
                                });
                                builder.setNegativeButton(R.string.write_support, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface errDialog, int which) {
                                        final Dialog dialog = new Dialog(that);
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
                                                                    null, true);
                                                        } else {
                                                            createErrorDialog(R.string.help_form_error, null, null).show();
                                                        }
                                                    }
                                                });
                                                supportDialogSendTask.execute();
                                            }
                                        });
                                        errDialog.dismiss();
                                        dialog.show();
                                    }
                                });
                                builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                alertDialog.show();

                                pinView.setItemBackgroundColor(getResources().getColor(R.color.wrong_pin));
                            }
                            log.info(String.format("Pin typed: %s. AsyncTask check pin enter", userPincodeValue.toString()));
                        }
                    });
                    processUserPincodeTaskTask.execute();
                }
            }
        });
        pinView.setItemBackgroundColor(Color.BLACK);
        pinView.setItemBackground(getResources().getDrawable(R.drawable.item_background));
        pinView.setItemBackgroundResources(R.drawable.item_background);
        pinView.setHideLineWhenFilled(false);

        pinView.setFocusableInTouchMode(true);
        pinView.requestFocus();

        TextView newUserLink = findViewById(R.id.enter_pin_new_user);
        newUserLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rroadvpn.net/order"));
                startActivity(browserIntent);
            }
        });

        location = getApplicationContext().getResources().getConfiguration().locale.getDisplayCountry();
    }

    @Override
    protected void onPause() {
        super.onPause();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(pinView.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        pinView.postDelayed(() -> {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(pinView, 0);
        }, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (processUserPincodeTaskTask != null) {
            this.processUserPincodeTaskTask.setListener(null);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(pinView.getWindowToken(), 0);
    }

    private static class ProcessUserPincodeTask extends AsyncTask<Void, Void, Integer> {
        private Logger log = LoggerFactory.getLogger(InputPinView.class);

        private ProcessUserPincodeListener listener;

        private UserVPNPolicyI userVPNPolicyI;
        private Editable userPincode;

        ProcessUserPincodeTask(UserVPNPolicyI userVPNPolicyI, Editable userPincode) {
            this.userVPNPolicyI = userVPNPolicyI;
            this.userPincode = userPincode;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                userVPNPolicyI.checkPinCode(Integer.valueOf(userPincode.toString()));
            } catch (UserPolicyException e) {
                log.error("UserServiceException: {}", e);
                return PROCESS_PINCODE_WRONG_ERROR_CODE;
            } catch (Exception e) {
                log.error("Exception: {}", e);
                return PROCESS_PINCODE_NO_NETWORK_ERROR_CODE;
            }

            try {

                userVPNPolicyI.createUserDevice(location);
            } catch (UserPolicyException e) {
                log.error("UserServiceException: {}", e);
                return PROCESS_PINCODE_UNKNOWN_ERROR_CODE;
            } catch (Exception e) {
                log.error("Exception: {}", e);
                return PROCESS_PINCODE_NO_NETWORK_ERROR_CODE;
            }

            return PROCESS_PINCODE_NO_ERROR;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (listener != null) {
                listener.onProcessUserPincodeListener(code);
            }
        }

        void setListener(ProcessUserPincodeListener listener) {
            this.listener = listener;
        }

        public interface ProcessUserPincodeListener {
            void onProcessUserPincodeListener(Integer value);
        }
    }

    private static class SupportDialogSendTask extends AsyncTask<Void, Void, Integer> {
        private Logger log = LoggerFactory.getLogger(SupportDialogSendTask.class);

        private SupportDialogSendTask.SupportDialogSendTaskListener listener;

        private ProgressDialog dialog;
        private UserVPNPolicyI userVPNPolicyI;
        private String email;
        private String description;
        private String logsDir;

        SupportDialogSendTask(UserVPNPolicyI userVPNPolicyI, String email, String description,
                              InputPinView activity) {
            this.userVPNPolicyI = userVPNPolicyI;
            this.email = email;
            this.description = description;
            this.logsDir = activity.getApplicationInfo().dataDir + "/logs";

            dialog = new ProgressDialog(activity);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Sending...");
            dialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            log.debug("support dialog send anonymous ticket");

            try {
                return this.userVPNPolicyI.sendAnonymousSupportTicket(email, description, logsDir);
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
        if (titleId != null) {
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

}