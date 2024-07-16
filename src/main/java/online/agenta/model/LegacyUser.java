package online.agenta.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.ToString;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

@ToString
public class LegacyUser extends AbstractUserAdapterFederatedStorage {

    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Date birthDate;
    private final String id;

    private LegacyUser(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, String username,
                       String email, String firstName, String lastName, Date birthDate, String id) {
        super(session, realm, storageProviderModel);
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String s) {

    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        System.out.println("CREEEEEEEEEEEEEE");
        return null;
    }

    public Date getBirthDate() {
        return birthDate;
    }


    @Override
    public String getFirstAttribute(String name) {
        System.out.println("HUI ID" + name);
        return getId();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        attributes.add(UserModel.USERNAME, getUsername());
        attributes.add(UserModel.EMAIL, getEmail());
        attributes.add(UserModel.FIRST_NAME, getFirstName());
        attributes.add(UserModel.LAST_NAME, getLastName());
        attributes.add(UserModel.IDP_USER_ID, getId());
        attributes.add("birthDate", getBirthDate().toString());
        return attributes;
    }

    public static class Builder {
        private final KeycloakSession session;
        private final RealmModel realm;
        private final ComponentModel storageProviderModel;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Date birthDate;
        private String id;

        public Builder(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, String username) {
            this.session = session;
            this.realm = realm;
            this.storageProviderModel = storageProviderModel;
            this.username = username;
        }

        public LegacyUser.Builder id(String id) {
            this.id = id;
            return this;
        }

        public LegacyUser.Builder email(String email) {
            this.email = email;
            return this;
        }

        public LegacyUser.Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public LegacyUser.Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public LegacyUser.Builder birthDate(Date birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public LegacyUser build() {
            return new LegacyUser(session, realm, storageProviderModel, username, email, firstName, lastName,
                    birthDate, id);

        }
    }
}
