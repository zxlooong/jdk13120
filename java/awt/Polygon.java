/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import sun.awt.geom.Crossings;

/**
 * The <code>Polygon</code> class encapsulates a description of a 
 * closed, two-dimensional region within a coordinate space. This 
 * region is bounded by an arbitrary number of line segments, each of 
 * which is one side of the polygon. Internally, a polygon
 * comprises of a list of (<i>x</i>,&nbsp;<i>y</i>) 
 * coordinate pairs, where each pair defines a <i>vertex</i> of the 
 * polygon, and two successive pairs are the endpoints of a 
 * line that is a side of the polygon. The first and final
 * pairs of (<i>x</i>,&nbsp;<i>y</i>) points are joined by a line segment 
 * that closes the polygon.  This <code>Polygon</code> is defined with
 * an even-odd winding rule.  See
 * {@link java.awt.geom.PathIterator#WIND_EVEN_ODD WIND_EVEN_ODD}
 * for a definition of the even-odd winding rule.
 * This class's hit-testing methods, which include the 
 * <code>contains</code>, <code>intersects</code> and <code>inside</code>
 * methods, use the <i>insideness</i> definition described in the 
 * {@link Shape} class comments.
 *
 * @version     1.26, 07/24/98
 * @author 	Sami Shaio
 * @author      Herb Jellinek
 * @see Shape
 * @since       JDK1.0
 */
public class Polygon implements Shape, java.io.Serializable {

    /**
     * The total number of points.
     * This value can be NULL.
     *
     * @serial
     * @see #addPoint(int, int)
     */
    public int npoints = 0;

    /**
     * The array of <i>x</i> coordinates. 
     *
     * @serial
     * @see #addPoint(int, int)
     */
    public int xpoints[] = new int[4];

    /**
     * The array of <i>y</i> coordinates. 
     *
     * @serial
     * @see #addPoint(int, int)
     */
    public int ypoints[] = new int[4];
    
    /**
     * Bounds of the polygon.
     * This value can be NULL.
     * Please see the javadoc comments getBounds().
     * 
     * @serial
     * @see #getBoundingBox()
     * @see #getBounds()
     */
    protected Rectangle bounds = null;
    
    /* 
     * JDK 1.1 serialVersionUID 
     */
    private static final long serialVersionUID = -6460061437900069969L;

    /**
     * Creates an empty polygon.
     */
    public Polygon() {
    }

