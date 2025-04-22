package org.conneqt.util;

public class EmailMaskerUtil {

    /**
     * Masks the email, keeping only the first 4 characters of the local part (before @)
     * and the domain part (after @), while masking the rest with asterisks.
     *
     * @param email the email to be masked
     * @return the masked email
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        String maskedLocalPart = localPart.length() > 4 ? localPart.substring(0, 4) + "****" : localPart + "****";

        return maskedLocalPart + "@" + domainPart;
    }
}
