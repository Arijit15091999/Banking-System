import java.sql.*;
import java.util.Properties;

public class DatabaseUtils implements AutoCloseable{
    private final String dbms;
    private final String database;
    private final String host;
    private final String driver;
    private final int portNumber;
    private final Properties userProperties;
    private Connection connection;


    public DatabaseUtils(String userName, String password) throws SQLException{
        this.userProperties = new Properties();
        this.userProperties.put("user", userName);
        this.userProperties.put("password", password);
        this.dbms = "mysql";
        this.host = "localhost";
        this.driver = "jdbc";
        this.portNumber = 3306;
        this.database = "banking_system";
        createConnection();
        setTransactionIsolationLevelToREAD_COMMITTED();
        getCurrentTransactionIsolationLevel();
    }

    public void createConnection() {
        String url = this.driver + ":" + this.dbms + "://" +
                this.host + ":" + this.portNumber + "/" + this.database.toLowerCase();
        System.out.println(url);
        try{
            this.connection = DriverManager.getConnection(url, this.userProperties);
            System.out.println("connected to database");
        }catch(SQLException e) {
            if(!ignoreSqlException(e.getSQLState())) {
                printSQLException(e);
            }
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    private void setTransactionIsolationLevelToREAD_COMMITTED() throws SQLException{
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    private void getCurrentTransactionIsolationLevel() throws SQLException{
        System.out.println("current isolation level = " + connection.getTransactionIsolation());
    }

    private void setTransactionIsolationLevelToSerializable() throws SQLException{
        setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }

    private void setTransactionIsolation(int level) throws SQLException {
        changeDatabaseTransactionIsolationLevel(level);
    }

    private void changeDatabaseTransactionIsolationLevel(int level) throws SQLException {
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try{
            DatabaseMetaData metaData = connection.getMetaData();

            if(level == Connection.TRANSACTION_NONE) {
                System.out.println("TRANSACTION_NONE not allowed");
                connection.setAutoCommit(true);
                return;
            }else if(level == connection.getTransactionIsolation()) {
                connection.setAutoCommit(true);
                return;
            }else if(level > connection.getTransactionIsolation()
                    &&
                    metaData.supportsTransactionIsolationLevel(level)
            ){
                connection.setTransactionIsolation(level);
            }
            connection.commit();
        }catch(SQLException e) {
            System.out.println("failed to set the level of isolation");
            System.out.println("rolling back..............");
            connection.rollback(savepoint);

            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }finally {
            connection.setAutoCommit(true);
        }
    }

//    private String getDatabaseName() {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("Database to connect");
//        String databaseName = sc.nextLine();
//        return databaseName;
//    }

    public static boolean ignoreSqlException(String sqlState) {
        if(sqlState == null) {
            System.out.println("sqlState is not defined");
            return false;
        }

        return sqlState.equalsIgnoreCase("X0Y32") ||
                sqlState.equalsIgnoreCase("42Y55") ||
                sqlState.equalsIgnoreCase("42S01");
    }

    public static void printSQLException(SQLException e) {
        while(e != null) {
            System.out.println("SqlState:- " + e.getSQLState());
            System.out.println("Error Code:- " + e.getErrorCode());
            System.out.println("message:- " + e.getMessage());

            Throwable t = e.getCause();

            while(t != null) {
                System.out.println("cause:- " + t);
                t = t.getCause();
            }
            e = e.getNextException();
        }
    }


    public static void printRowsAffected(int rows) {
        System.out.println("Query ok, " + rows + " rows affected");
    }

    @Override
    public void close(){
        try{
            if(this.connection != null)
                this.connection.close();
            System.out.println("connection closed");
        }catch(Exception e) {
            System.err.println(e);
        }
    }
}