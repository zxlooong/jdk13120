/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;

/**
 * Digit List. Private to DecimalFormat.
 * Handles the transcoding
 * between numeric values and strings of characters.  Only handles
 * non-negative numbers.  The division of labor between DigitList and
 * DecimalFormat is that DigitList handles the radix 10 representation
 * issues; DecimalFormat handles the locale-specific issues such as
 * positive/negative, grouping, decimal point, currency, and so on.
 *
 * A DigitList is really a representation of a floating point value.
 * It may be an integer value; we assume that a double has sufficient
 * precision to represent all digits of a long.
 *
 * The DigitList representation consists of a string of characters,
 * which are the digits radix 10, from '0' to '9'.  It also has a radix
 * 10 exponent associated with it.  The value represented by a DigitList
 * object can be computed by mulitplying the fraction f, where 0 <= f < 1,
 * derived by placing all the digits of the list to the right of the
 * decimal point, by 10^exponent.
 *
 * @see  Locale
 * @see  Format
 * @see  NumberFormat
 * @see  DecimalFormat
 * @see  ChoiceFormat
 * @see  MessageFormat
 * @version      1.25 02/06/02
 * @author       Mark Davis, Alan Liu
 */
final class DigitList implements Cloneable {
    /**
     * The maximum number of significant digits in an IEEE 754 double, that
     * is, in a Java double.  This must not be increased, or garbage digits
     * will be generated, and should not be decreased, or accuracy will be lost.
     */
    public static final int MAX_COUNT = 19; // == Long.toString(Long.MAX_VALUE).length()
    public static final int DBL_DIG = 17;

    /**
     * These data members are intentionally public and can be set directly.
     *
     * The value represented is given by placing the decimal point before
     * digits[decimalAt].  If decimalAt is < 0, then leading zeros between
     * the decimal point and the first nonzero digit are implied.  If decimalAt
     * is > count, then trailing zeros between the digits[count-1] and the
     * decimal point are implied.
     *
     * Equivalently, the represented value is given by f * 10^decimalAt.  Here
     * f is a value 0.1 <= f < 1 arrived at by placing the digits in Digits to
     * the right of the decimal.
     *
     * DigitList is normalized, so if it is non-zero, figits[0] is non-zero.  We
     * don't allow denormalized numbers because our exponent is effectively of
     * unlimited magnitude.  The count value contains the number of significant
     * digits present in digits[].
     *
     * Zero is represented by any DigitList with count == 0 or with each digits[i]
     * for all i <= count == '0'.
     */
    public int decimalAt = 0;
    public int count = 0;
    public byte[] digits = new byte[MAX_COUNT];

    /**
     * Return true if the represented number is zero.
     */
    boolean isZero()
    {
        for (int i=0; i<count; ++i) if (digits[i] != '0') return false;
        return true;
    }

    /**
     * Clears out the digits.
     * Use before appending them.
     * Typically, you set a series of digits with append, then at the point
     * you hit the decimal point, you set myDigitList.decimalAt = myDigitList.count;
     * then go on appending digits.
     */
    public void clear () {
        decimalAt = 0;
        count = 0;
    }
    /**
     * Appends digits to the list. Ignores all digits over MAX_COUNT,
     * since they are not significant for either longs or doubles.
     */
    public void append (int digit) {
        if (count < MAX_COUNT)
            digits[count++] = (byte) digit;
    }
    /**
     * Utility routine to get the value of the digit list
     * If (count == 0) this throws a NumberFormatException, which
     * mimics Long.parseLong().
     */
    public final double getDouble() {
        if (count == 0) return 0.0;
        StringBuffer temp = new StringBuffer(count);
        temp.append('.');
        for (int i = 0; i < count; ++i) temp.append((char)(digits[i]));
        temp.append('E');
        temp.append(Integer.toString(decimalAt));
        return Double.valueOf(temp.toString()).doubleValue();
        // long value = Long.parseLong(temp.toString());
        // return (value * Math.pow(10, decimalAt - count));
    }

    /**
     * Utility routine to get the value of the digit list.
     * If (count == 0) this returns 0, unlike Long.parseLong().
     */
    public final long getLong() {
        // for now, simple implementation; later, do proper IEEE native stuff

        if (count == 0) return 0;

        // We have to check for this, because this is the one NEGATIVE value
        // we represent.  If we tried to just pass the digits off to parseLong,
        // we'd get a parse failure.
        if (isLongMIN_VALUE()) return Long.MIN_VALUE;

        StringBuffer temp = new StringBuffer(count);
        for (int i = 0; i < decimalAt; ++i)
        {
            temp.append((i < count) ? (char)(digits[i]) : '0');
        }
        return Long.parseLong(temp.toString());
    }

