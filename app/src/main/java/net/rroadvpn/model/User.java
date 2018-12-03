package net.rroadvpn.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {
    private Logger log = LoggerFactory.getLogger(User.class);

    private String uuid;
    private String email;
    private String createdDate;
    private Boolean isEnabled;

    public User(String uuid, String email, String createdDate, Boolean isEnabled) {
        this.uuid = uuid;
        this.email = email;
        this.createdDate = createdDate;
        this.isEnabled = isEnabled;
    }

    public User(JSONObject userJson) throws JSONException {
        log.debug("get uuid");
        this.uuid = userJson.getString("uuid");
        log.debug("uuid: {}", this.uuid);
        log.debug("get email");
        this.email = userJson.getString("email");
        log.debug("email: {}", this.email);
        log.debug("get createdDate");
        this.createdDate = userJson.getString("created_date");
        log.debug("createdDate: {}", this.createdDate);
        log.debug("get isEnabled");
        this.isEnabled = userJson.getBoolean("enabled");
        log.debug("isEnabled: {}", this.isEnabled);
    }

    public String getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }
}
