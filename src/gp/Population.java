package gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Population {
    public List<Program> programs = new ArrayList<>();
    final private int popSize;
    private double bestFit = Double.MAX_VALUE;
    private Program bestProg;
    private String fn;
    public Population(int popSize){
        this.popSize = popSize;
    }
    public void generateRandomPrograms(int maxDepth, int varNum, int floor, int ceil, String filename){
        fn = filename;
        for(int i=0; i<popSize/2; i++){
            Program p = new Program(maxDepth, varNum, floor, ceil);
            p.setInputsOutputs(filename);
            p.genProgram();
            programs.add(p);
        }for(int i=0; i<popSize/2; i++){
            Program p = new Program(maxDepth, varNum, floor, ceil);
            p.setInputsOutputs(filename);
            p.genProgramGrwo();
            programs.add(p);
        }
    }
    public void generateRandomProgramsFromProgram(Program oldP){
        for(int i=0; i<popSize; i++){
            Program p = new Program(oldP);
            Node nextChildren = p.mutateRandomProgram(0, null, null);
            p.tree.children.add(nextChildren);
            programs.add(p);
        }
    }
    public void generatePrograms(int maxDepth, int varNum, int floor, int ceil, String filename, List<List<Integer>> inputs){
        fn = filename;
        for(int i=0; i<popSize; i++){
            Program p = new Program(maxDepth, varNum, floor, ceil);
            p.setInputsOutputsSub(filename, inputs);
            p.genProgram();
            programs.add(p);
        }
    }
    public void crossover(int players){
        int index1 = tournament(players);
        int index2 = tournament(players);

        Program firstProgram = programs.get(index1);
        Program secondProgram = programs.get(index2);

        Program newProg = crossPrograms(firstProgram, secondProgram);

        int neg = negativeTournament(players);
        if(newProg.getFit() <= programs.get(neg).getFit()){
            programs.set(index2, newProg);
        }
    }
    public Program crossPrograms(Program firstProgram,Program secondProgram){
        Program firstProgramCopy = new Program(firstProgram);
        Program secondProgramCopy = new Program(secondProgram);

        Node firstNodeToMutate = selectMutatableNode(firstProgramCopy.getTree());
        Node secondNodeToMutate = selectMatchingMutatableNode(secondProgramCopy.getTree(), firstNodeToMutate.nodeType);

        if (firstNodeToMutate != null && secondNodeToMutate != null) {
            replaceSubtree(firstProgramCopy.getTree(), firstNodeToMutate, secondNodeToMutate);
            replaceSubtree(secondProgramCopy.getTree(), secondNodeToMutate, firstNodeToMutate);
        }
        firstProgramCopy.setInputsOutputs(fn);
        secondProgramCopy.setInputsOutputs(fn);
        firstProgramCopy.calculateOutputs();
        firstProgramCopy.calculateFitness();
        secondProgramCopy.calculateOutputs();
        secondProgramCopy.calculateFitness();
        if(firstProgramCopy.getFit() > secondProgramCopy.getFit()){
            return secondProgramCopy;
        }
        else{
            return firstProgramCopy;
        }
    }
    public void mutate(int players, int maxDepth){
        int index = tournament(players);
        Program p = programs.get(index);

        Program newProg = mutateProgram(p, maxDepth);

        int neg = negativeTournament(players);
        if(newProg.getFit() <= programs.get(neg).getFit()){
            programs.set(index, newProg);
        }
    }
    public void mutateTheBest(int players, int maxDepth){
        Program p = bestProg;

        Program newProg = mutateProgram(p, maxDepth);

        int neg = negativeTournament(players);
        if(newProg.getFit() <= programs.get(neg).getFit()){
            programs.set(neg, newProg);
        }
    }
    public Program mutateProgram(Program p, int max){
        Program newProg = new Program(p);
        newProg.setMaxDepth(Integer.MAX_VALUE);
        newProg.setInputsOutputs(fn);
        Node nodeToMutate = selectMutatableNode(newProg.getTree());
        Node newNode = "block".equals(nodeToMutate.nodeType)
                ? newProg.mutateRandomProgram(nodeToMutate.depth, null, null)
                : newProg.mutateRandomProgram(nodeToMutate.depth, nodeToMutate.nodeType, null);
        replaceSubtree(newProg.getTree(), nodeToMutate, newNode);
        newProg.calculateOutputs();
        newProg.calculateFitness();
        return newProg;
    }
    private Node selectMutatableNode(Node node) {
        List<Node> matchingNodes = collectNodes(node);
        if (!matchingNodes.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(matchingNodes.size());
            return matchingNodes.get(randomIndex);
        }

        return null;
    }
    private Node selectMatchingMutatableNode(Node node, String type) {
        List<Node> matchingNodes = collectMatchingNodes(node, type);
        if (!matchingNodes.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(matchingNodes.size());
            return matchingNodes.get(randomIndex);
        }

        return null;
    }
    private List<Node> collectNodes(Node node){
        List<Node> matchingNodes = new ArrayList<>();
        if (node.canMutate) {
            matchingNodes.add(node);
        }
        for (Node child : node.children) {
            matchingNodes.addAll(collectNodes(child));
        }
        return matchingNodes;
    }
    private List<Node> collectMatchingNodes(Node node, String type){
        List<Node> matchingNodes = new ArrayList<>();
        if (node.canMutate && node.nodeType.equals(type)) {
            matchingNodes.add(node);
        }
        for (Node child : node.children) {
            matchingNodes.addAll(collectMatchingNodes(child, type));
        }
        return matchingNodes;
    }
    private void replaceSubtree(Node root, Node target, Node replacement) {
        if (root == null) {
            return;
        }

        for (int i = 0; i < root.children.size(); i++) {
            if (root.children.get(i) == target) {
                root.children.set(i, deepCopyNode(replacement));
                return;
            } else {
                replaceSubtree(root.children.get(i), target, replacement);
            }
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
    private int tournament(int players){
        Random random = new Random();
        if (players > programs.size()) {
            throw new IllegalArgumentException("Too many competitors for tournament");
        }

        int bestProgram = 0;
        double bestFit = Double.MAX_VALUE;

        for (int i = 0; i < players; i++) {
            int index = random.nextInt(programs.size());
            Program candidate = programs.get(index);

            if (candidate.fit != null && candidate.fit < bestFit) {
                bestFit = candidate.fit;
                bestProgram = index;
            }
        }
        return bestProgram;
    }
    private int negativeTournament(int players){
        Random random = new Random();
        if (players > programs.size()) {
            throw new IllegalArgumentException("Too many competitors for tournament");
        }

        int worstProgram = 0;
        double worstFit = -20;

        for (int i = 0; i < players; i++) {
            int index = random.nextInt(programs.size());
            Program candidate = programs.get(index);

            if (candidate.fit != null && candidate.fit > worstFit) {
                worstFit = candidate.fit;
                worstProgram = index;
            }
        }
        return worstProgram;
    }
    public double getBestFit(){return bestFit;}
    public void calculateBest(){
        for (Program program : programs) {
            if (program.fit != null && program.fit < bestFit) {
                bestFit = program.fit;
                bestProg = program;
            }
        }
    }
    public Program getBestProg(){return bestProg;}
    public float getAvgFit(){
        float avgFit = 0;
        for(Program prog: programs){
            avgFit += prog.getFit();
        }
        return (avgFit /this.popSize);
    }
}
