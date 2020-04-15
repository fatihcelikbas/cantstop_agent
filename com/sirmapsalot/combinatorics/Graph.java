package com.sirmapsalot.combinatorics;

import com.sirmapsalot.combinatorics.*;
import java.util.*;

/**
 * A collection of vertices and edges between those vertices.
 * Edges are between two vertices and may be directed or undirected
 * and weighted or unweighted.
 *
 * TO DO: equals(Object) methods for many classes should not throw
 * ClassCastException; should instead return null
 *
 * @author Jim Glenn
 * @version 0.1 3/27/2003
 * @version 0.2 8/27/2003 added interfaces and classes for different kinds of
 * edges
 * @version 0.3 9/4/2003 added Vertex class
 */

public class Graph
{
    /**
     * The vertex set for this graph.
     */

    private Vector verts;

    /**
     * Maps vertices to indices in the <CODE>verts</CODE> vector.
     */
    
    private HashMap vMap;

    /**
     * Common interface for all edges.
     */

    public static interface Edge
    {
	/**
	 * Returns all the vertices that are incident from this edge.
	 * For an undirected edge "incident from" is defined to be
	 * "incident on", so that both endpoints are returned.
	 *
	 * @return a collection of all vertices incident from this edge
	 */

	public Collection getIncidentFrom();
	
	/**
	 * Returns the vertex at the other end of this edge starting from
	 * the given vertex.
	 *
	 * @param start an vertex incident from this edge
	 * @return the other vertex indident on this edge
	 */

	public Vertex getDestination(Vertex start);

	/**
	 * Returns the other vertex indicent on this edge.
	 *
	 * @param u a vertex incident on this edge
	 * @return the other vertex incident on this edge
	 */

	public Vertex getOppositeVertex(Vertex v);

	/**
	 * Determines if the given vertex is incident on this edge.
	 *
	 * @param v the key of a vertex
	 * @return true iff that vertex is incident on this edge
	 */

	public boolean incidentOn(Vertex v);
    }

    /**
     * Common interface for directed edges.
     */

    public static interface DirectedEdge extends Edge
    {
	/**
	 * Returns the source vertex of this edge.
	 *
	 * @return the source vertex of this edge
	 */

	public Vertex getSource();

	/**
	 * Returns the destination vertex of this edge.
	 *
	 * @return the destination vertex of this edge
	 */

	public Vertex getDestination();
    }

    /**
     * Common interface for weighted edges.
     */

    public static interface WeightedEdge extends Edge
    {
	/**
	 * Returns the weight of this edge.
	 *
	 * @return the weight of this edge
	 */

	public double getWeight();
    }

    /**
     * An undirected, unweighted edge.
     */

    public static class UEdge implements Edge
    {
	/**
	 * The source vertex of this edge.  "Source" and "destination" are
	 * meaningless for directed edges; we use the terms for the sake
	 * of subclasses that implement directed edges.
	 */

	protected Vertex src;

	/**
	 * The destination vertex of this edge.  See note above about
	 * "source" and "destination" for undirected edges.
	 */

	protected Vertex dest;

	/**
	 * Constructs an edge between the given vertices.
	 *
	 * @param u one vertex incident on the new edge
	 * @param v the other vertex indicent on the new edge
	 */

	public UEdge(Vertex u, Vertex v)
	{
	    src = u;
	    dest = v;
	}

	/**
	 * Returns both endpoints of this edge.
	 *
	 * @return both endpoint of this edge
	 */

	public Collection getIncidentFrom()
	{
	    LinkedList result = new LinkedList();
	    
	    result.add(src);
	    result.add(dest);

	    return result;
	}
	
	/**
	 * Returns the vertex at the other end of this edge starting from
	 * the given vertex.
	 *
	 * @param start an vertex incident on this edge
	 * @return the other vertex indident on this edge
	 */

	public Vertex getDestination(Vertex start)
	{
	    if (start.equals(src))
		return dest;
	    else if (start.equals(dest))
		return src;
	    else
		throw new IllegalArgumentException("start vertex must be incident on this edge");
	}

	/**
	 * Returns the other vertex indicent on this edge.
	 *
	 * @param u a vertex incident on this edge
	 * @return the other vertex incident on this edge
	 */

	public Vertex getOppositeVertex(Vertex v)
	{
	    return getDestination(v);
	}

	/**
	 * Determines if the given vertex is incident on this edge.
	 *
	 * @param v a vertex
	 * @return true iff that vertex is incident on this edge
	 */

	public boolean incidentOn(Vertex v)
	{
	    return (v.equals(src) || v.equals(dest));
	}

	/**
	 * Determines if this edge is equal to the given object.
	 *
	 * @param o the object to compare this edge to
	 * @throws ClassCastException if <CODE>o</CODE> is not an edge
	 * @see #equals(Graph.UEdge)
	 */

	public boolean equals(Object o)
	{
	    return equals((UEdge)o);
	}

	/**
	 * Determines if this edge is equal to the given edge.
	 * Two undirected edges are equivalent they are incident on the same
	 * two vertices.
	 *
	 * @param e the edge to compare this one to
	 * @return true iff this edge is the same as the given edge
	 */

	public boolean equals(UEdge e)
	{
	    return ((src.equals(e.src) && dest.equals(e.dest))
		    || (src.equals(e.dest) && dest.equals(e.src)));

	}

	/**
	 * Returns a hash code for this edge.
	 *
	 * @return a hash code for this edge
	 */

	public int hashCode()
	{
	    return (src.hashCode() ^ dest.hashCode());
	}

	/**
	 * Returns a string representation of this edge.
	 *
	 * @return a string representation of this edge
	 */

	public String toString()
	{
	    StringBuffer result = new StringBuffer();

	    result.append('(');
	    result.append(src.toString());
	    result.append("->");
	    result.append(dest.toString());
	    result.append(')');
	    
	    return result.toString();
	}
    }

    /**
     * An directed, unweighted edge.
     */

    public static class DEdge implements DirectedEdge
    {
	/**
	 * The source vertex of this edge.  "Source" and "destination" are
	 * meaningless for directed edges; we use the terms for the sake
	 * of subclasses that implement directed edges.
	 */

	protected Vertex src;

	/**
	 * The destination vertex of this edge.  See note above about
	 * "source" and "destination" for undirected edges.
	 */

	protected Vertex dest;

