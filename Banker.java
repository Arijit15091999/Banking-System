public class Banker {
    private final String name;
    private int bankerId;
    private final int branchId;

    public Banker(String name, int branchId) {
        this.name = name;
        this.branchId = branchId;
    }

    public Banker(String name, int bankerId, int branchId) {
        this.name = name;
        this.bankerId = bankerId;
        this.branchId = branchId;
    }

    public String getName() {
        return this.name;
    }

    public int getBankerId() {
        return this.bankerId;
    }

    public int getBranchId() {
        return this.branchId;
    }
}
