import java.sql.SQLException;
import java.util.List;

public interface BankerDAOInterface {
    public abstract void createTable();
    public abstract void createBanker(Banker banker) throws SQLException;
    public abstract Banker getBankerWithID(int bankerId) throws SQLException;
    public abstract List<Banker> getAllBanker();
    public abstract void updateBankerName(int bankerId, String name) throws SQLException;
    public abstract void updateBankersBranch(int bankerId, int branchId) throws SQLException;
    public abstract boolean deleteBanker(int bankerId) throws SQLException;
}
