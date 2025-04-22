package org.conneqt;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class KonneqtAuthenticatorFactory implements RealmResourceProviderFactory {

    private static final Logger logger = Logger.getLogger(KonneqtAuthenticatorFactory.class);
    public static final String PROVIDER_ID = "authenticator";

    public KonneqtAuthenticatorFactory() {
        logger.info("=== Constructor KonneqtAuthenticatorFactory called ===");
    }

    @Override
    public String getId() {
        logger.info("KonneqtAuthenticatorFactory getId" + PROVIDER_ID);
        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession keycloakSession) {
        logger.info("Create new KonneqtAuthenticatorFactory");
        return new KonneqtAuthenticatorProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
        logger.info("Initializing KonneqtAuthenticatorFactory");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        logger.info("Post-Initializing KonneqtAuthenticatorFactory");

    }

    @Override
    public void close() {
        logger.info("Closing KonneqtAuthenticatorFactory");
    }

}
