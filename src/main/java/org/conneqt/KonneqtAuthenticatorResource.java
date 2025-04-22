package org.conneqt;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.conneqt.util.TokenValidator;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import io.jsonwebtoken.Claims;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Map;

public class KonneqtAuthenticatorResource {

    private static final Logger logger = Logger.getLogger(KonneqtAuthenticatorResource.class);
    private static final String SECRET_KEY = "super-strong-key-that-is-very-secure!";
    private static final String X_KONNEQT_TOKEN = "X-Konneqt-Token";

    private final KeycloakSession session;

    public KonneqtAuthenticatorResource(KeycloakSession session) {
        this.session = session;
        logger.info("KonneqtAuthenticatorResource constructor called");
    }

    @POST
    @Path("konneqt")
    public Response loginKonnect(@HeaderParam(X_KONNEQT_TOKEN) String konneqtToken) {
        try {
            TokenValidator.validateHeader(konneqtToken);

            Claims claims = extractTokenClaims(konneqtToken);
            TokenValidator.validateExpiration(claims);

            String email = claims.getSubject();

            return Response.ok(Map.of("result", email)).build();

        } catch (Exception e) {
            return handleTokenException(e);
        }
    }

    public Claims extractTokenClaims(String token) {
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Response handleTokenException(Exception e) {
        if (e instanceof ExpiredJwtException) {
            logger.warn("Token has expired", e);
            return unauthorized("Token has expired");
        } else if (e instanceof SignatureException) {
            logger.warn("Invalid token signature", e);
            return unauthorized("Invalid token signature");
        } else if (e instanceof MalformedJwtException || e instanceof UnsupportedJwtException || e instanceof IllegalArgumentException) {
            logger.error("Invalid token format", e);
            return badRequest("Invalid token format");
        }

        logger.error("Unexpected error while parsing token", e);
        return serverError("Unexpected error while parsing token");
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response unauthorized(String message) {
        return Response.status(Response.Status.UNAUTHORIZED).entity(message).build();
    }

    private Response serverError(String message) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }
}

