package net.rroadvpn.services;

import net.rroadvpn.exception.RESTException;
import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.model.User;
import net.rroadvpn.model.rest.RESTResponse;
import net.rroadvpn.services.rest.RESTService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class UsersAPIService extends RESTService implements UsersAPIServiceI {
    private Utilities utilities;
    private String deviceToken;
    private String deviceId;
    private Logger log = LoggerFactory.getLogger(UsersAPIService.class);


    UsersAPIService(PreferencesService preferencesService, String serviceURL) {
        super(preferencesService, serviceURL);
        this.deviceToken = this.preferencesService.getString(VPNAppPreferences.DEVICE_TOKEN);
        this.deviceId = this.preferencesService.getString(VPNAppPreferences.DEVICE_ID);
        this.utilities = new Utilities();
    }

    public User getUserByPinCode(Integer pincode) throws UserServiceException {
        log.debug("getUserByPinCode method enter");

        log.debug("create url");
        String url = String.format("%s/pincode/%s", this.getServiceURL(), String.valueOf(pincode));
        log.debug("url: {}", url);

        Map<String, String> headers = new HashMap<String, String>();

        log.debug("generate auth token");
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        RESTResponse ur;
        try {
            log.debug("do get call");
            ur = this.get(url, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        log.debug("check is request is ok");
        if (ur.getOk()) {
            log.debug("request is OK");

            log.debug("get data");
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                log.debug("data is instance of JSONObject");
                JSONObject data = (JSONObject) valueObj;
                log.debug("data: {}", data);
                try {
                    log.debug("get user from data");
                    User user = new User(data);

                    log.debug("save user uuid and email in preferences");
                    this.preferencesService.save(VPNAppPreferences.USER_UUID, user.getUuid());
                    this.preferencesService.save(VPNAppPreferences.USER_EMAIL, user.getEmail());
                    log.info("getUserByPinCode method exit");
                    return user;
                } catch (JSONException e) {
                    log.error("UserServiceException: {}", e);
                    throw new UserServiceException("JSON parse exception");
                }
            } else if (valueObj instanceof JSONArray) {
                log.debug("data is instance of JSONArray - bad situation");
                throw new UserServiceException("got more than one user by pincode");
            } else {
                throw new UserServiceException("got bad body");
            }
        } else {
            throw new UserServiceException("user is not OK");
        }
    }

    public User getUserByUuid(String uuid) throws UserServiceException {
        log.info("getUserByUuid method enter");

        String url = String.format("%s/%s/devices", this.getServiceURL(), String.valueOf(uuid));

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", this.deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        RESTResponse ur;
        try {
            log.debug("do get call");
            ur = this.get(url, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        if (ur.getOk()) {
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                JSONObject data = (JSONObject) valueObj;
                try {
                    return new User(data);
                } catch (JSONException e) {
                    log.error("JSONException: {}", e);
                    throw new UserServiceException(e);
                }
            } else if (valueObj instanceof JSONArray) {
                throw new UserServiceException("got more than one user by pincode");
            } else {
                throw new UserServiceException("unknown type of data");
            }
        } else {
            throw new UserServiceException("user is not OK");
        }

    }

    public void createUserDevice(String userUuid) throws UserServiceException {
        log.info("createUserDevice method enter");

        String url = String.format("%s/%s/devices", this.getServiceURL(), String.valueOf(userUuid));

        log.debug("URL for request: {}", url);
        String deviceId = String.valueOf(utilities.getRandomInt(100000, 999999));
        log.debug("Save to preference generated device id: {}", deviceId);
        this.preferencesService.save(VPNAppPreferences.DEVICE_ID, deviceId);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        log.debug("Prepare data for request");
        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("user_uuid", userUuid);
        userDevice.put("device_id", deviceId);
        userDevice.put("platform_id", VPNAppPreferences.DEVICE_PLATFORM_ID);
        userDevice.put("vpn_type_id", VPNAppPreferences.VPN_TYPE_ID);
        userDevice.put("is_active", true);
        userDevice.put("location", "test_android2");
        log.debug("Prepared: {}", userDevice.toString());

        RESTResponse ur;
        try {
            ur = this.post(url, userDevice, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        List<String> xDeviceTokenList = ur.getHeaders().get("x-device-token");
        if (xDeviceTokenList != null) {
            this.preferencesService.save(VPNAppPreferences.DEVICE_TOKEN, xDeviceTokenList.get(0));
            this.deviceToken = xDeviceTokenList.get(0);
        }

        List<String> userDeviceLocation = ur.getHeaders().get("Location");
        if (userDeviceLocation != null) {
            String location = userDeviceLocation.get(0);
            String userDeviceUuid = location.substring(location.lastIndexOf("/") + 1);
            this.preferencesService.save(VPNAppPreferences.USER_DEVICE_UUID, userDeviceUuid);
        }
        log.info("createUserDevice method exit");
    }

    public String getRandomServerUuid(String userUuid) throws UserServiceException {
        log.info("getRandomServerUuid method enter");

        String url = String.format("%s/%s/servers?random", this.getServiceURL(), String.valueOf(userUuid));
        Map<String, String> headers = new HashMap<String, String>();


        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }

        headers.put("x-auth-token", this.utilities.generateAuthToken());

        RESTResponse ur;
        try {
            ur = this.get(url, headers);
        } catch (RESTException e) {
            log.error("RESTException: {}", e);
            throw new UserServiceException(e);
        }

        if (ur.getStatus().equals("success")) {
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                JSONObject data = (JSONObject) valueObj;
                try {
                    String serverUuid = data.getString("uuid");
                    log.info("getRandomServerUuid method exit");
                    return serverUuid;
                } catch (JSONException e) {
                    log.error("JSONException: {}", e);
                    throw new UserServiceException(e);
                }
            } else if (valueObj instanceof JSONArray) {
                throw new UserServiceException("got more than one random server");
            } else {
                throw new UserServiceException("unknown type of data");
            }
        } else {
            throw new UserServiceException("get random server failed");
        }
    }

    public String getVPNConfigurationByUserAndServer(String userUuid, String serverUuid) throws UserServiceException {
        log.info("getVPNConfigurationByUserAndServer method enter. userUuid: {}, serverUuid: {}", userUuid, serverUuid);

        String url = String.format("%s/%s/servers/%s/configurations?vpn_type_id=%s&platform_id=%s",
                this.getServiceURL(), userUuid, serverUuid, VPNAppPreferences.VPN_TYPE_ID,
                VPNAppPreferences.DEVICE_PLATFORM_ID);
        Map<String, String> headers = new HashMap<String, String>();


        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        RESTResponse ur;
        try {
            ur = this.get(url, headers);
        } catch (RESTException e) {
            log.error("RESTException: {}", e);
            throw new UserServiceException(e);
        }

        if (ur.getStatus().equals("success")) {
            JSONObject valueObj = (JSONObject) ur.getData();
            if (valueObj.has("configuration")) {
                log.info("getVPNConfigurationByUserAndServer method exit");
                return valueObj.optString("configuration");
            }
        }
        throw new UserServiceException("get vpn config failed");
    }

    public void updateUserDevice(String userUuid, String userDeviceUuid, String virtualIp, String deviceIp) throws UserServiceException {
        log.info("updateUserDevice method enter");
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

        try {
            RESTResponse ur = this.put(url, userDevice, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        log.info("updateUserDevice method exit");
    }

    public void createConnection(String serverUuid, String virtualIp, String deviceIp, String email) throws UserServiceException {
        log.info("createConnection method enter");

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

        log.debug("DEVICE_ID " + this.deviceId + "\n VIRTUAL_IP " + virtualIp);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        user.put("connected_since", nowAsISO);

        users.put(email, user);

        HashMap<String, Object> connection = new HashMap<String, Object>();

        connection.put("server", server);
        connection.put("users", users);

        try {
            RESTResponse ur = this.post(url, connection, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        log.info("updateUserDevice method exit");
    }


    public void deleteConnection(String serverUuid, String email) throws UserServiceException {
        log.info("deleteConnection method enter");

        String url = String.format("%s/%s/connections", this.getServiceURL().replace("users", "vpns/servers"), serverUuid);

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
        user.put("bytes_i", 0);
        user.put("bytes_o", 0);
        user.put("device_id", this.deviceId);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        user.put("connected_since", nowAsISO);

        users.put(email, user);

        HashMap<String, Object> connection = new HashMap<String, Object>();

        connection.put("server", server);
        connection.put("users", users);

        log.debug(String.format("url: %s\nheaders: %s\nhasmap: %s", url, headers, connection));
        System.out.println();

        try {
            RESTResponse ur = this.delete(url, connection, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        log.info("deleteConnection method exit");
    }

    public void deleteUserDevice(String userUuid, String userDeviceUuid) throws UserServiceException {
        log.info("deleteUserDevice method enter");
        String url = String.format("%s/%s/devices/%s", this.getServiceURL(), userUuid, userDeviceUuid);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());


        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("uuid", userDeviceUuid);
        userDevice.put("user_uuid", userUuid);
        userDevice.put("modify_reason", "log out");

        try {
            RESTResponse ur = this.delete(url, userDevice, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        log.info("deleteUserDevice method exit");
    }
}
