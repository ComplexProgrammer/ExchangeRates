package exchangerates.complexprogrammer.uz.services;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import exchangerates.complexprogrammer.uz.R;
import exchangerates.complexprogrammer.uz.models.Response;

/** Number and date formatting that follows the app language: "12 845,67" rather than "12845.67". */
public class Formatters {
    private static final String API_DATE = "dd.MM.yyyy";

    private static Locale numberLocale(Context context) {
        Locale locale = LocaleHelper.getLocale(context);
        // Java has thin data for Uzbek; Russian conventions (space grouping, comma decimal) match local usage.
        return "uz".equals(locale.getLanguage()) ? new Locale("ru") : locale;
    }

    private static DecimalFormat decimal(Context context, int fraction) {
        DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(numberLocale(context));
        format.setMinimumFractionDigits(fraction);
        format.setMaximumFractionDigits(fraction);
        format.setGroupingUsed(true);
        return format;
    }

    public static String money(Context context, double value) {
        return decimal(context, 2).format(value);
    }

    public static String moneyWithUnit(Context context, double value) {
        return money(context, value) + " " + context.getString(R.string.sum);
    }

    /** "+14,26" / "−22,46" / "0,00". */
    public static String signed(Context context, double value) {
        String text = money(context, Math.abs(value));
        if (value > 0) return "+" + text;
        if (value < 0) return "−" + text;
        return text;
    }

    public static String signedPercent(Context context, double value) {
        String text = decimal(context, 2).format(Math.abs(value)) + "%";
        if (value > 0) return "+" + text;
        if (value < 0) return "−" + text;
        return text;
    }

    /** "+14,26 (+0,12%)". */
    public static String change(Context context, Response rate) {
        return signed(context, rate.getDiffValue()) + " (" + signedPercent(context, rate.getDiffPercent()) + ")";
    }

    /** "1 USD" or "10 JPY". */
    public static String nominal(Response rate) {
        return rate.getNominalValue() + " " + rate.getCcy();
    }

    /** Amount typed by the user, accepting "1 000,5", "1000.5" etc. Returns NaN when invalid. */
    public static double parseAmount(Context context, String text) {
        String cleaned = text.replace(" ", "").replace(" ", "").replace(" ", "").replace(',', '.');
        if (cleaned.isEmpty()) return Double.NaN;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /** Converter output: no grouping so it can be edited again, up to 2 decimals. */
    public static String plainAmount(Context context, double value) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(numberLocale(context)));
        return format.format(value);
    }

    public static Date parseApiDate(String date) {
        try {
            return new SimpleDateFormat(API_DATE, Locale.US).parse(date);
        } catch (ParseException | NullPointerException e) {
            return null;
        }
    }

    public static String shortDate(Context context, long time) {
        return new SimpleDateFormat("dd.MM", LocaleHelper.getLocale(context)).format(new Date(time));
    }

    public static String fullDate(Context context, long time) {
        return new SimpleDateFormat(API_DATE, LocaleHelper.getLocale(context)).format(new Date(time));
    }

    public static String dateTime(Context context, long time) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", LocaleHelper.getLocale(context)).format(new Date(time));
    }

    @ColorRes
    public static int trendColor(double diff) {
        if (diff > 0) return R.color.rate_up;
        if (diff < 0) return R.color.rate_down;
        return R.color.rate_flat;
    }

    @DrawableRes
    public static int trendIcon(double diff) {
        if (diff > 0) return R.drawable.ic_trending_up;
        if (diff < 0) return R.drawable.ic_trending_down;
        return R.drawable.ic_trending_flat;
    }
}
