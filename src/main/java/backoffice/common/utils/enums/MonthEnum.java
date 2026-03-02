package backoffice.common.utils.enums;

public enum MonthEnum {
    JANUARY("JANUARY", "jan"),
    FEBRUARY("FEBRUARY", "fev"),
    MARCH("MARCH", "mar"),
    APRIL("APRIL", "abr"),
    MAY("MAY", "mai"),
    JUNE("JUNE", "jun"),
    JULY("JULY", "jul"),
    AUGUST("AUGUST", "ago"),
    SEPTEMBER("SEPTEMBER", "set"),
    OCTOBER("OCTOBER", "out"),
    NOVEMBER("NOVEMBER", "nov"),
    DECEMBER("DECEMBER", "dez");

    private String key;
    private String value;

    MonthEnum(String key,String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
