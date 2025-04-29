package io.github.xhamera1.swiftcodeapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "swift_codes", indexes = {
        @Index(name = "idx_country_iso2", columnList = "country_iso2")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwiftCodeInfo {

    @Id
    @Column(name = "swift_code", length = 11, nullable = false, unique = true)
    private String swiftCode;

    @Column(name = "bank_name", nullable = false) // I assume that bank name cannot be null
    private String bankName;

    @Column(name = "address", length = 512, nullable = true)
    private String address;

    @Column(name = "town_name", nullable = true)
    private String townName;

    @Column(name = "country_iso2", length = 2, nullable = false) // I assume that country ISO2 code cannot be null
    private String countryISO2;

    @Column(name = "country_name", nullable = false) // I assume that country name cannot be null
    private String countryName;

    @Column(name = "is_headquarter", nullable = false)
    private boolean isHeadquarter;

    // The columns : CODE TYPE and TIME ZONE are omitted per the requirement "Redundant columns... may be omitted"

}
