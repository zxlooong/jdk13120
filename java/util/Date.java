/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.lang.ref.SoftReference;

/**
 * The class <code>Date</code> represents a specific instant
 * in time, with millisecond precision.
 * <p>
 * Prior to JDK&nbsp;1.1, the class <code>Date</code> had two additional
 * functions.  It allowed the interpretation of dates as year, month, day, hour,
 * minute, and second values.  It also allowed the formatting and parsing
 * of date strings.  Unfortunately, the API for these functions was not
 * amenable to internationalization.  As of JDK&nbsp;1.1, the
 * <code>Calendar</code> class should be used to convert between dates and time
 * fields and the <code>DateFormat</code> class should be used to format and
 * parse date strings.
 * The corresponding methods in <code>Date</code> are deprecated.
 * <p>
 * Although the <code>Date</code> class is intended to reflect 
 * coordinated universal time (UTC), it may not do so exactly, 
 * depending on the host environment of the Java Virtual Machine. 
 * Nearly all modern operating systems assume that 1&nbsp;day&nbsp;=
 * 24&nbsp;&times;&nbsp;60&nbsp;&times;&nbsp;60&nbsp;= 86400 seconds 
 * in all cases. In UTC, however, about once every year or two there 
 * is an extra second, called a "leap second." The leap 
 * second is always added as the last second of the day, and always 
 * on December 31 or June 30. For example, the last minute of the 
 * year 1995 was 61 seconds long, thanks to an added leap second. 
 * Most computer clocks are not accurate enough to be able to reflect 
 * the leap-second distinction. 
 * <p>
 * Some computer standards are defined in terms of Greenwich mean 
 * time (GMT), which is equivalent to universal time (UT).  GMT is 
 * the "civil" name for the standard; UT is the 
 * "scientific" name for the same standard. The 
 * distinction between UTC and UT is that UTC is based on an atomic 
 * clock and UT is based on astronomical observations, which for all 
 * practical purposes is an invisibly fine hair to split. Because the 
 * earth's rotation is not uniform (it slows down and speeds up 
 * in complicated ways), UT does not always flow uniformly. Leap 
 * seconds are introduced as needed into UTC so as to keep UTC within 
 * 0.9 seconds of UT1, which is a version of UT with certain 
 * corrections applied. There are other time and date systems as 
 * well; for example, the time scale used by the satellite-based 
 * global positioning system (GPS) is synchronized to UTC but is 
 * <i>not</i> adjusted for leap seconds. An interesting source of 
 * further information is the U.S. Naval Observatory, particularly 
 * the Directorate of Time at:
 * <blockquote><pre>
 *     <a href=http://tycho.usno.navy.mil>http://tycho.usno.navy.mil</a>
 * </pre></blockquote>
 * <p>
 * and their definitions of "Systems of Time" at:
 * <blockquote><pre>
 *     <a href=http://tycho.usno.navy.mil/systime.html>http://tycho.usno.navy.mil/systime.html</a>
 * </pre></blockquote>
 * <p>
 * In all methods of class <code>Date</code> that accept or return 
 * year, month, date, hours, minutes, and seconds values, the 
 * following representations are used: 
 * <ul>
 * <li>A year <i>y</i> is represented by the integer 
 *     <i>y</i>&nbsp;<code>-&nbsp;1900</code>. 
 * <li>A month is represented by an integer form 0 to 11; 0 is January, 
 *     1 is February, and so forth; thus 11 is December. 
 * <li>A date (day of month) is represented by an integer from 1 to 31 
 *     in the usual manner. 
 * <li>An hour is represented by an integer from 0 to 23. Thus, the hour 
 *     from midnight to 1 a.m. is hour 0, and the hour from noon to 1 
 *     p.m. is hour 12. 
 * <li>A minute is represented by an integer from 0 to 59 in the usual manner.
 * <li>A second is represented by an integer from 0 to 61; the values 60 and 
 *     61 occur only for leap seconds and even then only in Java 
 *     implementations that actually track leap seconds correctly. Because 
 *     of the manner in which leap seconds are currently introduced, it is 
 *     extremely unlikely that two leap seconds will occur in the same 
 *     minute, but this specification follows the date and time conventions 
 *     for ISO C.
 * </ul>
 * <p>
 * In all cases, arguments given to methods for these purposes need 
 * not fall within the indicated ranges; for example, a date may be 
 * specified as January 32 and is interpreted as meaning February 1.
 *
 * @author  James Gosling
 * @author  Arthur van Hoff
 * @author  Alan Liu
 * @version 1.70, 02/06/02
 * @see     java.text.DateFormat
 * @see     java.util.Calendar
 * @see     java.util.TimeZone
 * @since   JDK1.0
 */
public class Date implements java.io.Serializable, Cloneable, Comparable {
    /* DEFAULT ZONE SYNCHRONIZATION: Part of the usage model of Date
     * is that a Date object behaves like a Calendar object whose zone
     * is the current default TimeZone.  As a result, we must be
     * careful about keeping this phantom calendar in sync with the
     * default TimeZone.  There are three class and instance variables
     * to watch out for to achieve this.  (1)staticCal. Whenever this
     * object is used, it must be reset to the default zone. This is a
     * cheap operation which can be done directly (just a reference
     * assignment), so we just do it every time. (2)simpleFormatter.
     * Likewise, the DateFormat object we use to implement toString()
     * must be reset to the current default zone before use.  Again,
     * this is a cheap reference assignment. (3)cal. This is a little
     * more tricky.  Unlike the other cached static objects, cal has
     * state, and we don't want to monkey with it willy-nilly.  The
     * correct procedure is to change the zone in a way that doesn't
     * alter the time of this object.  This means getting the millis
     * (forcing a fields->time conversion), setting the zone, and then
     * restoring the millis.  The zone must be set before restoring
     * the millis.  Since this is an expensive operation, we only do
     * this when we have to. - liu 1.2b4 */