    /**
     * Return true if the number represented by this object can fit into
     * a long.
     * @param isPositive true if this number should be regarded as positive
     * @param ignoreNegativeZero true if -0 should be regarded as identical to
     * +0; otherwise they are considered distinct
     * @return true if this number fits into a Java long
     */
    boolean fitsIntoLong(boolean isPositive, boolean ignoreNegativeZero)
    {
        // Figure out if the result will fit in a long.  We have to
        // first look for nonzero digits after the decimal point;
        // then check the size.  If the digit count is 18 or less, then
        // the value can definitely be represented as a long.  If it is 19
        // then it may be too large.

        // Trim trailing zeros.  This does not change the represented value.
        while (count > 0 && digits[count - 1] == (byte)'0') --count;

        if (count == 0) {
            // Positive zero fits into a long, but negative zero can only
            // be represented as a double. - bug 4162852
            return isPositive || ignoreNegativeZero;
        }

        if (decimalAt < count || decimalAt > MAX_COUNT) return false;

        if (decimalAt < MAX_COUNT) return true;

        // At this point we have decimalAt == count, and count == MAX_COUNT.
        // The number will overflow if it is larger than 9223372036854775807
        // or smaller than -9223372036854775808.
        for (int i=0; i<count; ++i)
        {
            byte dig = digits[i], max = LONG_MIN_REP[i];
            if (dig > max) return false;
            if (dig < max) return true;
        }

        // At this point the first count digits match.  If decimalAt is less
        // than count, then the remaining digits are zero, and we return true.
        if (count < decimalAt) return true;

        // Now we have a representation of Long.MIN_VALUE, without the leading
        // negative sign.  If this represents a positive value, then it does
        // not fit; otherwise it fits.
        return !isPositive;
    }

    private static final boolean DEBUG = false;

    /**
     * Set the digit list to a representation of the given double value.
     * This method supports fixed-point notation.
     * @param source Value to be converted; must not be Inf, -Inf, Nan,
     * or a value <= 0.
     * @param maximumFractionDigits The most fractional digits which should
     * be converted.
     */
    public final void set(double source, int maximumFractionDigits)
    {
        set(source, maximumFractionDigits, true);
    }

    /**
     * Set the digit list to a representation of the given double value.
     * This method supports both fixed-point and exponential notation.
     * @param source Value to be converted; must not be Inf, -Inf, Nan,
     * or a value <= 0.
     * @param maximumDigits The most fractional or total digits which should
     * be converted.
     * @param fixedPoint If true, then maximumDigits is the maximum
     * fractional digits to be converted.  If false, total digits.
     */
    final void set(double source, int maximumDigits, boolean fixedPoint)
    {
        if (source == 0) source = 0;
        // Generate a representation of the form DDDDD, DDDDD.DDDDD, or
        // DDDDDE+/-DDDDD.
        String rep = Double.toString(source);

        decimalAt = -1;
        count = 0;
        int exponent = 0;
        // Number of zeros between decimal point and first non-zero digit after
        // decimal point, for numbers < 1.
        int leadingZerosAfterDecimal = 0;
        boolean nonZeroDigitSeen = false;
        for (int i=0; i < rep.length(); ++i)
        {
            char c = rep.charAt(i);
            if (c == '.')
            {
            decimalAt = count;
            }
            else if (c == 'e' || c == 'E')
            {
            exponent = Integer.valueOf(rep.substring(i+1)).intValue();
            break;
            }
            else if (count < MAX_COUNT)
            {
            if (!nonZeroDigitSeen)
            {
                nonZeroDigitSeen = (c != '0');
                if (!nonZeroDigitSeen && decimalAt != -1) ++leadingZerosAfterDecimal;
            }

            if (nonZeroDigitSeen) digits[count++] = (byte)c;
            }
        }
        if (decimalAt == -1) decimalAt = count;
        if (nonZeroDigitSeen) {
            decimalAt += exponent - leadingZerosAfterDecimal;
        }

        if (fixedPoint)
        {
            // The negative of the exponent represents the number of leading
            // zeros between the decimal and the first non-zero digit, for
            // a value < 0.1 (e.g., for 0.00123, -decimalAt == 2).  If this
            // is more than the maximum fraction digits, then we have an underflow
            // for the printed representation.
            if (-decimalAt > maximumDigits) {
                // Handle an underflow to zero when we round something like
                // 0.0009 to 2 fractional digits.
                count = 0;
                return;
            } else if (-decimalAt == maximumDigits) {
                // If we round 0.0009 to 3 fractional digits, then we have to
                // create a new one digit in the least significant location.
                if (shouldRoundUp(0)) {
                    count = 1;
                    ++decimalAt;
                    digits[0] = (byte)'1';
                } else {
                    count = 0;
                }
                return;
            }
            // else fall through
        }

        // Eliminate trailing zeros.
        while (count > 1 && digits[count - 1] == '0')
            --count;

        if (DEBUG) {
            System.out.println("Before rounding " + this);
        }

        // Eliminate digits beyond maximum digits to be displayed.
        // Round up if appropriate.
        round(fixedPoint ? (maximumDigits + decimalAt) : maximumDigits);

        if (DEBUG) {
            System.out.println("After rounding " + this);
        }

        // The following method also works, and does not rely on the specific
        // format generated by Double.toString().  However, it introduces significant
        // errors in the least-significant digits, which cause round-trip parse and
        // format operations to fail.  We retain this code for future reference;
        // the compiler will ignore it.
        if (false)
        {
            // Find the exponent for this value.  Our convention is 0.mmmm * 10^decimalAt,
            // so we need to add one.
            decimalAt = log10(source) + 1;

            // Compute the number of digits to generate based on the maximum fraction
            // digits and the exponent.  For example, if the exponent is -95 and the
            // maximum fraction digits is 100, then we'll have 95 leading zeros and only
            // 5 significant digits.

            count = maximumDigits + decimalAt;
            if (count > DBL_DIG) count = DBL_DIG;
            if (count < 0) count = 0;
            if (count == 0) return; // Return if we've underflowed to zero

            // Put the mantissa into a long.  We create a mantissa value in the
            // range 10^n-1 <= mantissa < 10^n, where n is the desired number of
            // digits.  If this is a small number << 1, decimalAt may be negative,
            // indicating leading zeros between the decimal point an digits[0]. A
            // decimalAt value of 0 indicates that the decimal point is before
            // digits[0].

            //System.out.println("d = " + source + " log = " + (Math.log(source) / LOG10));
            //System.out.println("d == 0.1 " + (source == 0.1));
            long mantissa = Math.round(source * Math.pow(10, count - decimalAt));
            String longRep = Long.toString(mantissa);

            // At this point we have a representation of exactly maxDecimalCount
            // characters.
            // FOLLOWING LINE FOR DEBUGGING ONLY.  THIS catches problems with log10 computation.
            if (longRep.length() != count)
            throw new Error("Rep=" + longRep + " rep.length=" + longRep.length() +
                    " exp.len=" + count + " " +
                    "val=" + source + " mant=" + mantissa +
                    " decimalAt=" + decimalAt);

            // Eliminate trailing zeros.
            while (count > 1 && longRep.charAt(count - 1) == '0')
            --count;

            // Copy digits over
            for (int i=0; i<count; ++i)
            digits[i] = (byte)longRep.charAt(i);
        }
    }

