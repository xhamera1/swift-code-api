package io.github.xhamera1.swiftcodeapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents a SWIFT/BIC code entry persisted in the database.
 * This JPA entity maps to the {@code swift_codes} table and stores details
 * associated with a specific SWIFT code, including bank information,
 * location, country details, and whether it represents a headquarters.
 * An index is defined on the {@code country_iso2} column to optimize country-specific queries.
 */
@Entity
@Table(name = "swift_codes", indexes = {
        @Index(name = "idx_country_iso2", columnList = "country_iso2")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwiftCodeInfo {

    /** Unique 8 or 11 character SWIFT/BIC code (Primary Key). */
    @Id
    @Column(name = "swift_code", length = 11, nullable = false, unique = true)
    private String swiftCode;

    /** Name of the bank or institution (Not Null). */
    @Column(name = "bank_name", nullable = false) // I assume that bank name cannot be null
    private String bankName;

    /** Physical address of the bank/branch (Nullable). */
    @Column(name = "address", length = 512, nullable = true)
    private String address;

    /** Town name of the bank/branch (Nullable). */
    @Column(name = "town_name", nullable = true)
    private String townName;

    /** ISO 3166-1 alpha-2 country code (e.g., "PL") (Not Null, Uppercase). */
    @Column(name = "country_iso2", length = 2, nullable = false) // I assume that country ISO2 code cannot be null
    private String countryISO2;

    /** Full country name (Not Null, Uppercase). */
    @Column(name = "country_name", nullable = false) // I assume that country name cannot be null
    private String countryName;

    /** {@code true} if this code represents a headquarters, {@code false} if a branch (Not Null). */
    @Column(name = "is_headquarter", nullable = false)
    private boolean isHeadquarter;

    // The columns : CODE TYPE and TIME ZONE are omitted per the requirement "Redundant columns... may be omitted"

}
