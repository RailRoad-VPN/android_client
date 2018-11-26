package net.rroadvpn.model;

public final class VPNAppPreferences {


    private static final String API_URL = "http://api.rroadvpn.net";
    private static final String API_VER = "v1";

    public static final String PREF_USER_GLOBAL_KEY = "user_details";
    public static final String USER_UUID = "user_uuid";
    public static final String DEVICE_ID = "device_id";
    public static final String USER_DEVICE_UUID = "user_device_uuid";
    public static final String USER_EMAIL = "user_email";
    public static final String DEVICE_TOKEN = "device_token";

    //TODO change 3 to 2 later
    public static final Integer DEVICE_PLATFORM_ID = 3;
    public static final Integer VPN_TYPE_ID = 1;

    public static String getUserServiceURL (String usersAPIResourceName){
        return API_URL + "/api/" + API_VER + "/" + usersAPIResourceName;
    }
}