    /* If cal is null, then fastTime indicates the time in millis.
     * Otherwise, fastTime is ignored, and cal indicates the time.
     * The cal object is only created if a setXxx call is made to
     * set a field.  For other operations, staticCal is used instead.
     */
    private transient Calendar cal;
    private transient long fastTime;

    private static Calendar staticCal = null;
    private static Calendar utcCal = null;
    private static int defaultCenturyStart = 0;

    /* use serialVersionUID from modified java.util.Date for
     * interoperability with JDK1.1. The Date was modified to write
     * and read only the UTC time.
     */
    private static final long serialVersionUID = 7523967970034938905L;

    /**
     * Caches for the DateFormatters used by various toString methods.
     */
    private static SoftReference simpleFormatter = null;
    private static SoftReference gmtFormatter = null;
    
    /**
     * Allocates a <code>Date</code> object and initializes it so that 
     * it represents the time at which it was allocated, measured to the 
     * nearest millisecond. 
     *
     * @see     java.lang.System#currentTimeMillis()
     */
    public Date() {
        this(System.currentTimeMillis());
    }

    /**
     * Allocates a <code>Date</code> object and initializes it to 
     * represent the specified number of milliseconds since the 
     * standard base time known as "the epoch", namely January 1, 
     * 1970, 00:00:00 GMT. 
     *
     * @param   date   the milliseconds since January 1, 1970, 00:00:00 GMT.
     * @see     java.lang.System#currentTimeMillis()
     */
    public Date(long date) {
        cal = null;
        fastTime = date;
    }

    /**
     * Allocates a <code>Date</code> object and initializes it so that 
     * it represents midnight, local time, at the beginning of the day 
     * specified by the <code>year</code>, <code>month</code>, and 
     * <code>date</code> arguments. 
     *
     * @param   year    the year minus 1900.
     * @param   month   the month between 0-11.
     * @param   date    the day of the month between 1-31.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(year + 1900, month, date)</code>
     * or <code>GregorianCalendar(year + 1900, month, date)</code>.
     */
    public Date(int year, int month, int date) {
        this(year, month, date, 0, 0, 0);
    }

    /**
     * Allocates a <code>Date</code> object and initializes it so that 
     * it represents the instant at the start of the minute specified by 
     * the <code>year</code>, <code>month</code>, <code>date</code>, 
     * <code>hrs</code>, and <code>min</code> arguments, in the local 
     * time zone. 
     *
     * @param   year    the year minus 1900.
     * @param   month   the month between 0-11.
     * @param   date    the day of the month between 1-31.
     * @param   hrs     the hours between 0-23.
     * @param   min     the minutes between 0-59.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(year + 1900, month, date,
     * hrs, min)</code> or <code>GregorianCalendar(year + 1900,
     * month, date, hrs, min)</code>.
     */
    public Date(int year, int month, int date, int hrs, int min) {
        this(year, month, date, hrs, min, 0);
    }

    /**
     * Allocates a <code>Date</code> object and initializes it so that 
     * it represents the instant at the start of the second specified 
     * by the <code>year</code>, <code>month</code>, <code>date</code>, 
     * <code>hrs</code>, <code>min</code>, and <code>sec</code> arguments, 
     * in the local time zone. 
     *
     * @param   year    the year minus 1900.
     * @param   month   the month between 0-11.
     * @param   date    the day of the month between 1-31.
     * @param   hrs     the hours between 0-23.
     * @param   min     the minutes between 0-59.
     * @param   sec     the seconds between 0-59.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(year + 1900, month, date,
     * hrs, min, sec)</code> or <code>GregorianCalendar(year + 1900,
     * month, date, hrs, min, sec)</code>.
     */
    public Date(int year, int month, int date, int hrs, int min, int sec) {
        cal = null;
        if (staticCal == null)
            makeStaticCalendars();
        synchronized (staticCal) {
            staticCal.setTimeZone(TimeZone.getDefault());
            staticCal.clear();
            staticCal.set(year + 1900, month, date, hrs, min, sec);
            fastTime = staticCal.getTimeInMillis();
        }
    }

    /**
     * Allocates a <code>Date</code> object and initializes it so that 
     * it represents the date and time indicated by the string 
     * <code>s</code>, which is interpreted as if by the 
     * {@link Date#parse} method. 
     *
     * @param   s   a string representation of the date.
     * @see     java.text.DateFormat
     * @see     java.util.Date#parse(java.lang.String)
     * @deprecated As of JDK version 1.1,
     * replaced by <code>DateFormat.parse(String s)</code>.
     */
    public Date(String s) {
        this(parse(s));
    }

    /**
     * Return a copy of this object.
     */
    public Object clone() {
        Date d = null;
        try {
            d = (Date)super.clone();
            if (d.cal != null) d.cal = (Calendar)d.cal.clone();
        } catch (CloneNotSupportedException e) {} // Won't happen
        return d;
    }
    
