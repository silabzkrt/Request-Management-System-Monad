package org.vaadin.example.shared.enums;

/**
 * All user role types in the system.
 * Matches the SQL CHECK constraint values on users_Sila.role.
 */
public enum UserTypes {
    ADMIN,
    YAZILIM_YONETICISI,
    MUSTERI,
    YAZILIMCI,
    URUN_SORUMLUSU;

    /** Returns the Spring Security role string, e.g. "ROLE_ADMIN". */
    public String getSpringRole() {
        return "ROLE_" + this.name();
    }

    /** Returns a numeric ID for this user type. */
    public int getUserTypeId() {
        return switch (this) {
            case ADMIN              -> 1;
            case YAZILIM_YONETICISI -> 2;
            case MUSTERI            -> 3;
            case YAZILIMCI          -> 4;
            case URUN_SORUMLUSU     -> 5;
        };
    }
}
