import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerDAO implements CustomerDAOInterface{
    private Connection connection;
    private CustomerDAO customerDAO;

    private CustomerDAO (Connection connection) {
        this.connection = connection;
    }

    public CustomerDAO getInstance(Connection connection) {
        if(customerDAO == null) {
            customerDAO = new CustomerDAO(connection);
        }
        return this.customerDAO;
    }

    public static Customer getDefaultCustomer() {
        return new Customer(
                -1,
                "Unknown",
                new Date(),
                "0000000000",
                -1
        );
    }

    @Override
    public void createTable() {
        String query = "CREATE TABLE customer(" +
                    "customer_id INT NOT NULL AUTO_INCREMENT," +
                    "customer_name VARCHAR(30) NOT NULL," +
                    "mobileno VARCHAR(10) NOT NULL," +
                    "dob DATE," +
                    "account_id INT NOT NULL," +
                    "PRIMARY KEY (customer_id)," +
                    "FOREIGN KEY (account_id) REFERENCES account(account_id)";
);


        try(Statement statement = this.connection.createStatement()){
            int rowsAffected = statement.executeUpdate(query);
            System.out.println("CUSTOMER TABLE CREATED");
            DatabaseUtils.printRowsAffected(rowsAffected);
        }catch(SQLException e) {
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }
    }



    @Override
    public void createCustomer(Customer customer) throws SQLException {
        String query = "INSERT INTO customer (customer_name, mobileno, dob, account_id) VALUES (?, ?, ?, ?)";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try(
                PreparedStatement statement = this.connection.prepareStatement(
                    query,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT
                )
        ){
            
            int rowsAffected = statement.executeUpdate();

            DatabaseUtils.printRowsAffected(rowsAffected);

            connection.commit();
        }catch(SQLException e) {
            System.out.println("Transaction failed");
            System.out.println("rolling back.........................");
            connection.rollback(savepoint);
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public Customer getCustomerWithID(int bankerId) throws SQLException {
        String getQuery = "SELECT * FROM banker_info WHERE banker_id = ?";
        Banker banker = getDefaultBanker();

        try(PreparedStatement statement = this.connection.prepareStatement(
                getQuery,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
        )){
            statement.setInt(1, bankerId);
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next()) {
                System.out.println("No Banker found with this ID:- " + bankerId);
                return banker;
            }
            resultSet.first();
            banker = new Banker(resultSet.getString(2), resultSet.getInt(1), resultSet.getInt(3));
        }catch(SQLException e) {
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }

        return banker;
    }

    @Override
    public List<Customer> getAllCustomer() {
        String getQuery = "SELECT * FROM banker_info";
        Banker banker = getDefaultBanker();
        List<Banker> list = new ArrayList<>();

        try(PreparedStatement statement = this.connection.prepareStatement(
                getQuery,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
        )){

            ResultSet resultSet = statement.executeQuery();

            list = getAllBankerHelper(resultSet);
        }catch(SQLException e) {
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }

        return list;
    }

    public List<Banker> getAllBankerHelper(ResultSet resultSet) throws SQLException{
        List<Banker> res = new ArrayList<>();

        while(resultSet.next()) {
            Banker banker = new Banker(resultSet.getString(2), resultSet.getInt(1), resultSet.getInt(3));
            res.add(banker);
        }

        return res;
    }

    @Override
    public void updateBankerName(int bankerId, String name) throws SQLException {
        String query = "SELECT * FROM banker_info WHERE banker_id = ?";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try(
                PreparedStatement statement = connection.prepareStatement(
                        query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                )
        ) {
            statement.setInt(1, bankerId);
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next()) {
                System.out.println("No Banker found with this ID:- " + bankerId);
                return;
            }
            resultSet.beforeFirst();

            System.out.println("\n before update");
            printBankers(resultSet);

            resultSet.first();

            resultSet.updateString(2, name);
            resultSet.updateRow();
            resultSet.beforeFirst();

            System.out.println("\n After update");
            printBankers(resultSet);

            connection.commit();
        }catch(SQLException e) {
            System.out.println("Transaction failed");
            System.out.println("rolling back.........................");
            connection.rollback(savepoint);
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public void updateBankersBranch(int bankerId, int branchId) throws SQLException {
        String query = "SELECT * FROM banker_info WHERE banker_id = ?";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try(
                PreparedStatement statement = connection.prepareStatement(
                        query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                )
        ) {
            statement.setInt(1, bankerId);
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next()) {
                System.out.println("No Banker found with this ID:- " + bankerId);
                return;
            }
            resultSet.beforeFirst();

            System.out.println("\n before update");
            printBankers(resultSet);

            resultSet.first();

            resultSet.updateInt(3, branchId);
            resultSet.updateRow();
            resultSet.beforeFirst();

            System.out.println("\n After update");
            printBankers(resultSet);

            connection.commit();
        }catch(SQLException e) {
            System.out.println("Transaction failed");
            System.out.println("rolling back.........................");
            connection.rollback(savepoint);
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public boolean deleteCustomer(int customerId) throws SQLException {
        String selectQuery = "SELECT * FROM banker_info WHERE banker_id = ?";
        String deleteQuery = "DELETE FROM banker_info WHERE banker_id = ?";

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
            selectStatement.setInt(1, bankerId);

            if(!selectStatement.executeQuery().next()) {
                System.out.println("No Banker found with this ID:- " + bankerId);
                return false;
            }

            deleteStatement.setInt(1, bankerId);

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

    private void printCustomers(ResultSet resultSet) throws SQLException{
        if(resultSet.isLast()) return;
        System.out.println("----------------------------------------------");
        while(resultSet.next()) {

            System.out.println("----------------------------------------------");
        }
    }
}
