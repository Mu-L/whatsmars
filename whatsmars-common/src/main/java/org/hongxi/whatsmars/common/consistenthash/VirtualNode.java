package org.hongxi.whatsmars.common.consistenthash;

/**
 * Virtual node on a consistent hash ring, mapping to a physical node.
 *
 * <p>Each physical node can have multiple virtual nodes (replicas) to
 * achieve a more uniform distribution of keys across the ring.</p>
 *
 * @param <T> the type of the underlying physical node
 */
public class VirtualNode<T extends Node> implements Node {
    final T physicalNode;
    final int replicaIndex;

    public VirtualNode(T physicalNode, int replicaIndex) {
        this.replicaIndex = replicaIndex;
        this.physicalNode = physicalNode;
    }

    @Override
    public String getKey() {
        return physicalNode.getKey() + "-" + replicaIndex;
    }

    public boolean isVirtualNodeOf(T pNode) {
        return physicalNode.getKey().equals(pNode.getKey());
    }

    public T getPhysicalNode() {
        return physicalNode;
    }
}
