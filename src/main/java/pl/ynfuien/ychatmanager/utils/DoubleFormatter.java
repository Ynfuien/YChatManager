package pl.ynfuien.ychatmanager.utils;

import java.text.DecimalFormat;

public class DoubleFormatter {
    private boolean cutDecimalZeros;
    private int exactDecimalPlaces = -1;
    private int maxDecimalPlaces = -1;

    public DoubleFormatter() {
        this.cutDecimalZeros = true;
        this.exactDecimalPlaces = 2;
    }
    public DoubleFormatter(boolean cutDecimalZeros, int exactDecimalPlaces) {
        this.cutDecimalZeros = cutDecimalZeros;
        this.exactDecimalPlaces = Math.max(exactDecimalPlaces, 0);
    }

    /**
     * Sets whether to cut decimal value in formatted number, if it's just zero(s). Default to true
     *
     * @param cut whether to set
     */
    public DoubleFormatter cutDecimalZeros(boolean cut) {
        this.cutDecimalZeros = cut;
        return this;
    }

    /**
     * Sets exact decimal places that formatted number will have.
     * <p>
     * <b>It's mutually exclusive with max decimal places!</b>
     *
     * @param places exact decimal places
     */
    public DoubleFormatter setExactDecimalPlaces(int places) {
        maxDecimalPlaces = -1;
        exactDecimalPlaces = Math.max(places, 0);
        return this;
    }

    /**
     * Sets max decimal places that formatted number will have.
     * <p>
     * <b>It's mutually exclusive with exact decimal places!</b>
     *
     * @param places exact decimal places
     */
    public DoubleFormatter setMaxDecimalPlaces(int places) {
        exactDecimalPlaces = -1;
        maxDecimalPlaces = Math.max(places, 0);
        return this;
    }

    /**
     * Formats given number with settings set before.
     *
     * @param number number to format
     */
    public String format(double number) {
        // Return simple int if number doesn't have decimal places and if this option is set
        if (cutDecimalZeros && (int) number == number) return Integer.toString((int) number);

        if (exactDecimalPlaces == -1 && maxDecimalPlaces == -1) return Double.toString(number);
        if (exactDecimalPlaces == 0 || maxDecimalPlaces == 0) return Integer.toString((int) number);

        DecimalFormat df = new DecimalFormat();
        if (exactDecimalPlaces != -1) {
            df.applyPattern("0." + "0".repeat(exactDecimalPlaces));
            return df.format(number);
        }

        df.applyPattern("#." + "#".repeat(maxDecimalPlaces));
        return df.format(number);
    }
}