	/**
	 * Constructs an edge between the given vertices.
	 *
	 * @param u one vertex incident on the new edge
	 * @param v the other vertex indicent on the new edge
	 */

	public DEdge(Vertex u, Vertex v)
	{
	    src = u;
	    dest = v;
	}

	/**
	 * Returns a the source of this edge, as a collection.
	 *
	 * @return a singleton collection containing the source of this edge
	 */

	public Collection getIncidentFrom()
	{
	    LinkedList result = new LinkedList();

	    result.add(src);

	    return result;
	}
	
	/**
	 * Returns the vertex at the other end of this edge starting from
	 * the given vertex.
	 *
	 * @param start the source of this edge
	 * @return the destination of this edge
	 */

	public Vertex getDestination(Vertex start)
	{
	    if (start.equals(src))
		return dest;
	    else
		throw new IllegalArgumentException("start vertex must be source of this edge");
	}

	/**
	 * Returns the other vertex indicent on this edge.
	 *
	 * @param u a vertex incident on this edge
	 * @return the other vertex incident on this edge
	 */

	public Vertex getOppositeVertex(Vertex v)
	{
	    if (v.equals(src))
		return dest;
	    else if (v.equals(dest))
		return src;
	    else
		throw new IllegalArgumentException("vertex must be incident on this edge");
	}
	
	/**
	 * Determines if the given vertex is incident on this edge.
	 *
	 * @param v a vertex
	 * @return true iff that vertex is incident on this edge
	 */

	public boolean incidentOn(Vertex v)
	{
	    return (v.equals(src) || v.equals(dest));
	}

	/**
	 * Returns the source vertex of this edge.
	 *
	 * @return the source vertex of this edge
	 */

	public Vertex getSource()
	{
	    return src;
	}

	/**
	 * Returns the destination vertex of this edge.
	 *
	 * @return the destination vertex of this edge
	 */

	public Vertex getDestination()
	{
	    return dest;
	}

	/**
	 * Determines if this edge is equal to the given object.
	 *
	 * @param o the object to compare this edge to
	 * @throws ClassCastException if <CODE>o</CODE> is not an edge
	 * @see #equals(Graph.DEdge)
	 */

	public boolean equals(Object o)
	{
	    return equals((DEdge)o);
	}

	/**
	 * Determines if this edge is equal to the given edge.
	 * Two directed edges are equivalent they are incident on the same
	 * two vertices and go in the same direction.
	 *
	 * @param e the edge to compare this one to
	 * @return true iff this edge is the same as the given edge
	 */

	public boolean equals(DEdge e)
	{
	    return (src.equals(e.src) && dest.equals(e.dest));
	}

	/**
	 * Returns a hash code for this edge.
	 *
	 * @return a hash code for this edge
	 */

	public int hashCode()
	{
	    return (src.hashCode() ^ dest.hashCode());
	}

	/**
	 * Returns a string representation of this edge.
	 *
	 * @return a string representation of this edge
	 */

	public String toString()
	{
	    StringBuffer result = new StringBuffer();

	    result.append('(');
	    result.append(src.toString());
	    result.append("->");
	    result.append(dest.toString());
	    result.append(')');
	    
	    return result.toString();
	}
    }

    /**
     * A weighted, undirected edge.
     */

    public static class WEdge extends UEdge implements WeightedEdge
    {

	/**
	 * The weight of this edge.
	 */

	protected double weight;

	/**
	 * Creates a new edge incident on the given vertices with the
	 * given weight.
	 *
	 * @param u one vertex incident on this new edge
	 * @param v the other vertex incident on this new edge
	 * @param w the weight of this new edge
	 */

	public WEdge(Vertex u, Vertex v, double w)
	{
	    super(u, v);

	    weight = w;
	}

	/**
	 * Returns the weight of this edge.
	 *
	 * @return the weight of this edge
	 */

	public double getWeight()
	{
	    return weight;
	}

	/**
	 * Returns a string representation of this edge.
	 *
	 * @return a string representation of this edge
	 */

	public String toString()
	{
	    return super.toString() + ":" + getWeight();
	}
    }

    /**
     * A weighted, undirected edge.
     */

    public static class WDEdge extends DEdge implements WeightedEdge
    {

	/**
	 * The weight of this edge.
	 */

	protected double weight;

	/**
	 * Creates a new edge incident on the given vertices with the
	 * given weight.
	 *
	 * @param u one vertex incident on this new edge
	 * @param v the other vertex incident on this new edge
	 * @param w the weight of this new edge
	 */

	public WDEdge(Vertex u, Vertex v, double w)
	{
	    super(u, v);

	    weight = w;
	}

	/**
	 * Returns the weight of this edge.
	 *
	 * @return the weight of this edge
	 */

	public double getWeight()
	{
	    return weight;
	}

	/**
	 * Returns a string representation of this edge.
	 *
	 * @return a string representation of this edge
	 */

	public String toString()
	{
	    return super.toString() + ":" + getWeight();
	}
    }

    /**
     * An iterator that iterates through the vertices of this graph in
     * the order they are finished by a depth first search.
     * The iterator can follow edges forwards or backwards.
     */

    public class DepthFirstIterator implements Iterator
    {
	/**
	 * The color of each vertex as seen by this iterator.  The
	 * color is white for unvisited vertices, gray for
	 * visited but unfinished vertices, and black for finished
	 * vertices.
	 */

	private int[] color;

	/**
	 * A color code for vertices.
	 */

	private static final int WHITE = 0;
	private static final int GRAY = 1;
	private static final int BLACK = 2;

	/**
	 * The direction this iterator follows edges.
	 */

	private int direction;

	/**
	 * A code for an edge traversal direction.
	 */

	private static final int FORWARD = 0;
	private static final int BACKWARD = 1;

	/**
	 * The order this iterator should visit components in.
	 * If this is <CODE>null</CODE>, the order will be determined
	 * by the graph's vertex ordering.
	 */

	private List order;

	/**
	 * The current location in the list that orders the components.
	 */

	private Iterator orderIterator;
	  
	/**
	 * The stack of vertices to process.
	 */

	private Stack vStack;

	/**
	 * Parallel to <CODE>vStack</CODE>; keeps track of iterators
	 * through the adjacency lists.
	 */

	private Stack iStack;

        /**
         * The visitor that processes vertices and edges as they are traversed.
         */

        private DFSVisitor visitor;

	/**
	 * Creates an iterator that will find all vertices in this graph.
	 */

