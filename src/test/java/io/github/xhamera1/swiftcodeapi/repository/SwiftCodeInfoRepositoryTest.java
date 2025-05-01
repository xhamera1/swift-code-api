package io.github.xhamera1.swiftcodeapi.repository;

import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SwiftCodeInfoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SwiftCodeInfoRepository repository;

    private SwiftCodeInfo hqPl;
    private SwiftCodeInfo branchPl8;
    private SwiftCodeInfo branchPl11;
    private SwiftCodeInfo hqAl;
    private SwiftCodeInfo branchAl;
    private SwiftCodeInfo hqDe;

    @BeforeEach
    void setUpDatabase() {
        hqPl = new SwiftCodeInfo("BANKPLPWXXX", "Bank Polski HQ", "Centrala PL", "Warszawa", "PL", "POLAND", true);
        branchPl8 = new SwiftCodeInfo("NBPAPLPW", "NBP Oddział 8", "Oddział NBP 8", "Warszawa", "PL", "POLAND", false);
        branchPl11 = new SwiftCodeInfo("BANKPLPWA01", "Bank Polski Oddział A01", "Oddział A01", "Kraków", "PL", "POLAND", false);
        hqAl = new SwiftCodeInfo("AAISALTRXXX", "United Bank Albania", "Address AL", "Tirana", "AL", "ALBANIA", true);
        branchAl = new SwiftCodeInfo("AAISALTRB02", "United Bank Albania B02", null, "Tirana B2", "AL", "ALBANIA", false);
        hqDe = new SwiftCodeInfo("DEUTDEFFXXX", "Deutsche Bank HQ", "Centrala DE", "Frankfurt", "DE", "GERMANY", true);

        entityManager.persist(hqPl);
        entityManager.persist(branchPl8);
        entityManager.persist(branchPl11);
        entityManager.persist(hqAl);
        entityManager.persist(branchAl);
        entityManager.persist(hqDe);
        entityManager.flush();
    }

    @Nested
    @DisplayName("Tests for findBySwiftCodeIgnoreCase")
    class FindBySwiftCodeIgnoreCaseTests {

        @Test
        @DisplayName("Should find SwiftCodeInfo when searching with exact matching case")
        void findBySwiftCodeIgnoreCase_whenExactCaseMatch_shouldReturnEntity() {
            Optional<SwiftCodeInfo> found = repository.findBySwiftCodeIgnoreCase("BANKPLPWXXX");
            assertThat(found).isPresent();
            assertThat(found.get().getSwiftCode()).isEqualTo("BANKPLPWXXX");
            assertThat(found.get().getBankName()).isEqualTo(hqPl.getBankName());
        }

        @Test
        @DisplayName("Should find SwiftCodeInfo when searching with lower case")
        void findBySwiftCodeIgnoreCase_whenLowerCase_shouldReturnEntity() {
            Optional<SwiftCodeInfo> found = repository.findBySwiftCodeIgnoreCase("bankplpwa01");
            assertThat(found).isPresent();
            assertThat(found.get().getSwiftCode()).isEqualTo("BANKPLPWA01");
            assertThat(found.get().getBankName()).isEqualTo(branchPl11.getBankName());
        }

        @Test
        @DisplayName("Should find SwiftCodeInfo when searching with mixed case")
        void findBySwiftCodeIgnoreCase_whenMixedCase_shouldReturnEntity() {
            Optional<SwiftCodeInfo> found = repository.findBySwiftCodeIgnoreCase("aAiSaLtRxXx");
            assertThat(found).isPresent();
            assertThat(found.get().getSwiftCode()).isEqualTo("AAISALTRXXX");
            assertThat(found.get().getBankName()).isEqualTo(hqAl.getBankName());
        }

        @Test
        @DisplayName("Should return empty Optional when SWIFT code does not exist")
        void findBySwiftCodeIgnoreCase_whenCodeDoesNotExist_shouldReturnEmpty() {
            Optional<SwiftCodeInfo> found = repository.findBySwiftCodeIgnoreCase("NONEXISTENT");
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Should return empty Optional when searching for null")
        void findBySwiftCodeIgnoreCase_whenSearchingForNull_shouldReturnEmptyOptional() {
            Optional<SwiftCodeInfo> found = repository.findBySwiftCodeIgnoreCase(null);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests for findByCountryISO2IgnoreCase")
    class FindByCountryISO2IgnoreCaseTests {

        @Test
        @DisplayName("Should find all codes for a given country ignoring case")
        void findByCountryISO2IgnoreCase_whenCountryExists_shouldReturnList() {
            List<SwiftCodeInfo> foundPl = repository.findByCountryISO2IgnoreCase("pl");
            assertThat(foundPl)
                    .hasSize(3)
                    .extracting(SwiftCodeInfo::getSwiftCode)
                    .containsExactlyInAnyOrder("BANKPLPWXXX", "NBPAPLPW", "BANKPLPWA01");

            List<SwiftCodeInfo> foundAl = repository.findByCountryISO2IgnoreCase("AL");
            assertThat(foundAl)
                    .hasSize(2)
                    .extracting(SwiftCodeInfo::getSwiftCode)
                    .containsExactlyInAnyOrder("AAISALTRXXX", "AAISALTRB02");
        }

        @Test
        @DisplayName("Should return empty list when country code does not exist")
        void findByCountryISO2IgnoreCase_whenCountryDoesNotExist_shouldReturnEmptyList() {
            List<SwiftCodeInfo> found = repository.findByCountryISO2IgnoreCase("XX");
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when searching for null country code")
        void findByCountryISO2IgnoreCase_whenSearchingForNull_shouldReturnEmptyList() {
            List<SwiftCodeInfo> found = repository.findByCountryISO2IgnoreCase(null);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests for existsBySwiftCodeIgnoreCase")
    class ExistsBySwiftCodeIgnoreCaseTests {

        @Test
        @DisplayName("Should return true when code exists (exact case)")
        void existsBySwiftCodeIgnoreCase_whenCodeExistsExactCase_shouldReturnTrue() {
            boolean exists = repository.existsBySwiftCodeIgnoreCase("BANKPLPWXXX");
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return true when code exists (different case)")
        void existsBySwiftCodeIgnoreCase_whenCodeExistsDifferentCase_shouldReturnTrue() {
            boolean exists = repository.existsBySwiftCodeIgnoreCase("nbpaplpw");
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when code does not exist")
        void existsBySwiftCodeIgnoreCase_whenCodeDoesNotExist_shouldReturnFalse() {
            boolean exists = repository.existsBySwiftCodeIgnoreCase("NONEXISTENT");
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false when searching for null")
        void existsBySwiftCodeIgnoreCase_whenSearchingForNull_shouldReturnFalse() {
            boolean exists = repository.existsBySwiftCodeIgnoreCase(null);
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests for findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase (Find Branches)")
    class FindBranchesTests {

        @Test
        @DisplayName("Should find branches for a given HQ prefix ignoring case")
        void findBranches_whenBranchesExist_shouldReturnList() {
            String prefix = "bankplpw";
            String excludeCode = "BANKPLPWXXX";

            List<SwiftCodeInfo> branches = repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(prefix, excludeCode);

            assertThat(branches)
                    .hasSize(1)
                    .extracting(SwiftCodeInfo::getSwiftCode)
                    .containsExactly("BANKPLPWA01");
        }

        @Test
        @DisplayName("Should find branches for HQ prefix ignoring case of excludeCode")
        void findBranches_whenExcludeCodeIsDifferentCase_shouldStillExclude() {
            String prefix = "BANKPLPW";
            String excludeCodeLower = "bankplpwxxx";

            List<SwiftCodeInfo> branches = repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(prefix, excludeCodeLower);

            assertThat(branches)
                    .hasSize(1)
                    .extracting(SwiftCodeInfo::getSwiftCode)
                    .containsExactly("BANKPLPWA01");
        }


        @Test
        @DisplayName("Should return empty list when no branches match prefix")
        void findBranches_whenNoBranchesMatchPrefix_shouldReturnEmptyList() {
            String prefix = "DEUTDEFF";
            String excludeCode = "DEUTDEFFXXX";

            List<SwiftCodeInfo> branches = repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(prefix, excludeCode);

            assertThat(branches).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when prefix does not match any code")
        void findBranches_whenPrefixNotFound_shouldReturnEmptyList() {
            String prefix = "NONEXIST";
            String excludeCode = "NONEXISTXXX";

            List<SwiftCodeInfo> branches = repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(prefix, excludeCode);

            assertThat(branches).isEmpty();
        }


        @Test
        @DisplayName("Should return empty list when searching with null prefix or excludeCode")
        void findBranches_whenNullInput_shouldReturnEmptyList() {
            List<SwiftCodeInfo> resultWithNullPrefix = repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(null, "BANKPLPWXXX");
            assertThat(resultWithNullPrefix).as("Result with null prefix").isEmpty();

            List<SwiftCodeInfo> resultWithNullExclude = repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase("BANKPLPW", null);
            assertThat(resultWithNullExclude).as("Result with null exclude code").isEmpty();

            List<SwiftCodeInfo> resultWithBothNull = repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(null, null);
            assertThat(resultWithBothNull).as("Result with both null").isEmpty();
        }
    }
}