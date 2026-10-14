package exchangerates.complexprogrammer.uz.models;

import java.io.Serializable;

/** One currency rate as returned by the API (Central Bank of Uzbekistan format). */
public class Response implements Serializable {

    private String id;
    private String Code;
    private String Ccy;
    private String CcyNm_RU;
    private String CcyNm_UZ;
    private String CcyNm_UZC;
    private String CcyNm_EN;
    private String Nominal;
    private String Rate;
    private String Diff;
    private String Date;

    public String getId() {
        return id;
    }

    public String getCode() {
        return Code;
    }

    public String getCcy() {
        return Ccy == null ? "" : Ccy;
    }

    public String getCcyNm_RU() {
        return CcyNm_RU;
    }

    public String getCcyNm_UZ() {
        return CcyNm_UZ;
    }

    public String getCcyNm_UZC() {
        return CcyNm_UZC;
    }

    public String getCcyNm_EN() {
        return CcyNm_EN;
    }

    public String getNominal() {
        return Nominal;
    }

    public String getRate() {
        return Rate;
    }

    public String getDiff() {
        return Diff;
    }

    public String getDate() {
        return Date == null ? "" : Date;
    }

    /** Currency name in the given content language ("uz", "uzc", "ru" or "en"). */
    public String getName(String lang) {
        String name;
        switch (lang) {
            case "uz":
                name = CcyNm_UZ;
                break;
            case "uzc":
                name = CcyNm_UZC;
                break;
            case "ru":
                name = CcyNm_RU;
                break;
            default:
                name = CcyNm_EN;
        }
        return name == null || name.isEmpty() ? getCcy() : name;
    }

    /** Whether the query matches the code or any of the localized names. */
    public boolean matches(String query) {
        String q = query.trim().toLowerCase();
        if (q.isEmpty()) return true;
        return contains(Ccy, q) || contains(Code, q) || contains(CcyNm_UZ, q) || contains(CcyNm_UZC, q)
                || contains(CcyNm_RU, q) || contains(CcyNm_EN, q);
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    public int getNominalValue() {
        try {
            int n = Integer.parseInt(Nominal.trim());
            return n > 0 ? n : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    /** Price of {@link #getNominalValue()} units in so'm. */
    public double getRateValue() {
        return parse(Rate);
    }

    public double getDiffValue() {
        return parse(Diff);
    }

    /** Price of a single unit in so'm. */
    public double getUnitRate() {
        return getRateValue() / getNominalValue();
    }

    /** Change relative to the previous rate, in percent. */
    public double getDiffPercent() {
        double previous = getRateValue() - getDiffValue();
        return previous == 0 ? 0 : getDiffValue() / previous * 100;
    }

    private static double parse(String value) {
        if (value == null) return 0;
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
