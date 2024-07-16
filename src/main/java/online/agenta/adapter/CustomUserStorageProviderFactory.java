package online.agenta.adapter;

import online.agenta.api.UsersApi;
import online.agenta.storage.provider.CustomUserStorageProvider;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class CustomUserStorageProviderFactory implements UserStorageProviderFactory<CustomUserStorageProvider> {
    @Override
    public CustomUserStorageProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        UsersApi usersApi = new UsersApi();
        usersApi.setCustomBaseUrl("http://app:3000");
        System.out.println("Try to create");
        return new CustomUserStorageProvider(keycloakSession, componentModel, usersApi);
    }
    @Override
    public String getId() {
        return "user-provider";
    }
}
