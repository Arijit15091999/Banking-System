import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BranchDAOInterface {
    public abstract void createBranch(Branch branch) throws SQLException;
    public abstract Optional<Branch> getBranchByID(int branchId) throws SQLException;
    public List<Branch> getAllBranch();
    public abstract boolean deleteBranch(int branchId) throws SQLException;
    public abstract void updateBranchAddress(int branchId,  String newAddress) throws SQLException;
    public abstract void updateAsset(int branchId, int newAsset) throws SQLException;
}