	private DepthFirstIterator()
	{
	    this(FORWARD);
	}

	/**
	 * Creates an iterator that will find all vertices in this graph
	 * by following edges in the given direction.
	 */

	private DepthFirstIterator(int dir)
	{
	    LinkedList l = new LinkedList();
	    l.addAll(verts);
	    init(l, dir);
	}

	/**
	 * Creates an iterator that will find all vertices reachable from
	 * the given source.
	 *
	 * @param s the source vertex
	 */

	private DepthFirstIterator(Object s)
	{
	    this(s, FORWARD);
	}

	/**
	 * Creates an iterator that will find all vertices that are
	 * reachable from or can reach the given vertex.
	 *
	 * @param v the key of a vertex
	 * @param d the direction to traverse edges
	 */

	private DepthFirstIterator(Object v, int dir)
	{
	    LinkedList l = new LinkedList();
	    if (vMap.containsKey(v))
		l.add(getVertex(v));
	    init(l, dir);
	}

	/**
	 * Creates an iterator that will find all vertices that are
	 * reachable from or can reach the given vertex.
	 *
	 * @param l a list of <CODE>Vertex</CODE>s that gives
	 * the order in which to visit components
	 * @param d the direction to traverse edges
	 */

	private DepthFirstIterator(List l, int dir, DFSVisitor visit)
	{
            visitor = visit;
	    init(l, dir);
	}

	/**
	 * Initializes this iterator to go through vertices in the
	 * given order and to traverse order in the given direction.
	 *
	 * @param l a list of <CODE>Vertex</CODE> objects giving
	 * the order in which to start searches
	 * @param dir the direction in which to traverse edges
	 */

	private void init(List l, int dir)
	{
	    vStack = new Stack();
	    iStack = new Stack();
	    direction = dir;
	    if (l != null)
		order = l;
	    else
		{
		    order = new LinkedList();
		    order.addAll(verts);
		}
	    orderIterator = order.iterator();
	    
	    // initialize all vertices to white

	    color = new int[verts.size()];
	    for (int v = 0; v < verts.size(); v++)
		color[v] = WHITE;
	    
	    // move to the first component
	    
	    nextComponent();
	}

	/**
	 * Returns an iterator through the list of vertices adjacent to
	 * the given vertex or though the list of vertices this vertex
	 * is adjacent to, depending on the direction this iterator
	 * traverses edges.
	 *
	 * @param v a vertex
	 * @return an iterator through the list of neighbors of v
	 */

	private Iterator getIterator(Vertex v)
	{
	    if (direction == FORWARD)
		return v.outgoingIterator();
	    else
		return v.incomingIterator();
	}

	/**
	 * Determines if this iterator has more vertices to visit in
	 * the current component.
	 *
	 * @return true iff this iterator has more vertices to visit
	 */

	public boolean hasNext()
	{
	    return (!vStack.empty());
	}

	/**
	 * Moves this iterator to the next unvisted vertex in its
	 * ordering.  There is no effect if this iterator has not
	 * finished the current component.
	 */

	public void nextComponent()
	{
	    if (vStack.empty() && orderIterator.hasNext())
		{
		    // find the next vertex in the order that has not been
		    // visited

		    Vertex v = null;
		    while (orderIterator.hasNext() &&
			   (v = (Vertex)(orderIterator.next())) != null
			   && color[getIndex(v.getKey())] != WHITE)
			v = null;

		    if (v != null)
			{
			    vStack.add(v);
			    iStack.add(getIterator(v));
			    color[getIndex(v.getKey())] = GRAY;

                            // visit the starting point
                            if (visitor != null)
                                {
                                    visitor.start(Graph.this, v);
                                }
			}
		}
	}

	/**
	 * Returns the next vertex finished by this iterator.
	 *
	 * @return the next vertex
	 */

	public Object next()
	{
	    if (hasNext())
		{
		    Vertex v = (Vertex)(vStack.pop());
		    Iterator i = (Iterator)(iStack.pop());

		    // find next white neighbor of v

		    Edge e = null;
		    int uIndex = -1;
		    while (i.hasNext()
			   && (e = (Edge)(i.next())) != null
			   && (uIndex = getIndex(e.getOppositeVertex(v).getKey())) != -1
			   && color[uIndex] != WHITE)
			{
			    e = null;
			}

		    if (e != null)
			{
			    // found a white neighbor -- visit it

			    vStack.push(v);
			    iStack.push(i);
			    color[uIndex] = GRAY;
			    vStack.push(e.getOppositeVertex(v));
			    iStack.push(getIterator(e.getOppositeVertex(v)));

                            // start the neighbor
                            if (visitor != null)
                                {
                                    visitor.traverse(Graph.this, v, e.getOppositeVertex(v));
                                    visitor.start(Graph.this, e.getOppositeVertex(v));
                                }

			    return next();
			}
		    else
			{
			    // no white neighbors -- this vertex is finished

			    int vIndex = getIndex(v.getKey());
			    color[vIndex] = BLACK;

                            // finish this vertex
                            if (visitor != null)
                                {
                                    visitor.finish(Graph.this, v);
                                    if (vStack.size() > 0)
                                        {
                                            visitor.backtrack(Graph.this, v, (Vertex)(vStack.peek()));
                                        }
                                }

			    return v;
			}
		}
	    else
		return null;
	}

        /**
         * Runs depth first search using this iterator and the given visitor.
         *
         * @param visit a visitor
         */
        public void depthFirstSearch()
        {
            while (hasNext())
                {
                    next();
                }
        }

	/**
	 * Returns the parent in the DFS tree of the vertex last
	 * returned by <CODE>next</CODE>.
	 *
	 * @return the parent of the vertex returned by <CODE>next</CODE>
	 */

	public Vertex getParent()
	{
	    if (vStack.empty())
		return null;
	    else
		return (Vertex)(vStack.peek());
	}

	/**
	 * Returns the path in the DFS forest from the root of
	 * one tree in the forest to the vertex most
	 * recently returned by <CODE>next</CODE>.  The path does
	 * not include the vertex returned by <CODE>next</CODE>.
	 *
	 * @return path to the vertex returned by <CODE>next</CODE>
	 */

	public List getPath()
	{
	    return new LinkedList(vStack);
	}

	/**
	 * Unsupported.
	 *
	 * @throws UnsupportedOperationException
	 */