    /**
     * Determines the date and time based on the arguments. The 
     * arguments are interpreted as a year, month, day of the month, 
     * hour of the day, minute within the hour, and second within the 
     * minute, exactly as for the <tt>Date</tt> constructor with six 
     * arguments, except that the arguments are interpreted relative 
     * to UTC rather than to the local time zone. The time indecated is 
     * returned represented as the distance, measured in milliseconds, 
     * of that time from the epoch (00:00:00 GMT on January 1, 1970).
     *
     * @param   year    the year minus 1900.
     * @param   month   the month between 0-11.
     * @param   date    the day of the month between 1-31.
     * @param   hrs     the hours between 0-23.
     * @param   min     the minutes between 0-59.
     * @param   sec     the seconds between 0-59.
     * @return  the number of milliseconds since January 1, 1970, 00:00:00 GMT for
     *          the date and time specified by the arguments. 
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(year + 1900, month, date,
     * hrs, min, sec)</code> or <code>GregorianCalendar(year + 1900,
     * month, date, hrs, min, sec)</code>, using a UTC
     * <code>TimeZone</code>, followed by <code>Calendar.getTime().getTime()</code>.
     */
    public static long UTC(int year, int month, int date,
                           int hrs, int min, int sec) {
        if (utcCal == null)
            makeStaticCalendars();
        synchronized (utcCal) {
            utcCal.clear();
            utcCal.set(year + 1900, month, date, hrs, min, sec);
            return utcCal.getTimeInMillis();
        }
    }

