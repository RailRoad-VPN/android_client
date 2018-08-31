package net.rroadvpn.services;

import net.rroadvpn.exception.UserServiceException;
import net.rroadvpn.model.User;
import net.rroadvpn.model.rest.RESTResponse;
import net.rroadvpn.services.rest.RESTService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UsersService extends RESTService {

    public UsersService(String serviceURL) {
        super(serviceURL);
    }

    public User getUserByPinCode(Integer pincode) throws UserServiceException {

        String url = String.format("%s/pincode/%s", this.getServiceURL(), String.valueOf(pincode));

        RESTResponse ur = this.get(url, null);

        if (ur.isOk) {


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
                throw new UserServiceException("Got more than one user by pincode");
            }


        } else {
            //todo
        }
        return null;
    }

//    public User getUserByUuid(String uuid) {
//        String url = this.getServiceURL() + "/" + uuid;
//
//        RESTResponse ur = this.get(null, null);
//
//        if (ur.isOk) {
//            HashMap data = ur.data;
//            String respUuid = (String) data.get("uuid");
//            String eMail = (String) data.get("email");
//            String created_date = (String) data.get("created_date");
//            String created_date = (String) data.get("created_date");
//            return new User(uuid, eMail, created_date);
//        } else {
//            //todo
//        }
//        return null;
//    }
}