    /**
     * Round the representation to the given number of digits.
     * @param maximumDigits The maximum number of digits to be shown.
     * Upon return, count will be less than or equal to maximumDigits.
     */
    private final void round(int maximumDigits)
    {
        // Eliminate digits beyond maximum digits to be displayed.
        // Round up if appropriate.
        if (maximumDigits >= 0 && maximumDigits < count)
        {
            if (shouldRoundUp(maximumDigits)) {
                // Rounding up involved incrementing digits from LSD to MSD.
                // In most cases this is simple, but in a worst case situation
                // (9999..99) we have to adjust the decimalAt value.
                for (;;)
                {
                    --maximumDigits;
                    if (maximumDigits < 0)
                    {
                        // We have all 9's, so we increment to a single digit
                        // of one and adjust the exponent.
                        digits[0] = (byte) '1';
                        ++decimalAt;
                        maximumDigits = 0; // Adjust the count
                        break;
                    }

                    ++digits[maximumDigits];
                    if (digits[maximumDigits] <= '9') break;
                    // digits[maximumDigits] = '0'; // Unnecessary since we'll truncate this
                }
                ++maximumDigits; // Increment for use as count
            }
            count = maximumDigits;

            // Eliminate trailing zeros.
            while (count > 1 && digits[count-1] == '0') {
                --count;
            }
        }
    }


    /**
     * Return true if truncating the representation to the given number
     * of digits will result in an increment to the last digit.  This
     * method implements half-even rounding, the default rounding mode.
     * [bnf]
     * @param maximumDigits the number of digits to keep, from 0 to
     * <code>count-1</code>.  If 0, then all digits are rounded away, and
     * this method returns true if a one should be generated (e.g., formatting
     * 0.09 with "#.#").
     * @return true if digit <code>maximumDigits-1</code> should be
     * incremented
     */
    private boolean shouldRoundUp(int maximumDigits) {
        boolean increment = false;
        // Implement IEEE half-even rounding
        if (maximumDigits < count) {
            if (digits[maximumDigits] > '5') {
                return true;
            } else if (digits[maximumDigits] == '5' ) {
                for (int i=maximumDigits+1; i<count; ++i) {
                    if (digits[i] != '0') {
                        return true;
                    }
                }
                return maximumDigits > 0 && (digits[maximumDigits-1] % 2 != 0);
            }
	}
        return false;
    }

