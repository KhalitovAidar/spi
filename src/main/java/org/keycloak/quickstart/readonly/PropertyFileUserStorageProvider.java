package org.keycloak.quickstart.readonly;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        UserQueryProvider,
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
            return createAdapter(realm, userDto);
        } catch (Exception e) {
            log.error("Error while handling getUserByUsername", e);
            return null;
        }
    }

    protected UserModel createAdapter(RealmModel realm, UserDto user) {
        return new AbstractUserAdapter(session, realm, model) {
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
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> map, Integer firstResult, Integer maxResult) {
        System.out.println("searchForUserStream");
        System.out.println(map.keySet());
        System.out.println(map.get("keycloak.session.realm.users.query.search"));
        System.out.println(map.values());
        System.out.println(firstResult);
        System.out.println(maxResult);

        List<UserDto> userDtoList;
        StringBuilder filter = new StringBuilder();
        String fields = "*";
        String omit = "";
        String sort = "";

        if(map.get("keycloak.session.realm.users.query.include_service_account").equals("false")) {
            String fullTextSearch = map.get("keycloak.session.realm.users.query.search");
            userDtoList = client.list(
                    ListRequestDTO.builder()
                                .filter(null)
                                .fields(fields)
                                .omit(omit)
                                .sort(sort)
                                .q(fullTextSearch)
                                .offset(firstResult)
                                .limit(maxResult)
                                .build()
            );
            return userDtoList.stream().map((user) -> createAdapter(realmModel, user));
        }
        else {
            for(String key: map.keySet()) {
                if (!key.equals("keycloak.session.realm.users.query.include_service_account")) {
                    switch (key) {
                        case "username", "email" -> filter.append("&email=%s".formatted(key));
                        case "firstName" -> filter.append("&name.first=%s".formatted(key));
                        case "lastName" -> filter.append("&name.last=%s".formatted(key));
                    }
                }
            }

            int indexToDelete = filter.indexOf("&");
            if (indexToDelete != -1)
                filter.deleteCharAt(indexToDelete);

            userDtoList = client.list(
                    ListRequestDTO.builder()
                            .filter(filter.toString())
                            .fields(fields)
                            .omit(omit)
                            .sort(sort)
                            .q(null)
                            .offset(firstResult)
                            .limit(maxResult)
                            .build()
            );

            return userDtoList.stream().map((user) -> createAdapter(realmModel, user));
        }
        return null;
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
}