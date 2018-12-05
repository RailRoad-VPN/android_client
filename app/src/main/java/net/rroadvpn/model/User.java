package net.rroadvpn.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {
    private Logger log = LoggerFactory.getLogger(User.class);

    private String uuid;
    private String email;

    public User(String uuid, String email) {
        this.uuid = uuid;
        this.email = email;
    }

    public User(JSONObject userJson) throws JSONException {
        log.debug("get uuid");
        this.uuid = userJson.getString("uuid");
        log.debug("uuid: {}", this.uuid);
        log.debug("get email");
        this.email = userJson.getString("email");
        log.debug("email: {}", this.email);
    }

    public String getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }
}
