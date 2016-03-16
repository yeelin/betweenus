package com.example.yeelin.projects.betweenus.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by ninjakiki on 8/27/15.
 */
public class FormattingUtils {
    private static DecimalFormat decimalFormatter;
    public static final double ONE_KM_IN_METERS = 1000.0;
    public static final double ONE_MIN_IN_SECS = 60.0;

    /**
     * Returns a decimal formatter that rounds up half and
     * shows up to the given digits after the decimal point.
     * @param maxFractionDigits
     * @return
     */
    public static DecimalFormat getDecimalFormatter(int maxFractionDigits) {
        if (decimalFormatter == null) {
            decimalFormatter = (DecimalFormat) DecimalFormat.getInstance();
        }
        decimalFormatter.setRoundingMode(RoundingMode.HALF_UP);
        decimalFormatter.setMaximumFractionDigits(maxFractionDigits);
        return decimalFormatter;
    }

    /**
     * Returns a decimal formatter that doesn't do any rounding and
     * shows up to the given digits after the decimal point
     * @param maxFractionDigits
     * @return
     */
    public static DecimalFormat getDecimalFormatterNoRounding(int maxFractionDigits) {
        if (decimalFormatter == null) {
            decimalFormatter = (DecimalFormat) DecimalFormat.getInstance();
        }
        decimalFormatter.setRoundingMode(RoundingMode.UNNECESSARY);
        decimalFormatter.setMaximumFractionDigits(maxFractionDigits);
        return decimalFormatter;
    }
}
