package net.rroadvpn.services;

import net.rroadvpn.services.rest.RESTService;
import net.rroadvpn.services.rest.RESTServiceI;

public class UsersService extends RESTService {

    public UsersService(String serviceURL) {
        super(serviceURL);
    }

    public void getUserByPinCode(Integer pincode) {
        String url = this.getServiceURL() + "/pincode/" + String.valueOf(pincode);
        this.get(null, null, null);
    }
}