    /**
     * Utility routine to set the value of the digit list from a long
     */
    public final void set(long source)
    {
        set(source, 0);
    }

    /**
     * Set the digit list to a representation of the given long value.
     * @param source Value to be converted; must be >= 0 or ==
     * Long.MIN_VALUE.
     * @param maximumDigits The most digits which should be converted.
     * If maximumDigits is lower than the number of significant digits
     * in source, the representation will be rounded.  Ignored if <= 0.
     */
    public final void set(long source, int maximumDigits)
    {
        // This method does not expect a negative number. However,
        // "source" can be a Long.MIN_VALUE (-9223372036854775808),
        // if the number being formatted is a Long.MIN_VALUE.  In that
        // case, it will be formatted as -Long.MIN_VALUE, a number
        // which is outside the legal range of a long, but which can
        // be represented by DigitList.
        if (source <= 0) {
            if (source == Long.MIN_VALUE) {
                decimalAt = count = MAX_COUNT;
                System.arraycopy(LONG_MIN_REP, 0, digits, 0, count);
            } else {
                decimalAt = count = 0; // Values <= 0 format as zero
            }
        } else {
            // Rewritten to improve performance.  I used to call
            // Long.toString(), which was about 4x slower than this code.
            int left = MAX_COUNT;
            int right;
            while (source > 0) {
                digits[--left] = (byte) ('0' + (source % 10));
                source /= 10;
            }
            decimalAt = MAX_COUNT - left;
            // Don't copy trailing zeros.  We are guaranteed that there is at
            // least one non-zero digit, so we don't have to check lower bounds.
            for (right = MAX_COUNT - 1; digits[right] == '0'; --right) {}
            count = right - left + 1;
            System.arraycopy(digits, left, digits, 0, count);
        }        
        if (maximumDigits > 0) round(maximumDigits);
    }

    /**
     * equality test between two digit lists.
     */
    public boolean equals(Object obj) {
        if (this == obj)                      // quick check
            return true;
        if (!(obj instanceof DigitList))         // (1) same object?
            return false;
        DigitList other = (DigitList) obj;
        if (count != other.count ||
        decimalAt != other.decimalAt)
            return false;
        for (int i = 0; i < count; i++)
            if (digits[i] != other.digits[i])
                return false;
        return true;
    }

    /**
     * Generates the hash code for the digit list.
     */
    public int hashCode() {
        int hashcode = decimalAt;

        for (int i = 0; i < count; i++)
            hashcode = hashcode * 37 + digits[i];

        return hashcode;
    }

    /**
     * Returns true if this DigitList represents Long.MIN_VALUE;
     * false, otherwise.  This is required so that getLong() works.
     */
    private boolean isLongMIN_VALUE()
    {
        if (decimalAt != count || count != MAX_COUNT)
            return false;

            for (int i = 0; i < count; ++i)
        {
            if (digits[i] != LONG_MIN_REP[i]) return false;
        }

        return true;
    }

    private static byte[] LONG_MIN_REP;

    static
    {
        // Store the representation of LONG_MIN without the leading '-'
        String s = Long.toString(Long.MIN_VALUE);
        LONG_MIN_REP = new byte[MAX_COUNT];
        for (int i=0; i < MAX_COUNT; ++i)
        {
            LONG_MIN_REP[i] = (byte)s.charAt(i + 1);
        }
    }

    /**
     * Return the floor of the log base 10 of a given double.
     * This method compensates for inaccuracies which arise naturally when
     * computing logs, and always give the correct value.  The parameter
     * must be positive and finite.
     */
    private static final int log10(double d)
    {
        // The reason this routine is needed is that simply taking the
        // log and dividing by log10 yields a result which may be off
        // by 1 due to rounding errors.  For example, the naive log10
        // of 1.0e300 taken this way is 299, rather than 300.
        double log10 = Math.log(d) / LOG10;
        int ilog10 = (int)Math.floor(log10);
        // Positive logs could be too small, e.g. 0.99 instead of 1.0
        if (log10 > 0 && d >= Math.pow(10, ilog10 + 1))
        {
            ++ilog10;
        }
        // Negative logs could be too big, e.g. -0.99 instead of -1.0
        else if (log10 < 0 && d < Math.pow(10, ilog10))
        {
            --ilog10;
        }
        return ilog10;
    }

    private static final double LOG10 = Math.log(10.0);

    public String toString()
    {
        if (isZero()) return "0";
        StringBuffer buf = new StringBuffer("0.");
        for (int i=0; i<count; ++i) buf.append((char)digits[i]);
        buf.append("x10^");
        buf.append(decimalAt);
        return buf.toString();
    }
}
