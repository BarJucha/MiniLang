package gp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.lang.Math;

public class Program {
    public Node tree;
    public Double fit = 0.0;
    private List<List<Integer>> inputs = new ArrayList<>();
    private List<List<Integer>> expectedOutputs = new ArrayList<>();
    private List<List<Integer>> outputs = new ArrayList<>();
    private int varNum;
    private int maxDepth;
    private List<String> variables = new ArrayList<>();
    private int floor;
    private int ceil;

    public Program(int maxDepth, int varNum, int floor, int ceil){
        this.maxDepth = maxDepth;
        this.varNum = varNum;
        this.floor = floor;
        this.ceil = ceil;
    }
    public Program(Program p){
        this.tree = deepCopyNode(p.tree);
        this.fit = 0.0;
        this.varNum = p.varNum;
        this.maxDepth = p.maxDepth;
        this.floor = p.floor;
        this.ceil = p.ceil;
        variables = p.getVariables();
    }
    public void setMaxDepth(int depth){maxDepth = depth;}
    public void genProgram(){
        this.tree = generateRandomProgram(0, "start", null);
        calculateOutputs();
        calculateFitness();
    }

    public void genProgramGrwo(){
        this.tree = mutateRandomProgram(0, "start", null);
        calculateOutputs();
        calculateFitness();
    }

