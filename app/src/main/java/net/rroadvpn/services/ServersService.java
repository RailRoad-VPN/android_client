package net.rroadvpn.services;

import net.rroadvpn.services.rest.RESTService;

public class ServersService extends RESTService {

    public ServersService(PreferencesService preferencesService, String serviceURL) {
        super(preferencesService, serviceURL);
    }
}
