package org.conneqt.authenticator;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.core.Response;
import org.conneqt.KonneqtAuthenticatorResource;
import org.conneqt.util.TokenValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.KeycloakSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class KonneqtAuthenticatorResourceTest {

    private static final String SECRET_KEY = "super-strong-key-that-is-very-secure!";

    @Mock
    private KeycloakSession keycloakSession;

    @Mock
    private TokenValidator tokenValidator;

    @InjectMocks
    private KonneqtAuthenticatorResource resource;

    @Test
    void shouldReturnBadRequestWhenTokenIsMissing() {
        Response response = resource.loginKonnect(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Invalid token format", response.getEntity());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsExpired() {
        Key wrongKey = Keys.hmacShaKeyFor("super-strong-key-that-is-very-secure!".getBytes(StandardCharsets.UTF_8));

        String expiredToken = generateToken("user@example.com", -10_000L, wrongKey);

        Response response = resource.loginKonnect(expiredToken);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Token has expired", response.getEntity());

    }


    @Test
    void shouldReturnUnauthorizedWhenTokenSignatureInvalid() {
        Key wrongKey = Keys.hmacShaKeyFor("super-strong-key-that-is-very-different!".getBytes(StandardCharsets.UTF_8));
        String invalidToken = generateToken("user@example.com", 10000, wrongKey);

        Response response = resource.loginKonnect(invalidToken);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Invalid token signature", response.getEntity());
    }

    @Test
    void shouldReturnBadRequestWhenTokenIsMalformed() {
        String malformedToken = "invalid.token.without.parts";

        Response response = resource.loginKonnect(malformedToken);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Invalid token format", response.getEntity());
    }

    @Test
    void shouldReturnOkWhenTokenIsValid() {
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String token = generateToken("valid@example.com", 10000, key);

        Response response = resource.loginKonnect(token);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertEquals("valid@example.com", body.get("result"));
    }

    private String generateToken(String subject, long expirationMillis, Key key) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }
}
