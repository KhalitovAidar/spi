package org.keycloak.quickstart.readonly;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import online.agenta.ApiException;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.openapitools.client.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.agenta.api.UsersApi;

@Slf4j
@RequiredArgsConstructor
public class PropertyFileUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        UserQueryProvider,
        CredentialInputValidator,
        CredentialInputUpdater
    {
    protected final KeycloakSession session;
    protected final ComponentModel model;
    private final UsersApi client;

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        try {
            User userDto = client.usersHttpControllerGetUserByEmail(username);
            return createAdapter(realm, userDto);
        } catch (Exception e) {
            log.error("Error while handling getUserByUsername", e);
            return null;
        }
    }

    protected UserModel createAdapter(RealmModel realm, User user) {
        return new AbstractUserAdapter(session, realm, model) {
            @Override
            protected Set<RoleModel> getRoleMappingsInternal() {
                return super.getRoleMappingsInternal();
            }

            @Override
            public boolean hasRole(RoleModel role) {
                log.debug("contains: {}", getAdminEmails().contains(user.getEmail()));
                if (getAdminEmails().contains(user.getEmail()))
                    return true;
                return super.hasRole(role);
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }

            @Override
            public String getFirstName() {
                return user.getName().getFirst();
            }

            @Override
            public String getLastName() {
                return user.getName().getLast();
            }

            @Override
            public String getEmail() {
                return user.getEmail();
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
        log.debug("isValid");
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            log.debug("gui");
            return false;
        }

        UserRequestValidateDto userRequestValidateDto = new UserRequestValidateDto();
        userRequestValidateDto.setEmail(user.getUsername());
        userRequestValidateDto.setPassword(input.getChallengeResponse());
        log.debug("userRequestValidateDto: {}", userRequestValidateDto);
        try {
            log.debug("before usersHttpControllerValidate");
            client.usersHttpControllerValidate(userRequestValidateDto);
            log.debug("after usersHttpControllerValidate");
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
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> map, Integer firstResult, Integer maxResult) {
        String fullTextSearch = getFullTextSearch(map);
        log.debug("fullTextSearch: {}", fullTextSearch);
        String filter = getFilter(map);
        log.debug("filter: {}", filter);


        try {
            log.debug("Call with args");
            List200Response data = client.callList(
                    filter,
                    null,
                    null,
                    null,
                    new BigDecimal(firstResult),
                    new BigDecimal(maxResult),
                    fullTextSearch
            );
            log.debug("Received data: {}", data);
            List<User> users = data.getData();
            Stream<UserModel> userModelStream = users.stream().map((user) -> createAdapter(realmModel, user));
            log.debug("Mapped users: {}", userModelStream);

            return userModelStream;
        } catch (ApiException e) {
            log.error("Error while handling", e);
            throw new RuntimeException(e);
        }
    }

    private static String getFullTextSearch(Map<String, String> map) {
        log.debug("FullTextSearch");
        try {
            return map.get("keycloak.session.realm.users.query.search");
        } catch (NullPointerException e) {
            return null;
        }
    }

    private static String getFilter(Map<String, String> map) {

        try {
            log.debug("GetFilter: {}", map);
            StringBuilder filter = new StringBuilder();

            for(String key: map.keySet()) {
                if (!key.equals("keycloak.session.realm.users.query.include_service_account")) {
                    switch (key) {
                        case "username", "email" -> filter.append("&email=%s".formatted(map.get(key)));
                        case "firstName" -> filter.append("&name.first=%s".formatted(map.get(key)));
                        case "lastName" -> filter.append("&name.last=%s".formatted(map.get(key)));
                    }
                }
            }

            int indexToDelete = filter.indexOf("&");
            if (indexToDelete != -1)
                filter.deleteCharAt(indexToDelete);

            return filter.toString();
        } catch (Exception e) {
            log.error("Error in getFilter", e);
            throw e;
        }
    }

        @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return null;
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return Stream.empty();
    }

    @Override
    public void close() {
    }

    private List<String> getAdminEmails() {
        return List.of(
            "khalitovaidar2404@gmail.com",
            "adelkhalitov1@gmail.com",
            "railhalitov02@gmail.com",
            "pal@alternativa-kzn.ru"
        );
    }
}