    public Node generateRandomProgram(int depth, String specific, String concrete) {
        if ("start".equals(specific)){
            List<Node> blockChildren = new ArrayList<>();
            for (int i=0; i<this.varNum; i++){
                String variable = "x" + i;
                this.variables.add(variable);
                int variable_ = new Random().nextInt(ceil + Math.abs(floor)) + floor;
                blockChildren.add(new Node("assign", "block", new ArrayList<>(Arrays.asList(
                        new Node(variable, "leaf", null, depth + 2, false),
                        new Node(""+(variable_), "leaf", null, depth+2, false)
                )), depth + 1, false));
            }
            int numBlocks = new Random().nextInt(2) + 2;
            for (int i = 0; i < numBlocks; i++) {
                blockChildren.add(generateRandomProgram(depth + 1, null, null));
            }
            return new Node("block", "block", blockChildren, depth, false);

        }
        if ("leaf".equals(specific)) {
            if("var".equals(concrete)){
                String variable = variables.get(new Random().nextInt(variables.size()));
                return new Node(variable, "leaf", null, depth);
            }
            if(new Random().nextDouble() < 0.5){
                return new Node(""+(new Random().nextInt(ceil + Math.abs(floor)) + floor), "leaf", null, depth);
            }
            else{
                if((new Random().nextDouble() < 0.5 | inputs.isEmpty()) && !variables.isEmpty()){
                    String variable = variables.get(new Random().nextInt(variables.size()));
                    return new Node(variable, "leaf", null,  depth);
                }
                else{
                    String inputVar = "input" + (new Random().nextInt(inputs.get(0).size()) + 1);
                    return new Node(inputVar, "leaf", null,  depth);
                }
            }
        }

        if ("compare".equals(specific)) {
            if(depth < this.maxDepth - 2 & new Random().nextDouble() > 0.8){
                String[] operators = {"|", "&"};
                String op = operators[new Random().nextInt(operators.length)];
                return new Node(op, "compare", new ArrayList<>(Arrays.asList(
                        generateRandomProgram(depth + 1, "compare", null),
                        generateRandomProgram(depth + 1, "compare", null)
                )), depth);
            }
            else{
                String[] operators = {">", "<", "==", "!=", ">="};
                String op = operators[new Random().nextInt(operators.length)];
                if(new Random().nextDouble() > 0.5){
                    return new Node(op, "compare", new ArrayList<>(Arrays.asList(
                            generateRandomProgram(depth + 1, "operator", null),
                            generateRandomProgram(depth + 1, "operator", null)
                    )), depth);
                }
                else{
                    return new Node(op, "compare", new ArrayList<>(Arrays.asList(
                            generateRandomProgram(depth + 1, "leaf", null),
                            generateRandomProgram(depth + 1, "leaf", null)
                    )), depth);
                }
            }
        }

        if ("operator".equals(specific)) {
            String[] operators = {"+", "-", "*", "/"};
            String op = operators[new Random().nextInt(operators.length)];
            if(depth < this.maxDepth - 2 & new Random().nextDouble() < 0.8){
                return new Node(op, "operator", new ArrayList<>(Arrays.asList(
                        generateRandomProgram(depth + 1, "operator", null),
                        generateRandomProgram(depth + 1, "operator", null)
                )), depth);
            }
            else {
                return new Node(op, "operator", new ArrayList<>(Arrays.asList(
                        generateRandomProgram(depth + 1, "leaf", null),
                        generateRandomProgram(depth + 1, "leaf", null)
                )), depth);
            }
        }

        String[] instructions = {"output"};
        if(this.varNum > 0){
            instructions = Arrays.copyOf(instructions, instructions.length + 1);
            instructions[instructions.length - 1] = "assign";
        }
        if((depth + 2) < this.maxDepth){
            instructions = Arrays.copyOf(instructions, instructions.length + 3); // powiększamy tablicę o 1 element
            instructions[instructions.length - 3] = "block";
            instructions[instructions.length - 2] = "if";
            instructions[instructions.length - 1] = "while";
        }
        String instruction = instructions[new Random().nextInt(instructions.length)];

        switch (instruction) {
            case "block":
                int numBlocks = new Random().nextInt(2) + 2;
                List<Node> blockChildren = new ArrayList<>();
                for (int i = 0; i < numBlocks; i++) {
                    blockChildren.add(generateRandomProgram(depth + 1, null, null));
                }
                return new Node("block", "block", blockChildren,  depth);

            case "assign":
                return new Node("assign", "block", new ArrayList<>(Arrays.asList(
                        generateRandomProgram(depth + 1, "leaf", "var"),
                        generateRandomProgram(depth + 1, new Random().nextBoolean() ? "operator" : "leaf", null)
                )), depth);

            case "if":
                return new Node(instruction, "block", new ArrayList<>(Arrays.asList(
                        generateRandomProgram(depth + 1, "compare", null),
                        generateRandomProgram(depth + 1, null, null),
                        new Node("block", "block", new ArrayList<>(Arrays.asList(
                                generateRandomProgram(depth + 2, null, null)
                        )), depth + 1)
                )),  depth);
            case "while":
                return new Node(instruction, "block", new ArrayList<>(Arrays.asList(
                        generateRandomProgram(depth + 1, "compare", null),
                        generateRandomProgram(depth + 1, null, null)
                )),  depth);

            case "output":
                return new Node("output", "block", Collections.singletonList(
                        generateRandomProgram(depth + 1, "leaf", null)
                ),  depth);
        }
        return null;
    }
    public Node mutateRandomProgram(int depth, String specific, String concrete) {
        if ("start".equals(specific)){
            List<Node> blockChildren = new ArrayList<>();
            for (int i=0; i<this.varNum; i++){
                String variable = "x" + i;
                this.variables.add(variable);
                int variable_ = new Random().nextInt(ceil + Math.abs(floor)) + floor;
                blockChildren.add(new Node("assign", "block", new ArrayList<>(Arrays.asList(
                        new Node(variable, "leaf", null, depth + 2, false),
                        new Node(""+(variable_), "leaf", null, depth+2, false)
                )), depth + 1, false));
            }
            int numBlocks = new Random().nextInt(2) + 2;
            for (int i = 0; i < numBlocks; i++) {
                blockChildren.add(mutateRandomProgram(depth + 1, null, null));
            }
            return new Node("block", "block", blockChildren, depth, false);

        }
        if ("leaf".equals(specific)) {
            if("var".equals(concrete)){
                String variable = variables.get(new Random().nextInt(variables.size()));
                return new Node(variable, "leaf", null, depth);
            }
            if(new Random().nextDouble() < 0.5){
                return new Node(""+(new Random().nextInt(ceil + Math.abs(floor)) + floor), "leaf", null, depth);
            }
            else{
                if((new Random().nextDouble() < 0.5 | inputs.isEmpty()) && !variables.isEmpty()){
                    String variable = variables.get(new Random().nextInt(variables.size()));
                    return new Node(variable, "leaf", null,  depth);
                }
                else{
                    String inputVar = "input" + (new Random().nextInt(inputs.get(0).size()) + 1);
                    return new Node(inputVar, "leaf", null,  depth);
                }
            }
        }

        if ("compare".equals(specific)) {
            if(depth < this.maxDepth - 2 & new Random().nextDouble() > 0.7){
                String[] operators = {"|", "&"};
                String op = operators[new Random().nextInt(operators.length)];
                return new Node(op, "compare", new ArrayList<>(Arrays.asList(
                        mutateRandomProgram(depth + 1, "compare", null),
                        mutateRandomProgram(depth + 1, "compare", null)
                )), depth);
            }
            else{
                String[] operators = {">", "<", "==", "!=", ">="};
                String op = operators[new Random().nextInt(operators.length)];
                if(new Random().nextDouble() > 0.4){
                    return new Node(op, "compare", new ArrayList<>(Arrays.asList(
                            mutateRandomProgram(depth + 1, "operator", null),
                            mutateRandomProgram(depth + 1, "operator", null)
                    )), depth);
                }
                else{
                    return new Node(op, "compare", new ArrayList<>(Arrays.asList(
                            mutateRandomProgram(depth + 1, "leaf", null),
                            mutateRandomProgram(depth + 1, "leaf", null)
                    )), depth);
                }
            }
        }

        if ("operator".equals(specific)) {
            String[] operators = {"+", "-", "*", "/"};
            String op = operators[new Random().nextInt(operators.length)];
            if(depth < this.maxDepth - 2 & new Random().nextDouble() < 0.3){
                return new Node(op, "operator", new ArrayList<>(Arrays.asList(
                        mutateRandomProgram(depth + 1, "operator", null),
                        mutateRandomProgram(depth + 1, "operator", null)
                )), depth);
            }
            else {
                return new Node(op, "operator", new ArrayList<>(Arrays.asList(
                        mutateRandomProgram(depth + 1, "leaf", null),
                        mutateRandomProgram(depth + 1, "leaf", null)
                )), depth);
            }
        }

        String[] instructions = {"output"};
        if(this.varNum > 0){
            instructions = Arrays.copyOf(instructions, instructions.length + 1);
            instructions[instructions.length - 1] = "assign";
        }
        if((depth + 2) < this.maxDepth & new Random().nextDouble() < 0.3){
            instructions = Arrays.copyOf(instructions, instructions.length + 3); // powiększamy tablicę o 1 element
            instructions[instructions.length - 3] = "block";
            instructions[instructions.length - 2] = "if";
            instructions[instructions.length - 1] = "while";
        }
        String instruction = instructions[new Random().nextInt(instructions.length)];

        switch (instruction) {
            case "block":
                int numBlocks = new Random().nextInt(2) + 2;
                List<Node> blockChildren = new ArrayList<>();
                for (int i = 0; i < numBlocks; i++) {
                    blockChildren.add(mutateRandomProgram(depth + 1, null, null));
                }
                return new Node("block", "block", blockChildren,  depth);

            case "assign":
                return new Node("assign", "block", new ArrayList<>(Arrays.asList(
                        mutateRandomProgram(depth + 1, "leaf", "var"),
                        mutateRandomProgram(depth + 1, new Random().nextBoolean() ? "operator" : "leaf", null)
                )), depth);

            case "if":
                return new Node(instruction, "block", new ArrayList<>(Arrays.asList(
                        mutateRandomProgram(depth + 1, "compare", null),
                        mutateRandomProgram(depth + 1, null, null),
                        new Node("block", "block", new ArrayList<>(Arrays.asList(
                                mutateRandomProgram(depth + 2, null, null)
                        )), depth + 1)
                )),  depth);
            case "while":
                return new Node(instruction, "block", new ArrayList<>(Arrays.asList(
                        mutateRandomProgram(depth + 1, "compare", null),
                        mutateRandomProgram(depth + 1, null, null)
                )),  depth);

            case "output":
                return new Node("output", "block", Collections.singletonList(
                        mutateRandomProgram(depth + 1, "leaf", null)
                ),  depth);
        }
        return null;
    }
    public void setTree(Node tree){
        this.tree = tree;
    }
    public void calculateOutputs(){
        for(int i=0; i<this.inputs.size(); i++){
            ProgramTester pt = new ProgramTester();
            this.outputs.add(pt.testProgram(this.tree, this.inputs.get(i)));
            this.fit += pt.penalty;
        }
    }
    public void calculateFitness(){
        for(int i =0 ; i<expectedOutputs.size(); i++){
            calcFit(expectedOutputs.get(i), outputs.get(i));
        }
    }

