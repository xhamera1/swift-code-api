package io.github.xhamera1.swiftcodeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * The main entry point for the SWIFT Code REST API application.
 */
@SpringBootApplication
public class SwiftCodeApiApplication {

	/**
	 * The main method which serves as the entry point for the Java application.
	 * It delegates to Spring Boot's {@link SpringApplication#run} method to launch the application.
	 *
	 * @param args Command line arguments passed to the application (not typically used directly in web applications).
	 */
	public static void main(String[] args) {
		SpringApplication.run(SwiftCodeApiApplication.class, args);
	}

}
