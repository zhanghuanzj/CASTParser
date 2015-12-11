package com.iseu.MDGHandle.Edges;

import storage.DGEdge;

import com.iseu.MDGHandle.Nodes.Node;

public class Edge {
	private Node from;
	private Node to;
	ThreadEdgeType type;
	DGEdge edgeType;
	
	public Node getFrom() {
		return from;
	}

	public void setFrom(Node from) {
		this.from = from;
	}

	public Node getTo() {
		return to;
	}

	public void setTo(Node to) {
		this.to = to;
	}

	public ThreadEdgeType getType() {
		return type;
	}

	public void setType(ThreadEdgeType type) {
		this.type = type;
	}

	public DGEdge getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(DGEdge edgeType) {
		this.edgeType = edgeType;
	}

	public Edge(Node from,Node to,ThreadEdgeType type) {
		this.from = from;
		this.to = to;
		this.type = type;
	}
	
	public Edge(Node from,Node to,DGEdge type) {
		this.from = from;
		this.to = to;
		this.edgeType = type;
	}
	@Override
	public String toString() {
		return "ThreadEdgeType: "+type+"\n{\nFROM:\n"+from+"\nTO:\n"+to+"\n}";
	}
}
