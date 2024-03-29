/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

/**
 * Interface to describe a structural piece of a document.  It
 * is intended to capture the spirit of an SGML element.
 *
 * @author  Timothy Prinzing
 * @version 1.18 02/06/02
 */
public interface Element {

    /**
     * Fetches the document associated with this element.
     *
     * @return the document
     */
    public Document getDocument();

    /**
     * Fetches the parent element.  If the element is a root level
     * element returns null.
     *
     * @return the parent element
     */
    public Element getParentElement();

    /**
     * Fetches the name of the element.  If the element is used to
     * represent some type of structure, this would be the type
     * name.
     *
     * @return the element name
     */
    public String getName();

    /**
     * Fetches the collection of attributes this element contains.
     *
     * @return the attributes for the element
     */
    public AttributeSet getAttributes();

    /**
     * Fetches the offset from the beginning of the document
     * that this element begins at.  If this element has
     * children, this will be the offset of the first child.
     *
     * @return the starting offset >= 0
     */
    public int getStartOffset();

    /**
     * Fetches the offset from the beginning of the document
     * that this element ends at.  If this element has
     * children, this will be the end offset of the last child.
     * <p>
     * All the default Document implementations descend from AbstractDocument.
     * AbstractDocument models an implied break at the end of
     * the document. As a result of this, it is possible for this to
     * return a value greater than the length of the document.
     *
     * @return the ending offset >= 0
     * @see AbstractDocument
     */
    public int getEndOffset();

    /**
     * Gets the child element index closest to the given offset.
     * The offset is specified relative to the begining of the
     * document.
     *
     * @param offset the specified offset >= 0
     * @return the element index >= 0
     */
    public int getElementIndex(int offset);

    /**
     * Gets the number of child elements contained by this element.
     * If this element is a leaf, a count of zero is returned.
     *
     * @return the number of child elements >= 0
     */
    public int getElementCount();

    /**
     * Fetches the child element at the given index.
     *
     * @param index the specified index >= 0
     * @return the child element
     */
    public Element getElement(int index);

    /**
     * Is this element a leaf element?
     *
     * @return true if a leaf element else false
     */
    public boolean isLeaf();


}

