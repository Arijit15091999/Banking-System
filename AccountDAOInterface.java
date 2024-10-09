import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface AccountDAOInterface {
    public abstract void createTable();
    public abstract boolean createAccount(int balance, int branchId, String accountType) throws SQLException;
    public abstract Account getDefaultAccount();
    public abstract Account getAccountByID(int accountId);
    public abstract List<Account> getAllAccounts();
    public abstract boolean updateAccount(Map<String, String> map) throws SQLException;
    public abstract void processInputMap(Map<String, String> map);
    public abstract boolean deleteAccount(int accountId) throws SQLException;
}
