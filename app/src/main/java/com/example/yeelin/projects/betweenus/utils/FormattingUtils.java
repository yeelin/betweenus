package com.example.yeelin.projects.betweenus.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by ninjakiki on 8/27/15.
 */
public class FormattingUtils {
    private static DecimalFormat decimalFormatter;

    /**
     * Returns a decimal formatter that rounds up and shows up to 2 digits after the decimal point.
     * @return
     */
    public static DecimalFormat getDecimalFormatter() {
        if (decimalFormatter == null) {
            decimalFormatter = (DecimalFormat) DecimalFormat.getInstance();
            decimalFormatter.setRoundingMode(RoundingMode.HALF_UP);
            decimalFormatter.setMaximumFractionDigits(2);
        }
        return decimalFormatter;
    }
}