    /**
     * Attempts to interpret the string <tt>s</tt> as a representation 
     * of a date and time. If the attempt is successful, the time 
     * indicated is returned represented as the distance, measured in 
     * milliseconds, of that time from the epoch (00:00:00 GMT on 
     * January 1, 1970). If the attempt fails, an 
     * <tt>IllegalArgumentException</tt> is thrown.
     * <p>
     * It accepts many syntaxes; in particular, it recognizes the IETF 
     * standard date syntax: "Sat, 12 Aug 1995 13:30:00 GMT". It also 
     * understands the continental U.S. time-zone abbreviations, but for 
     * general use, a time-zone offset should be used: "Sat, 12 Aug 1995 
     * 13:30:00 GMT+0430" (4 hours, 30 minutes west of the Greenwich 
     * meridian). If no time zone is specified, the local time zone is 
     * assumed. GMT and UTC are considered equivalent.
     * <p>
     * The string <tt>s</tt> is processed from left to right, looking for 
     * data of interest. Any material in <tt>s</tt> that is within the 
     * ASCII parenthesis characters <tt>(</tt> and <tt>)</tt> is ignored. 
     * Parentheses may be nested. Otherwise, the only characters permitted 
     * within <tt>s</tt> are these ASCII characters:
     * <blockquote><pre>
     * abcdefghijklmnopqrstuvwxyz
     * ABCDEFGHIJKLMNOPQRSTUVWXYZ
     * 0123456789,+-:/</pre></blockquote>
     * and whitespace characters.<p>
     * A consecutive sequence of decimal digits is treated as a decimal 
     * number:<ul>
     * <li>If a number is preceded by <tt>+</tt> or <tt>-</tt> and a year 
     *     has already been recognized, then the number is a time-zone 
     *     offset. If the number is less than 24, it is an offset measured 
     *     in hours. Otherwise, it is regarded as an offset in minutes, 
     *     expressed in 24-hour time format without punctuation. A 
     *     preceding <tt>-</tt> means a westward offset. Time zone offsets 
     *     are always relative to UTC (Greenwich). Thus, for example, 
     *     <tt>-5</tt> occurring in the string would mean "five hours west 
     *     of Greenwich" and <tt>+0430</tt> would mean "four hours and 
     *     thirty minutes east of Greenwich." It is permitted for the 
     *     string to specify <tt>GMT</tt>, <tt>UT</tt>, or <tt>UTC</tt> 
     *     redundantly-for example, <tt>GMT-5</tt> or <tt>utc+0430</tt>.
     * <li>The number is regarded as a year number if one of the
     *     following conditions is true:
     * <ul>
     *     <li>The number is equal to or greater than 70 and followed by a
     *         space, comma, slash, or end of string
     *     <li>The number is less than 70, and both a month and a day of
     *         the month have already been recognized</li>
     * </ul>
     *     If the recognized year number is less than 100, it is
     *     interpreted as an abbreviated year relative to a century of
     *     which dates are within 80 years before and 19 years after
     *     the time when the Date class is initialized.
     *     After adjusting the year number, 1900 is subtracted from
     *     it. For example, if the current year is 1999 then years in
     *     the range 19 to 99 are assumed to mean 1919 to 1999, while
     *     years from 0 to 18 are assumed to mean 2000 to 2018.  Note
     *     that this is slightly different from the interpretation of
     *     years less than 100 that is used in {@link java.text.SimpleDateFormat}.
     * <li>If the number is followed by a colon, it is regarded as an hour, 
     *     unless an hour has already been recognized, in which case it is 
     *     regarded as a minute.
     * <li>If the number is followed by a slash, it is regarded as a month 
     *     (it is decreased by 1 to produce a number in the range <tt>0</tt> 
     *     to <tt>11</tt>), unless a month has already been recognized, in 
     *     which case it is regarded as a day of the month.
     * <li>If the number is followed by whitespace, a comma, a hyphen, or 
     *     end of string, then if an hour has been recognized but not a 
     *     minute, it is regarded as a minute; otherwise, if a minute has 
     *     been recognized but not a second, it is regarded as a second; 
     *     otherwise, it is regarded as a day of the month. </ul><p>
     * A consecutive sequence of letters is regarded as a word and treated 
     * as follows:<ul>
     * <li>A word that matches <tt>AM</tt>, ignoring case, is ignored (but 
     *     the parse fails if an hour has not been recognized or is less 
     *     than <tt>1</tt> or greater than <tt>12</tt>).
     * <li>A word that matches <tt>PM</tt>, ignoring case, adds <tt>12</tt> 
     *     to the hour (but the parse fails if an hour has not been 
     *     recognized or is less than <tt>1</tt> or greater than <tt>12</tt>).
     * <li>Any word that matches any prefix of <tt>SUNDAY, MONDAY, TUESDAY, 
     *     WEDNESDAY, THURSDAY, FRIDAY</tt>, or <tt>SATURDAY</tt>, ignoring 
     *     case, is ignored. For example, <tt>sat, Friday, TUE</tt>, and 
     *     <tt>Thurs</tt> are ignored.
     * <li>Otherwise, any word that matches any prefix of <tt>JANUARY, 
     *     FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, 
     *     OCTOBER, NOVEMBER</tt>, or <tt>DECEMBER</tt>, ignoring case, and 
     *     considering them in the order given here, is recognized as
     *     specifying a month and is converted to a number (<tt>0</tt> to 
     *     <tt>11</tt>). For example, <tt>aug, Sept, april</tt>, and 
     *     <tt>NOV</tt> are recognized as months. So is <tt>Ma</tt>, which 
     *     is recognized as <tt>MARCH</tt>, not <tt>MAY</tt>.
     * <li>Any word that matches <tt>GMT, UT</tt>, or <tt>UTC</tt>, ignoring 
     *     case, is treated as referring to UTC. 
     * <li>Any word that matches <tt>EST, CST, MST</tt>, or <tt>PST</tt>, 
     *     ignoring case, is recognized as referring to the time zone in 
     *     North America that is five, six, seven, or eight hours west of 
     *     Greenwich, respectively. Any word that matches <tt>EDT, CDT, 
     *     MDT</tt>, or <tt>PDT</tt>, ignoring case, is recognized as 
     *     referring to the same time zone, respectively, during daylight 
     *     saving time.</ul><p>
     * Once the entire string s has been scanned, it is converted to a time 
     * result in one of two ways. If a time zone or time-zone offset has been 
     * recognized, then the year, month, day of month, hour, minute, and 
     * second are interpreted in UTC and then the time-zone offset is 
     * applied. Otherwise, the year, month, day of month, hour, minute, and 
     * second are interpreted in the local time zone.
     *
     * @param   s   a string to be parsed as a date.
     * @return  the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *          represented by the string argument.
     * @see     java.text.DateFormat
     * @deprecated As of JDK version 1.1,
     * replaced by <code>DateFormat.parse(String s)</code>.
     */
    public static long parse(String s) {
        if (staticCal == null)
            makeStaticCalendars(); // Called only for side-effect of setting defaultCenturyStart
        
        int year = Integer.MIN_VALUE;
        int mon = -1;
        int mday = -1;
        int hour = -1;
        int min = -1;
        int sec = -1;
        int millis = -1;
        int c = -1;
        int i = 0;
        int n = -1;
        int wst = -1;
        int tzoffset = -1;
        int prevc = 0;
    syntax:
        {
            if (s == null)
                break syntax;
            int limit = s.length();
            while (i < limit) {
                c = s.charAt(i);
                i++;
                if (c <= ' ' || c == ',')
                    continue;
                if (c == '(') { // skip comments
                    int depth = 1;
                    while (i < limit) {
                        c = s.charAt(i);
                        i++;
                        if (c == '(') depth++;
                        else if (c == ')')
                            if (--depth <= 0)
                                break;
                    }
                    continue;
                }
                if ('0' <= c && c <= '9') {
                    n = c - '0';
                    while (i < limit && '0' <= (c = s.charAt(i)) && c <= '9') {
                        n = n * 10 + c - '0';
                        i++;
                    }
                    if (prevc == '+' || prevc == '-' && year != Integer.MIN_VALUE) {
                        // timezone offset
                        if (n < 24)
                            n = n * 60; // EG. "GMT-3"
                        else
                            n = n % 100 + n / 100 * 60; // eg "GMT-0430"
                        if (prevc == '+')   // plus means east of GMT
                            n = -n;
                        if (tzoffset != 0 && tzoffset != -1)
                            break syntax;
                        tzoffset = n;
                    } else if (n >= 70)
                        if (year != Integer.MIN_VALUE)
                            break syntax;
                        else if (c <= ' ' || c == ',' || c == '/' || i >= limit)
                            // year = n < 1900 ? n : n - 1900;
                            year = n;
                        else
                            break syntax;
                    else if (c == ':')
                        if (hour < 0)
                            hour = (byte) n;
                        else if (min < 0)
                            min = (byte) n;
                        else
                            break syntax;
                    else if (c == '/')
                        if (mon < 0)
                            mon = (byte) (n - 1);
                        else if (mday < 0)
                            mday = (byte) n;
                        else
                            break syntax;
                    else if (i < limit && c != ',' && c > ' ' && c != '-')
                        break syntax;
                    else if (hour >= 0 && min < 0)
                        min = (byte) n;
                    else if (min >= 0 && sec < 0)
                        sec = (byte) n;
                    else if (mday < 0)
                        mday = (byte) n;
                    // Handle two-digit years < 70 (70-99 handled above).
                    else if (year == Integer.MIN_VALUE && mon >= 0 && mday >= 0)
                        year = n;
                    else
                        break syntax;
                    prevc = 0;
                } else if (c == '/' || c == ':' || c == '+' || c == '-')
                    prevc = c;
                else {
                    int st = i - 1;
                    while (i < limit) {
                        c = s.charAt(i);
                        if (!('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z'))
                            break;
                        i++;
                    }
                    if (i <= st + 1)
                        break syntax;
                    int k;
                    for (k = wtb.length; --k >= 0;)
                        if (wtb[k].regionMatches(true, 0, s, st, i - st)) {
                            int action = ttb[k];
                            if (action != 0) {
                                if (action == 1) {  // pm
                                    if (hour > 12 || hour < 1)
                                        break syntax;
                                    else if (hour < 12)
                                        hour += 12;
                                } else if (action == 14) {  // am
                                    if (hour > 12 || hour < 1)
                                        break syntax;
                                    else if (hour == 12)
                                        hour = 0;
                                } else if (action <= 13) {  // month!
                                    if (mon < 0)
                                        mon = (byte) (action - 2);
                                    else
                                        break syntax;
                                } else {
                                    tzoffset = action - 10000;
                                }
                            }
                            break;
                        }
                    if (k < 0)
                        break syntax;
                    prevc = 0;
                }
            }
            if (year == Integer.MIN_VALUE || mon < 0 || mday < 0)
                break syntax;
            // Parse 2-digit years within the correct default century.
            if (year < 100) {
                year += (defaultCenturyStart / 100) * 100;
                if (year < defaultCenturyStart) year += 100;
            }
            year -= 1900;
            if (sec < 0)
                sec = 0;
            if (min < 0)
                min = 0;
            if (hour < 0)
                hour = 0;
            if (tzoffset == -1) // no time zone specified, have to use local
                return new Date (year, mon, mday, hour, min, sec).getTime();
            return UTC(year, mon, mday, hour, min, sec) + tzoffset * (60 * 1000);
        }
        // syntax error
        throw new IllegalArgumentException();
    }
    private final static String wtb[] = {
        "am", "pm",
        "monday", "tuesday", "wednesday", "thursday", "friday",
        "saturday", "sunday",
        "january", "february", "march", "april", "may", "june",
        "july", "august", "september", "october", "november", "december",
        "gmt", "ut", "utc", "est", "edt", "cst", "cdt",
        "mst", "mdt", "pst", "pdt"
        // this time zone table needs to be expanded
    };
    private final static int ttb[] = {
        14, 1, 0, 0, 0, 0, 0, 0, 0,
        2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
        10000 + 0, 10000 + 0, 10000 + 0,    // GMT/UT/UTC
        10000 + 5 * 60, 10000 + 4 * 60, // EST/EDT
        10000 + 6 * 60, 10000 + 5 * 60,
        10000 + 7 * 60, 10000 + 6 * 60,
        10000 + 8 * 60, 10000 + 7 * 60
    };

