package org.conneqt.authenticator;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.conneqt.util.ConfigUtil;
import org.conneqt.util.EmailMaskerUtil;
import org.conneqt.util.TokenValidator;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import io.jsonwebtoken.Claims;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class KonneqtAuthenticatorResource {

    private static final Logger logger = Logger.getLogger(KonneqtAuthenticatorResource.class);
    private static final String SECRET_KEY = ConfigUtil.getSecretKey();
    private static final String X_KONNEQT_TOKEN = "X-Konneqt-Token";
    private static final String PARAM_TOKEN = "?token=";

    private final KeycloakSession session;

    public KonneqtAuthenticatorResource(KeycloakSession session) {
        logger.info("KonneqtAuthenticatorResource initialized.");
        this.session = session;
    }

    @POST
    @Path("konneqt")
    @Produces("application/json")
    @Consumes("application/json")
    public Response loginKonnect(@HeaderParam(X_KONNEQT_TOKEN) String konneqtToken) {
        logger.info("Received request for login via Konneqt.");

        try {
            validateTokenHeader(konneqtToken);
            Claims claims = extractAndValidateClaims(konneqtToken);
            String email = claims.getSubject();

            logger.infof("Valid token. Extracted email: %s", EmailMaskerUtil.maskEmail(email));

            UserModel user = findOrCreateUser(email);
            logger.infof("User found or created: %s", EmailMaskerUtil.maskEmail(user.getUsername()));

            String newToken = generateUserToken(user);
            logger.info("Token successfully generated for the user.");

            String redirectUrl = buildRedirectUrl(newToken);
            logger.infof("Redirecting to: %s", redirectUrl);

            return redirectTo(redirectUrl);

        } catch (Exception e) {
            return handleTokenException(e);
        }
    }

    @OPTIONS
    @Path("konneqt")
    public Response preflight() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, X-Konneqt-Token")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .build();
    }

    private void validateTokenHeader(String token) {
        logger.debug("Validating token header.");
        TokenValidator.validateHeader(token);
    }

    private Claims extractAndValidateClaims(String token) {
        logger.debug("Extracting and validating token claims.");
        Claims claims = extractTokenClaims(token);
        TokenValidator.validateExpiration(claims);
        return claims;
    }

    private UserModel findOrCreateUser(String email) {
        RealmModel realm = session.getContext().getRealm();
        UserProvider userProvider = session.users();
        UserModel user = userProvider.getUserByEmail(realm, email);

        if (user == null) {
            user = userProvider.addUser(realm, email);
            user.setEmail(email);
            user.setEnabled(true);
            user.setUsername(email);
            logger.infof("User created automatically: %s", EmailMaskerUtil.maskEmail(email));
        }

        return user;
    }

    private String generateUserToken(UserModel user) {
        logger.debug("Generating new JWT token.");
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 3600_000;

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("username", user.getUsername())
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expMillis))
                .signWith(key)
                .compact();
    }

    private Claims extractTokenClaims(String token) {
        logger.debug("Decoding JWT token.");
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildRedirectUrl(String token) {
        return ConfigUtil.getRedirectUrl() + PARAM_TOKEN + token;
    }

    private Response redirectTo(String url) {
        return Response.ok()
                .type(MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, X-Konneqt-Token")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Expose-Headers", "X-Konneqt-Redirect")
                .header("X-Konneqt-Redirect", url)
                .build();
    }



    private Response handleTokenException(Exception e) {
        if (e instanceof ExpiredJwtException) {
            logger.warn("Token has expired.", e);
            return unauthorized("Token has expired");
        } else if (e instanceof SignatureException) {
            logger.warn("Invalid token signature.", e);
            return unauthorized("Invalid token signature");
        } else if (e instanceof MalformedJwtException || e instanceof UnsupportedJwtException || e instanceof IllegalArgumentException) {
            logger.error("Invalid token format.", e);
            return badRequest("Invalid token format");
        }

        logger.error("Unexpected error while processing the token.", e);
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
