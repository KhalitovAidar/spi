package online.agenta.storage.provider;

import lombok.RequiredArgsConstructor;
import lombok.var;
import online.agenta.api.UsersApi;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import online.agenta.model.LegacyUser;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.openapitools.client.model.UserDto;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
@RequiredArgsConstructor
public class CustomUserStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        UserRegistrationProvider,
        UserQueryProvider,
        CredentialInputValidator {

    private final KeycloakSession ksession;
    private final ComponentModel model;
    private final UsersApi usersApi;

    @Override
    public boolean supportsCredentialType(String s) {
        return PasswordCredentialModel.TYPE.endsWith(s);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String s) {
        return supportsCredentialType(s);
    }

    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        System.out.println("1) ISVALID");
        if (!supportsCredentialType(credentialInput.getType())) {
            return false;
        }

        try {
            System.out.println("ISVALID");
            return userModel.getEmail().equals(credentialInput.getChallengeResponse());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(RealmModel realmModel, String s) {
        try {
            System.out.println("getUserById");
            UserDto user = usersApi.usersHttpControllerGetUserById(s);
            return mapUser(realmModel, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realmModel, String s) {
        try {
            System.out.println("getUserByUsername");
            UserDto user = usersApi.usersHttpControllerGetUserByEmail(s);
            System.out.println(user);
            var map =  mapUser(realmModel, user);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realmModel, String s) {
        try {
            System.out.println("getUserByEmail");
            UserDto user = usersApi.usersHttpControllerGetUserByEmail(s);
            System.out.println(user);
            var map =  mapUser(realmModel, user);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> map, Integer integer, Integer integer1) {
        System.out.println("searchForUserStream");
        UserModel userModel = new LegacyUser.Builder(ksession, realmModel, model, "khalitovaidar2404@gmail.com")
                .email("khalitovaidar2404@gmail.com")
                .firstName("Айдар")
                .lastName("Марсович")
                .id("123123")
                .roles(Set.of("admin"))
                .build();
        userModel.setEnabled(true);
        return Stream.of(userModel);
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
        System.out.println("getGroupMembersStream");
        throw new RuntimeException("getGroupMembersStream");
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        System.out.println("searchForUserByUserAttributeStream");
        UserModel userModel = new LegacyUser.Builder(ksession, realmModel, model, "khalitovaidar2404@gmail.com")
                .email("khalitovaidar2404@gmail.com")
                .firstName("Айдар")
                .lastName("Марсович")
                .id("123123")
                .roles(Set.of("admin"))
                .build();
        userModel.setEnabled(true);
        return Stream.of(userModel);
    }

    private UserModel mapUser(RealmModel realm, UserDto user) {
        Set<String> roles = new HashSet<>();
        roles.add("admin");

        UserModel userModel = new LegacyUser.Builder(ksession, realm, model, user.getEmail())
                .email(user.getEmail())
                .firstName(user.getName().getFirst())
                .lastName(user.getName().getLast())
                .id("123123")
                .roles(roles)
                .build();
        userModel.setEnabled(true);
        return userModel;
    }

    @Override
    public UserModel addUser(RealmModel realmModel, String s) {
        return null;
    }

    @Override
    public boolean removeUser(RealmModel realmModel, UserModel userModel) {
        return false;
    }
}
