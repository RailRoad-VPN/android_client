package net.rroadvpn.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDevice {
    private Logger log = LoggerFactory.getLogger(User.class);

    private String uuid;
    private String userUuid;
    private int platformId;
    private int vpnTypeId;
    private String deviceId;
    private String location;
    private boolean isActive;
    private String modifyReason;

    public UserDevice(String uuid, String userUuid, int platformId, int vpnTypeId, String deviceId, String location, boolean isActive, String modifyReason) {
        this.uuid = uuid;
        this.userUuid = userUuid;
        this.platformId = platformId;
        this.vpnTypeId = vpnTypeId;
        this.deviceId = deviceId;
        this.location = location;
        this.isActive = isActive;
        this.modifyReason = modifyReason;
    }

    public UserDevice(JSONObject userDeviceJson) throws JSONException {
        this.uuid = userDeviceJson.getString("uuid");
        this.uuid = userDeviceJson.getString("uuid");
        this.userUuid = userDeviceJson.getString("user_uuid");
        this.platformId = userDeviceJson.getInt("platform_id");
        this.vpnTypeId = userDeviceJson.getInt("vpn_type_id");
        this.deviceId = userDeviceJson.getString("device_id");
        this.location = userDeviceJson.getString("location");
        this.isActive = userDeviceJson.getBoolean("is_active");
        this.modifyReason = userDeviceJson.getString("modify_reason");
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    public int getVpnTypeId() {
        return vpnTypeId;
    }

    public void setVpnTypeId(int vpnTypeId) {
        this.vpnTypeId = vpnTypeId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getModifyReason() {
        return modifyReason;
    }

    public void setModifyReason(String modifyReason) {
        this.modifyReason = modifyReason;
    }
}