	public void remove()
	{
	    throw new UnsupportedOperationException();
	}
    }

    /**
     * A visitor to plug into depth first search.
     */
    public interface DFSVisitor
    {
        public void traverse(Graph g, Vertex v, Vertex w);
        public void start(Graph g, Vertex v);
        public void finish(Graph g, Vertex v);
        public void backtrack(Graph g, Vertex w, Vertex v);
    }

    /**
     * The default DFS visitor.
     */
    public static abstract class AbstractDFSVisitor implements DFSVisitor
    {
        protected Map<Object, Integer> startTime;
        protected Map<Object, Integer> finishTime;
        protected int time;

        public AbstractDFSVisitor()
        {
            startTime = new HashMap<Object, Integer>();
            finishTime= new HashMap<Object, Integer>();
            time = 0;
        }

        @Override
        public void start(Graph g, Vertex v)
        {
            startTime.put(v, time++);
        }

        @Override
        public void finish(Graph g, Vertex v)
        {
            finishTime.put(v, time++);
        }

        @Override
        public void traverse(Graph g, Vertex v, Vertex w)
        {
        }

        @Override
        public void backtrack(Graph g, Vertex v, Vertex w)
        {
        }
    }

    /**
     * A DFS iterator for finding articulation points.
     */
    public static class BCCDFSVisitor extends AbstractDFSVisitor
    {
        protected Map<Object, Integer> backLink;
        protected Object root;
        protected int rootChildren;
        protected Set<Object> articulationPoints;

        public BCCDFSVisitor()
        {
            backLink = new HashMap<Object, Integer>();
            articulationPoints = new HashSet<Object>();
            root = null;
            rootChildren = 0;
        }

        @Override
        public void start(Graph g, Vertex v)
        {
            // System.out.println("starting " + v);

            super.start(g, v);

            if (root == null)
                {
                    root = v;
                }

            backLink.put(v, startTime.get(v));
        }

        @Override
        public void finish(Graph g, Vertex v)
        {
            // System.out.println("finishing " + v);

            super.finish(g, v);

            // go over edges from v, looking at back link values
            Iterator i = v.outgoingIterator();
            while (i.hasNext())
                {
                    Vertex w = ((Edge)i.next()).getOppositeVertex(v);
                    int b = startTime.get(w);
                    // System.out.println("Examining " + v + " to " + w);
                    backLink.put(v, Math.min(backLink.get(v), b));
                }
            // System.out.println("back link value is " + backLink.get(v));
        }

        @Override
        public void traverse(Graph g, Vertex v, Vertex w)
        {
            // System.out.println("traversing " + v + " to " + w);

            super.traverse(g, v, w);

            // check for 2nd child of root, making root an articulation point
            if (v == root)
                {
                    rootChildren++;
                    if (rootChildren == 2)
                        {
                            articulationPoints.add(root);
                        }
                }
        }

        @Override
        public void backtrack(Graph g, Vertex w, Vertex v)
        {
            // System.out.println("backtracking " + w + " to " + v);

            // check value passed up to parent; if >= parent's start time the parent is an articulation point
            backLink.put(v, Math.min(backLink.get(v), backLink.get(w)));
            if (v != root && backLink.get(w) >= startTime.get(v))
                {
                    // System.out.println(w + " returned " + backLink.get(w) + " to " + v + "[" + startTime.get(v) + "]");
                    articulationPoints.add(v);
                }
        }

        public Set<Object> getArticulationPoints()
        {
            return articulationPoints;
        }
    }

    /**
     * A vertex in this graph.  A vertex has a key that is used to identify
     * it and a label that can be any piece of data.  The incoming and outgoing
     * edges can be obtained.
     */

    public static class Vertex
    {
	/**
	 * The unique key for this vertex.
	 */

	private Object id;

	/**
	 * The label for this vertex.
	 */

	private Object label;

	/**
	 * The outgoing edges for this vertex.
	 */

	private List outgoing;

	/**
	 * The incoming edges for this vertex.
	 */

	private List incoming;

	/**
	 * Constructs a new vertex with the given key, a <CODE>null</CODE>
	 * label, and no incident edges.
	 *
	 * @param key the key for this new vertex
	 */

	private Vertex(Object key)
	{
	    id = key;
	    label = null;
	    incoming = new LinkedList();
	    outgoing = new LinkedList();
	}

	/**
	 * Gets the key of this vertex.
	 *
	 * @return the key of this vertex
	 */

	public Object getKey()
	{
	    return id;
	}

	/**
	 * Sets the label for this vertex.
	 *
	 * @param l the new label for this vertex
	 */

	public void setLabel(Object l)
	{
	    label = l;
	}

	/**
	 * Gets the label for this vertex.
	 *
	 * @return the label for this vertex
	 */

	public Object getLabel()
	{
	    return label;
	}

	/**
	 * Returns an iterator through the edges leaving this vertex.
	 *
	 * @return an iterator through the edges leaving this vertex.
	 */

	public Iterator outgoingIterator()
	{
	    return outgoing.iterator();
	}

	/**
	 * Returns an iterator through the edges entering this vertex.
	 *
	 * @return an iterator through the edges entering this vertex.
	 */

	public Iterator incomingIterator()
	{
	    return incoming.iterator();
	}

	/**
	 * Determines if this vertex is equal to the given object.
	 * The two objects will be equal iff the other object is also
	 * a vertex and their keys match.
	 *
	 * @param o the object to compare this vertex to
	 * @return true iff <CODE>o</CODE> is a <CODE>Vertex</CODE> with the
	 * same key as this vertex
	 * @throws ClassCastException if <CODE>o</CODE> is not a <CODE>Vertex</CODE>
	 */

	public boolean equals(Object o)
	{
	    return equals((Vertex)o);
	}

	/**
	 * Determines if this vertex is equal to the given vertex.  Two
	 * vertices are equal if and only if their keys are equal
	 * according to the keys' <CODE>equals</CODE> method.
	 *
	 * @param v the vertex to compare this vertex to
	 * @return true iff the two vertices are equal
	 */

	public boolean equals(Vertex v)
	{
	    return id.equals(v.id);
	}

	/**
	 * Returns a hash code for this vertex.
	 *
	 * @return a hash code for this vertex
	 */

	public int hashCode()
	{
	    return id.hashCode();
	}

	/**
	 * Returns a string representation of this vertex.
	 *
	 * @return a string representation of this vertex.
	 */

