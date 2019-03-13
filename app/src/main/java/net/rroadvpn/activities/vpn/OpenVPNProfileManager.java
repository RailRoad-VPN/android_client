package net.rroadvpn.activities.vpn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.rroadvpn.openvpn.VpnProfile;
import net.rroadvpn.openvpn.core.ConfigParser;

public class OpenVPNProfileManager {

    private VpnProfile vpnProfile;

    private byte[] profileData;
    private String profileName = "convertedProfile";

    public OpenVPNProfileManager(byte[] data, String profileName) {
        this.profileData = data;
        this.profileName = profileName;
    }

    public VpnProfile getVPNProfile() throws OpenVPNProfileException {
        ConfigParser cp = new ConfigParser();

        try {
            InputStream inputStream = new ByteArrayInputStream(this.profileData);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            cp.parseConfig(inputStreamReader);
            this.vpnProfile = cp.convertProfile(this.profileName);
            return this.vpnProfile;
        } catch (IOException | ConfigParser.ConfigParseError e) {
           System.out.println(e.getLocalizedMessage());
            throw new OpenVPNProfileException();
        }
    }
}
