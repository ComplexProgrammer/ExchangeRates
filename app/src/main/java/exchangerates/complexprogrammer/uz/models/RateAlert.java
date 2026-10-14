package exchangerates.complexprogrammer.uz.models;

/** A one-shot notification when a currency crosses a threshold (per {@link Response#getRateValue()}). */
public class RateAlert {
    public String ccy;
    public boolean above;
    public double value;

    public RateAlert(String ccy, boolean above, double value) {
        this.ccy = ccy;
        this.above = above;
        this.value = value;
    }

    public boolean isTriggeredBy(double rate) {
        return above ? rate > value : rate < value;
    }
}