    /**
     * Returns a value that is the result of subtracting 1900 from the 
     * year that contains or begins with the instant in time represented 
     * by this <code>Date</code> object, as interpreted in the local 
     * time zone.
     *
     * @return  the year represented by this date, minus 1900.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.get(Calendar.YEAR) - 1900</code>.
     */
    public int getYear() {
        return getField(Calendar.YEAR) - 1900;
    }

    /**
     * Sets the year of this <tt>Date</tt> object to be the specified 
     * value plus 1900. This <code>Date</code> object is modified so 
     * that it represents a point in time within the specified year, 
     * with the month, date, hour, minute, and second the same as 
     * before, as interpreted in the local time zone. (Of course, if 
     * the date was February 29, for example, and the year is set to a 
     * non-leap year, then the new date will be treated as if it were 
     * on March 1.)
     *
     * @param   year    the year value.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.YEAR, year + 1900)</code>.
     */
    public void setYear(int year) {
        setField(Calendar.YEAR, year + 1900);
    }

    /**
     * Returns a number representing the month that contains or begins 
     * with the instant in time represented by this <tt>Date</tt> object. 
     * The value returned is between <code>0</code> and <code>11</code>, 
     * with the value <code>0</code> representing January.
     *
     * @return  the month represented by this date.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.get(Calendar.MONTH)</code>.
     */
    public int getMonth() {
        return getField(Calendar.MONTH);
    }

    /**
     * Sets the month of this date to the specified value. This 
     * <tt>Date</tt> object is modified so that it represents a point 
     * in time within the specified month, with the year, date, hour, 
     * minute, and second the same as before, as interpreted in the 
     * local time zone. If the date was October 31, for example, and 
     * the month is set to June, then the new date will be treated as 
     * if it were on July 1, because June has only 30 days.
     *
     * @param   month   the month value between 0-11.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.MONTH, int month)</code>.
     */
    public void setMonth(int month) {
        setField(Calendar.MONTH, month);
    }

    /**
     * Returns the day of the month represented by this <tt>Date</tt> object. 
     * The value returned is between <code>1</code> and <code>31</code> 
     * representing the day of the month that contains or begins with the 
     * instant in time represented by this <tt>Date</tt> object, as 
     * interpreted in the local time zone.
     *
     * @return  the day of the month represented by this date.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.get(Calendar.DAY_OF_MONTH)</code>.
     * @deprecated
     */
    public int getDate() {
        return getField(Calendar.DATE);
    }

    /**
     * Sets the day of the month of this <tt>Date</tt> object to the 
     * specified value. This <tt>Date</tt> object is modified so that 
     * it represents a point in time within the specified day of the 
     * month, with the year, month, hour, minute, and second the same 
     * as before, as interpreted in the local time zone. If the date 
     * was April 30, for example, and the date is set to 31, then it 
     * will be treated as if it were on May 1, because April has only 
     * 30 days.
     *
     * @param   date   the day of the month value between 1-31.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.DAY_OF_MONTH, int date)</code>.
     */
    public void setDate(int date) {
        setField(Calendar.DATE, date);
    }

