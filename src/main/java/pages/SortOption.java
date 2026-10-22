package pages;

/** Values of the sort dropdown's <option> elements, verified against the live site. */
public enum SortOption {
    NAME_A_TO_Z("az"),
    NAME_Z_TO_A("za"),
    PRICE_LOW_TO_HIGH("lohi"),
    PRICE_HIGH_TO_LOW("hilo");

    private final String value;

    SortOption(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
