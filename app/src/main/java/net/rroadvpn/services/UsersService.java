package net.rroadvpn.services;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.User;
import net.rroadvpn.model.rest.RESTResponse;
import net.rroadvpn.services.rest.RESTService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class UsersService extends RESTService {

    public UsersService(String serviceURL) {
        super(serviceURL);
    }

    public User getUserByPinCode(Integer pincode) throws UserServiceException {

        String url = String.format("%s/pincode/%s", this.getServiceURL(), String.valueOf(pincode));

        RESTResponse ur = this.get(url, null);

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

    public User getUserByUuid(String uuid) throws UserServiceException {

        String url = String.format("%s/%s/devices", this.getServiceURL(), String.valueOf(uuid));

        RESTResponse ur = this.get(url, null);

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

    public void postUserDevice(String uuid) throws UserServiceException {
        String url = String.format("%s/%s/devices", this.getServiceURL(), String.valueOf(uuid));

//todo generate UUID of device
        String deviceUuid = String.valueOf(UUID.randomUUID());

        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("user_guid", uuid);
        userDevice.put("device_id", deviceUuid);
        userDevice.put("platform_id", 2);
        userDevice.put("vpn_type_id", 1);
        userDevice.put("location", "test_android");

        RESTResponse ur = this.post(url, userDevice, null);

        if (!ur.getOk()) {
           throw new UserServiceException("bad post");
        }
    }
}
