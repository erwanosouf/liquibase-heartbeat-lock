package change;


import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import io.erwanosouf.liquibase.lockservice.HeartbeatLockService;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

class MyTest {
/*
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }
    */


    // Test method to verify the behavior of the Liquibase extension
    @Test
    void testLiquibaseWithExtension() throws LiquibaseException {
        ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

        Database database = DatabaseFactory.getInstance().openDatabase(
                "jdbc:postgresql://localhost:5432/postgres",
                "owner_1",
                "owner_1",
                Properties.class.getName(),
                resourceAccessor);

        HeartbeatLockService.MAINTENANCE_CONNECTION = (liquibase.database.jvm.JdbcConnection) DatabaseFactory.getInstance().openConnection(
                "jdbc:postgresql://localhost:5432/postgres",
                "owner_1",
                "owner_1",
                Properties.class.getName(),
                resourceAccessor);


        // Create a new instance of the Liquibase class with the custom extension
        Liquibase liquibase = new Liquibase("example.changelog.xml", resourceAccessor, database);

        // Perform the update operation
        liquibase.update(new Contexts(), new LabelExpression());

    }

}
