package org.conneqt.authenticator;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;

import java.util.HashMap;
import java.util.Map;

public class KonneqtAuthenticatorResource {

    private static final Logger logger = Logger.getLogger(KonneqtAuthenticatorResource.class);

    public KonneqtAuthenticatorResource(KeycloakSession session) {
        logger.info("KonneqtAuthenticatorResource constructor called");
    }

    @GET
    @Path("konneqt")
    public Response loginKonnect(@HeaderParam("X-Konneqt-Token") String konneqtToken) {

        logger.info("Received X-Konneqt-Token: " + konneqtToken);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "teste");

        if (konneqtToken == null || konneqtToken.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing X-Konneqt-Token header")
                    .build();
        }

        return Response.ok(result).build();
    }
}
