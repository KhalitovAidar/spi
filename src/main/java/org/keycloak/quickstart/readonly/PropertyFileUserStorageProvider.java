package org.keycloak.quickstart.readonly;

import java.util.stream.Stream;

import online.agenta.ApiException;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.credential.PasswordUserCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;
import org.openapitools.client.model.UserDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.agenta.api.UsersApi;
import org.openapitools.client.model.UserRequestValidateDto;

@Slf4j
@RequiredArgsConstructor
public class PropertyFileUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator,
        CredentialInputUpdater {
    protected final KeycloakSession session;
    protected final ComponentModel model;

    private final UsersApi client;

    // UserLookupProvider methods

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        try {
            UserDto userDto = client.usersHttpControllerGetUserByEmail(username);
            return createAdapter(realm, userDto.getEmail());
        } catch (Exception e) {
            log.error("Error while handling getUserByUsername", e);
            return null;
        }
    }

    protected UserModel createAdapter(RealmModel realm, String username) {
        return new AbstractUserAdapter(session, realm, model) {
            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public void removeRequiredAction(String action) {
            }

            @Override
            public void addRequiredAction(String action) {

            }

            @Override
            public SubjectCredentialManager credentialManager() {
                return new UserCredentialManager(session, realm, this);
            }
        };
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(realm, username);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null;
    }

    // CredentialInputValidator methods

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return true;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel))
            return false;

        UserRequestValidateDto userRequestValidateDto = new UserRequestValidateDto();
        userRequestValidateDto.setEmail(user.getUsername());
        userRequestValidateDto.setPassword(input.getChallengeResponse());

        try {
            client.usersHttpControllerValidate(userRequestValidateDto);
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    // CredentialInputUpdater methods

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (input.getType().equals(PasswordCredentialModel.TYPE))
            throw new ReadOnlyException("user is read only for this update");

        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {

    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return Stream.empty();
    }

    @Override
    public void close() {

    }
}