	public String toString()
	{
	    return id.toString();
	}

	public String toEdgeString()
	{
	    StringBuffer buf = new StringBuffer(id.toString());
	    buf.append(" edges to:[");
	    for (Object o : outgoing)
		{
		    Edge e = (Edge)o;
		    buf.append(e.getOppositeVertex(this).id + ",");
		}
	    buf.append("]");

	    return buf.toString();
	}

	/**
	 * Returns the outdegree of this vertex.
	 *
	 * @return the outdegree of this vertex
	 */

	public int outdegree()
	{
	    return outgoing.size();
	}

	/**
	 * Returns the indegree of this vertex.
	 *
	 * @return the indegree of this vertex
	 */

	public int indegree()
	{
	    return incoming.size();
	}
    }

    /**
     * Constructs an empty graph.
     */

    public Graph()
    {
	verts = new Vector();
	vMap = new HashMap();
    }

    /**
     * Adds the given vertex to this graph.  There is no effect if the
     * vertex is already in this graph.
     *
     * @param v the vertex to add
     * @return the new <CODE>Vertex</CODE>, or <CODE>null</CODE> if the vertex
     * was already present
     */

    public Vertex addVertex(Object v)
    {
	if (!vMap.containsKey(v))
	    {
		// get the next available index and put it in the map

		vMap.put(v, new Integer(verts.size()));
		
		// add v to vertex list
		
		Vertex newV = new Vertex(v);
		verts.add(newV);

		return newV;
	    }
	else
	    return null;
    }

    /**
     * Removes the given vertex and all of its incoming and outgoing
     * edges from this graph.
     * There is no effect if the vertex is not in this graph.
     *
     * @param v the vertex to remove
     */

    public void removeVertex(Object v)
    {
	if (vMap.containsKey(v))
	    {
		Vertex vert = getVertex(v);
		int vIndex = getIndex(v);

		// remove all outgoing edges

		Iterator i = vert.outgoingIterator();
		while (i.hasNext())
		    {
			Edge e = (Edge)(i.next());
			Vertex opp = e.getOppositeVertex(vert);
			removeIncidentEdges(opp.incoming, vert);
		    }

		// remove all incoming edges

		i = vert.incomingIterator();
		while (i.hasNext())
		    {
			Edge e = (Edge)(i.next());
			Vertex opp = e.getOppositeVertex(vert);
			removeIncidentEdges(vert.outgoing, vert);
		    }

		// copy last vertex over v

		verts.setElementAt(verts.elementAt(verts.size() - 1), vIndex);

		// renumber what used to be the last vertex

		vMap.put(((Vertex)(verts.elementAt(vIndex))).getKey(), new Integer(vIndex));

		// remove dead vertex from the map and shorten the vectors

		vMap.remove(v);
		verts.removeElementAt(verts.size() - 1);
	    }
    }

    /**
     * Removes all edges from the given list that are incident on the given
     * vertex.
     *
     * @param l a list of <CODE>Edge</CODE>s
     * @param v a vertex
     */

    private static void removeIncidentEdges(List l, Vertex v)
    {
	Iterator i = l.iterator();
	while (i.hasNext())
	    {
		if (((Edge)(i.next())).incidentOn(v))
		    i.remove();
	    }
    }

    /**
     * Adds the given directed edge to this graph.
     * If the edge already exists, there is no effect.
     * If either vertex does not exist in the graph, it is added.
     *
     * @param src the source vertex of the new edge
     * @param dest the destination vertex of the new edge
     */

    public void addEdge(Object src, Object dest)
    {
	Vertex v1, v2;

	if (!hasVertex(src))
	    {
		v1 = addVertex(src);
	    }
	else
	    v1 = getVertex(src);

	if (!hasVertex(dest))
	    {
		v2 = addVertex(dest);
	    }
	else
	    v2 = getVertex(dest);
	
	addEdge(new DEdge(v1, v2));
    }

    /**
     * Adds the given directed edge to this graph.  If the edge
     * already exists, there is no effect.  If either vertex does not
     * exists, it is added.
     *
     * @param u one incident vertex for the new edge
     * @param v the other incident vertex for the new edge
     */

    public void addUndirectedEdge(Object u, Object v)
    {
	Vertex v1, v2;

	if (!hasVertex(u))
	    v1 = addVertex(u);
	else
	    v1 = getVertex(u);

	if (!hasVertex(v))
	    v2 = addVertex(v);
	else
	    v2 = getVertex(v);
	
	addEdge(new UEdge(v1, v2));
    }

    /**
     * Adds the given weighted, undirected edge to this graph.  If the
     * edge exists, it is replaced with a new edge.  If either vertex
     * does not exists, it is added.
     *
     * @param u one incident vertex for the new edge
     * @param v the other incident vertex for the new edge
     * @param w the weight of the new edge
     */

    public void addUndirectedEdge(Object u, Object v, double w)
    {
	Vertex v1, v2;

	if (!hasVertex(u))
	    v1 = addVertex(u);
	else
	    v1 = getVertex(u);

	if (!hasVertex(v))
	    v2 = addVertex(v);
	else
	    v2 = getVertex(v);
	
	addEdge(new WEdge(v1, v2, w));
    }

    /**
     * Adds the given directed edge to this graph.
     * If the edge exists, it is replaced with the new edge.
     * If either vertex does not exist in the graph, it is added.
     *
     * @param u the source vertex of the new edge
     * @param v the destination vertex of the new edge
     * @param w the weight of the new edge
     */

    public void addEdge(Object u, Object v, double w)
    {
	Vertex v1, v2;

	if (!hasVertex(u))
	    {
		v1 = addVertex(u);
	    }
	else
	    v1 = getVertex(u);

	if (!hasVertex(v))
	    {
		v2 = addVertex(v);
	    }
				
	else
	    v2 = getVertex(v);
	
	addEdge(new WDEdge(v1, v2, w));
    }
    
    /**
     * Adds the given edge to this graph.
     * Both endpoints must already be in this graph.
     *
     * @param e the edge to add
     */

