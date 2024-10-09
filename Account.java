public class Account {
    private int accountId;
    private int balance;
    private String accountType;
    private int branchId;

    public Account(int accountId, int balance, int branchId, String accountType) {
        this.accountId = accountId;
        this.balance = balance;
        this.branchId = branchId;
        this.accountType = accountType;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public int getBalance() {
        return this.balance;
    }

    public int getBranchId() { return this.branchId; }

    public String getAccountType() {
        return this.accountType;
    }
}
