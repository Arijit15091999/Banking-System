import Utility.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface AccountDAOInterface {
    void createTable();
    public abstract boolean createAccount(int balance, int branchId, String accountType) throws SQLException;
    public abstract Account getDefaultAccount();
    public abstract Account getAccountByID(int accountId);
    public abstract List<Account> getAllAccounts();
    public abstract boolean updateAccount(int accountId, Map<String, String> map) throws SQLException;
    public abstract List<Pair> processInputMap(ResultSet resultSet, Map<String, String> map) throws SQLException;
    public abstract boolean deleteAccount(int accountId) throws SQLException;
}
