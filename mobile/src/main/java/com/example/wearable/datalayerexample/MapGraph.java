package com.example.wearable.datalayerexample;

import com.example.wearable.datalayerexample.GNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by 김영훈 on 2015-04-28.
 */
public class MapGraph {
    final static int INF = 99999;
    int num = 0;
    int max;
    int edgeArr[][];
    private GNode VertexArr[];

    int select;
    int distance[];
    boolean found[];

    private Map<Integer, Integer> predecessors;

    public MapGraph (int all) {
        max = all;
        edgeArr = new int[all][all];
        for(int i=0; i<all; i++)
            for(int j=0; j<all; j++)
                edgeArr[i][j] = INF;
        VertexArr = new GNode[all];

        distance = new int[all];
        found = new boolean[all];
    }

    public boolean insertVertex (int no, GNode node) {
        if (num == max)
            return false;
        VertexArr[no] = node;
        return true;
    }

    public boolean insertEdge (int node1, int node2, int weight) {
        if ((VertexArr[node1] == null) || (VertexArr[node2] == null))
            return false;
        edgeArr[node1][node2] = weight;
        edgeArr[node2][node1] = weight;
        return true;
    }

    public GNode getVertex (int no) {
        return VertexArr[no];
    }

    public int getEdge (int node1, int node2) {
        return edgeArr[node1][node2];
    }

    public void executeDijk(int start) {
        int u;
        predecessors = new HashMap<Integer, Integer>();

        select = start;

        for(int i=0; i<max; i++) {
            distance[i] = edgeArr[start][i];
            found[i] = false;
        }
        found[start] = true;
        distance[start] = 0;
        for(int i=0; i<max-2; i++) {
            u = choose(distance, found);
            found[u] = true;
            for(int w=0; w<max; w++)
                if(!found[w])
                    if(distance[u]+edgeArr[u][w]<distance[w]) {
                        distance[w] = distance[u] + edgeArr[u][w];
                        predecessors.put(new Integer(w), new Integer(u));
                    }
        }
    }

    private int choose (int distance[], boolean found[]) {
        int min, pos;
        min = 2147483647;
        pos = -1;
        for (int i=0; i<max; i++)
            if ( distance[i] < min && !found[i] ) {
                min = distance[i];
                pos = i;
            }
        return pos;
    }

    public LinkedList<GNode> getPath (int target) {
        LinkedList<GNode> path = new LinkedList<GNode>();
        int step = target;
        if (predecessors.get(new Integer(step)) == null) {
            return null;
        }

        path.add(VertexArr[target]);
        while (predecessors.get(new Integer(step)) != null) {
            step = predecessors.get(new Integer(step));
            path.add(VertexArr[step]);
        }
        path.add(VertexArr[select]);

        Collections.reverse(path);
        return path;
    }

}