    /**
     * Returns the day of the week represented by this date. The 
     * returned value (<tt>0</tt> = Sunday, <tt>1</tt> = Monday, 
     * <tt>2</tt> = Tuesday, <tt>3</tt> = Wednesday, <tt>4</tt> = 
     * Thursday, <tt>5</tt> = Friday, <tt>6</tt> = Saturday) 
     * represents the day of the week that contains or begins with 
     * the instant in time represented by this <tt>Date</tt> object, 
     * as interpreted in the local time zone.
     *
     * @return  the day of the week represented by this date.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.get(Calendar.DAY_OF_WEEK)</code>.
     */
    public int getDay() {
        return getField(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
    }

    /**
     * Returns the hour represented by this <tt>Date</tt> object. The 
     * returned value is a number (<tt>0</tt> through <tt>23</tt>) 
     * representing the hour within the day that contains or begins 
     * with the instant in time represented by this <tt>Date</tt> 
     * object, as interpreted in the local time zone.
     *
     * @return  the hour represented by this date.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.get(Calendar.HOUR_OF_DAY)</code>.
     */
    public int getHours() {
        return getField(Calendar.HOUR_OF_DAY);
    }

    /**
     * Sets the hour of this <tt>Date</tt> object to the specified value. 
     * This <tt>Date</tt> object is modified so that it represents a point 
     * in time within the specified hour of the day, with the year, month, 
     * date, minute, and second the same as before, as interpreted in the 
     * local time zone.
     *
     * @param   hours   the hour value.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.HOUR_OF_DAY, int hours)</code>.
     */
    public void setHours(int hours) {
        setField(Calendar.HOUR_OF_DAY, hours);
    }

    /**
     * Returns the number of minutes past the hour represented by this date, 
     * as interpreted in the local time zone. 
     * The value returned is between <code>0</code> and <code>59</code>.
     *
     * @return  the number of minutes past the hour represented by this date.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.get(Calendar.MINUTE)</code>.
     */
    public int getMinutes() {
        return getField(Calendar.MINUTE);
    }

    /**
     * Sets the minutes of this <tt>Date</tt> object to the specified value. 
     * This <tt>Date</tt> object is modified so that it represents a point 
     * in time within the specified minute of the hour, with the year, month, 
     * date, hour, and second the same as before, as interpreted in the 
     * local time zone.
     *
     * @param   minutes   the value of the minutes.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.MINUTE, int minutes)</code>.
     */
    public void setMinutes(int minutes) {
        setField(Calendar.MINUTE, minutes);
    }

    /**
     * Returns the number of seconds past the minute represented by this date.
     * The value returned is between <code>0</code> and <code>61</code>. The
     * values <code>60</code> and <code>61</code> can only occur on those 
     * Java Virtual Machines that take leap seconds into account.
     *
     * @return  the number of seconds past the minute represented by this date.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.get(Calendar.SECOND)</code>.
     */
    public int getSeconds() {
        return getField(Calendar.SECOND);
    }

    /**
     * Sets the seconds of this <tt>Date</tt> to the specified value. 
     * This <tt>Date</tt> object is modified so that it represents a 
     * point in time within the specified second of the minute, with 
     * the year, month, date, hour, and minute the same as before, as 
     * interpreted in the local time zone.
     *
     * @param   seconds   the seconds value.
     * @see     java.util.Calendar
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.SECOND, int seconds)</code>. 
     */
    public void setSeconds(int seconds) {
        setField(Calendar.SECOND, seconds);
    }

    /**
     * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * represented by this <tt>Date</tt> object.
     *
     * @return  the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *          represented by this date.
     */
    public long getTime() {
        return (cal == null) ? fastTime : cal.getTimeInMillis();
    }

    /**
     * Sets this <tt>Date</tt> object to represent a point in time that is 
     * <tt>time</tt> milliseconds after January 1, 1970 00:00:00 GMT. 
     *
     * @param   time   the number of milliseconds.
     */
    public void setTime(long time) {
        if (cal == null) {
            fastTime = time;
        }
        else {
            cal.setTimeInMillis(time);
        }
    }

    /**
     * Tests if this date is before the specified date.
     *
     * @param   when   a date.
     * @return  <code>true</code> if and only if the instant of time 
     *            represented by this <tt>Date</tt> object is strictly 
     *            earlier than the instant represented by <tt>when</tt>;
     *          <code>false</code> otherwise.
     */
    public boolean before(Date when) {
        return getTime() < when.getTime();
    }

    /**
     * Tests if this date is after the specified date.
     *
     * @param   when   a date.
     * @return  <code>true</code> if and only if the instant represented 
     *          by this <tt>Date</tt> object is strictly later than the 
     *          instant represented by <tt>when</tt>; 
     *          <code>false</code> otherwise.
     */
    public boolean after(Date when) {
        return getTime() > when.getTime();
    }

    /**
     * Compares two dates for equality.
     * The result is <code>true</code> if and only if the argument is 
     * not <code>null</code> and is a <code>Date</code> object that 
     * represents the same point in time, to the millisecond, as this object.
     * <p>
     * Thus, two <code>Date</code> objects are equal if and only if the 
     * <code>getTime</code> method returns the same <code>long</code> 
     * value for both. 
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     * @see     java.util.Date#getTime()
     */
    public boolean equals(Object obj) {
        return obj instanceof Date && getTime() == ((Date) obj).getTime();
    }

    /**
     * Compares two Dates for ordering.
     *
     * @param   anotherDate   the <code>Date</code> to be compared.
     * @return  the value <code>0</code> if the argument Date is equal to
     *          this Date; a value less than <code>0</code> if this Date
     *          is before the Date argument; and a value greater than
     *      <code>0</code> if this Date is after the Date argument.
     * @since   1.2
     */
    public int compareTo(Date anotherDate) {
    long thisTime = this.getTime();
    long anotherTime = anotherDate.getTime();
    return (thisTime<anotherTime ? -1 : (thisTime==anotherTime ? 0 : 1));
    }

    /**
     * Compares this Date to another Object.  If the Object is a Date,
     * this function behaves like <code>compareTo(Date)</code>.  Otherwise,
     * it throws a <code>ClassCastException</code> (as Dates are comparable
     * only to other Dates).
     *
     * @param   o the <code>Object</code> to be compared.
     * @return  the value <code>0</code> if the argument is a Date
     *      equal to this Date; a value less than <code>0</code> if the
     *      argument is a Date after this Date; and a value greater than
     *      <code>0</code> if the argument is a Date before this Date.
     * @exception ClassCastException if the argument is not a
     *        <code>Date</code>. 
     * @see     java.lang.Comparable
     * @since   1.2
     */
    public int compareTo(Object o) {
    return compareTo((Date)o);
    }

    /**
     * Returns a hash code value for this object. The result is the 
     * exclusive OR of the two halves of the primitive <tt>long</tt> 
     * value returned by the {@link Date#getTime} 
     * method. That is, the hash code is the value of the expression:
     * <blockquote><pre>
     * (int)(this.getTime()^(this.getTime() >>> 32))</pre></blockquote>
     *
     * @return  a hash code value for this object. 
     */
    public int hashCode() {
        long ht = getTime();
        return (int) ht ^ (int) (ht >> 32);
    }

    /**
     * Converts this <code>Date</code> object to a <code>String</code> 
     * of the form:
     * <blockquote><pre>
     * dow mon dd hh:mm:ss zzz yyyy</pre></blockquote>
     * where:<ul>
     * <li><tt>dow</tt> is the day of the week (<tt>Sun, Mon, Tue, Wed, 
     *     Thu, Fri, Sat</tt>).
     * <li><tt>mon</tt> is the month (<tt>Jan, Feb, Mar, Apr, May, Jun, 
     *     Jul, Aug, Sep, Oct, Nov, Dec</tt>).
     * <li><tt>dd</tt> is the day of the month (<tt>01</tt> through 
     *     <tt>31</tt>), as two decimal digits.
     * <li><tt>hh</tt> is the hour of the day (<tt>00</tt> through 
     *     <tt>23</tt>), as two decimal digits.
     * <li><tt>mm</tt> is the minute within the hour (<tt>00</tt> through 
     *     <tt>59</tt>), as two decimal digits.
     * <li><tt>ss</tt> is the second within the minute (<tt>00</tt> through 
     *     <tt>61</tt>, as two decimal digits.
     * <li><tt>zzz</tt> is the time zone (and may reflect daylight savings 
     *     time). Standard time zone abbreviations include those 
     *     recognized by the method <tt>parse</tt>. If time zone 
     *     informationi is not available, then <tt>zzz</tt> is empty - 
     *     that is, it consists of no characters at all.
     * <li><tt>yyyy</tt> is the year, as four decimal digits.
     * </ul>
     *
     * @return  a string representation of this date. 
     * @see     java.util.Date#toLocaleString()
     * @see     java.util.Date#toGMTString()
     */
    public String toString() {
	DateFormat formatter = null;
	if (simpleFormatter != null) {
	    formatter = (DateFormat)simpleFormatter.get();
	}
	if (formatter == null) {
	    /* No cache yet, or cached formatter GC'd */
	    formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
					     Locale.US);
	    simpleFormatter = new SoftReference(formatter);
	}
        synchronized (formatter) {
            formatter.setTimeZone(TimeZone.getDefault());
            return formatter.format(this);
        }
    }

