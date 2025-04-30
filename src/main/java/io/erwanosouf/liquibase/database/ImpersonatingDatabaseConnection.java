package io.erwanosouf.liquibase.database;

import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;

public class ImpersonatingDatabaseConnection extends JdbcConnection {

	private String impersonatedUser = "owners";

	public ImpersonatingDatabaseConnection() {

	}

	@Override
	public void open(final String url, final Driver driverObject, final Properties driverProperties) throws DatabaseException {
		super.open(url, driverObject, driverProperties);
		impersonate();
	}

	@Override
	public int getPriority() {
		return 200;
	}

	protected void impersonate() throws DatabaseException {
		// Use a RawParameterizedStatement and Executor to execute the impersonation command
		System.out.println("Impersonating user: " + impersonatedUser);
		try {
			try (Statement statement = createStatement()) {
				statement.execute("SET ROLE " + impersonatedUser);
			}
			commit();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

}
