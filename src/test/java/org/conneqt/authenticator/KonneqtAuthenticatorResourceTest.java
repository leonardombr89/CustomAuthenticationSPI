package org.conneqt.authenticator;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.core.Response;
import org.conneqt.util.ConfigUtil;
import org.conneqt.util.TokenValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KonneqtAuthenticatorResourceTest {

    private static final String SECRET_KEY = ConfigUtil.getSecretKey();
    private static final String REDIRECT_URL = ConfigUtil.getRedirectUrl();

    @Mock
    private KeycloakSession keycloakSession;

    @Mock
    private KeycloakContext keycloakContext;

    @Mock
    private RealmModel realmModel;

    @Mock
    private TokenValidator tokenValidator;

    @Mock
    private UserProvider userProvider;

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
    void shouldReturnRedirectWhenTokenIsValid() {
        String email = "user@example.com";

        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String validToken = generateToken(email, 10000, key);

        when(keycloakSession.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getRealm()).thenReturn(realmModel);

        when(keycloakSession.users()).thenReturn(userProvider);
        when(userProvider.getUserByEmail(realmModel, email)).thenReturn(mock(UserModel.class));

        Response response = resource.loginKonnect(validToken);
        String location = response.getHeaderString("X-Konneqt-Redirect");

        assertNotNull(location);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(location.contains(REDIRECT_URL));

    }

    @Test
    void shouldCreateUserIfNotExists() {

        String email = "user@example.com";

        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String validToken = generateToken(email, 10000, key);

        when(keycloakSession.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getRealm()).thenReturn(realmModel);

        when(keycloakSession.users()).thenReturn(userProvider);
        when(userProvider.getUserByEmail(realmModel, email)).thenReturn(null);
        when(userProvider.addUser(realmModel, email)).thenReturn(mock(UserModel.class));

        Response response = resource.loginKonnect(validToken);

        verify(userProvider).addUser(realmModel, email);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String location = response.getHeaderString("X-Konneqt-Redirect");
        assertNotNull(location);
        assertTrue(location.contains(REDIRECT_URL));
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
