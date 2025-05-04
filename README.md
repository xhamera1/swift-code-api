# SWIFT Code API Service

* **Author:** Patryk Chamera
* **Email:** chamerapatryk@gmail.com
* **Phone:** +48 536 330 101
* **GitHub:** [github.com/xhamera1](https://github.com/xhamera1)
* **LinkedIn:** [linkedin.com/in/patryk-chamera-309835289](https://www.linkedin.com/in/patryk-chamera-309835289/)


## Project Overview

This project implements a RESTful API service for managing and querying SWIFT/BIC (Bank Identifier Code) data. The application parses SWIFT code information from a CSV file during initialization, stores it in a relational database (MySQL), and exposes endpoints to retrieve, add, and delete SWIFT code entries.

Key functionalities include:
* Parsing and validating SWIFT code data based on specific rules (e.g., headquarters vs. branch identification, country code consistency).
* Storing SWIFT data efficiently, with support for fast querying by SWIFT code or country ISO2 code.
* Exposing a REST API with endpoints for retrieving individual SWIFT code details (including associated branches for headquarters), retrieving all codes for a specific country, adding new SWIFT codes, and deleting existing codes.
* Ensuring data integrity through validation constraints and consistency checks.
* Robust error handling via a global exception handler.
* Comprehensive unit and integration testing coverage.
* Full containerization using Docker and Docker Compose for both the application and the database.

This application was developed as a home exercise for the Remitly Poland internship program (May 2025).

## Features

* **Data Initialization:** Loads SWIFT code data from a CSV file (`swift_code_data.csv`) into the database on application startup (only if the database is empty).
* **REST API Endpoints:** Provides the following endpoints under the `/v1/swift-codes` base path:
    * `GET /{swift-code}`: Retrieve details for a specific SWIFT code. Returns associated branches if the code represents a headquarters.
    * `GET /country/{countryISO2code}`: Retrieve all SWIFT codes (headquarters and branches) for a given country ISO2 code.
    * `POST /`: Add a new SWIFT code entry.
    * `DELETE /{swift-code}`: Delete an existing SWIFT code entry.
* **Data Validation:**
    * Validates the format of SWIFT codes (8 or 11 characters, specific structure).
    * Validates the format and length of country ISO2 codes.
    * Ensures consistency between the country code embedded in the SWIFT code and the provided country ISO2 code during creation.
    * Ensures consistency between the `isHeadquarter` flag and the SWIFT code format (ending in "XXX").
    * Validates required fields for creating new entries.
* **Headquarters/Branch Logic:** Correctly identifies headquarters ("XXX" suffix) and branches, and associates branches with their corresponding headquarters based on the first 8 characters.
* **Data Formatting:** Stores and returns country codes (ISO2) and country names in uppercase, as required.
* **Persistence:** Uses Spring Data JPA with Hibernate to persist data in a MySQL database. Includes an index on the `country_iso2` column for optimized querying.
* **Containerization:** Fully containerized using Docker and Docker Compose, allowing for easy setup and deployment.
* **Testing:** Includes a comprehensive suite of unit tests (Mockito) and integration tests (Spring Boot Test, DataJpaTest, H2 database) covering service logic, controller endpoints, repository interactions, and data initialization.
* **Error Handling:** Implements a global exception handler (`@ControllerAdvice`) to provide consistent and informative error responses (e.g., 404 Not Found, 400 Bad Request, 409 Conflict, 500 Internal Server Error).

## Technologies Used

* **Backend:** Java 21, Spring Boot 3.4.5 (Web, Data JPA, Validation)
* **Database:** MySQL 8.4
* **ORM:** Hibernate (via Spring Data JPA)
* **Build Tool:** Apache Maven
* **Containerization:** Docker, Docker Compose
* **Libraries:**
    * Lombok (Code generation reduction)
    * Apache Commons CSV (CSV Parsing)
    * Slf4j (Logging facade)
* **Testing:**
    * JUnit 5
    * Mockito
    * Spring Boot Test
    * H2 Database (In-memory DB for integration tests)

## Prerequisites

Before you begin, ensure you have the following installed on your system:

* **Git:** For cloning the repository.
* **JDK 21:** Java Development Kit, version 21 or a later compatible version.
* **Apache Maven:** Version 3.6+ (or use the included Maven Wrapper `mvnw`).
* **Docker Desktop:** Or Docker Engine & Docker Compose v2 CLI. Required for running the application and database easily.

## Setup

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/xhamera1/swift-code-api.git
    cd swift-code-api
    ```

2.  **Configure Environment Variables:**
    * This project uses a `.env` file to manage sensitive configuration like database passwords for Docker Compose.
    * Copy the example file:
        ```bash
        cp .env.example .env
        ```
    * **IMPORTANT:** Open the newly created `.env` file in a text editor.
    * **Replace the placeholder values**, especially for `MYSQL_ROOT_PASSWORD` and `MYSQL_PASSWORD`, with your own **strong, secure passwords**.
        ```dotenv
        # Example .env content (DO NOT USE THESE VALUES IN PRODUCTION)
        MYSQL_ROOT_PASSWORD=your_secure_root_password
        MYSQL_DATABASE=swift_api_db # You can keep this default
        MYSQL_USER=swiftapiuser      # You can keep this default
        MYSQL_PASSWORD=your_secure_user_password
        ```
    * **DO NOT commit the `.env` file to Git.** It is already included in the `.gitignore` file.

## Running the Application (Docker Compose - Recommended)

This is the recommended method for running the application as it sets up both the application and the required MySQL database within containers, matching the deployment requirement.

1.  **Ensure Docker is Running:** Start Docker Desktop or ensure the Docker daemon is active.
2.  **Start Services:** From the root directory of the project (where `docker-compose.yml` is located), run:
    ```bash
    docker-compose up --build -d
    ```
    * `--build`: Forces Docker Compose to build the application image based on the `Dockerfile` (important for first run or after code changes).
    * `-d`: Runs the containers in detached mode (in the background).
3.  **Wait for Startup:** Docker Compose will pull the MySQL image (if not already present), build the application image, and start both containers. The application container (`spring_app_swift`) depends on the database container (`mysql_db_swift`) being healthy, so it might take a moment to start fully. The initial data loading from the CSV file will also occur during the application startup.
4.  **Check Container Status (Optional):**
    ```bash
    docker-compose ps
    ```
    You should see both `mysql_db_swift` and `spring_app_swift` with a status indicating they are running/up.

5.  **Port Mappings:** The `docker-compose.yml` file defines the following port mappings (`HOST:CONTAINER`):
    * **Application (`app` service):** `8080:8080` - The Spring Boot application running inside the container on port 8080 is mapped to port 8080 on your host machine (`localhost`).
    * **Database (`db` service):** `3307:3306` - The MySQL database running inside the container on the standard port 3306 is mapped to port 3307 on your host machine (`localhost`). This allows you to connect to the database directly using a database client if needed (e.g., using hostname `localhost`, port `3307`, user `swiftapiuser`, and the password set in your `.env` file).

6.  **Access the API:** Once the application starts successfully, the API endpoints will be accessible on your host machine at:
    `http://localhost:8080`
    (e.g., `http://localhost:8080/v1/swift-codes/country/PL`)

7.  **View Logs (Optional):**
    ```bash
    docker-compose logs -f app  # View application logs
    docker-compose logs -f db   # View database logs
    ```
    *(Press `Ctrl + C` to stop following logs)*

8.  **Stop Services:** To stop and remove the containers and network defined in the `docker-compose.yml` file, run:
    ```bash
    docker-compose down
    ```
    *(Use `docker-compose down -v` if you also want to remove the named volume `mysql_swift_data`, effectively deleting the database data)*

## Running Tests

The project includes a comprehensive test suite covering different layers of the application.

1.  **Execute Tests:** From the root directory of the project, run the following Maven command:
    ```bash
    mvn test
    ```
    *(Alternatively, use the Maven wrapper: `./mvnw test` on Linux/macOS or `mvnw.cmd test` on Windows)*
2.  **Test Environment:** The tests run using an in-memory H2 database configured in `src/test/resources/application.properties`. They **do not** require Docker or the external MySQL database to be running.

## API Endpoints

The API provides the following endpoints under the base path `/v1/swift-codes`.

---

**1. Get SWIFT Code Details**

* **Path:** `GET /v1/swift-codes/{swift-code}`
* **Description:** Retrieves details for a single SWIFT code (either headquarters or branch). If the code represents a headquarters (ends in "XXX"), the response includes a list of associated branch codes.
* **Path Parameter:**
    * `swift-code` (string, required): The 8 or 11-character SWIFT/BIC code (case-insensitive).
* **Success Response (200 OK):**
    * *Example for Headquarters (`GET /v1/swift-codes/CITIUYMMXXX`):*
        ```json
        {
          "address": "CERRITO 455 CIUDAD VIEJA MONTEVIDEO, MONTEVIDEO, 11000",
          "bankName": "CITIBANK N.A. URUGUAY",
          "countryISO2": "UY",
          "countryName": "URUGUAY",
          "isHeadquarter": true,
          "swiftCode": "CITIUYMMXXX",
          "branches": [
            {
              "address": "COLONIA, COLONIA",
              "bankName": "CITIBANK N.A. URUGUAY",
              "countryISO2": "UY",
              "isHeadquarter": false,
              "swiftCode": "CITIUYMMCOL"
            },
            {
              "address": "PUNTA DEL ESTE, MALDONADO, 20000",
              "bankName": "CITIBANK N.A. URUGUAY",
              "countryISO2": "UY",
              "isHeadquarter": false,
              "swiftCode": "CITIUYMMPDE"
            }
          ]
        }
        ```
    * *Example for Branch (`GET /v1/swift-codes/CITIUYMMCOL`):*
        ```json
        {
          "address": "COLONIA, COLONIA",
          "bankName": "CITIBANK N.A. URUGUAY",
          "countryISO2": "UY",
          "countryName": "URUGUAY",
          "isHeadquarter": false,
          "swiftCode": "CITIUYMMCOL"
        }
        ```
* **Error Responses:**
    * `404 Not Found`: If the specified `swift-code` does not exist.
        ```json
        { "message": "SWIFT code '...' not found." }
        ```

---

---

**2. Get SWIFT Codes by Country**

* **Path:** `GET /v1/swift-codes/country/{countryISO2code}`
* **Description:** Retrieves a list of all SWIFT codes (both headquarters and branches) associated with a specific country.
* **Path Parameter:**
    * `countryISO2code` (string, required): The 2-letter ISO 3166-1 alpha-2 country code (case-insensitive).
* **Success Response (200 OK):**
    * *Example for a Country with Codes (`GET /v1/swift-codes/country/PL`):*
        ```json
        {
          "countryISO2": "PL",
          "countryName": "POLAND",
          "swiftCodes": [
            {
              "address": "UL. LEGNICKA 48 C-D  WROCLAW, DOLNOSLASKIE, 54-202",
              "bankName": "CREDIT AGRICOLE BANK POLSKA S.A.",
              "countryISO2": "PL",
              "isHeadquarter": true,
              "swiftCode": "AGRIPLPRXXX"
            },
            {
              "address": "STRZEGOMSKA 42C  WROCLAW, DOLNOSLASKIE, 53-611",
              "bankName": "SANTANDER CONSUMER BANK SPOLKA AKCYJNA",
              "countryISO2": "PL",
              "isHeadquarter": true,
              "swiftCode": "AIPOPLP1XXX"
            },
            {
              "address": "WARSZAWA, MAZOWIECKIE",
              "bankName": "ALIOR BANK SPOLKA AKCYJNA",
              "countryISO2": "PL",
              "isHeadquarter": false,
              "swiftCode": "ALBPPLP1BMW"
            }
            // ... potentially more codes for the country
          ]
        }
        ```
    * *Example for a Country with No Codes (`GET /v1/swift-codes/country/XX`):*
        ```json
        {
          "countryISO2": "XX",
          "countryName": "",
          "swiftCodes": []
        }
        ```
       * **Note on Behavior:** As a design decision, requesting codes for a country code that does not exist in the database (or has no associated SWIFT codes) returns a `200 OK` status with an empty `swiftCodes` list, rather than a `404 Not Found`. This reflects that the operation to find codes matching the given country criteria was successful, even if the result set is empty. Returning a `404 Not Found` specifically for an *invalid* or *unknown* country ISO2 code would require validating the input against a definitive list of all official country codes. Since such validation or list was not specified in the project requirements or provided data, the current approach consistently returns the set of codes found in the existing data for the requested country code.

---

**3. Add New SWIFT Code**

* **Path:** `POST /v1/swift-codes`
* **Description:** Adds a new SWIFT code entry to the database. Performs validation on the input data.
* **Request Body:** (Content-Type: `application/json`)
    * *Example:*
        ```json
        {
          "address": "ul. Testowa 4, Warszawa, Mazowieckie, 00-001",
          "bankName": "Testowy 4 Bank Polski S.A.",
          "countryISO2": "BG",
          "countryName": "CountryWithBGcode",
          "isHeadquarter": false,
          "swiftCode": "AAAJBG21"
        }
        ```
    * *Note on `address` field:* While the source CSV data allows the `address` (and `townName`) field to be null or empty, this API endpoint requires the `address` field to be provided and non-blank when creating a new entry, as enforced by validation rules (`@NotBlank`). This design choice ensures higher data quality for entries created interactively via the API, compared to the initial bulk load which might tolerate incomplete source data.
* **Success Response (201 Created):**
    * *Example:*
        ```json
        { "message": "SWIFT code 'AAAJBG21' added successfully." }
        ```
* **Error Responses:**
    * **`400 Bad Request` (Validation Error):** Returned if input data fails validation constraints (e.g., missing required fields, invalid format, incorrect size).
        * *Example (Invalid SWIFT Format):*
            ```json
            { "message": "Validation failed: 'swiftCode': Invalid SWIFT/BIC format. Should be an 8 to 11-character identifier (e.g., BANKPLPWXXX, BANKDEFF)" }
            ```
        * *Example (Missing Required Field):*
            ```json
            { "message": "Validation failed: 'bankName': Bank name cannot be blank" }
            ```
    * **`400 Bad Request` (Data Consistency Error):** Returned if the provided data has internal inconsistencies checked by the service layer.
        * *Example (Inconsistent `isHeadquarter` Flag):*
            ```json
            { "message": "Provided 'isHeadquarter' flag (true) is inconsistent with the SWIFT code format (BBBJBG55)." }
            ```
        * *Example (Country Code Mismatch):*
            ```json
            { "message": "Data consistency error: The country code from SWIFT ('BG' in 'BBBJBG55') does not match the provided Country ISO2 ('XX')." }
            ```
    * **`409 Conflict` (Resource Already Exists):** Returned if a SWIFT code with the same value already exists in the database.
        * *Example:*
            ```json
            { "message": "SWIFT code 'AAAJBG21' already exists." }
            ```
    * **`415 Unsupported Media Type`:** Returned if the `Content-Type` header of the request is not `application/json`.

---

**4. Delete SWIFT Code**

* **Path:** `DELETE /v1/swift-codes/{swift-code}`
* **Description:** Deletes the SWIFT code entry matching the provided code.
* **Path Parameter:**
    * `swift-code` (string, required): The 8 or 11-character SWIFT/BIC code to delete (case-insensitive).
* **Success Response (200 OK):**
    * *Example (for `DELETE /v1/swift-codes/AAISALTRXXX`):*
        ```json
        { "message": "SWIFT code 'AAISALTRXXX' deleted successfully." }
        ```
* **Error Responses:**
    * **`404 Not Found`:** Returned if the specified `swift-code` does not exist in the database.
        * *Example (for `DELETE /v1/swift-codes/XXXXXXXXXXX`):*
            ```json
            { "message": "SWIFT code 'XXXXXXXXXXX' not found, cannot delete." }
            ```

---

**5. General Error Handling: Unknown Paths**

* **Scenario:** Making a request to a path not defined by the API (e.g., `GET /v1/swift-codes/some/other/path` or `GET /v1/invalid-path`).
* **Response (`404 Not Found`):** The API will return a `404 Not Found` status code indicating the requested path was not found on this server. The response body provides details.
* *Example (for `GET /v1/swift-codes/some/other/path`):*
    ```json
    {
        "message": "The requested resource path '/v1/swift-codes/some/other/path' could not be found on this server."
    }
    ```

---

---
## Project Structure

The project follows a standard Maven project structure. Key files and directories include:

```text
.
├── .dockerignore             # Specifies files ignored by Docker build context
├── .env.example              # Example environment variables file for Docker Compose (template for .env)
├── .gitignore                # Specifies files ignored by Git version control
├── docker-compose.yml        # Docker Compose configuration to run the application and MySQL database
├── Dockerfile                # Instructions for building the application's Docker image
├── LICENSE                   # Contains the project's software license information (e.g., MIT)
├── mvnw / mvnw.cmd           # Maven wrapper scripts for consistent builds across environments
├── pom.xml                   # Maven project configuration (dependencies, build plugins, project info)
├── README.md                 # This file - project documentation
└── src                       # Source code and resources root directory
    ├── main                  # Main application code and resources
    │   ├── java/.../swiftcodeapi/ # Root package for application Java source code
    │   │   ├── ...             # (Packages: controller, dto, exceptions, model, repository, service)
    │   └── resources         # Non-Java resources (properties, initial data)
    │       ├── ...             # (Files: application.properties, data/swift_code_data.csv)
    └── test                  # Test code and resources root directory
        ├── java/.../swiftcodeapi/ # Root package for Java test source code
        │   ├── ...             # (Test classes mirroring main structure)
        └── resources         # Test-specific resources
            ├── ...             # (Files: application.properties for H2, data/swift_code_data.csv for tests)

```
**Explanation:**

* ```.dockerignore```: Lists files/directories excluded when building the Docker image, keeping it lean.
* ```.env.example```: Template file showing required environment variables for `docker-compose`. **Crucially, you must copy this to `.env` and fill it with your actual secrets (like database passwords).** The `.env` file itself is ignored by Git.
* ```.gitignore```: Specifies files/directories intentionally untracked by Git (e.g., build outputs, IDE files, the `.env` file).
* ```docker-compose.yml```: Defines the services (application, database), networks, and volumes for running the project using Docker Compose.
* ```Dockerfile```: Contains step-by-step instructions to build the container image for the Java application.
* ```LICENSE```: The file detailing the software license under which the project is distributed (e.g., MIT, Apache 2.0).
* ```mvnw``` / ```mvnw.cmd```: Maven wrapper scripts, allowing the project to be built using a specific Maven version without needing a system-wide Maven installation.
* ```pom.xml```: The core Maven Project Object Model file, defining dependencies, build steps, plugins, and project metadata.
* ```README.md```: (This documentation file) Provides essential information about the project.
* ```src/main/java```: Holds the core Java source code, structured by feature or layer (e.g., `controller` for API endpoints, `service` for business logic, `repository` for data access, `model` for data entities, `dto` for data transfer objects, `exceptions` for error handling).
* ```src/main/resources```: Contains non-Java resources like configuration files (`application.properties` - although Docker Compose uses environment variables for DB connection) and data files (`data/swift_code_data.csv` for the initial load).
* ```src/test/java```: Holds the unit and integration test code, typically mirroring the package structure of `src/main/java`.
* ```src/test/resources```: Contains resources needed only for tests, such as test-specific configuration (`application.properties` defining the H2 database) and test data files.

*(Note: Directories like `.idea`, `.mvn` internals, and `target` are standard development/build folders and are typically omitted from high-level structure descriptions in README files, as they are either environment-specific or generated artifacts).*


## Data Handling Details

* **Initial Data Source:** The application expects a CSV file named `swift_code_data.csv` located in `src/main/resources/data`. This file is parsed by the `DataInitializer` service upon application startup if the `swift_codes` database table is empty.
* **CSV Parsing Rules:**
    * Uses Apache Commons CSV.
    * Expects specific headers (defined in `DataInitializer`).
    * Skips the header record.
    * Trims whitespace from values.
    * Ignores empty lines.
    * Validates critical fields (SWIFT Code, Country ISO2, Bank Name, Country Name) - records with missing critical data are skipped.
    * Validates SWIFT code length (must be 8 or 11 characters) - invalid records are skipped.
    * Validates consistency between the 5th and 6th characters of the SWIFT code and the `COUNTRY ISO2 CODE` column (case-insensitive) - inconsistent records are skipped.
    * Columns `CODE TYPE` and `TIME ZONE` from the CSV are ignored.
    * `ADDRESS` and `TOWN NAME` columns can be empty/null in the CSV; this is handled during mapping to the `SwiftCodeInfo` entity.
* **Database Persistence:**
    * Data is stored in the `swift_codes` table (as defined by the `SwiftCodeInfo` JPA entity).
    * The `swift_code` column is the primary key (unique, max length 11).
    * `country_iso2` and `country_name` are stored in uppercase.
    * The `is_headquarter` boolean column is derived from the SWIFT code format (ends with "XXX") during CSV parsing and POST requests.
    * An index (`idx_country_iso2`) is created on the `country_iso2` column to optimize queries by country.
* **API Data Handling:**
    * The `POST /v1/swift-codes` endpoint performs rigorous validation on incoming data using annotations in `SwiftCodeRequest` and additional checks in `SwiftCodeApiService` (existence, country consistency, headquarter flag consistency).
    * All data retrieved via the API reflects the formatting rules (e.g., uppercase country codes/names).
    * The address logic in responses prioritizes the `address` field from the database; if `address` is null/empty, it falls back to `townName`. If both are null/empty, an empty string is returned for the address field in the DTO.
