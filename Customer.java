import java.util.Date;

public class Customer {
    private int customer_id;
    private String customer_name;
    private Date dob;
    private String mobileNo;
    private int accountId;

    public Customer (
            int customer_id,
            String customer_name,
            Date dob,
            String mobileNo,
            int accountId
    ) {
        this.customer_id = customer_id;
        this.customer_name = customer_name;
        this.dob = dob;
        this.mobileNo = mobileNo;
        this.accountId = accountId;
    }

    public int getCustomer_id() {
        return this.customer_id;
    }

    public String getCustomer_name() {
        return this.customer_name;
    }

    public Date getDob() {
        return this.dob;
    }

    public String getMobileNo(){
        return this.mobileNo;
    }

    public int getAccountId() {
        return this.accountId;
    }

    @Override
    public String toString() {
        return "customer_id:- " + customer_id +
                "customer_name:- " + customer_name +
                "dob:- " + dob +
                "mobileNo:- " + mobileNo +
                "accountId:- " + accountId;
    }
}