    private void addEdge(Edge e)
    {
	// for every "from" endpoint, add the new edge to outgoing list
	// for from and incoming list of other endpoint

	Collection fromVerts = e.getIncidentFrom();
	Iterator i = fromVerts.iterator();
	while (i.hasNext())
	    {
		// get "from" and "to" for this direction of the edge

		Vertex u = (Vertex)(i.next());
		Vertex v = e.getDestination(u);

		// add edge to outgoing list of source vertex
		
		List out = u.outgoing;
		ListIterator j = out.listIterator();
		Object o = null;
		while (j.hasNext() && !(o = j.next()).equals(e))
		    o = null;
		if (o != null)
		    j.set(e);
		else
		    out.add(e);
		
		// add edge to incoming list of destination vertex
		
		List in = v.incoming;
		j = in.listIterator();
		o = null;
		while (j.hasNext() && !(o = j.next()).equals(e))
		    o = null;
		if (o != null)
		    j.set(e);
		else
		    in.add(e);
	    }
    }

    /**
     * Adds the given edge to this graph, regardless of whether
     * there is already an edge between the corresponding vertices.
     */

    private void addMultiEdge(Edge e)
    {
	// for every "from" endpoint, add the new edge to outgoing list
	// for from and incoming list of other endpoint

	Collection fromVerts = e.getIncidentFrom();
	Iterator i = fromVerts.iterator();
	while (i.hasNext())
	    {
		// get "from" and "to" for this direction of the edge

		Vertex u = (Vertex)(i.next());
		Vertex v = e.getDestination(u);

		// add edge to outgoing list of source vertex
		
		u.outgoing.add(e);
		
		// add edge to incoming list of destination vertex
		
		v.incoming.add(e);
	    }
    }

    /**
     * Determines if this graph contains the given vertex.
     *
     * @param v an object
     * @return true iff that object is a vertex in this graph
     */

    public boolean hasVertex(Object v)
    {
	return vMap.containsKey(v);
    }

    /**
     * Returns the vertex in this graoh with the given key.
     * If no such vertex exists, returns <CODE>null</CODE>.
     *
     * @param key the key of the vertex to retrieve
     * @return the vertex with that key
     */

    public Vertex getVertex(Object key)
    {
	Integer index = (Integer)(vMap.get(key));
	if (index == null)
	    return null;
	else
	    return (Vertex)(verts.elementAt(index.intValue()));
    }

    /**
     * Determines if this graph contains a vertex incident from the
     * first vertex and incident to the second.
     * Returns false if either vertex is not in this graph.
     *
     * @param u the source vertex
     * @param v the destination vertex
     */

    public boolean hasEdge(Object u, Object v)
    {
	return (getEdge(u, v) != null);
    }

    /**
     * Returns the weight of an in this graph that is incident from
     * the first vertex and incident to the second.  Returns 
     * infinity if no such weighted edge exists in this graph.
     * The result is undefined if both a weighted and unweighted edge exist.
     *
     * @param u the key of the source vertex of the edge
     * @param v the key of the destination vertex of the edge
     * @return the weight of the edge
     */

    public double getWeight(Object u, Object v)
    {
	Edge e = getEdge(u, v);
	
	if (e != null && e instanceof WeightedEdge)
	    return ((WeightedEdge)e).getWeight();
	else
	    return Double.POSITIVE_INFINITY;
    }

    /**
     * Returns the first edge found between the given vertices.
     * Returns <CODE>null</CODE> if the given edge does not exist in
     * this graph.
     *
     * @param u the source vertex
     * @param v the destination vertex
     */

    private Edge getEdge(Object u, Object v)
    {
	if (vMap.containsKey(u) && vMap.containsKey(v))
	    {
		Vertex v1 = getVertex(u);
		Vertex v2 = getVertex(v);

		if (v1.outgoing.size() < v2.incoming.size())
		    return searchEdgeList(v1.outgoing, v1, v2);
		else
		    return searchEdgeList(v2.incoming, v1, v2);
	    }
	else
	    return null;
    }
    
    /**
     * Returns the edge from the given list that is incident from u
     * and to v.
     *
     * @param l a list of edges
     * @param u the source vertex to search for
     * @param v the destination vertex to search for
     */

    private static Edge searchEdgeList(List l, Vertex u, Vertex v)
    {
	Iterator i = l.iterator();
	Edge e = null;
	while (i.hasNext()
	       && ((e = (Edge)(i.next())) != null)
	       && (!e.getIncidentFrom().contains(u)
		   || !e.getDestination(u).equals(v)))
	    e = null;
	return e;
    }

    /**
     * Returns the integer index of the given vertex.  Returns
     * <CODE>-1</CODE> if the vertex is not in this graph.
     *
     * @param v the key of the vertex to get the index of
     * @return the index of that vertex
     */

    protected int getIndex(Object v)
    {
	Integer index = (Integer)(vMap.get(v));
	if (index == null)
	    return -1;
	else
	    return index.intValue();
    }

    /**
     * Returns the number of vertices in this graph.
     *
     * @return the number of vertices
     */

    public int countVertices()
    {
	return verts.size();
    }

    /**
     * Returns a string representation of this graph.
     *
     * @return a string representation of this graph
     */

    public String toString()
    {
	StringBuffer result = new StringBuffer();

	for (int vIndex = 0; vIndex < verts.size(); vIndex++)
	    {
		Vertex v = (Vertex)(verts.elementAt(vIndex));

		result.append("Vertex " + vIndex + ": " + v);
		result.append("\n\toutgoing: " + v.outgoing);
		result.append("\n\tincoming: " + v.incoming);
		result.append("\n");
	    }

	return result.toString();
    }

    /**
     * Returns an iterator that will visit all the vertices in this graph
     * as they are finished by a depth first search.
     *
     * @return a depth first iterator
     */

    public DepthFirstIterator depthFirstIterator()
    {
	return new DepthFirstIterator();
    }

    /**
     * Returns an iterator that will visit all the vertices in this
     * graph that are reachable from s.
     *
     * @param s the source vertex
     * @return a depth first iterator
     */

    public DepthFirstIterator depthFirstIterator(Object s)
    {
	return new DepthFirstIterator(s);
    }

    /**
     * Returns an iterator that uses the given visitor.
     *
     * @param visit a visitor
     */
    public DepthFirstIterator depthFirstIteratorWithVisitor(DFSVisitor visit)
    {
        LinkedList l = new LinkedList();
        l.addAll(verts);
        return new DepthFirstIterator(l, DepthFirstIterator.FORWARD, visit);
    }

    /**
     * Returns the strongly connected components of this graph.
     * The object returns is a <CODE>List</CODE> that contains,
     * for each component, a <CODE>List</CODE> of the vertices in
     * that component.
     */

