package edu.epam.fop.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A very small, self-contained JDBC connection pool.
 * <p>
 * It pulls database and pool settings from the same <code>db.properties</code> file:
 * <ul>
 *     <li>jdbc.driverClassName</li>
 *     <li>jdbc.url</li>
 *     <li>jdbc.username</li>
 *     <li>jdbc.password</li>
 *     <li>pool.initialSize (optional, default 5)</li>
 *     <li>pool.maxSize (optional, default 20)</li>
 *     <li>pool.borrowTimeoutMillis (optional, default 30000)</li>
 * </ul>
 * <p>
 * The implementation is blocking: if the pool is exhausted and maxSize has been reached,
 * {@link #getConnection()} waits until another thread returns a connection or the timeout expires.
 */
public final class ConnectionPool {

    private static final String PROPERTIES_FILE = "/db.properties";
    private static final Properties props = new Properties();

    private static final BlockingQueue<Connection> idleConnections;
    private static final AtomicInteger totalConnections = new AtomicInteger();
    private static final int MAX_SIZE;
    private static final long BORROW_TIMEOUT_MILLIS;

    private static final ThreadLocal<Connection> TX_CONNECTION = new ThreadLocal<>();

    static {
        try {
            props.load(ConnectionPool.class.getResourceAsStream(PROPERTIES_FILE));
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Failed to load DB properties or driver: " + e);
        }

        int initial = Integer.parseInt(props.getProperty("pool.initialSize", "5"));
        MAX_SIZE = Integer.parseInt(props.getProperty("pool.maxSize", "20"));
        BORROW_TIMEOUT_MILLIS = Long.parseLong(props.getProperty("pool.borrowTimeoutMillis", "30000"));

        idleConnections = new LinkedBlockingQueue<>(MAX_SIZE);

        for (int i = 0; i < initial; i++) {
            try {
                idleConnections.offer(createNewConnection());
            } catch (SQLException e) {
                throw new ExceptionInInitializerError("Failed to create initial connections: " + e);
            }
        }
    }

    private ConnectionPool() { }

    /**
     * Borrows a connection from the pool. Blocks up to {@code pool.borrowTimeoutMillis} if necessary.
     * @throws SQLException if timeout occurs or connection cannot be created.
     */
    public static Connection getConnection() throws SQLException {
        // If current thread has an active transactional connection – return it (no wrapping to avoid premature close)
        Connection txConn = TX_CONNECTION.get();
        if(txConn != null){
            return txConn;
        }

        Connection conn = idleConnections.poll();
        if (conn != null && isConnectionValid(conn)) {
            return wrap(conn);
        }

        // No idle connection, try to create new if we have capacity
        if (totalConnections.get() < MAX_SIZE) {
            synchronized (ConnectionPool.class) {
                if (totalConnections.get() < MAX_SIZE) {
                    return wrap(createNewConnection());
                }
            }
        }

        try {
            conn = idleConnections.poll(BORROW_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            if (conn == null) {
                throw new SQLException("Timeout waiting for a free database connection");
            }
            if (!isConnectionValid(conn)) {
                // Try once more recursively
                return getConnection();
            }
            return wrap(conn);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a database connection", ie);
        }
    }

    /**
     * Returns the connection back to the pool. If the pool is already full, the connection is closed.
     */
    public static void releaseConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!isConnectionValid(conn)) {
                totalConnections.decrementAndGet();
                closeQuietly(conn);
                return;
            }
            if (!idleConnections.offer(conn)) {
                // Queue is full – close the connection, we exceeded maxIdle
                totalConnections.decrementAndGet();
                closeQuietly(conn);
            }
        } catch (Exception e) {
            closeQuietly(conn);
        }
    }

    /**
     * Closes and removes all connections. After shutdown the pool cannot be used.
     */
    public static synchronized void shutdown() {
        Connection conn;
        while ((conn = idleConnections.poll()) != null) {
            closeQuietly(conn);
            totalConnections.decrementAndGet();
        }
    }

    private static Connection createNewConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                props.getProperty("jdbc.url"),
                props.getProperty("jdbc.username"),
                props.getProperty("jdbc.password"));
        totalConnections.incrementAndGet();
        return conn;
    }

    private static boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private static void closeQuietly(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ignored) {
        }
    }

    private static Connection wrap(Connection physical) {
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                ConnectionPool.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        // If this connection is bound to a transaction, defer closing
                        if (physical.equals(TX_CONNECTION.get())) {
                            // no-op during transaction
                            return null;
                        }
                        releaseConnection(physical);
                        return null;
                    }
                    return method.invoke(physical, args);
                });
    }

    // Package-private helper used by JdbcTransactionManager
    static void bindTransactionalConnection(Connection conn){
        TX_CONNECTION.set(conn);
    }

    static Connection unbindTransactionalConnection(){
        Connection c = TX_CONNECTION.get();
        TX_CONNECTION.remove();
        return c;
    }
} 