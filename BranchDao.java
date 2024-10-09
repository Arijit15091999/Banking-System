import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BranchDao implements BranchDAOInterface{
    private final Connection connection;

    public BranchDao(Connection connection) throws SQLException{
        this.connection = connection;
        createTable();
    }

    public static Branch getDefaultBranch() {
        return new Branch(-1, "none", -1, "none");
    }

    private void createTable() throws SQLException{
        String query = "CREATE TABLE branch(" +
                    "branch_id INT NOT NULL AUTO_INCREMENT," +
                    "branch_name VARCHAR(30) NOT NULL," +
                    "assets INT NOT NULL," +
                    "branch_address VARCHAR(255) NOT NULL," +
                    "PRIMARY KEY(branch_id));";

        try(Statement statement = this.connection.createStatement()){

            int rowsAffected = statement.executeUpdate(query);
            System.out.println("BRANCH TABLE CREATED");
            DatabaseUtils.printRowsAffected(rowsAffected);

        }catch(SQLException e) {
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }
    }

    @Override
    public void createBranch(Branch branch) throws SQLException {
        String query = "INSERT INTO branch (branch_name, assets, branch_address) VALUES (?, ?, ?)";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try(PreparedStatement statement = this.connection.prepareStatement(
                query,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
        )){
            statement.setString(1, branch.getBranchName());
            statement.setInt(2, branch.getAsset());
            statement.setString(3, branch.getBranchAddress());

            int res = statement.executeUpdate();

            DatabaseUtils.printRowsAffected(res);

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
    }

    @Override
    public Optional<Branch> getBranchByID(int branchId) throws SQLException {
        String query = "SELECT * FROM BRANCH WHERE branch_id = ?";
        Branch branch = getDefaultBranch();
        Savepoint savepoint = connection.setSavepoint();
        connection.setAutoCommit(false);

        try(PreparedStatement statement = this.connection.prepareStatement(
                query,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
        )){
            statement.setInt(1, branchId);
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next()) {
                System.out.println("No Branch found with Branch ID:- " + branchId);
                return Optional.of(branch);
            }

            resultSet.first();

            branch = new Branch(
                    resultSet.getInt(1),
                    resultSet.getString(2),
                    resultSet.getInt(3),
                    resultSet.getString(4)
                    );

//            return Optional.of(branch);
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

        return Optional.of(branch);
    }

    @Override
    public List<Branch> getAllBranch() {
        String query = "SELECT * FROM BRANCH";
        List<Branch> res = new ArrayList<Branch>();

        try(PreparedStatement statement = this.connection.prepareStatement(
                query,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
        )){
            ResultSet resultSet = statement.executeQuery();

            res = getAllBranchHelper(resultSet);

        }catch(SQLException e) {
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }

        return res;
    }

    @Override
    public boolean deleteBranch(int branchId) throws SQLException {
        String selectQuery = "SELECT * FROM branch WHERE branch_id = ?";
        String deleteQuery = "DELETE FROM branch WHERE branch_id = ?";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

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
            selectStatement.setInt(1, branchId);
            deleteStatement.setInt(1, branchId);
            ResultSet resultSet = selectStatement.executeQuery();

            if(!resultSet.next()) {
                System.out.println("No Branch found with id:- " + branchId);
            }else{
                System.out.println("table before delete \n");
                seeAllBranches();

                int rowsAffected = deleteStatement.executeUpdate();

                DatabaseUtils.printRowsAffected(rowsAffected);

                System.out.println("table after delete \n");

                seeAllBranches();
            }

            connection.commit();

            return true;

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

    @Override
    public void updateBranchAddress(int branchId, String newAddress) throws SQLException {
        String selectQuery = "SELECT * FROM branch WHERE branch_id = ?";
        String updateQuery = "UPDATE branch SET branch_address = ? WHERE branch_id = ?";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try(
                PreparedStatement selectStatement = connection.prepareStatement(
                        selectQuery,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                );
                PreparedStatement updateStatement = connection.prepareStatement(
                        updateQuery,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                );
        ) {
            selectStatement.setInt(1, branchId);
            updateStatement.setString(1, newAddress);
            updateStatement.setInt(2, branchId);

            ResultSet resultSet = selectStatement.executeQuery();

            if(!resultSet.next()) {
                System.out.println("No Branch found with id:- " + branchId);
            }else{
                System.out.println("table before update \n");
                seeAllBranches();

                int rowsAffected = updateStatement.executeUpdate();

                DatabaseUtils.printRowsAffected(rowsAffected);
            }

            connection.commit();

            System.out.println("table after update \n");

            seeAllBranches();

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
    }

    @Override
    public void updateAsset(int branchId, int newAsset) throws SQLException {
        String selectQuery = "SELECT * FROM branch WHERE branch_id = ?";
        String updateQuery = "UPDATE branch SET assets = ? WHERE branch_id = ?";
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint();

        try(
                PreparedStatement selectStatement = connection.prepareStatement(
                        selectQuery,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                );
                PreparedStatement updateStatement = connection.prepareStatement(
                        updateQuery,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT
                );
        ) {
            selectStatement.setInt(1, branchId);
            updateStatement.setInt(1, newAsset);
            updateStatement.setInt(2, branchId);

            ResultSet resultSet = selectStatement.executeQuery();

            if(!resultSet.next()) {
                System.out.println("No Branch found with id:- " + branchId);
            }else{
                System.out.println("table before update \n");
                seeAllBranches();

                int rowsAffected = updateStatement.executeUpdate();

                DatabaseUtils.printRowsAffected(rowsAffected);

                System.out.println("table after update \n");

                seeAllBranches();
            }

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
    }


    private List<Branch> getAllBranchHelper(ResultSet resultSet) {

        List<Branch> res = new ArrayList<>();

        try{
            while(resultSet.next()){
                int branchId = resultSet.getInt(1);
                String branchName = resultSet.getString(2);
                int asset = resultSet.getInt(3);
                String branchAddress = resultSet.getString(4);

                Branch branch = new Branch(branchId, branchName, asset, branchAddress);

                res.add(branch);
            }
        }catch(SQLException e){
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }

        return res;
    }

    public void seeAllBranches() {
        String query = "SELECT * FROM BRANCH";

        try(PreparedStatement statement = this.connection.prepareStatement(
                query,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
        )){
            ResultSet resultSet = statement.executeQuery();

            seeAllBranchesHelper(resultSet);

        }catch(SQLException e) {
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }
    }

    private void seeAllBranchesHelper(ResultSet resultSet) {
        System.out.println("---------------Branch Table----------------");

        try{
            while(resultSet.next()){
                int branchId = resultSet.getInt(1);
                String branchName = resultSet.getString(2);
                int asset = resultSet.getInt(3);
                String branchAddress = resultSet.getString(4);

                System.out.println("Branch ID:- " + branchId);
                System.out.println("Branch Name:- " + branchName);
                System.out.println("Asset:- " + asset);
                System.out.println("Branch Address:- " + branchAddress);
                System.out.println();
                System.out.println("-------------------------------------------");
            }
        }catch(SQLException e){
            if(!DatabaseUtils.ignoreSqlException(e.getSQLState())) {
                DatabaseUtils.printSQLException(e);
            }
        }

    }
}
