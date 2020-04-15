package com.sirmapsalot.combinatorics;

import java.util.Scanner;

public class BCC
{
    public static void main(String[] args)
    {

        Graph g = new Graph();
        
        // read in graph
        Scanner in = new Scanner(System.in);

        while (in.hasNext())
            {
                g.addUndirectedEdge(in.next(), in.next());
            }

        Graph.BCCDFSVisitor visitor = new Graph.BCCDFSVisitor();
        Graph.DepthFirstIterator i = g.depthFirstIteratorWithVisitor(visitor);

        i.depthFirstSearch();

        System.out.println(visitor.getArticulationPoints());
    }
}

        
        