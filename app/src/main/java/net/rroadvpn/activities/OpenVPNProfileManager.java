package net.rroadvpn.activities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.rroadvpn.openvpn.VpnProfile;
import net.rroadvpn.openvpn.core.ConfigParser;

public class OpenVPNProfileManager {

    private VpnProfile vpnProfile;

    private byte[] profileData;

    public OpenVPNProfileManager(byte[] data) {
        this.profileData = data;
    }

    public VpnProfile getVPNProfile() throws OpenVPNProfileException {
        ConfigParser cp = new ConfigParser();

        try {
            InputStream inputStream = new ByteArrayInputStream(this.profileData);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            cp.parseConfig(inputStreamReader);
            this.vpnProfile = cp.convertProfile();
            return this.vpnProfile;
        } catch (IOException | ConfigParser.ConfigParseError e) {
            System.out.println(e.getLocalizedMessage());
            throw new OpenVPNProfileException();
        }
    }
}
