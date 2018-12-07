package net.rroadvpn.activities.pin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.MeasureUnit;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.widget.TextView;
import android.widget.Toast;

import net.rroadvpn.activities.VPNActivity;
import net.rroadvpn.exception.UserPolicyException;
import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.activities.BaseActivity;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UserVPNPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.support.constraint.Constraints.TAG;

public class InputPinView extends BaseActivity {
    private Logger log = LoggerFactory.getLogger(InputPinView.class);

    private PinView pinView;

    private ProcessUserPincodeTask processUserPincodeTaskTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferencesService preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
        UserVPNPolicy userVPNPolicy = new UserVPNPolicy(preferencesService);

        setContentView(R.layout.input_pin_view);

        pinView = (PinView) findViewById(R.id.pinView);
        pinView.setTextColor(
                ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme()));
        pinView.setTextColor(
                ResourcesCompat.getColorStateList(getResources(), R.color.text_colors, getTheme()));
        pinView.setTextSize(40);
//        pinView.setLineColor(
//                ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
//        pinView.setLineColor(
//                ResourcesCompat.getColorStateList(getResources(), R.color.line_colors, getTheme()));
        pinView.setItemCount(4);
        pinView.setItemHeight(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_size));
        pinView.setItemWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_size));
//        pinView.setItemRadius(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_radius));
        pinView.setItemSpacing(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_spacing));
//        pinView.setLineWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_line_width));
        pinView.setAnimationEnable(true);// start animation when adding text
        pinView.setCursorVisible(false);
        pinView.setCursorColor(
                ResourcesCompat.getColor(getResources(), R.color.line_selected, getTheme()));
        pinView.setCursorWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_cursor_width));
        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged() called with: s = [" + s + "], start = [" + start + "], before = [" + before + "], count = [" + count + "]");
                if (start == 3 && before == 1) {
                    pinView.setItemBackgroundColor(getResources().getColor(R.color.line_selected));
                }
            }

            @Override
            public void afterTextChanged(Editable userPincodeValue) {
                if (userPincodeValue.length() == 4) {
                    log.info(String.format("Pin typed: %s. AsyncTask check pin enter", userPincodeValue.toString()));
                    processUserPincodeTaskTask = new ProcessUserPincodeTask(userVPNPolicy, userPincodeValue);
                    processUserPincodeTaskTask.setListener(new ProcessUserPincodeTask.ProcessUserPincodeListener() {
                        @Override
                        public void onProcessUserPincodeListener(Boolean isOk) {
                            if (isOk) {
                                Intent intent = new Intent(getBaseContext(), VPNActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getBaseContext(), "Wrong pin", Toast.LENGTH_LONG).show();
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
                // TODO l10n
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://rroadvpn.net/en/profile"));
                startActivity(browserIntent);
            }
        });
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

    private static class ProcessUserPincodeTask extends AsyncTask<Void, Void, Boolean> {
        private Logger log = LoggerFactory.getLogger(InputPinView.class);

        private ProcessUserPincodeListener listener;

        private UserVPNPolicy userVPNPolicy;
        private Editable userPincode;

        ProcessUserPincodeTask(UserVPNPolicy userVPNPolicy, Editable userPincode) {
            this.userVPNPolicy = userVPNPolicy;
            this.userPincode = userPincode;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                userVPNPolicy.checkPinCode(Integer.valueOf(userPincode.toString()));
            } catch (UserPolicyException e) {
                log.error("UserServiceException: {}", e);
                return false;
            }

            try {
                userVPNPolicy.createUserDevice();
            } catch (UserPolicyException e) {
                log.error("UserServiceException: {}", e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            super.onPostExecute(isOk);
            if (listener != null) {
                listener.onProcessUserPincodeListener(isOk);
            }
        }

        void setListener(ProcessUserPincodeListener listener) {
            this.listener = listener;
        }

        public interface ProcessUserPincodeListener {
            void onProcessUserPincodeListener(Boolean value);
        }
    }
}