    public List getStronglyConnectedComponents()
    {
	List components = new LinkedList();
	LinkedList finished = new LinkedList();

	// do DFS, getting vertices in order of decreasing finishing time

	DepthFirstIterator i = depthFirstIterator();
	while (i.hasNext())
	    {
		while (i.hasNext())
		    finished.addFirst(i.next());
		i.nextComponent();
	    }
	/*
	System.out.println("scc:" + countVertices() + " vertices total");
	System.out.println("scc:" + finished.size() + " vertices found");
	*/

	// mark all vertices unvisited

	int[] color = new int[countVertices()];
	for (int vIndex = 0; vIndex < countVertices(); vIndex++)
	    color[vIndex] = DepthFirstIterator.WHITE;

	// start DFS from each vertex in order of decreasing finishing time

	i = new DepthFirstIterator(finished, DepthFirstIterator.BACKWARD);
	while (i.hasNext())
	    {
		List component = new LinkedList();
		while (i.hasNext())
		    component.add(i.next());
		components.add(component);
		i.nextComponent();
	    }
	
	return components;
    }

    /**
     * Returns the component graph of this graph.
     *
     * @return the component graph of this graph
     */

    public Graph getComponentGraph()
    {
	Graph result = new Graph();

	List components = getStronglyConnectedComponents();

	// add vertices to the new graph and set up mapping from
	// vertices in the original graph to vertices in the new graph

	HashMap rep = new HashMap(); // maps verts to component (rep in new)
	HashMap index = new HashMap(); // maps verts to index of comp
	int componentCount = 0;

	Iterator i = components.iterator();
	while (i.hasNext())
	    {
		List comp = (List)(i.next());

		result.addVertex(comp);

		Iterator j = comp.iterator();
		while (j.hasNext())
		    {
			Vertex v = (Vertex)(j.next());
			rep.put(v, comp);
			index.put(v, new Integer(componentCount));
		    }

		componentCount++;
	    }

	// for each edge in the original graph, add an edge between
	// representatives in the new graph

	boolean[] seen = new boolean[componentCount];
	for (int c = 0; c < seen.length; c++)
	    seen[c] = false;

	i = result.vertexIterator();
	while (i.hasNext())
	    {
		List comp = (List)(((Vertex)(i.next())).getKey());

		// iterate over all vertices in this component

		Iterator j = comp.iterator();
		while (j.hasNext())
		    {
			Vertex u = (Vertex)(j.next());

			// iterate over all outgoing edges (the result of
			// this loop and the outer one is that we
			// will iterate over all edges out of the
			// current component)

			Iterator k = u.outgoingIterator();
			while (k.hasNext())
			    {
				DirectedEdge e = (DirectedEdge)(k.next());
				Vertex v = e.getDestination();
				Vertex uRep = result.getVertex(rep.get(u));
				Vertex vRep = result.getVertex(rep.get(v));

				if (uRep != vRep && !seen[((Integer)(index.get(v))).intValue()])
				    {
					// use addMultiEdge here because by
					// maintaining seen, we know we're not
					// adding duplicates

					result.addMultiEdge(new DEdge(uRep, vRep));
					seen[((Integer)(index.get(v))).intValue()] = true;
				    }
			    }
		    }

		// clear seen

		j = comp.iterator();
		while (j.hasNext())
		    {
			Vertex u = (Vertex)(j.next());

			Iterator k = u.outgoing.iterator();
			while (k.hasNext())
			    {
				Vertex v = ((DirectedEdge)(k.next())).getDestination();

				seen[((Integer)(index.get(v))).intValue()] = false;
			    }
		    }
	    }

	return result;
    }

    /**
     * Returns an iterator through the edges that 
     * the given vertex is incident from.
     *
     * @param v the key of a vertex
     * @return an iterator through the vertices adjacent to <CODE>v</CODE>
     */

    public Iterator edgeIterator(Object v)
    {
	return getVertex(v).outgoingIterator();
    }


    /**
     * Returns an iterator through this graph's vertices.
     *
     * @return an iterator through this graph's vertices
     */

    public Iterator vertexIterator()
    {
	return verts.iterator();
    }

    /**
     * Returns the outdegree of the given vertex in this graph.  The
     * outdegree of <CODE>v</CODE> is the number of vertices that are
     * adjacent to <CODE>v</CODE>.  Returns 0 if the vertex is not in
     * this graph.
     *
     * @param v the key of a vertex
     * @return the outdegree of that vertex
     */

    public int getOutdegree(Object v)
    {
	if (vMap.containsKey(v))
	    return getVertex(v).outgoing.size();
	else
	    return 0;
    }

    /**
     * Returns the indegree of the given vertex in this graph.  The
     * indegree of <CODE>v</CODE> is the number of vertices
     * <CODE>v</CODE> is adjacent to.  Returns 0 if the vertex is not in
     * this graph.
     *
     * @param v the key of a vertex
     * @return the indegree of that vertex
     */

    public int getIndegree(Object v)
    {
	if (vMap.containsKey(v))
	    return getVertex(v).incoming.size();
	else
	    return 0;
    }

    /**
     * Returns a path from the given source to the given destination in this
     * graph, or <CODE>null</CODE> if no such path exists.
     *
     * @param src the key of the source vertex
     * @param dest the key of the destination vertex
     * @return a path from the source to the destination
     */

    public List findPath(Object src, Object dest)
    {
	Vertex destV = getVertex(dest);

	if (hasVertex(src) && hasVertex(dest))
	    {
		DepthFirstIterator i = new DepthFirstIterator(src);
		Vertex v = null;

		while (i.hasNext() && !(v = (Vertex)(i.next())).equals(destV))
		    v = null;

		if (v != null)
		    {
			List path = i.getPath();
			path.add(dest);
			return path;
		    }
		else
		    return null;

	    }
	else
	    return null;
    }

    /**
     * Returns the shortest paths tree starting from the given vertex
     * in this graph.  All vertices are included in the resulting graph
     * whether they are reachable from the source or not.  
     * The resulting graph is constructed so that <CODE>findPath</CODE>
     * in that graph returns the shortest path in this graph.  This
     * method may not return the actual shortest paths if this graph
     * contains negative weight edges.
     *
     * @param src the key of a vertex in this graph
     * @return the shortest paths tree starting from that vertex
     */

