package net.rroadvpn.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

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
        this.uuid = userJson.getString("uuid");
        this.email = userJson.getString("email");
        this.createdDate = userJson.getString("created_date");
        this.isEnabled = userJson.getBoolean("enabled");
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
