package de.rwth.comsys.samrad.preserv.utilz;

/**
 * Truncates a floating point *x_f* to *d* decimal places,
 * then scales it up to factor 10**(nd).
 * i.e. the result can be safely used in a product with *d*
 * decimal places of accuracy and at maximum *n* factors.
 *
 * Note: the input array is edited in-place
 */
public class Utilz {

    public static long[] tns(long[] x, int d, int n) {
        int index = 0;
        for (long x_i : x) {
            x[index++] = ((int) (x_i * Math.pow(10, d))) * (long) (Math.pow(10, (n * d - d)));
        }
        return x;
    }
}
