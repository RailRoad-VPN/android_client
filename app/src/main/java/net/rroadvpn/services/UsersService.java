package net.rroadvpn.services;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.model.User;
import net.rroadvpn.model.rest.RESTResponse;
import net.rroadvpn.services.rest.RESTService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class UsersService extends RESTService {
    private Utilities utilities;
    private String deviceToken;
    private String deviceId;

    public UsersService(PreferencesService preferencesService, String serviceURL) {
        super(preferencesService, serviceURL);
        this.deviceToken = this.preferencesService.getString(VPNAppPreferences.DEVICE_TOKEN);
        this.deviceId = this.preferencesService.getString(VPNAppPreferences.DEVICE_ID);
        this.utilities = new Utilities();
    }

    public User getUserByPinCode(Integer pincode) throws UserServiceException {

        String url = String.format("%s/pincode/%s", this.getServiceURL(), String.valueOf(pincode));


        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", this.deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        RESTResponse ur = this.get(url, headers);

        if (ur.getOk()) {
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                JSONObject data = (JSONObject) valueObj;
                try {
                    User user = new User(data);
                    this.preferencesService.save(VPNAppPreferences.USER_UUID, user.getUuid());
                    this.preferencesService.save(VPNAppPreferences.USER_EMAIL, user.getEmail());
                    return user;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (valueObj instanceof JSONArray) {
                throw new UserServiceException("got more than one user by pincode");
            }
        } else {
            throw new UserServiceException("user is not OK");
        }
        return null;
    }

    public User getUserByUuid(String uuid) throws UserServiceException {

        String url = String.format("%s/%s/devices", this.getServiceURL(), String.valueOf(uuid));

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", this.deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        RESTResponse ur = this.get(url, headers);

        if (ur.getOk()) {
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                JSONObject data = (JSONObject) valueObj;
                try {
                    return new User(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (valueObj instanceof JSONArray) {
                throw new UserServiceException("got more than one user by pincode");
            }
        } else {
            throw new UserServiceException("user is not OK");
        }
        return null;

    }

    public void createUserDevice(String userUuid) throws UserServiceException {
        String url = String.format("%s/%s/devices", this.getServiceURL(), String.valueOf(userUuid));

        String deviceId = String.valueOf(utilities.getRandomInt(100000, 999999));
        this.preferencesService.save(VPNAppPreferences.DEVICE_ID, deviceId);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("x-auth-token", this.utilities.generateAuthToken());


        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("user_uuid", userUuid);
        userDevice.put("device_id", deviceId);
        userDevice.put("platform_id", VPNAppPreferences.DEVICE_PLATFORM_ID);
        userDevice.put("vpn_type_id", VPNAppPreferences.VPN_TYPE_ID);
        userDevice.put("is_active", true);
        userDevice.put("location", "test_android2");

        RESTResponse ur = this.post(url, userDevice, headers);

        List<String> xDeviceTokenList = ur.getHeaders().get("x-device-token");
        if (xDeviceTokenList.size() > 0) {
            this.preferencesService.save(VPNAppPreferences.DEVICE_TOKEN, xDeviceTokenList.get(0));
            this.deviceToken = xDeviceTokenList.get(0);
        }

        List<String> userDeviceLocation = ur.getHeaders().get("location");
        if (ur.getHeaders().get("Location").size() > 0) {
            String location = userDeviceLocation.get(0);
            String userDeviceUuid = location.substring(location.lastIndexOf("/") + 1);
            this.preferencesService.save(VPNAppPreferences.USER_DEVICE_UUID, userDeviceUuid);
        }
    }

    public String getRandomServerUuid(String userUuid) throws UserServiceException {
        System.out.println("getRandomServerUuid method");

        String url = String.format("%s/%s/servers?random", this.getServiceURL(), String.valueOf(userUuid));
        Map<String, String> headers = new HashMap<String, String>();


        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());
        RESTResponse ur = this.get(url, headers);

        if (ur.getStatus().equals("success")) {
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                JSONObject data = (JSONObject) valueObj;
                try {
                    return data.getString("uuid");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (valueObj instanceof JSONArray) {
                throw new UserServiceException("got more than one random server");
            }
        } else {
            throw new UserServiceException("get random server failed");
        }
        return null;
    }

    public String getVpnConfigByUuid(String userUuid, String serverUuid) throws UserServiceException {
        System.out.println(String.format("getRandomServerUuid method with parameters userUuid: %s, serverUuid: %s", userUuid, serverUuid));

        String url = String.format("%s/%s/servers/%s/configurations?vpn_type_id=%s&platform_id=%s",
                this.getServiceURL(), userUuid, serverUuid, VPNAppPreferences.VPN_TYPE_ID,
                VPNAppPreferences.DEVICE_PLATFORM_ID);
        Map<String, String> headers = new HashMap<String, String>();


        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        RESTResponse ur = this.get(url, headers);

        if (ur.getStatus().equals("success")) {
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                JSONObject data = (JSONObject) valueObj;
                try {
                    return data.getString("configuration");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (valueObj instanceof JSONArray) {
                throw new UserServiceException("got more than one vpn config");
            }
        } else {
            throw new UserServiceException("get vpn config failed");
        }
        return null;
    }

    public void updateUserDevice(String userUuid, String userDeviceUuid, String virtualIp, String deviceIp) throws UserServiceException {
        String url = String.format("%s/%s/devices/%s", this.getServiceURL(), userUuid, userDeviceUuid);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());


        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("uuid", userDeviceUuid);
        userDevice.put("user_uuid", userUuid);
        userDevice.put("virtual_ip", virtualIp);
        userDevice.put("device_ip", deviceIp);
        userDevice.put("is_active", true);
        userDevice.put("location", "updated_test_android2");
        userDevice.put("device_id", this.deviceId);
        userDevice.put("platform_id", VPNAppPreferences.DEVICE_PLATFORM_ID);
        userDevice.put("vpn_type_id", VPNAppPreferences.VPN_TYPE_ID);
        userDevice.put("modify_reason", "set virtual_ip");

        RESTResponse ur = this.put(url, userDevice, headers);
    }

    public void createConnection(String serverUuid, String virtualIp, String deviceIp, String email) throws UserServiceException {

        String url = String.format("%s/%s/connections", this.getServiceURL().replace("users", "vpns/servers"), serverUuid);
        System.out.println(url);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());


        HashMap<String, Object> server = new HashMap<>();

        server.put("uuid", serverUuid);
        server.put("type", "openvpn");


        HashMap<String, Object> users = new HashMap<String, Object>();
        HashMap<String, Object> user = new HashMap<String, Object>();

        user.put("email", email);
        user.put("device_ip", deviceIp);
        user.put("virtual_ip", virtualIp);
        user.put("bytes_i", 0);
        user.put("bytes_o", 0);
        user.put("device_id", this.deviceId);

        System.out.println("DEVICE_ID " + this.deviceId + "\n VIRTUAL_IP " + virtualIp);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        user.put("connected_since", nowAsISO);

        users.put(email, user);

        HashMap<String, Object> connection = new HashMap<String, Object>();

        connection.put("server", server);
        connection.put("users", users);

        RESTResponse ur = this.post(url, connection, headers);


    }


}
