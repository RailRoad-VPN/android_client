package net.rroadvpn.services;

import net.rroadvpn.exception.RESTException;
import net.rroadvpn.exception.UserDeviceNotFoundException;
import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.UserDevice;
import net.rroadvpn.model.VPNAppPreferences;
import net.rroadvpn.model.User;
import net.rroadvpn.model.rest.RESTError;
import net.rroadvpn.model.rest.RESTResponse;
import net.rroadvpn.services.rest.RESTService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Base64;

import java.net.HttpURLConnection;
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

        String url = String.format("%s/pincode/%s", this.getServiceURL(), String.valueOf(pincode));
        log.info("URL: " + url);

        Map<String, String> headers = new HashMap<String, String>();

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
        log.info("URL: " + url);

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

    public void createUserDevice(String userUuid, String deviceId, String location,
                                 boolean isActive) throws UserServiceException {
        log.info("createUserDevice method enter");

        String url = String.format("%s/%s/devices", this.getServiceURL(), String.valueOf(userUuid));
        log.info("URL: " + url);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("user_uuid", userUuid);
        userDevice.put("device_id", deviceId);
        userDevice.put("platform_id", VPNAppPreferences.DEVICE_PLATFORM_ID);
        userDevice.put("vpn_type_id", VPNAppPreferences.VPN_TYPE_ID);
        userDevice.put("is_active", true);
        userDevice.put("location", location);

        RESTResponse ur;
        try {
            ur = this.post(url, userDevice, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        if (ur.code != HttpURLConnection.HTTP_CREATED || !ur.getStatus().equals("success")) {
            throw new UserServiceException("we can't create user device");
        }

        log.info("get device token from header x-device-token");
        List<String> xDeviceTokenList = ur.getHeaders().get("x-device-token");
        if (xDeviceTokenList != null) {
            this.deviceToken = xDeviceTokenList.get(0);
            log.debug("Save to preference generated device token: {}", this.deviceToken);
            this.preferencesService.save(VPNAppPreferences.DEVICE_TOKEN, this.deviceToken);
        }

        log.info("get user device uuid form header Location");
        List<String> userDeviceLocation = ur.getHeaders().get("Location");
        if (userDeviceLocation != null) {
            String locationUrl = userDeviceLocation.get(0);
            String userDeviceUuid = locationUrl.substring(locationUrl.lastIndexOf("/") + 1);
            log.debug("Save to preference generated device uuid: {}", userDeviceUuid);
            this.preferencesService.save(VPNAppPreferences.USER_DEVICE_UUID, userDeviceUuid);
        }
        log.info("createUserDevice method exit");
    }

    public String getRandomServerUuid(String userUuid) throws UserServiceException {
        log.info("getRandomServerUuid method enter");

        String url = String.format("%s/%s/servers?random&type_id=" + VPNAppPreferences.VPN_TYPE_ID, this.getServiceURL(), String.valueOf(userUuid));
        log.info("URL: " + url);

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

        if (ur.code != HttpURLConnection.HTTP_OK || !ur.getStatus().equals("success")) {
            throw new UserServiceException("we can't get random server");
        }

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
    }

    public String getVPNConfigurationByUserAndServer(String userUuid, String serverUuid) throws UserServiceException {
        log.info("getVPNConfigurationByUserAndServer method enter. userUuid: {}, serverUuid: {}", userUuid, serverUuid);

        String url = String.format("%s/%s/servers/%s/configurations?vpn_type_id=%s&platform_id=%s",
                this.getServiceURL(), userUuid, serverUuid, VPNAppPreferences.VPN_TYPE_ID,
                VPNAppPreferences.DEVICE_PLATFORM_ID);
        log.info("URL: " + url);
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

        if (ur.code != HttpURLConnection.HTTP_OK || !ur.getStatus().equals("success")) {
            throw new UserServiceException("get vpn config failed");
        }

        JSONObject valueObj = (JSONObject) ur.getData();
        if (valueObj.has("configuration")) {
            log.info("getVPNConfigurationByUserAndServer method exit");
            return valueObj.optString("configuration");
        } else {
            throw new UserServiceException("get vpn config failed");
        }
    }

    public void updateUserDevice(String deviceUuid, String userUuid, String location, boolean isActive, String modifyReason)
            throws UserServiceException {
        log.info("updateUserDevice method enter");

        String url = String.format("%s/%s/devices/%s", this.getServiceURL(), userUuid, deviceUuid);
        log.info("URL: " + url);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());


        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("uuid", deviceUuid);
        userDevice.put("user_uuid", userUuid);
        userDevice.put("is_active", isActive);
        userDevice.put("location", location);
        userDevice.put("device_id", this.deviceId);
        userDevice.put("platform_id", VPNAppPreferences.DEVICE_PLATFORM_ID);
        userDevice.put("vpn_type_id", VPNAppPreferences.VPN_TYPE_ID);
        userDevice.put("modify_reason", modifyReason);

        RESTResponse ur;
        try {
            ur = this.put(url, userDevice, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }


        if (ur.code != HttpURLConnection.HTTP_OK || !ur.getStatus().equals("success")) {
            throw new UserServiceException("we can't update connection");
        }

        log.info("updateUserDevice method exit");
    }

    public String createConnection(String userUuid, String serverUuid, String userDeviceUuid,
                                   String deviceIp, String virtualIp, Long bytesI, Long bytesO)
            throws UserServiceException {
        log.info("createConnection method enter");

        String url = String.format("%s/%s/servers/%s/connections", this.getServiceURL(), userUuid, serverUuid);
        log.info("URL: " + url);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());


        HashMap<String, Object> connection = new HashMap<>();

        connection.put("user_uuid", userUuid);
        connection.put("server_uuid", serverUuid);
        connection.put("user_device_uuid", userDeviceUuid);
        connection.put("device_ip", deviceIp);
        connection.put("virtual_ip", virtualIp);
        connection.put("bytes_i", bytesI);
        connection.put("bytes_o", bytesO);
        connection.put("device_id", this.deviceId);
        connection.put("is_connected", true);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        connection.put("connected_since", nowAsISO);

        RESTResponse ur;
        try {
            ur = this.post(url, connection, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        if (ur.code != HttpURLConnection.HTTP_CREATED || !ur.getStatus().equals("success")) {
            throw new UserServiceException("we can't create connection");
        }

        List<String> connectionLocationHeaderList = ur.getHeaders().get("Location");
        if (connectionLocationHeaderList != null) {
            String locationUrl = connectionLocationHeaderList.get(0);
            String connectionUuid = locationUrl.substring(locationUrl.lastIndexOf("/") + 1);
            this.preferencesService.save(VPNAppPreferences.CONNECTION_UUID, connectionUuid);
            log.info("createConnection method exit");
            return connectionUuid;
        } else {
            throw new UserServiceException("there is no Location header in response");
        }
    }

    public void updateConnection(String connectionUuid, String userUuid, String serverUuid,
                                 String userDeviceUuid, Long bytesI, Long bytesO,
                                 Boolean isConnected, String modifyReason)
            throws UserServiceException {

        String url = String.format("%s/%s/servers/%s/connections/%s", this.getServiceURL(),
                userUuid, serverUuid, connectionUuid);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        HashMap<String, Object> connection = new HashMap<>();

        connection.put("uuid", connectionUuid);
        connection.put("user_uuid", userUuid);
        connection.put("server_uuid", serverUuid);
        connection.put("bytes_i", bytesI);
        connection.put("bytes_o", bytesO);
        connection.put("device_id", this.deviceId);
        connection.put("is_connected", isConnected);
        connection.put("user_device_uuid", userDeviceUuid);
        connection.put("modify_reason", modifyReason);

        RESTResponse ur;
        try {
            ur = this.put(url, connection, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        if (ur.code != HttpURLConnection.HTTP_OK || !ur.getStatus().equals("success")) {
            throw new UserServiceException("we can't update connection");
        }
    }

    public void deleteUserDevice(String userUuid, String userDeviceUuid) throws UserServiceException {
        log.info("deleteUserDevice method enter");
        String url = String.format("%s/%s/devices/%s", this.getServiceURL(), userUuid, userDeviceUuid);
        log.info("URL: " + url);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());


        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("uuid", userDeviceUuid);
        userDevice.put("user_uuid", userUuid);
        userDevice.put("modify_reason", "log out");

        RESTResponse ur;
        try {
            ur = this.delete(url, userDevice, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        if (ur.code != HttpURLConnection.HTTP_OK || !ur.getStatus().equals("success")) {
            throw new UserServiceException("we can't delete user device");
        }

        log.info("deleteUserDevice method exit");
    }

    public UserDevice getUserDevice(String userUuid, String uuid) throws UserServiceException,
            UserDeviceNotFoundException {
        String url = String.format("%s/%s/devices/%s", this.getServiceURL(),
                String.valueOf(userUuid), String.valueOf(uuid));
        log.info("URL: " + url);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", this.deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        RESTResponse ur;
        try {
            ur = this.get(url, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        if (ur.getCode() == 404) {
            throw new UserDeviceNotFoundException("user device WAS NOT found");
        }

        if (ur.getOk()) {
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                JSONObject data = (JSONObject) valueObj;
                try {
                    return new UserDevice(data);
                } catch (JSONException e) {
                    log.error("JSONException: {}", e);
                    throw new UserServiceException(e);
                }
            } else if (valueObj instanceof JSONArray) {
                throw new UserServiceException("got MORE THAN ONE user device by uuid");
            } else {
                throw new UserServiceException("unknown type of data");
            }
        } else {
            throw new UserServiceException("user device IS NOT OK");
        }
    }

    public int createSupportTicket(String userUuid, String email, String description,
                                   Map<String, Object> extraInfo, byte[] zipFile)
            throws UserServiceException {
        log.info("createSupportTicket method enter");

        String url = String.format("%s/%s/tickets", this.getServiceURL(), String.valueOf(userUuid));
        log.info("URL: " + url);

        Map<String, String> headers = new HashMap<String, String>();
        if (!this.deviceToken.equals("")) {
            headers.put("x-device-token", this.deviceToken);
        }
        headers.put("x-auth-token", this.utilities.generateAuthToken());

        HashMap<String, Object> ticket = new HashMap<>();

        ticket.put("contact_email", email);
        ticket.put("description", description);
        if (extraInfo != null) {
            ticket.put("extra_info", extraInfo);
        }
        if (zipFile != null) {
            String s = new String(Base64.encode(zipFile));
            ticket.put("zipfile", s);
        }

        RESTResponse ur;
        try {
            ur = this.post(url, ticket, headers);
        } catch (RESTException e) {
            throw new UserServiceException(e);
        }

        if (ur.code != HttpURLConnection.HTTP_CREATED || !ur.getStatus().equals("success")) {
            throw new UserServiceException("we can't create user ticket");
        }

        List<String> ticketLocationHeaderList = ur.getHeaders().get("Location");
        if (ticketLocationHeaderList != null) {
            String locationUrl = ticketLocationHeaderList.get(0);
            int connectionUuid = Integer.valueOf(locationUrl.substring(locationUrl.lastIndexOf("/") + 1));

            log.info("createSupportTicket method exit");
            return connectionUuid;
        } else {
            throw new UserServiceException("there is no Location header in response");
        }
    }
}