    private void calcFit(List<Integer> expectedOutputs, List<Integer> outputs){
        if(outputs.isEmpty()){
            this.fit += 10;
        }
        else{
            if(outputs.size() > expectedOutputs.size()){
                for(int j=0; j<expectedOutputs.size(); j++){
                    this.fit += Math.abs(outputs.get(j) -
                            expectedOutputs.get(j));
                }
                this.fit += outputs.size() - expectedOutputs.size();
            }
            else{
                for(int j=0; j<outputs.size(); j++){
                    this.fit += Math.abs(outputs.get(j) -
                            expectedOutputs.get(j));
                }
                this.fit +=  expectedOutputs.size() - outputs.size();
            }
        }
    }

    public Double getFit() {
        return fit;
    }
    public Node getTree(){return tree;}
    public int getMaxDepth(){return maxDepth;}
    public int getVarNum(){return  varNum;}
    public List<List<Integer>> getInputs(){return inputs;}
    public List<List<Integer>> getExpectedOutputs(){return expectedOutputs;}
    public List<List<Integer>> getOutputs(){return outputs;}
    public List<String> getVariables(){return variables;}
    public void setInputsOutputs(String filename){
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<Integer> inp = new ArrayList<>();
                List<Integer> out = new ArrayList<>();
                String[] parts = line.split(";");
                if (parts.length < 2 || parts.length > 3) {
                    throw new IllegalArgumentException("Invalid format: missing ';'");
                }
                for (String input : parts[0].split(",")) {
                    inp.add(Integer.parseInt(input.trim()));
                }
                for (String output : parts[1].split(",")) {
                    out.add(Integer.parseInt(output.trim()));
                }
                this.inputs.add(inp);
                this.expectedOutputs.add(out);
            }
        } catch (IOException e) {
            System.err.println("File not found: " + e.getMessage());
        }
    }
    public void setInputsOutputsSub(String filename, List<List<Integer>> inputs){
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<Integer> out = new ArrayList<>();
                String[] parts = line.split(";");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid format: missing ';'");
                }
                for (String output : parts[2].split(",")) {
                    out.add(Integer.parseInt(output.trim()));
                }
                this.expectedOutputs.add(out);
            }
        } catch (IOException e) {
            System.err.println("File not found: " + e.getMessage());
        }
        this.inputs = new ArrayList<>();

        for (List<Integer> input : inputs) {
            List<Integer> oneIn = new ArrayList<>(input);
            this.inputs.add(new ArrayList<>(oneIn));
        }
    }
    private Node deepCopyNode(Node original) {
        if (original == null) {
            return null;
        }

        Node copy = new Node(original.value, original.nodeType, null, original.depth, original.canMutate);

        for (Node child : original.children) {
            copy.children.add(deepCopyNode(child));
        }

        return copy;
    }
}
