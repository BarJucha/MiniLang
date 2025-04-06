package gp;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
public class NodeDeserializer {

    public static Node deserialize(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String data = reader.readLine();
            return deserializeNode(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Node deserializeNode(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        int colonIndex = data.indexOf(':');
        int openBracketIndex = data.indexOf('[');
        String nodeInfo = colonIndex != -1 ? data.substring(0, colonIndex) : data;
        String[] parts = nodeInfo.split(",");

        String value = parts[0];
        String nodeType = parts[1];
        int depth = Integer.parseInt(parts[2]);
        boolean canMutate = Boolean.parseBoolean(parts[3]);

        Node node = new Node(value, nodeType, null, depth, canMutate);

        if (colonIndex != -1 && openBracketIndex != -1) {
            String childrenData = data.substring(openBracketIndex + 1, data.length() - 1); // wycięcie dzieci
            String[] childNodes = splitChildren(childrenData);
            for (String child : childNodes) {
                node.children.add(deserializeNode(child));
            }
        }

        return node;
    }

    private static String[] splitChildren(String data) {
        List<String> children = new ArrayList<>();
        int bracketCount = 0;
        StringBuilder current = new StringBuilder();
        for (char c : data.toCharArray()) {
            if (c == '[') bracketCount++;
            if (c == ']') bracketCount--;
            if (c == ',' && bracketCount == 0) {
                children.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            children.add(current.toString());
        }
        return children.toArray(new String[0]);
    }
}

