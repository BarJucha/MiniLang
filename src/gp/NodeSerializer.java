package gp;

import java.io.*;
import java.util.List;

public class NodeSerializer {

    public static void serialize(Node node, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            serializeNode(node, writer);
            System.out.println("Node serialized to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void serializeNode(Node node, PrintWriter writer) {
        writer.print(node.value + "," + node.nodeType + "," + node.depth + "," + node.canMutate);
        if (!node.children.isEmpty()) {
            writer.print(":[");
            for (int i = 0; i < node.children.size(); i++) {
                serializeNode(node.children.get(i), writer);
                if (i < node.children.size() - 1) {
                    writer.print(",");
                }
            }
            writer.print("]");
        }
    }
}

