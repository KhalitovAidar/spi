package org.keycloak.quickstart.readonly;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

import online.agenta.api.UsersApi;

public class PropertyFileUserStorageProviderFactory
        implements UserStorageProviderFactory<PropertyFileUserStorageProvider> {
    public static final String PROVIDER_NAME = "readonly-property-file";

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    @Override
    public PropertyFileUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new PropertyFileUserStorageProvider(session, model, createUserApi());
    }

    private static UsersApi createUserApi() {
        UsersApi usersApi = new UsersApi();
        usersApi.setCustomBaseUrl("http://app:3000");

        return usersApi;
    }
}
