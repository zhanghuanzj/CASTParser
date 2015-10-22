package com.iseu.MDGHandle.Edges;

import com.iseu.MDGHandle.Nodes.Node;

public class Edge {
	private Node from;
	private Node to;
	ThreadEdgeType type;
	public Edge(Node from,Node to,ThreadEdgeType type) {
		this.from = from;
		this.to = to;
		this.type = type;
	}
	@Override
	public String toString() {
		return "ThreadEdgeType: "+type+"\n{\nFROM:\n"+from+"\nTO:\n"+to+"\n}";
	}
}
