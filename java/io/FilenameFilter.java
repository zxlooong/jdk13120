/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.io;

/**
 * Instances of classes that implement this interface are used to
 * filter filenames. These instances are used to filter directory
 * listings in the <code>list</code> method of class
 * <code>File</code>, and by the Abstract Window Toolkit's file
 * dialog component.
 *
 * @author  Arthur van Hoff
 * @author  Jonathan Payne
 * @version 1.21, 02/06/02
 * @see     java.awt.FileDialog#setFilenameFilter(java.io.FilenameFilter)
 * @see     java.io.File
 * @see     java.io.File#list(java.io.FilenameFilter)
 * @since   JDK1.0
 */
public
interface FilenameFilter {
    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param   dir    the directory in which the file was found.
     * @param   name   the name of the file.
     * @return  <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    boolean accept(File dir, String name);
}
