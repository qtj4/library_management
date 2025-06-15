package edu.epam.fop.dao;

import java.sql.Connection;

public class JdbcTransactionManager {

    private JdbcTransactionManager(){}

    public static Connection begin() throws Exception {
        Connection conn = ConnectionPool.getConnection();
        conn.setAutoCommit(false);
        ConnectionPool.bindTransactionalConnection(conn);
        return conn;
    }

    public static void commit() {
        Connection conn = ConnectionPool.unbindTransactionalConnection();
        if(conn!=null){
            try { conn.commit(); } catch(Exception e){ org.slf4j.LoggerFactory.getLogger(JdbcTransactionManager.class).error("Commit failed", e);}
            // return to pool via close()
            try { conn.close(); } catch(Exception e){ org.slf4j.LoggerFactory.getLogger(JdbcTransactionManager.class).warn("Close failed", e);}
        }
    }

    public static void rollback() {
        Connection conn = ConnectionPool.unbindTransactionalConnection();
        if(conn!=null){
            try { conn.rollback(); } catch(Exception e){ org.slf4j.LoggerFactory.getLogger(JdbcTransactionManager.class).error("Rollback failed", e);}
            try { conn.close(); } catch(Exception e){ org.slf4j.LoggerFactory.getLogger(JdbcTransactionManager.class).warn("Close failed", e);}
        }
    }
} 