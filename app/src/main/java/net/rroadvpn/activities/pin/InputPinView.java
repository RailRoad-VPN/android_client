package net.rroadvpn.activities.pin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import net.rroadvpn.activities.NewMainActivity2;
import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.model.User;
import net.rroadvpn.openvpn.R;
import net.rroadvpn.openvpn.activities.BaseActivity;
import net.rroadvpn.services.PreferencesService;
import net.rroadvpn.services.UsersService;

import java.util.Objects;

import static android.support.constraint.Constraints.TAG;

public class InputPinView extends BaseActivity {
    PreferencesService preferencesService;
    UsersService usersService;
    PinView pinView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(this.getActionBar()).hide();

        this.preferencesService = new PreferencesService(this, VPNAppPreferences.PREF_USER_GLOBAL_KEY);
        String userServiceURL = VPNAppPreferences.getUserServiceURL("users");

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
                if (start == 3 && before == 1) {
                    pinView.setItemBackgroundColor(getResources().getColor(R.color.line_selected));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println(s);
                if (s.length() == 4) {
                    try {
                        User user = usersService.getUserByPinCode(Integer.valueOf(s.toString()));
                        System.out.println(user.getEmail());
                    } catch (UserServiceException e) {
                        Toast.makeText(getBaseContext(), "Wrong pin", Toast.LENGTH_LONG).show();
                        pinView.setItemBackgroundColor(getResources().getColor(R.color.wrong_pin));


                        e.printStackTrace();
                        return;
                    }

                    String userUuid = preferencesService.getString(VPNAppPreferences.USER_UUID);
                    //test post userDevice
                    try {
                        usersService.createUserDevice(userUuid);
                    } catch (UserServiceException e) {
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getBaseContext(), NewMainActivity2.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        pinView.setItemBackgroundColor(Color.BLACK);
        pinView.setItemBackground(getResources().getDrawable(R.drawable.item_background));
        pinView.setItemBackgroundResources(R.drawable.item_background);
        pinView.setHideLineWhenFilled(false);

        pinView.setFocusableInTouchMode(true);
        pinView.requestFocus();

        SpannableString ss = new SpannableString(getString(R.string.enter_a_pin_or_sign_up));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 15, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) findViewById(R.id.pin_tip);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
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