/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
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
 * class CompactATypeArray : use only on primitive data types
 * Provides a compact way to store information that is indexed by Unicode
 * values, such as character properties, types, keyboard values, etc.This
 * is very useful when you have a block of Unicode data that contains
 * significant values while the rest of the Unicode data is unused in the
 * application or when you have a lot of redundance, such as where all 21,000
 * Han ideographs have the same value.  However, lookup is much faster than a
 * hash table.
 * A compact array of any primitive data type serves two purposes:
 * <UL type = round>
 *     <LI>Fast access of the indexed values.
 *     <LI>Smaller memory footprint.
 * </UL>
 * A compact array is composed of a index array and value array.  The index
 * array contains the indicies of Unicode characters to the value array.
 *
 * @see                CompactShortArray
 * @see                CompactByteArray
 * @see                CompactIntArray
 * @see                CompactCharArray
 * @version            1.14 02/06/02
 * @author             Helena Shih
 */
final class CompactStringArray implements Cloneable {

    /**
     * The total number of Unicode characters.
     */
    public static  final int UNICODECOUNT =65536;

    /**
     * Default constructor for CompactStringArray, the default value of the
     * compact array is "".
     */
    public CompactStringArray()
    {
        this("");
    }
    /**
     * Constructor for CompactStringArray.
     * @param defaultValue the default value of the compact array.
     */
    public CompactStringArray(String defaultValue)
    {
        int i;
        values = new char[UNICODECOUNT]; /*type = char*/
        indices = new short[INDEXCOUNT];
        setElementAt((char)0,'\uFFFF',defaultValue);
        for (i = 0; i < INDEXCOUNT; ++i) {
            indices[i] = (short)(i<<BLOCKSHIFT);
        }
        isCompact = false;
    }
    /**
     * Constructor for CompactStringArray.
     * @param indexArray the indicies of the compact array.
     * @param newValues the values of the compact array.
     * @exception IllegalArgumentException If the index is out of range.
     */
    public CompactStringArray(short indexArray[],
                              char[] newValues,
                              String exceptions)
    {
        int i;
        if (indexArray.length != INDEXCOUNT)
            throw new IllegalArgumentException("Index out of bounds.");
        for (i = 0; i < INDEXCOUNT; ++i) {
            short index = indexArray[i];
            if ((index < 0) || (index >= newValues.length+BLOCKCOUNT))
                throw new IllegalArgumentException("Index out of bounds.");
        }
        indices = indexArray;
        values = newValues;
    }
    /**
     * Get the mapped value (String) of a Unicode character.
     * @param index the character to get the mapped value with
     * @param toAppendTo the string buffer to append the values to
     */
    public void elementAt(char index, StringBuffer toAppendTo)
    {
        char result = (values[(indices[index>>BLOCKSHIFT] & 0xFFFF) +
                             (index & BLOCKMASK)]);
        if (result >= '\uE000' && result <= '\uF800') {
            for (int i = (int) result - 0xE000; ; ++i) {
                result = exceptions.charAt(i);
                if (result == '\uFFFF') return;
                toAppendTo.append(result);
            }
        } else {
            toAppendTo.append(result);
        }
    }
    /**
     * Get the mapped value of a Unicode character.
     * @param index the character to get the mapped value with
     * @return the mapped value of the given character
     */
    public String elementAt(char index) {
        StringBuffer result = new StringBuffer();
        elementAt(index,result);
        return result.toString();
    }
    /**
     * Set a new value for a Unicode character.
     * Set automatically expands the array if it is compacted.
     * @param index the character to set the mapped value with
     * @param value the new mapped value
     */
    public void setElementAt(char index, String value)
    {
        if (isCompact)
            expand();
        if (value.length() == 1) {
            char ch = value.charAt(0);
            if (ch < '\uE000' || ch >= '\uF800') {
                values[(int)index] = ch;
                return;
            }
        }
        // search for the string to see if it is already present
        String temp = value + '\uFFFF';
        int position = exceptions.toString().indexOf(temp);
        if (position != -1) {
            values[(int)index] = (char)(0xE000 + position);
            return;
        };
        // if not found, append.
        values[(int)index] = (char) (0xE000 + exceptions.length());
        for (int i = 0; i < value.length(); ++i) {
            exceptions.append(value.charAt(i));
        }
        exceptions.append('\uFFFF');    // termination
    }
    /**
     * Set new values for a range of Unicode character.
     * @param start the starting offset of the range
     * @param end the ending offset of the range
     * @param value the new mapped value
     */
   public void setElementAt(char start, char end, String value)
    {
        if (start >= end) return; // catch degenerate case
        setElementAt(start,value);
        char firstValue = values[(int)start];
        for (int i = start + 1; i <= end; ++i) {
            values[i] = firstValue;
        }
    }
    /**
     * Compact the array.
     */
    public void compact()
    {
        if (isCompact == false) {
            char[]      tempIndex;
            int                     tempIndexCount;
            char[]          tempArray;
            short           iBlock, iIndex;

            // make temp storage, larger than we need
            tempIndex = new char[UNICODECOUNT];
            // set up first block.
            tempIndexCount = BLOCKCOUNT;
            for (iIndex = 0; iIndex < BLOCKCOUNT; ++iIndex) {
                tempIndex[iIndex] = (char)iIndex;
            }; // endfor (iIndex = 0; .....)
            indices[0] = (short)0;

            // for each successive block, find out its first position
            // in the compacted array
            for (iBlock = 1; iBlock < INDEXCOUNT; ++iBlock) {
                int     newCount, firstPosition, block;
                block = iBlock<<BLOCKSHIFT;
                if (DEBUGSMALL) if (block > DEBUGSMALLLIMIT) break;
                firstPosition = FindOverlappingPosition(block, tempIndex,
                                                        tempIndexCount);

                newCount = firstPosition + BLOCKCOUNT;
                if (newCount > tempIndexCount) {
                    for (iIndex = (short)tempIndexCount;
                         iIndex < newCount;
                         ++iIndex) {
                        tempIndex[iIndex]
                            = (char)(iIndex - firstPosition + block);
                    } // endfor (iIndex = tempIndexCount....)
                    tempIndexCount = newCount;
                } // endif (newCount > tempIndexCount)
                indices[iBlock] = (short)firstPosition;
            } // endfor (iBlock = 1.....)

            // now allocate and copy the items into the array
            tempArray = new char[tempIndexCount];
            for (iIndex = 0; iIndex < tempIndexCount; ++iIndex) {
                tempArray[iIndex] = values[tempIndex[iIndex]];
            }
            values = null;
            values = tempArray;
            isCompact = true;
        } // endif (isCompact != false)
    }
    /** For internal use only.  Do not modify the result, the behavior of
     * modified results are undefined.
     */
    public short getIndexArray()[]
    {
        return indices;
    }
    /** For internal use only.  Do not modify the result, the behavior of
      * modified results are undefined.
      */
    public char getStringArray()[]
    {
        return values;
    }
    /**
     * Overrides Cloneable
     */
    public Object clone()
    {
        try {
            CompactStringArray other = (CompactStringArray) super.clone();
            other.values = (char[])values.clone();
            other.indices = (short[])indices.clone();
            other.exceptions = new StringBuffer(exceptions.toString());
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    /**
     * Compares the equality of two compact array objects.
     * @param obj the compact array object to be compared with this.
     * @return true if the current compact array object is the same
     * as the compact array object obj; false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj)                      // quick check
            return true;
        if (getClass() != obj.getClass())         // same class?
            return false;
        CompactStringArray other = (CompactStringArray) obj;
        for (int i = 0; i < UNICODECOUNT; i++) {
            // could be sped up later
            if (elementAt((char)i) != other.elementAt((char)i))
                return false;
        }
        return true; // we made it through the guantlet.
    }
    /**
     * Generates the hash code for the compact array object
     */

    public int hashCode() {
        int result = 0;
        int increment = Math.min(3, values.length/16);
        for (int i = 0; i < values.length; i+= increment) {
            result = result * 37 + values[i];
        }
        return result;
    }
    /**
      * package private : for internal use only
      */
    void writeArrays()
    {
        int i;
        int cnt = 0;
        if (values.length > 0)
            cnt = values.length;
        else
            cnt = values.length + UNICODECOUNT;
        System.out.println("{");
        for (i = 0; i < INDEXCOUNT-1; i++)
        {
            System.out.print("(short)" + (int)((getIndexArrayValue(i) >= 0) ?
                (int)getIndexArrayValue(i) :
                (int)(getIndexArrayValue(i)+UNICODECOUNT)) + ", ");
            if (i != 0)
                if (i % 10 == 0)
                    System.out.println();
        }
        System.out.println("(char)" +
                           (int)((getIndexArrayValue(INDEXCOUNT-1) >= 0) ?
                           (int)getIndexArrayValue(i) :
                           (int)(getIndexArrayValue(i)+UNICODECOUNT)) + " }");
        System.out.println("{");
        for (i = 0; i < cnt-1; i++)
        {
            char ch = getArrayValue(i);
            if (ch < 0x20 || (ch > 0x7E && ch < 0xA0) || ch > 0x100)
                System.out.print("(char)0x" +
                    Integer.toString((int)ch,16).toUpperCase() + ",");
            else System.out.print("\'" + ch + "\',");
            if (i != 0)
                if (i % 10 == 0)
                    System.out.println();
        }
        System.out.println("(char)" + (int)getArrayValue(cnt-1) + " }");
        System.out.println("\"" + exceptions.toString() + "\"");
    }
    // Print char Array  : Debug only
    void printIndex(char start, short count)
    {
        int i;
        for (i = start; i < count; ++i)
        {
            System.out.println(i + " -> : " +
                               (int)((indices[i] >= 0) ?
                                     indices[i] : indices[i] + UNICODECOUNT));
        }
        System.out.println();
    }
    void printPlainArray(int start,int count, char[] tempIndex)
    {
        int iIndex;
        if (tempIndex != null)
        {
            for (iIndex     = start; iIndex < start + count; ++iIndex)
            {
                System.out.print(" " + (int)getArrayValue(tempIndex[iIndex]));
            }
        }
        else
        {
            for (iIndex = start; iIndex < start + count; ++iIndex)
            {
                System.out.print(" " + (int)getArrayValue(iIndex));
            }
        }
        System.out.println("    Range: start " + start + " , count " + count);
    }
    /**
     * private functions
     */
    /**
      * Expanded takes the array back to a 65536 element array
      */
    private void expand()
    {
        int i;
        if (isCompact) {
            char[]  tempArray;
            tempArray = new char[UNICODECOUNT];
            for (i = 0; i < UNICODECOUNT; ++i) {
                tempArray[i] =(values[((int)indices[i>>BLOCKSHIFT] & 0xFFFF) +
                                     (i & BLOCKMASK)]);;
            }
            for (i = 0; i < INDEXCOUNT; ++i) {
                indices[i] = (short)(i<<BLOCKSHIFT);
            }
            values = null;
            values = tempArray;
            isCompact = false;
        }
    }
    // # of elements in the indexed array
    private short capacity()
    {
        return (short)values.length;
    }
    private char getArrayValue(int n)
    {
        return values[n];
    }
    private short getIndexArrayValue(int n)
    {
        return indices[n];
    }
    private int
    FindOverlappingPosition(int start, char[] tempIndex, int tempIndexCount)
    {
        int i;
        short j;
        short currentCount;

        if (DEBUGOVERLAP && start < DEBUGSHOWOVERLAPLIMIT) {
            printPlainArray(start, BLOCKCOUNT, null);
            printPlainArray(0, tempIndexCount, tempIndex);
        }
        for (i = 0; i < tempIndexCount; i += BLOCKCOUNT) {
            currentCount = (short)BLOCKCOUNT;
            if (i + BLOCKCOUNT > tempIndexCount) {
                currentCount = (short)(tempIndexCount - i);
            }
            for (j = 0; j < currentCount; ++j) {
                if (values[start + j] != values[tempIndex[i + j]]) break;
            }
            if (j == currentCount) break;
        }
        if (DEBUGOVERLAP && start < DEBUGSHOWOVERLAPLIMIT) {
            for (j = 1; j < i; ++j) {
                System.out.print(" ");
            }
            printPlainArray(start, BLOCKCOUNT, null);
            System.out.println("    Found At: " + i);
        }
        return i;
    }

    private static  final int DEBUGSHOWOVERLAPLIMIT = 100;
    private static  final boolean DEBUGTRACE = false;
    private static  final boolean DEBUGSMALL = false;
    private static  final boolean DEBUGOVERLAP = false;
    private static  final int DEBUGSMALLLIMIT = 30000;
    private static  final int BLOCKSHIFT =7;
    private static  final int BLOCKCOUNT =(1<<BLOCKSHIFT);
    private static  final int INDEXSHIFT =(16-BLOCKSHIFT);
    private static  final int INDEXCOUNT =(1<<INDEXSHIFT);
    private static  final int BLOCKMASK = BLOCKCOUNT - 1;

    private char[] values;  // char -> short (char parameterized short)
    private short indices[];
    private StringBuffer exceptions = new StringBuffer();
    private boolean isCompact;
};
