package Utility;

public class Pair {
    public int keyInt;
    public String keyStr;
    public String value;

    public Pair(int keyInt, String value) {
        this.keyInt = keyInt;
        this.value = value;
    }

    public Pair(String keyStr, String value) {
        this.keyStr = keyStr;
        this.value = value;
    }
}
