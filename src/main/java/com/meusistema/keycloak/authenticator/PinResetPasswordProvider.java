package com.meusistema.keycloak.authenticator;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class PinResetPasswordProvider implements RealmResourceProvider {
    private final KeycloakSession session;

    public PinResetPasswordProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new PinResetPasswordResource(session);
    }

    @Override
    public void close() {
    }
}
