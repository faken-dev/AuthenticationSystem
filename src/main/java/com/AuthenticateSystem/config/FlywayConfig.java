package com.AuthenticateSystem.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    /**
     * Creates a CommandLineRunner bean that executes Flyway database migrations.
     * <p>
     * This runner will execute after the application context is fully initialized,
     * ensuring all necessary beans are available before migration begins.
     * </p>
     *
     * @param dataSource the DataSource to be used for database migrations
     * @return CommandLineRunner that performs the migration
     */
    @Bean
    public CommandLineRunner runFlyway(DataSource dataSource) {
        return args -> {
            logMigrationStart();

            Flyway flyway = configureFlyway(dataSource);

            logMigrationInfo(flyway);

            executeMigration(flyway);
        };
    }

    /**
     * Configures Flyway instance with custom settings.
     *
     * @param dataSource the DataSource for database connection
     * @return configured Flyway instance
     */
    private Flyway configureFlyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)  // Enable baseline for existing databases
                .baselineVersion("0")      // Set initial baseline version
                .load();
    }

    /**
     * Logs migration information including total, pending, and applied migrations.
     *
     * @param flyway the Flyway instance to retrieve information from
     */
    private void logMigrationInfo(Flyway flyway) {
        var info = flyway.info();
        System.out.println("Migration Status:");
        System.out.println("  - Total migrations found: " + info.all().length);
        System.out.println("  - Pending migrations: " + info.pending().length);
        System.out.println("  - Applied migrations: " + info.applied().length);
    }

    /**
     * Executes the database migration and logs the results.
     *
     * @param flyway the Flyway instance to execute migration
     */
    private void executeMigration(Flyway flyway) {
        var result = flyway.migrate();
        System.out.println("\nMigration Result:");
        System.out.println("  - Successfully applied " + result.migrationsExecuted + " migration(s)");
        logMigrationEnd();
    }

    /**
     * Logs the start of the migration process.
     */
    private void logMigrationStart() {
        System.out.println("=================================");
        System.out.println("FLYWAY MIGRATION STARTED");
        System.out.println("=================================");
    }

    /**
     * Logs the completion of the migration process.
     */
    private void logMigrationEnd() {
        System.out.println("=================================");
        System.out.println("FLYWAY MIGRATION COMPLETED");
        System.out.println("=================================");
    }
}