    /**
     * Constructs and initializes a <code>Polygon</code> from the specified 
     * parameters. 
     * @param xpoints an array of <i>x</i> coordinates
     * @param ypoints an array of <i>y</i> coordinates
     * @param npoints the total number of points in the    
     *				<code>Polygon</code>
     * @exception  NegativeArraySizeException if the value of
     *                       <code>npoints</code> is negative.
     * @exception  IndexOutOfBoundsException if <code>npoints</code> is
     *             greater than the length of <code>xpoints</code>
     *             or the length of <code>ypoints</code>.
     * @exception  NullPointerException if <code>xpoints</code> or
     *             <code>ypoints</code> is <code>null</code>.
     */
    public Polygon(int xpoints[], int ypoints[], int npoints) {
	this.npoints = npoints;
	this.xpoints = new int[npoints];
	this.ypoints = new int[npoints];
	System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
	System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);	
    }
    
    /**
     * Translates the vertices of the <code>Polygon</code> by 
     * <code>deltaX</code> along the x axis and by 
     * <code>deltaY</code> along the y axis.
     * @param deltaX the amount to translate along the <i>x</i> axis
     * @param deltaY the amount to translate along the <i>y</i> axis
     * @since JDK1.1
     */
    public void translate(int deltaX, int deltaY) {
	for (int i = 0; i < npoints; i++) {
	    xpoints[i] += deltaX;
	    ypoints[i] += deltaY;
	}
	if (bounds != null) {
	    bounds.translate(deltaX, deltaY);
	}
    }

    /*
     * Calculates the bounding box of the points passed to the constructor.
     * Sets <code>bounds</code> to the result.
     * @param xpoints[] array of <i>x</i> coordinates
     * @param ypoints[] array of <i>y</i> coordinates
     * @param npoints the total number of points
     */
    void calculateBounds(int xpoints[], int ypoints[], int npoints) {
	int boundsMinX = Integer.MAX_VALUE;
	int boundsMinY = Integer.MAX_VALUE;
	int boundsMaxX = Integer.MIN_VALUE;
	int boundsMaxY = Integer.MIN_VALUE;
	
	for (int i = 0; i < npoints; i++) {
	    int x = xpoints[i];
	    boundsMinX = Math.min(boundsMinX, x);
	    boundsMaxX = Math.max(boundsMaxX, x);
	    int y = ypoints[i];
	    boundsMinY = Math.min(boundsMinY, y);
	    boundsMaxY = Math.max(boundsMaxY, y);
	}
	bounds = new Rectangle(boundsMinX, boundsMinY,
			       boundsMaxX - boundsMinX,
			       boundsMaxY - boundsMinY);
    }

    /*
     * Resizes the bounding box to accomodate the specified coordinates.
     * @param x,&nbsp;y the specified coordinates
     */
    void updateBounds(int x, int y) {
	if (x < bounds.x) {
	    bounds.width = bounds.width + (bounds.x - x);
	    bounds.x = x;
	}
	else {
	    bounds.width = Math.max(bounds.width, x - bounds.x);
	    // bounds.x = bounds.x;
	}

	if (y < bounds.y) {
	    bounds.height = bounds.height + (bounds.y - y);
	    bounds.y = y;
	}
	else {
	    bounds.height = Math.max(bounds.height, y - bounds.y);
	    // bounds.y = bounds.y;
	}
    }	

    /**
     * Appends the specified coordinates to this <code>Polygon</code>. 
     * <p>
     * If an operation that calculates the bounding box of this     
     * <code>Polygon</code> has already been performed, such as  
     * <code>getBounds</code> or <code>contains</code>, then this 
     * method updates the bounding box. 
     * @param       x,&nbsp;y   the specified coordinates
     * @see         java.awt.Polygon#getBounds
     * @see         java.awt.Polygon#contains
     */
    public void addPoint(int x, int y) {
	if (npoints == xpoints.length) {
	    int tmp[];

	    tmp = new int[npoints * 2];
	    System.arraycopy(xpoints, 0, tmp, 0, npoints);
	    xpoints = tmp;

	    tmp = new int[npoints * 2];
	    System.arraycopy(ypoints, 0, tmp, 0, npoints);
	    ypoints = tmp;
	}
	xpoints[npoints] = x;
	ypoints[npoints] = y;
	npoints++;
	if (bounds != null) {
	    updateBounds(x, y);
	}
    }

    /**
     * Gets the bounding box of this <code>Polygon</code>. 
     * The bounding box is the smallest {@link Rectangle} whose
     * sides are parallel to the x and y axes of the 
     * coordinate space, and can completely contain the <code>Polygon</code>.
     * @return a <code>Rectangle</code> that defines the bounds of this 
     * <code>Polygon</code>.
     * @since       JDK1.1
     */
    public Rectangle getBounds() {
	return getBoundingBox();
    }

    /**
     * Returns the bounds of this <code>Polygon</code>.
     * @return the bounds of this <code>Polygon</code>.
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getBounds()</code>.
     */
    public Rectangle getBoundingBox() {
	if (bounds == null) {
	    calculateBounds(xpoints, ypoints, npoints);
	}
	return bounds;
    }

    /**
     * Determines whether the specified {@link Point} is inside this 
     * <code>Polygon</code>.
     * @param p the specified <code>Point</code> to be tested
     * @return <code>true</code> if the <code>Polygon</code> contains the
     * 			<code>Point</code>; <code>false</code> otherwise.
     * @see #contains(double, double)
     */
    public boolean contains(Point p) {
	return contains(p.x, p.y);
    }

    /**
     * Determines whether the specified coordinates are inside this 
     * <code>Polygon</code>.   
     * <p>
     * @param x,&nbsp;y  the specified coordinates to be tested
     * @return  <code>true</code> if this <code>Polygon</code> contains
     * 			the specified coordinates, (<i>x</i>,&nbsp;<i>y</i>);  
     * 			<code>false</code> otherwise.
     * @see #contains(double, double)
     * @since      JDK1.1
     */
    public boolean contains(int x, int y) {
	return contains((double) x, (double) y);
    }

    /**
     * Determines whether the specified coordinates are contained in this 
     * <code>Polygon</code>.
     * @param x,&nbsp;y  the specified coordinates to be tested
     * @return  <code>true</code> if this <code>Polygon</code> contains
     * 		the specified coordinates, (<i>x</i>,&nbsp;<i>y</i>);  
     * 		<code>false</code> otherwise.
     * @see #contains(double, double)
     * @deprecated As of JDK version 1.1,
     * replaced by <code>contains(int, int)</code>.
     */
    public boolean inside(int x, int y) {
	return contains((double) x, (double) y);
    }

    /**
     * Returns the high precision bounding box of the {@link Shape}.
     * @return a {@link Rectangle2D} that precisely
     *		bounds the <code>Shape</code>.
     */
    public Rectangle2D getBounds2D() {
	Rectangle r = getBounds();
	return new Rectangle2D.Float(r.x, r.y, r.width, r.height);
    }


    /**
     * Determines whether the specified coordinates are inside this
     * <code>Polygon</code>.  For the definition of
     * <i>insideness</i>, see the class comments of {@link Shape}.
     * @param x,&nbsp;y the specified coordinates
     * @return <code>true</code> if this <code>Polygon</code> contains the
     * specified coordinates; <code>false</code> otherwise.
     */
    public boolean contains(double x, double y) {
        if (npoints <= 2 || !getBoundingBox().contains(x, y)) {
	    return false;
	}
	int hits = 0;

	int lastx = xpoints[npoints - 1];
	int lasty = ypoints[npoints - 1];
	int curx, cury;

	// Walk the edges of the polygon
	for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
	    curx = xpoints[i];
	    cury = ypoints[i];

	    if (cury == lasty) {
		continue;
	    }

	    int leftx;
	    if (curx < lastx) {
		if (x >= lastx) {
		    continue;
		}
		leftx = curx;
	    } else {
		if (x >= curx) {
		    continue;
		}
		leftx = lastx;
	    }

	    double test1, test2;
	    if (cury < lasty) {
		if (y < cury || y >= lasty) {
		    continue;
		}
		if (x < leftx) {
		    hits++;
		    continue;
		}
		test1 = x - curx;
		test2 = y - cury;
	    } else {
		if (y < lasty || y >= cury) {
		    continue;
		}
		if (x < leftx) {
		    hits++;
		    continue;
		}
		test1 = x - lastx;
		test2 = y - lasty;
	    }

	    if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
		hits++;
	    }
	}

	return ((hits & 1) != 0);
    }

    private Crossings getCrossings(double xlo, double ylo,
				   double xhi, double yhi)
    {
	Crossings cross = new Crossings.EvenOdd(xlo, ylo, xhi, yhi);
	int lastx = xpoints[npoints - 1];
	int lasty = ypoints[npoints - 1];
	int curx, cury;

	// Walk the edges of the polygon
	for (int i = 0; i < npoints; i++) {
	    curx = xpoints[i];
	    cury = ypoints[i];
	    if (cross.accumulateLine(lastx, lasty, curx, cury)) {
		return null;
	    }
	    lastx = curx;
	    lasty = cury;
	}

	return cross;
    }

    /**
     * Tests if a specified {@link Point2D} is inside the boundary of this 
     * <code>Polygon</code>.
     * @param p a specified <code>Point2D</code>
     * @return <code>true</code> if this <code>Polygon</code> contains the 
     * 		specified <code>Point2D</code>; <code>false</code>
     *          otherwise.
     * @see #contains(double, double)
     */
    public boolean contains(Point2D p) {
	return contains(p.getX(), p.getY());
    }

    /**
     * Tests if the interior of this <code>Polygon</code> intersects the 
     * interior of a specified set of rectangular coordinates.
     * @param x,&nbsp;y the coordinates of the specified rectangular
     *			shape's top-left corner
     * @param w the width of the specified rectangular shape
     * @param h the height of the specified rectangular shape
     * @return <code>true</code> if the interior of this 
     *			<code>Polygon</code> and the interior of the
     *			specified set of rectangular 
     * 			coordinates intersect each other;
     *			<code>false</code> otherwise.
     */
    public boolean intersects(double x, double y, double w, double h) {
	if (npoints <= 0 || !getBoundingBox().intersects(x, y, w, h)) {
	    return false;
	}

	Crossings cross = getCrossings(x, y, x+w, y+h);
	return (cross == null || !cross.isEmpty());
    }

    /**
     * Tests if the interior of this <code>Polygon</code> intersects the
     * interior of a specified <code>Rectangle2D</code>.
     * @param r a specified <code>Rectangle2D</code>
     * @return <code>true</code> if this <code>Polygon</code> and the
     * 			interior of the specified <code>Rectangle2D</code>
     * 			intersect each other; <code>false</code>
     * 			otherwise.
     */
    public boolean intersects(Rectangle2D r) {
	return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Tests if the interior of this <code>Polygon</code> entirely
     * contains the specified set of rectangular coordinates.
     * @param x,&nbsp;y the coordinate of the top-left corner of the
     * 			specified set of rectangular coordinates
     * @param w the width of the set of rectangular coordinates
     * @param h the height of the set of rectangular coordinates
     * @return <code>true</code> if this <code>Polygon</code> entirely
     * 			contains the specified set of rectangular
     * 			coordinates; <code>false</code> otherwise.
     */
    public boolean contains(double x, double y, double w, double h) {
	if (npoints <= 0 || !getBoundingBox().intersects(x, y, w, h)) {
	    return false;
	}

	Crossings cross = getCrossings(x, y, x+w, y+h);
	return (cross != null && cross.covers(y, y+h));
    }

    /**
     * Tests if the interior of this <code>Polygon</code> entirely
     * contains the specified <code>Rectangle2D</code>.
     * @param r the specified <code>Rectangle2D</code>
     * @return <code>true</code> if this <code>Polygon</code> entirely
     * 			contains the specified <code>Rectangle2D</code>;
     *			<code>false</code> otherwise.
     * @see contains(double, double, double, double)
     */
    public boolean contains(Rectangle2D r) {
	return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Returns an iterator object that iterates along the boundary of this 
     * <code>Polygon</code> and provides access to the geometry
     * of the outline of this <code>Polygon</code>.  An optional
     * {@link AffineTransform} can be specified so that the coordinates 
     * returned in the iteration are transformed accordingly.
     * @param at an optional <code>AffineTransform</code> to be applied to the
     * 		coordinates as they are returned in the iteration, or 
     *		<code>null</code> if untransformed coordinates are desired
     * @return a {@link PathIterator} object that provides access to the
     *		geometry of this <code>Polygon</code>.      
     */
    public PathIterator getPathIterator(AffineTransform at) {
	return new PolygonPathIterator(this, at);
    }

    /**
     * Returns an iterator object that iterates along the boundary of
     * the <code>Shape</code> and provides access to the geometry of the 
     * outline of the <code>Shape</code>.  Only SEG_MOVETO, SEG_LINETO, and 
     * SEG_CLOSE point types are returned by the iterator.
     * Since polygons are already flat, the <code>flatness</code> parameter
     * is ignored.  An optional <code>AffineTransform</code> can be specified 
     * in which case the coordinates returned in the iteration are transformed
     * accordingly.
     * @param at an optional <code>AffineTransform</code> to be applied to the
     * 		coordinates as they are returned in the iteration, or 
     *		<code>null</code> if untransformed coordinates are desired
     * @param flatness the maximum amount that the control points
     * 		for a given curve can vary from colinear before a subdivided
     *		curve is replaced by a straight line connecting the 
     * 		endpoints.  Since polygons are already flat the
     * 		<code>flatness</code> parameter is ignored.
     * @return a <code>PathIterator</code> object that provides access to the
     * 		<code>Shape</code> object's geometry.
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
	return getPathIterator(at);
    }

    class PolygonPathIterator implements PathIterator {
	Polygon poly;
	AffineTransform transform;
	int index;

	public PolygonPathIterator(Polygon pg, AffineTransform at) {
	    poly = pg;
	    transform = at;
	}

	/**
	 * Returns the winding rule for determining the interior of the
	 * path.
         * @return an integer representing the current winding rule.
	 * @see PathIterator#WIND_NON_ZERO
	 */
	public int getWindingRule() {
	    return WIND_EVEN_ODD;
	}

	/**
	 * Tests if there are more points to read.
	 * @return <code>true</code> if there are more points to read;
         *          <code>false</code> otherwise.
	 */
	public boolean isDone() {
	    return index > poly.npoints;
	}

	/**
	 * Moves the iterator forwards, along the primary direction of 
         * traversal, to the next segment of the path when there are
	 * more points in that direction.
	 */
	public void next() {
	    index++;
	}

	/**
	 * Returns the coordinates and type of the current path segment in
	 * the iteration.
	 * The return value is the path segment type:
	 * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
	 * A <code>float</code> array of length 2 must be passed in and
         * can be used to store the coordinates of the point(s).
	 * Each point is stored as a pair of <code>float</code> x,&nbsp;y
         * coordinates.  SEG_MOVETO and SEG_LINETO types return one
         * point, and SEG_CLOSE does not return any points.
         * @param coords a <code>float</code> array that specifies the
         * coordinates of the point(s)
         * @return an integer representing the type and coordinates of the 
         * 		current path segment.
	 * @see PathIterator#SEG_MOVETO
	 * @see PathIterator#SEG_LINETO
	 * @see PathIterator#SEG_CLOSE
	 */
	public int currentSegment(float[] coords) {
	    if (index >= poly.npoints) {
		return SEG_CLOSE;
	    }
	    coords[0] = poly.xpoints[index];
	    coords[1] = poly.ypoints[index];
	    if (transform != null) {
		transform.transform(coords, 0, coords, 0, 1);
	    }
	    return (index == 0 ? SEG_MOVETO : SEG_LINETO);
	}

	/**
	 * Returns the coordinates and type of the current path segment in
	 * the iteration.
	 * The return value is the path segment type:
	 * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
	 * A <code>double</code> array of length 2 must be passed in and
         * can be used to store the coordinates of the point(s).
	 * Each point is stored as a pair of <code>double</code> x,&nbsp;y
         * coordinates.
	 * SEG_MOVETO and SEG_LINETO types return one point,
	 * and SEG_CLOSE does not return any points.
         * @param coords a <code>double</code> array that specifies the
         * coordinates of the point(s)
         * @return an integer representing the type and coordinates of the 
         * 		current path segment.
	 * @see PathIterator#SEG_MOVETO
	 * @see PathIterator#SEG_LINETO
	 * @see PathIterator#SEG_CLOSE
	 */
	public int currentSegment(double[] coords) {
	    if (index >= poly.npoints) {
		return SEG_CLOSE;
	    }
	    coords[0] = poly.xpoints[index];
	    coords[1] = poly.ypoints[index];
	    if (transform != null) {
		transform.transform(coords, 0, coords, 0, 1);
	    }
	    return (index == 0 ? SEG_MOVETO : SEG_LINETO);
	}
    }
}
