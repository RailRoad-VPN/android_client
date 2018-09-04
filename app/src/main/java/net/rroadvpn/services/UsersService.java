package net.rroadvpn.services;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.Preferences;
import net.rroadvpn.model.User;
import net.rroadvpn.model.rest.RESTResponse;
import net.rroadvpn.services.rest.RESTService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UsersService extends RESTService {

    public UsersService(PreferencesService preferencesService, String serviceURL) {
        super(preferencesService, serviceURL);
    }

    public User getUserByPinCode(Integer pincode) throws UserServiceException {

        String url = String.format("%s/pincode/%s", this.getServiceURL(), String.valueOf(pincode));

        RESTResponse ur = this.get(url, null);

        if (ur.getOk()) {
            Object valueObj = ur.getData();
            if (valueObj instanceof JSONObject) {
                JSONObject data = (JSONObject) valueObj;
                try {
                    User user = new User(data);
                    this.preferencesService.save(Preferences.USER_UUID, user.getUuid());
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

    public void createUserDevice(String uuid) throws UserServiceException {
        String url = String.format("%s/%s/devices", this.getServiceURL(), String.valueOf(uuid));

        String deviceUuid = String.valueOf(UUID.randomUUID());
        this.preferencesService.save(Preferences.DEVICE_UUID, deviceUuid);
        
        HashMap<String, Object> userDevice = new HashMap<String, Object>();
        userDevice.put("user_uuid", uuid);
        userDevice.put("device_id", deviceUuid);
        userDevice.put("platform_id", 2);
        userDevice.put("vpn_type_id", 1);
        userDevice.put("is_active", true);
        userDevice.put("location", "test_android2");

        RESTResponse ur = this.post(url, userDevice, null);

        for (Map.Entry<String, List<String>> entry : ur.getHeaders().entrySet())
        {
          System.out.println(entry.getKey() + " - " + entry.getValue());
          if (entry.getKey().toLowerCase().equals("x-device-token")){
              this.preferencesService.save(Preferences.DEVICE_TOKEN, entry.getValue().get(0));
              break;
          }
        }
    }
}
