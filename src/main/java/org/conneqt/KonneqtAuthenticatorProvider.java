package org.conneqt;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class KonneqtAuthenticatorProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public KonneqtAuthenticatorProvider(KeycloakSession session) {
        this.session = session;
    }


    @Override
    public Object getResource() {
        return new KonneqtAuthenticatorResource(session);
    }

    @Override
    public void close() {

    }
}
