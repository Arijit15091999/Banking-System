import java.sql.SQLException;
import java.util.List;

public interface CustomerDAOInterface {
    public abstract void createTable();
    public void createCustomer(Customer customer) throws SQLException;
    public Customer getCustomerWithID(int bankerId) throws SQLException;
    public List<Customer> getAllCustomer();
    public boolean deleteCustomer(int bankerId) throws SQLException;
}
