public class Branch {
    private final String branchName;
    private int branchId;
    private final int asset;
    private final String branchAddress;

    public Branch(String branchName, String branchAddress, int asset) {
        this.branchName = branchName;
        this.asset = asset;
        this.branchAddress = branchAddress;
    }

    public Branch( int branchId, String branchName, int asset, String branchAddress) {
        this.branchName = branchName;
        this.branchId = branchId;
        this.asset = asset;
        this.branchAddress = branchAddress;
    }

    public String getBranchName() {
        return this.branchName;
    }

    public int getBranchId() {
        return this.branchId;
    }

    public int getAsset() {
        return asset;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    @Override
    public String toString() {
        return "\nBranchId:- " + this.branchId +
                "\nBranchName:- " + this.branchName +
                "\nAddress:- " + this.branchAddress + "\n";
    }
}
