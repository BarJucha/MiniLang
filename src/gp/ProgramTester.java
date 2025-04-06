package gp;
import java.util.*;

public class ProgramTester {
    private Map<String, Integer> variables = new HashMap<>();
    private List<Integer> outputs = new ArrayList<>();
    private List<Integer> inputs = new ArrayList<>();
    public int penalty = 0;
    private static final int MAX_ITERATIONS = 100;
    private int iterations = 0; // Liczba wykonanych operacji
    private final int MAX_GLOBAL_ITERATIONS = 1000;


    public List<Integer> testProgram(Node program, List<Integer> input) {
        variables.clear();
        outputs.clear();

        this.inputs.addAll(input);

        executeNode(program);

        return getOutputs();
    }

    private void executeNode(Node node) {
        if (iterations >= MAX_ITERATIONS) {
            penalty += 1000;
            return;
        }

        iterations++;
        switch (node.value) {
            case "block":
                int currentIt = 0;
                for (Node child : node.children) {
                    currentIt += 1;
                    executeNode(child);
                    if(currentIt >= MAX_ITERATIONS){
                        penalty += 100;
                        break;
                    }
                }
                break;

            case "assign":
                String variableName = node.children.get(0).value;
                int value = evaluateExpression(node.children.get(1));
                variables.put(variableName, value);
                break;

            case "if":
                if (evaluateComparison(node.children.get(0))) {
                    executeNode(node.children.get(1));
                }
                else{
                    executeNode(node.children.get(2));
                }
                break;

            case "while":
                int currentIt2 = 0;
                while (evaluateComparison(node.children.get(0))) {
                    currentIt2 += 1;
                    executeNode(node.children.get(1));
                    if (currentIt2 >= MAX_ITERATIONS){
                        penalty += 100;
                        break;
                    }
                }
                break;

            case "output":
                int outputValue = evaluateExpression(node.children.get(0));
                outputs.add(outputValue);
                break;

            default:
                throw new IllegalArgumentException("Unsupported operation: " + node.value);
        }
    }

    private boolean evaluateComparison(Node node) {

        switch (node.value) {
            case "<": return evaluateExpression(node.children.get(0)) < evaluateExpression(node.children.get(1));
            case ">": return evaluateExpression(node.children.get(0)) > evaluateExpression(node.children.get(1));
            case "==": return evaluateExpression(node.children.get(0)) == evaluateExpression(node.children.get(1));
            case "!=": return evaluateExpression(node.children.get(0)) != evaluateExpression(node.children.get(1));
            case "<=": return evaluateExpression(node.children.get(0)) <= evaluateExpression(node.children.get(1));
            case ">=": return evaluateExpression(node.children.get(0)) >= evaluateExpression(node.children.get(1));
            case "&": return evaluateComparison(node.children.get(0)) & evaluateComparison(node.children.get(1));
            case "|": return evaluateComparison(node.children.get(0)) | evaluateComparison(node.children.get(1));
            default:
                throw new IllegalArgumentException("Unsupported comparison: " + node.value);
        }
    }

    private int evaluateExpression(Node node) {
        switch (node.nodeType) {
            case "leaf":
                if (variables.containsKey(node.value)) {
                    return variables.get(node.value);
                }
                if (node.value.contains("input")){
                    int input_num = Integer.parseInt(node.value.substring(5));
                    return inputs.get(input_num - 1);
                }
                else {
                    if(isNumeric(node.value)){
                        return Integer.parseInt(node.value);
                    }
                    else{
                        penalty += 100;
                        return 0;
                    }
                }

            case "operator":
                switch (node.value) {
                    case "+": return evaluateExpression(node.children.get(0)) + evaluateExpression(node.children.get(1));
                    case "-": return evaluateExpression(node.children.get(0)) - evaluateExpression(node.children.get(1));
                    case "*": return evaluateExpression(node.children.get(0)) * evaluateExpression(node.children.get(1));
                    case "/":
                        if (evaluateExpression(node.children.get(1)) == 0) {
                            return evaluateExpression(node.children.get(0));
                        }
                        return evaluateExpression(node.children.get(0)) / evaluateExpression(node.children.get(1));
                    default:
                        throw new IllegalArgumentException("Unsupported operator: " + node.value);
                }

            default:
                throw new IllegalArgumentException("Unsupported expression type: " + node.nodeType);
        }
    }

    public List<Integer> getOutputs(){
        return this.outputs;
    }
    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str); // Próba konwersji na liczbę
            return true;
        } catch (NumberFormatException e) {
            return false; // Nie jest liczbą
        }
    }
}
