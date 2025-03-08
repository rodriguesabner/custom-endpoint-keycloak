package com.meusistema.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class PinResetPasswordFactory implements RealmResourceProviderFactory {
    private static final Logger logger = Logger.getLogger(PinResetPasswordFactory.class);
    public static final String PROVIDER_ID = "reset-password";

    @Override
    public String getId() {
        logger.info("PinResetPasswordFactory getId called:" + PROVIDER_ID);

        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession keycloakSession) {
        logger.info("Creating new PinResetPasswordFactory");

        return new PinResetPasswordProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
        logger.info("Initializing PinResetPasswordFactory");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        logger.info("Post-Initializing PinResetPasswordFactory");
    }

    @Override
    public void close() {
        logger.info("Closing PinResetPasswordFactory");
    }
}
