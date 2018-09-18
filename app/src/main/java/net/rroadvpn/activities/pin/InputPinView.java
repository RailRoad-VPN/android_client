package net.rroadvpn.activities.pin;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.Preferences;
import net.rroadvpn.model.User;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UsersService;

import static android.support.constraint.Constraints.TAG;

public class InputPinView extends BaseActivity {
    PreferencesService preferencesService;
    UsersService usersService;
    PinView pinView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.preferencesService = new PreferencesService(this, Preferences.PREF_USER_GLOBAL_KEY);
        String apiURL = "http://internal.novicorp.com:61885";
        String apiVer = "v1";

        String usersAPIResourceName = "users";
        String userServiceURL = apiURL + "/api/" + apiVer + "/" + usersAPIResourceName;
        this.usersService = new UsersService(preferencesService, userServiceURL);

        setContentView(R.layout.input_pin_view);

        pinView = findViewById(R.id.pinView);
        pinView.setTextColor(
                ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme()));
        pinView.setTextColor(
                ResourcesCompat.getColorStateList(getResources(), R.color.text_colors, getTheme()));
        pinView.setLineColor(
                ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
        pinView.setLineColor(
                ResourcesCompat.getColorStateList(getResources(), R.color.line_colors, getTheme()));
        pinView.setItemCount(4);
        pinView.setItemHeight(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_size));
        pinView.setItemWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_size));
        pinView.setItemRadius(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_radius));
        pinView.setItemSpacing(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_spacing));
        pinView.setLineWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_line_width));
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
            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println(s);
                if (s.length() == 4) {
                    try {
                        User user = usersService.getUserByPinCode(Integer.valueOf(s.toString()));
                        System.out.println(user.getEmail());
                    } catch (UserServiceException e) {
                        Toast.makeText(getBaseContext(), "wrong pin?", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    String userUuid = preferencesService.getString(Preferences.USER_UUID);
                    //test post userDevice
                    try {
                        usersService.createUserDevice(userUuid);
                    } catch (UserServiceException e) {
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    System.out.println(preferencesService.getString(Preferences.DEVICE_TOKEN));
                }
            }
        });
        pinView.setItemBackgroundColor(Color.BLACK);
        pinView.setItemBackground(getResources().getDrawable(R.drawable.item_background));
        pinView.setItemBackgroundResources(R.drawable.item_background);
        pinView.setHideLineWhenFilled(false);

        pinView.setFocusableInTouchMode(true);
        pinView.requestFocus();
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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(pinView.getWindowToken(), 0);
    }
}