package io.github.xhamera1.swiftcodeapi.repository;


import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Manages database operations for SwiftCodeInfo entities using Spring Data JPA.
 */
@Repository
public interface SwiftCodeInfoRepository extends JpaRepository<SwiftCodeInfo, String> {

    /**
     * Finds a SWIFT code entry by its code, ignoring case differences.
     * Returns an Optional, empty if no matching code is found.
     *
     * @param swiftCode the SWIFT code to search for
     * @return Optional containing the found SwiftCodeInfo, or empty if none match
     */
    Optional<SwiftCodeInfo> findBySwiftCodeIgnoreCase(String swiftCode);

    /**
     * Retrieves all SWIFT code entries for a specific country, identified by its ISO2 code.
     * The country code comparison ignores case.
     *
     * @param countryISO2 the 2-letter country ISO code (case is ignored)
     * @return List of matching SwiftCodeInfo entries; empty list if none are found
     */
    List<SwiftCodeInfo> findByCountryISO2IgnoreCase(String countryISO2);



    /**
     * Finds potential branch codes associated with a headquarter's SWIFT code prefix.
     * This searches for codes starting with the {@code prefix} (typically the first 8 chars of an HQ code)
     * but excludes the exact {@code swiftCodeToExclude} (the HQ code itself). Comparisons ignore case.
     *
     * @param prefix the SWIFT code prefix (e.g., first 8 characters)
     * @param swiftCodeToExclude the exact SWIFT code to exclude from the results
     * @return List of SwiftCodeInfo entities considered branches for the given prefix
     */
    List<SwiftCodeInfo> findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(String prefix, String swiftCodeToExclude);


    /**
     * Checks if a SWIFT code entry with the given code already exists in the database, ignoring case.
     *
     * @param swiftCode the SWIFT code to check
     * @return true if an entry with this code exists (case-insensitive), false otherwise
     */
    boolean existsBySwiftCodeIgnoreCase(String swiftCode);

}
