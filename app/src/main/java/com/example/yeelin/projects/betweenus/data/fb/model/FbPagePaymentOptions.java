package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 11/2/15.
 */
public class FbPagePaymentOptions {
    private final int amex;
    private final int cash_only;
    private final int discover;
    private final int mastercard;
    private final int visa;

    public FbPagePaymentOptions(int amex, int cash_only, int discover, int mastercard, int visa) {
        this.amex = amex;
        this.cash_only = cash_only;
        this.discover = discover;
        this.mastercard = mastercard;
        this.visa = visa;
    }

    @Override
    public String toString() {
        return String.format("[amex:%d, cash:%d, discover:%d, master:%d, visa:%d]",
                amex, cash_only, discover, mastercard, visa);
    }
}
