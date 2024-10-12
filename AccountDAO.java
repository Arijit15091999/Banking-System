import Utility.Pair;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountDAO implements AccountDAOInterface{
    private final Connection connection;
    private AccountDAO accountDAO;

    private AccountDAO(Connection connection) {
        this.connection = connection;
        this.createTable();
    }

    public AccountDAO getInstance(Connection connection) {
        if(accountDAO == null) {
            accountDAO = new AccountDAO(connection);
        }
        return accountDAO;
    }


    @Override
    public void createTable() {
        String query = "CREATE TABLE account(" +
                "account_id INT NOT NULL AUTO_INCREMENT," +
                "account_balance INT NOT NULL," +
                "branch_id INT NOT NULL," +
                "account_type VARCHAR(30) NOT NULL," +
                "PRIMARY KEY (account_id)," +
                "FOREIGN KEY (branch_id) REFERENCES branch(branch_id));";


        try(Statement statement = this.connection.createStatement()){
            int rowsAffected = statement.executeUpdate(query);
            System.out.println("ACCOUNT TABLE CREATED");
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

        try(
            PreparedStatement preparedStatement = connection.prepareStatement(
                query,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
                );
        ) {
            preparedStatement.setString(1, accountType);
            preparedStatement.setInt(2, balance);
            preparedStatement.setInt(3, branchId);
            int rowsAffected = preparedStatement.executeUpdate();
            DatabaseUtils.printRowsAffected(rowsAffected);
            isSuccessful = true;
            connection.commit();
        }catch(SQLException e) {
            System.out.println("Account creation failed");
            System.out.println("Rolling back transaction");
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
            connection.rollback(savepoint);
        }finally {
            connection.setAutoCommit(true);
        }

        return isSuccessful;
    }

    @Override
    public Account getDefaultAccount() {
        return new Account(-1, 0, 0, "SAVINGS");
    }

    @Override
    public Account getAccountByID(int accountId) {
        String query = "SELECT * FROM account WHERE account_id = ?";
        Account account = getDefaultAccount();

        try(
            PreparedStatement preparedStatement = connection.prepareStatement(
                query,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
            )
        ){
            preparedStatement.setInt(1, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                account = new Account(
                    resultSet.getInt(1),
                    resultSet.getInt(2),
                    resultSet.getInt(3),
                    resultSet.getString(4)
                );
            }else{
                System.out.println("Account not found");
            }
        }catch(SQLException e) {
            System.out.println("Account not found");
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }
        return account;
    }

    public void showAllAccounts() {
        List<Account> list = getAllAccounts();
        System.out.println("------------------Accounts-------------");
        for(Account account : list) {
            System.out.println(account);
            System.out.println("--------------------------------------");
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        String query = "SELECT * FROM account";
        List<Account> accounts = new ArrayList<>();

        try(
            PreparedStatement preparedStatement = connection.prepareStatement(
                query,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
            )
        ){
            ResultSet resultSet = preparedStatement.executeQuery();
            accounts = getAllAccountsHelper(resultSet);
        }catch(SQLException e) {
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }
        return accounts;
    }


    private List<Account> getAllAccountsHelper(ResultSet resultSet) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        while(resultSet.next()) {
            accounts.add(
                    new Account(
                            resultSet.getInt(1),
                            resultSet.getInt(2),
                            resultSet.getInt(3),
                            resultSet.getString(4)
                    )
            );
        }
        return accounts;
    }

    @Override
    public boolean updateAccount(int accountId, Map<String, String> map) throws SQLException {
        String query = "SELECT * FROM account WHERE account_id = ?";

        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try(
                PreparedStatement preparedStatement = connection.prepareStatement(
                        query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                )
        ) {
            preparedStatement.setInt(1, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            if(!resultSet.next()) {
                System.out.println("No Account found with id:- " + accountId);
                connection.setAutoCommit(true);
                return false;
            }

            List<Pair> list = processInputMap(resultSet, map);

            if(list.isEmpty()) {
                System.out.println("Enter correct fields");
                connection.setAutoCommit(true);
                return false;
            }

            String updateQuery = processQueryString(list, resultSetMetaData);
            PreparedStatement updateStatement = connection.prepareStatement(
                    updateQuery
            );

            updateStatement.setInt(1, accountId);

            int rowsAffected = updateStatement.executeUpdate();

            DatabaseUtils.printRowsAffected(rowsAffected);

            updateStatement.close();
            connection.commit();
        }catch(SQLException e) {
            System.out.println("transaction failed");
            System.out.println("rolling back................");
            connection.rollback(savepoint);

            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }finally {
            connection.setAutoCommit(true);
        }
        return false;
    }

    private String processQueryString(List<Pair> list, ResultSetMetaData resultSetMetaData) throws SQLException{
        String updateQueryFront = " UPDATE ACCOUNT SET ";
        String updateQueryBack = " WHERE account_id = ? ";
        StringBuilder sb = new StringBuilder(updateQueryFront);

        for(int i = 0; i < list.size(); i++) {
            Pair pair = list.get(i);
            int key = pair.keyInt;
            String value = pair.value;
            String columnName = resultSetMetaData.getColumnName(key);
            if(columnName.equals("account_id")) continue;
            String temp = " " + columnName + " = " + value;

            if(i != list.size() - 1) {
                temp += ",";
            }
            sb.append(temp);
        }

        sb.append(updateQueryBack);

//        System.out.println(sb.toString());

        return sb.toString();

    }

    @Override
    public List<Pair> processInputMap(ResultSet resultSet, Map<String, String> map) throws SQLException{
        List<Pair> list = new ArrayList<>();
        resultSet.first();

        for(Map.Entry<String, String> entry : map.entrySet()) {
            try{
                int columnIndex = resultSet.findColumn(entry.getKey());
                list.add(new Pair(columnIndex, entry.getValue()));
            }catch(SQLException e) {
                if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                    DatabaseUtils.printSQLException(e);
                }
            }
        }

        return list;
    }

    @Override
    public boolean deleteAccount(int accountId) throws SQLException {
        String selectQuery = "SELECT * FROM account WHERE account_id = ?";
        String deleteQuery = "DELETE FROM account WHERE account_id = ?";

        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        boolean isSuccessful = false;

        try(
                PreparedStatement selectStatement = connection.prepareStatement(
                        selectQuery,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                );
                PreparedStatement deleteStatement = connection.prepareStatement(
                        deleteQuery,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                );
        ) {
            selectStatement.setInt(1, accountId);

            if(!selectStatement.executeQuery().next()) {
                System.out.println("No account found with this ID:- " + accountId);
                return false;
            }

            deleteStatement.setInt(1, accountId);

            int rowsAffected = deleteStatement.executeUpdate();

            DatabaseUtils.printRowsAffected(rowsAffected);

            connection.commit();
            isSuccessful = true;
        }catch(SQLException e) {
            System.out.println("Transaction failed");
            System.out.println("rolling back.........................");
            connection.rollback(savepoint);
            isSuccessful = false;
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }finally {
            connection.setAutoCommit(true);
        }

        return isSuccessful;
    }
}
