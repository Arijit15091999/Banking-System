import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class AccountDAO implements AccountDAOInterface{
    private final Connection connection;

    public AccountDAO(Connection connection) {
        this.connection = connection;
        this.createTable();
    }

    @Override
    public void createTable() {
        String query = "CREATE TABLE account(" +
                "account_id INT NOT NULL AUTO_INCREMENT," +
                "account_type VARCHAR(30) NOT NULL," +
                "account_balance INT NOT NULL," +
                "branch_id INT NOT NULL," +
                "PRIMARY KEY (account_id)," +
                "FOREIGN KEY (branch_id) REFERENCES branch(branch_id));";


        try(Statement statement = this.connection.createStatement()){
            int rowsAffected = statement.executeUpdate(query);
            System.out.println("BANKER_INFO TABLE CREATED");
            DatabaseUtils.printRowsAffected(rowsAffected);
        }catch(SQLException e) {
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }
    }

    @Override
    public boolean createAccount(int balance, int branchId, String accountType) throws SQLException {
        boolean isSuccessful = false;

        String query = "INSERT INTO account (account_type, account_balance, branch_id) VALUES(?, ?, ?)";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        return isSuccessful;
    }

    @Override
    public Account getDefaultAccount() {
        return null;
    }

    @Override
    public Account getAccountByID(int accountId) {
        return null;
    }

    @Override
    public List<Account> getAllAccounts() {
        return null;
    }

    @Override
    public boolean updateAccount(Map<String, String> map) throws SQLException {
        return false;
    }

    @Override
    public void processInputMap(Map<String, String> map) {

    }

    @Override
    public boolean deleteAccount(int accountId) throws SQLException {
        return false;
    }
}