    public Graph dijkstraShortestPaths(Object src)
    {
	// initialize Dijkstra's algorithm

	PriorityQueue pq = new PriorityQueue();
	HashSet finished = new HashSet();
	HashMap pi = new HashMap();
	HashMap dist = new HashMap();

	pq.addItem(getVertex(src), 0.0);
	while (pq.getSize() > 0)
	    {
		double d = pq.peekPriority();
		Vertex u = (Vertex)(pq.removeItem());
		finished.add(u);
		dist.put(u, new Double(d));

		Iterator i = u.outgoingIterator();
		while (i.hasNext())
		    {
			WeightedEdge e = (WeightedEdge)(i.next());
			Vertex v = e.getDestination(u);
			double w = e.getWeight();

			if (!finished.contains(v)
			    && pq.decreasePriority(v, d + w))
			    pi.put(v, u);
		    }
	    }

	// make a new graph with the same vertices as this one and edges from
	// the predecessor table pi

	Graph t = new Graph();
	Iterator i = vertexIterator();
	while (i.hasNext())
	    {
		Vertex v = (Vertex)(i.next());
		t.addVertex(v.getKey());
		if (pi.containsKey(v))
		    t.addEdge(((Vertex)(pi.get(v))).getKey(),
			      v.getKey(),
			      getWeight(((Vertex)(pi.get(v))).getKey(),
					v.getKey()));
	    }

	return t;
    }

    public ShortestPaths getDijkstraShortestPaths(Object src)
    {
	// initialize Dijkstra's algorithm

	PQueue<Vertex,Double> pq = new PQueue<Vertex, Double>();
	Set<Vertex> finished = new HashSet<Vertex>();
	Map<Vertex, Vertex> pi = new HashMap<Vertex,Vertex>();
	HashMap<Vertex,Double> dist = new HashMap<Vertex,Double>();

	pq.addItem(getVertex(src), 0.0);
	pi.put(getVertex(src), null);
	while (pq.getSize() > 0)
	    {
		double d = pq.peekPriority();
		Vertex u = pq.removeItem();
		finished.add(u);
		dist.put(u, d);

		Iterator i = u.outgoingIterator();
		while (i.hasNext())
		    {
			WeightedEdge e = (WeightedEdge)(i.next());
			Vertex v = e.getDestination(u);
			double w = e.getWeight();

			if (!finished.contains(v)
			    && pq.decreasePriority(v, d + w))
			    pi.put(v, u);
		    }
	    }

	return new ShortestPaths(dist, pi);
    }

    public class ShortestPaths
    {
	/**
	 * distances for each vertex
	 */
	private Map<Vertex,Double> dist;

	/**
	 * presecessors on shortest paths for each vertex
	 */
	private Map<Vertex,Vertex> pi;

	public ShortestPaths(Map<Vertex, Double> d, Map<Vertex, Vertex> p)
	{
	    dist = d;
	    pi = p;
	}

	/**
	 * Returns the distance from the source to the given vertex.
	 */
	public double getDistance(Object v)
	{
	    v = getVertex(v);
	    if (dist.containsKey(v))
		{
		    return dist.get(v);
		}
	    else
		{
		    return Double.POSITIVE_INFINITY;
		}
	}

	/**
	 * Returns the shortest path from the source to the given vertex, or null if
	 * there is no such path.
	 */
	public List<?> getPath(Object v)
	{
	    if (pi.containsKey(getVertex(v)))
		{
		    List<Object> path = new LinkedList<Object>();
		    path.add(v);
		    Vertex u = getVertex(v);
		    while (u != null)
			{
			    if (pi.containsKey(u))
				{
				    u = pi.get(u);
				    if (u != null)
					{
					    path.add(0, u.getKey());
					}
				}
			    else
				{
				    u = null;
				}
			}

		    return path;
		}
	    else
		{
		    return null;
		}
	}

	public String toString()
	{
	    return dist.toString() + "\n" + pi.toString();
	}
    }

    /**
     * Topologically sorts this graph.  If this graph is not a DAG
     * then bad things will happen.
     *
     * @return a list of vertices in topological order from sources
     * to sinks
     */

    public List topologicalSort()
    {
	LinkedList result = new LinkedList();

	Iterator i = depthFirstIterator();
	while (i.hasNext())
	    result.addFirst(i.next());

	return result;
    }

    public static void main(String[] args)
    {
	// Dijkstra example from Ch. 24 of CLRS

	Graph g = new Graph();

	g.addEdge("s", "t", 10);
	g.addEdge("s", "y", 5);
	g.addEdge("t", "y", 2);
	g.addEdge("t", "x", 1);
	g.addEdge("y", "t", 3);
	g.addEdge("y", "x", 9);
	g.addEdge("y", "z", 2);
	g.addEdge("x", "z", 4);
	g.addEdge("z", "x", 6);
	g.addEdge("z", "s", 7);


	System.out.println(g.getEdge("x", "z"));
	System.out.println(g.getEdge("z", "x"));
	// extra vertex to test unreachable vertices

	g.addVertex("w");

	Graph t = g.dijkstraShortestPaths("s");
	ShortestPaths sp = g.getDijkstraShortestPaths("s");
	System.out.println(sp);

	System.out.println(t.findPath("s", "s"));
	System.out.println(sp.getPath("s"));

	System.out.println(t.findPath("s", "t"));
	System.out.println(sp.getPath("t"));

	System.out.println(t.findPath("s", "w"));
	System.out.println(sp.getPath("w"));

	System.out.println(t.findPath("s", "x"));
	System.out.println(sp.getPath("x"));

	System.out.println(t.findPath("s", "y"));
	System.out.println(sp.getPath("y"));

	System.out.println(t.findPath("s", "z"));
	System.out.println(sp.getPath("z"));

	// SCC example from Ch. 22 of CLRS

	Graph scc = new Graph();
	scc.addEdge("a", "b");
	scc.addEdge("b", "e");
	scc.addEdge("e", "a");
	scc.addEdge("b", "f");
	scc.addEdge("e", "f");
	scc.addEdge("b", "c");
	scc.addEdge("c", "d");
	scc.addEdge("d", "c");
	scc.addEdge("c", "g");
	scc.addEdge("d", "h");
	scc.addEdge("f", "g");
	scc.addEdge("g", "f");
	scc.addEdge("g", "h");

	System.out.println(scc.getComponentGraph());
	System.out.println(scc.getComponentGraph().topologicalSort());
    }
}
