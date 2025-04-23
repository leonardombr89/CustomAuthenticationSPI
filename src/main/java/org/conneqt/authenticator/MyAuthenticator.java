package org.conneqt.authenticator;

import jakarta.ws.rs.core.HttpHeaders;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import java.util.List;
import java.util.logging.Logger;

public class MyAuthenticator implements org.keycloak.authentication.Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        HttpHeaders headers = context.getHttpRequest().getHttpHeaders();
        List<String> tokenHeader = headers.getRequestHeader("X-Konneqt-Token");

        if (tokenHeader == null || tokenHeader.isEmpty()) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        String email = tokenHeader.get(0).trim(); // O "token" é o email

        RealmModel realm = context.getRealm();
        UserProvider userProvider = context.getSession().users();
        UserModel user = userProvider.getUserByEmail(realm, email);

        if (user == null) {
            user = userProvider.addUser(realm, email);
            user.setEmail(email);
            user.setEnabled(true);
            user.setUsername(email);
        }

        context.setUser(user);
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
