package gp;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public String value;
    public String nodeType;
    public List<Node> children;
    public int depth;
    public boolean canMutate;

    public Node(String value, String nodeType, List<Node> children, int depth) {
        this.value = value;
        this.nodeType = nodeType;
        this.children = children != null ? children : new ArrayList<>();
        this.depth = depth;
        this.canMutate = true;
    }

    public Node(String value, String nodeType) {
        this(value, nodeType, null, 0);
    }
    public Node(String value, String nodeType, List<Node> children, int depth, boolean mutate) {
        this.value = value;
        this.nodeType = nodeType;
        this.children = children != null ? children : new ArrayList<>();
        this.depth = depth;
        this.canMutate = mutate;
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return value;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (!this.value.equals("block")){sb.append(value);}
        for (Node child : children) {
            sb.append(" ").append(child);
        }
        sb.append(")");
        return sb.toString();
    }
}