    /**
     * Creates a string representation of this <tt>Date</tt> object in an 
     * implementation-dependent form. The intent is that the form should 
     * be familiar to the user of the Java application, wherever it may 
     * happen to be running. The intent is comparable to that of the 
     * "<code>%c</code>" format supported by the <code>strftime()</code> 
     * function of ISO&nbsp;C. 
     *
     * @return  a string representation of this date, using the locale
     *          conventions.
     * @see     java.text.DateFormat
     * @see     java.util.Date#toString()
     * @see     java.util.Date#toGMTString()
     * @deprecated As of JDK version 1.1,
     * replaced by <code>DateFormat.format(Date date)</code>.
     */
    public String toLocaleString() {
    DateFormat formatter = DateFormat.getDateTimeInstance();
    return formatter.format(this);
    }

    /**
     * Creates a string representation of this <tt>Date</tt> object of 
     * the form:
     * <blockquote<pre>
     * d mon yyyy hh:mm:ss GMT</pre></blockquote>
     * where:<ul>
     * <li><i>d</i> is the day of the month (<tt>1</tt> through <tt>31</tt>), 
     *     as one or two decimal digits.
     * <li><i>mon</i> is the month (<tt>Jan, Feb, Mar, Apr, May, Jun, Jul, 
     *     Aug, Sep, Oct, Nov, Dec</tt>).
     * <li><i>yyyy</i> i sthe year, as four decimal digits.
     * <li><i>hh</i> is the hour of the day (<tt>00</tt> through <tt>23</tt>), 
     *     as two decimal digits.
     * <li><i>mm</i> is the minute within the hour (<tt>00</tt> through 
     *     <tt>59</tt>), as two decimal digits.
     * <li><i>ss</i> is the second within the minute (<tt>00</tt> through 
     *     <tt>61</tt>), as two decimal digits.
     * <li><i>GMT</i> is exactly the ASCII letters "<tt>GMT</tt>" to indicate 
     *     Greenwich Mean Time.
     * </ul><p>
     * The result does not depend on the local time zone.
     * 
     * @return  a string representation of this date, using the Internet GMT
     *          conventions.
     * @see     java.text.DateFormat
     * @see     java.util.Date#toString()
     * @see     java.util.Date#toLocaleString()
     * @deprecated As of JDK version 1.1,
     * replaced by <code>DateFormat.format(Date date)</code>, using a
     * GMT <code>TimeZone</code>.
     */
    public String toGMTString() {
    DateFormat formatter = null;
    if (gmtFormatter != null) {
        formatter = (DateFormat)gmtFormatter.get();
    }
    if (formatter == null) {
        /* No cache yet, or cached formatter GC'd */
        formatter = new SimpleDateFormat("d MMM yyyy HH:mm:ss 'GMT'", 
                         Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        gmtFormatter = new SoftReference(formatter);
    }
        return formatter.format(this);
    }

    /**
     * Returns the offset, measured in minutes, for the local time zone 
     * relative to UTC that is appropriate for the time represented by 
     * this <tt>Date</tt> object. 
     * <p>
     * For example, in Massachusetts, five time zones west of Greenwich:
     * <blockquote><pre>
     * new Date(96, 1, 14).getTimezoneOffset() returns 300</pre></blockquote>
     * because on February 14, 1996, standard time (Eastern Standard Time) 
     * is in use, which is offset five hours from UTC; but:
     * <blockquote><pre>
     * new Date(96, 5, 1).getTimezoneOffset() returns 240</pre></blockquote>
     * because on May 1, 1996, daylight savings time (Eastern Daylight Time) 
     * is in use, which is offset only four hours from UTC.<p>
     * This method produces teh same result as if it computed:
     * <blockquote><pre>
     * (this.getTime() - UTC(this.getYear(), 
     *                       this.getMonth(), 
     *                       this.getDate(),
     *                       this.getHours(), 
     *                       this.getMinutes(), 
     *                       this.getSeconds())) / (60 * 1000)
     * </pre></blockquote>
     *
     * @return  the time-zone offset, in minutes, for the current locale.
     * @see     java.util.Calendar
     * @see     java.util.TimeZone
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.get(Calendar.ZONE_OFFSET) +
     * Calendar.get(Calendar.DST_OFFSET)</code>.
     */
    public int getTimezoneOffset() {
        int offset;
        if (cal == null) {
            if (staticCal == null)
                makeStaticCalendars();
            synchronized (staticCal) {
                staticCal.setTimeZone(TimeZone.getDefault());
                staticCal.setTimeInMillis(getTime());
                offset = staticCal.get(Calendar.ZONE_OFFSET) +
                    staticCal.get(Calendar.DST_OFFSET);
            }
        }
        else {
            TimeZone defaultZone = TimeZone.getDefault();
            if (!defaultZone.equals(cal.getTimeZone())) {
                long ms = cal.getTimeInMillis();
                cal.setTimeZone(TimeZone.getDefault());
                cal.setTimeInMillis(ms);
            }
            offset = cal.get(Calendar.ZONE_OFFSET) +
                cal.get(Calendar.DST_OFFSET);
        }
        return -(offset / 1000 / 60);  // convert to minutes
    }

    /**
     * Save the state of this object to a stream (i.e., serialize it).
     *
     * @serialData The value returned by <code>getTime()</code>
     *		   is emitted (long).  This represents the offset from
     *             January 1, 1970, 00:00:00 GMT in milliseconds.
     */
    private void writeObject(ObjectOutputStream s)
         throws IOException
    {
        s.writeLong(getTime());
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        fastTime = s.readLong();
        // we expect to have cal == null here
    }

    /**
     * Return a field for this date by looking it up in a Calendar object.
     *
     * @return the field value
     * @see    java.util.Calendar
     * @param  field the field to return
     */
    private final int getField(int field) {
        if (cal == null) {
            if (staticCal == null)
                makeStaticCalendars();
            synchronized (staticCal) {
                staticCal.setTimeZone(TimeZone.getDefault());
                staticCal.setTimeInMillis(fastTime);
                return staticCal.get(field);
            }
        }
        else {
            TimeZone defaultZone = TimeZone.getDefault();
            if (!defaultZone.equals(cal.getTimeZone())) {
                long ms = cal.getTimeInMillis();
                cal.setTimeZone(TimeZone.getDefault());
                cal.setTimeInMillis(ms);
            }
            return cal.get(field);
        }
    }

    /**
     * Set a field for this day.
     *
     * @param field the field to set
     * @param value the value to set it to
     * @see java.util.Calendar
     */
    private final void setField(int field, int value) {
        if (cal == null) {
            cal = new GregorianCalendar();
            cal.setTimeInMillis(fastTime);
        }
        cal.set(field, value);
    }

    private synchronized static void makeStaticCalendars() {
	if (staticCal == null) {
	    GregorianCalendar calendar = new GregorianCalendar();
	    utcCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	    defaultCenturyStart = calendar.get(Calendar.YEAR) - 80;
	    staticCal = calendar;
	}
    }
}
