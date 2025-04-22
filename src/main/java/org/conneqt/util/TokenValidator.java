package org.conneqt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.jboss.logging.Logger;

import java.util.Date;

public class TokenValidator {

    private static final Logger logger = Logger.getLogger(TokenValidator.class);

    public static void validateHeader(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.error("Missing X-Konneqt-Token header");
            throw new IllegalArgumentException();
        }
    }

    public static void validateExpiration(Claims claims) {
        if (claims.getExpiration().before(new Date())) {
            throw new ExpiredJwtException(null, claims, null);
        }
    }
}

