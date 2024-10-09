import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BankerDAO implements BankerDAOInterface{
    private final Connection connection;

    public BankerDAO(Connection connection) {
        this.connection = connection;
        createTable();
    }

    private static Banker getDefaultBanker() {
        return new Banker("unknown", -1, -1);
    }

    @Override
    public void createTable() {
        String query = "CREATE TABLE banker_info(" +
                "banker_id INT NOT NULL AUTO_INCREMENT," +
                "banker_name VARCHAR(255) NOT NULL," +
                "branch_id INT NOT NULL," +
                "PRIMARY KEY (banker_id)," +
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
    public void createBanker(Banker banker) throws SQLException {
        String query = "INSERT INTO banker_info (banker_name, branch_id) VALUES (?, ?)";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try(PreparedStatement statement = this.connection.prepareStatement(
                query,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
        )){
            statement.setString(1, banker.getName());
            statement.setInt(2, banker.getBranchId());

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
    public Banker getBankerWithID(int bankerId) throws SQLException {
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
    public List<Banker> getAllBanker() {
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
    public boolean deleteBanker(int bankerId) throws SQLException {
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

    private void printBankers(ResultSet resultSet) throws SQLException{
        if(resultSet.isLast()) return;
        System.out.println("----------------------------------------------");
        while(resultSet.next()) {
            int bankerId = resultSet.getInt(1);
            String name = resultSet.getString(2);
            int branchId = resultSet.getInt(3);

            System.out.println("BankerID:- " + bankerId);
            System.out.println("Name:- " + name);
            System.out.println("BranchId:- " + branchId);
            System.out.println("----------------------------------------------");
        }
    }
}
