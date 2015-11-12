package storage;

import org.neo4j.graphdb.Node;

/**
 * 依赖图节点接口
 * @author think
 *
 */
public interface StDGNode {
	public void setDGNode(Node node);
	public StDGNode getDGNode(Node node);
}
