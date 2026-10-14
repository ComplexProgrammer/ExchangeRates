package exchangerates.complexprogrammer.uz.models;

/** One point on the rate history chart. */
public class HistoryPoint {
    public final long time;
    public final double rate;

    public HistoryPoint(long time, double rate) {
        this.time = time;
        this.rate = rate;
    }
}
