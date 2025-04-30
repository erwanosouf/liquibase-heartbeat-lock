package io.erwanosouf.liquibase.lockservice;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import liquibase.Scope;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LockException;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.StandardLockService;

import static java.util.concurrent.TimeUnit.*;

public class HeartbeatLockService extends StandardLockService {

	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private Duration heartbeatInterval = Duration.ofMillis(5000L);
	private Duration timeout = Duration.ofMinutes(1L);
	private ScheduledFuture<?> heartbeat;

	public static JdbcConnection MAINTENANCE_CONNECTION = null;


	public void heartbeat() {
		// Use a RawParameterizedStatement and Executor to execute the heartbeat command
		// Or see the generator for the Lock statement
		//Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
		// executor.execute();
		System.out.println("Heartbeat " + Instant.now());
		try {
			try (Statement statement = MAINTENANCE_CONNECTION.createStatement()) {
				statement.execute("UPDATE DATABASECHANGELOGLOCK SET LOCKGRANTED = now() WHERE LOCKED = true AND id = 1");
			}
			MAINTENANCE_CONNECTION.commit();
			System.out.println("Done Heartbeat " + Instant.now());
		} catch (Exception e) {
			System.out.println(e);
		}
	}


	@Override
	public boolean acquireLock() throws LockException {
		releaseLockIfOlderThanTimeout();
		boolean locked = super.acquireLock();
		if (locked && heartbeat == null) {
			startHeartbeat();
		}
		return locked;
	}

	private void startHeartbeat() {
		System.out.println("Starting heartbeat");
		long delayMillis = heartbeatInterval.toMillis();
		heartbeat = scheduler.scheduleWithFixedDelay(this::heartbeat, delayMillis, delayMillis, MILLISECONDS);
	}

	private void releaseLockIfOlderThanTimeout() throws LockException {
		DatabaseChangeLogLock[] locks = listLocks();
		if (locks.length > 0) {
			DatabaseChangeLogLock lock = locks[0];
			Duration lockDuration = Duration.between(lock.getLockGranted().toInstant(), Instant.now());
			if (lockDuration.compareTo(timeout) > 0) {
				System.out.println("Lock is older than timeout, releasing it");
				releaseLock();
			}
		}
	}

	@Override
	public void releaseLock() throws LockException {
		try {
			super.releaseLock();
		} finally {
			stopHeartbeat();
		}
	}

	private void stopHeartbeat() {
		System.out.println("Stopping heartbeat");
		if (heartbeat != null) {
			heartbeat.cancel(false);
		}
	}

	@Override
	public int getPriority() {
		return 200;
	}

}
