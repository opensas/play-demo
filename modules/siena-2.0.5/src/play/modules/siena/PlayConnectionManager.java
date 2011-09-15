package play.modules.siena;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import play.db.DB;
import siena.SienaException;
import siena.jdbc.ConnectionManager;

public class PlayConnectionManager implements ConnectionManager {

    public Connection getConnection() {
        return DB.getConnection();
    }
    
    public DataSource getDataSource() {
        return DB.datasource;
    }


    public void init(Properties properties) {
    }

    public void beginTransaction(int isolationLevel) {
        try {
            Connection c = getConnection();
            c.setAutoCommit(false);
            c.setTransactionIsolation(isolationLevel);
        } catch (SQLException e) {
            throw new SienaException(e);
        }
    }
    
    public void beginTransaction() {
        try {
            Connection c = getConnection();
            c.setAutoCommit(false);
        } catch (SQLException e) {
            throw new SienaException(e);
        }
    }

    public void commitTransaction() {
        try {
            Connection c = getConnection();
            c.commit();
        } catch (SQLException e) {
            throw new SienaException(e);
        }
    }

    public void rollbackTransaction() {
        try {
            Connection c = getConnection();
            c.rollback();
        } catch (SQLException e) {
            throw new SienaException(e);
        }
    }

    public void closeConnection() {
        // play will do it
    }
}
