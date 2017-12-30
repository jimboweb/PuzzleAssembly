package com.jimboweb.puzzleassembly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class PuzzleAssembly {

    private final int numberOfInputs = 25;

    /**
     * general method to solve the problem
     *
     */
    private void AssemblePuzzle(Inputter inputter){
        //haha look at me using inversion of control
        this.inputter = inputter;
        List<String> inputs = (ArrayList<String>)inputter.input(numberOfInputs);
        Map<String, Integer> colorToInt;
        List<String> colorsUsed = new ArrayList<>();
        List<List<String>> brokenUpInputs = breakupInputs(inputs);
        colorToInt = makeColorToIntMap(brokenUpInputs);
        List<Square> squares = makeSquareList(colorToInt,brokenUpInputs);


    }

    /**
     * creates the Square objects
     * @param colorToInt a map matching each color to an integer
     * @param brokenUpInputs list of list of colors
     * @return List of square objects
     */
    private ArrayList<Square> makeSquareList(Map<String,Integer> colorToInt,List<List<String>> brokenUpInputs){
        List<Square> squares = new ArrayList<>();
        for(List<String> colors:brokenUpInputs){
            squares.add(new Square(colorToInt,colors.get(0),colors.get(1),colors.get(2),colors.get(3)));
        }
        return (ArrayList<Square>) squares;
    }


    /**
     * just breaks up inputs of four colors by commas and gets rid of parens
     * @param inputs "(red,green,blue,yellow), (...)..."
     * @return {{red, green, blue, yellow},{...}...}
     * @throws IllegalArgumentException it's for a square so each line should have four colors
     */
    private ArrayList<List<String>> breakupInputs(List<String> inputs) throws IllegalArgumentException {
        ArrayList<List<String>> rtrn = new ArrayList<>();
        for(String input:inputs){
            input = input.substring(input.indexOf("("),input.indexOf(")"));
            String[] nextInput = input.split("");
            if(nextInput.length!=4){
                throw new IllegalArgumentException("at least one input line does not have four colors");
            }
            rtrn.add(new ArrayList<String>(Arrays.asList(nextInput)));
        }

        return rtrn;
    }

    /**
     * creates a color-integer map for the colors
     * @param brokenUpInputs will be in form "red","green" etc.
     * @return {"red",1...}
     */
    private Map<String,Integer> makeColorToIntMap(List<List<String>> brokenUpInputs){
        Map<String,Integer> rtrn = new HashMap<>();
        rtrn.put("black",0);
        int nextInt = 1;
        for(List<String> colors:brokenUpInputs)
        for(String color:colors){
            if(!rtrn.containsKey(color)){
                rtrn.put(color,nextInt);
                nextInt++;
            }
        }
        return rtrn;
    }

    class DeBruinSquareGraph extends DeBruijnGraph<DeBruijnSquareEdge,DeBruijnSquareNode>{
        public DeBruinSquareGraph(List<Square> squares) {
            // TODO: 12/30/17 create a square graph from list of squares
        }
    }

    class DeBruijnSquareNode extends DeBruijnGraphNode<DeBruijnSquareEdge>{
        int squareRef;
        public DeBruijnSquareNode(int index){
            super(index);
            this.squareRef=index;
        }

        public int getSquareRef() {
            return squareRef;
        }
    }

    class DeBruijnSquareEdge extends DeBruijnGraphEdge<DeBruijnSquareNode>{
        int squareRef;
        public DeBruijnSquareEdge(DeBruijnSquareNode firsteNode, DeBruijnSquareNode secondNode, int i){
            super(firsteNode,secondNode,i);
            this.squareRef=i;
        }

        public int getSquareRef() {
            return squareRef;
        }
    }


    /**
     * Generic graph
     * @param <E> some kind of Edge interface
     * @param <N> some kind of Node interface
     */
    interface Graph<E extends Edge, N extends Node>{
        public void addNode(N n);
        public N getNode(int i);
        public void addEdge(E edge);
        public E getEdge(int i);
        public List<? extends N> getNodes();
        public List<? extends E> getEdges();

    }


    // interfaces and abstract objects related to the graph
    /**
     * Generic directed edge
     * @param <N> some kind of node interface
     */
    interface Edge<N extends Node>{
        public N getNode(int i);
        public List<? extends N> getNodes();
        public int getIndex();
    }

    /**
     * Generic Node with directed edge
     *
     * @param <E> some kind of Edge interface
     */
    interface Node<E extends Edge>{
        public void addEdge(E e, boolean start);
        public List<? extends Edge> getEdges();
        public E getEdge(int i);
        public int getIndex();
        public boolean getStartOrEnd(Edge e);
    }

    /**
     * a DeBruijn implemntation of the Graph interface
     * allows nodes to be glued together
     */
    abstract class DeBruijnGraph<E extends DeBruijnGraphEdge, N extends DeBruijnGraphNode> implements Graph<E,N>{
        private List<E> edges;
        private List<N> nodes;
        public DeBruijnGraph(){
            edges = new ArrayList<>();
            nodes = new ArrayList<>();
        }
        public void addNode(N n){
            nodes.add(n);
        }
        public void addEdge(E e){
            edges.add(e);
        }

        public List<? extends E> getEdges() {
            return edges;
        }

        public List<? extends N> getNodes() {
            return nodes;
        }
        public E getEdge(int i){
            if(i>edges.size()){
                throw new IllegalArgumentException("requested index " + i + "but edges has length " + edges.size());
            }
            return edges.get(i);
        }
        public N getNode(int i){
            if(i>nodes.size()){
                throw new IllegalArgumentException("requested index " + i + "but edges has length " + nodes.size());
            }
            return nodes.get(i);
        }
        void glueNodes(List<N> nodesToGlue){
            N firstNode = nodesToGlue.get(0);
            for(N node:nodesToGlue){
                node.glueToNode(firstNode);
                if(!node.equals(firstNode)) {
                    nodes.remove(node);
                }
            }

        }
    }

    /**
     * A DeBruijn implementation of the node interface
     * Node can be glued to other nodes
     */
    abstract class DeBruijnGraphNode<E extends DeBruijnGraphEdge> implements Node<E>{
        private List<E> edges = new ArrayList<>();
        private HashMap<Integer,Boolean> startOrEnd = new HashMap<>();
        int index;
        private void addEdge(E e){
            edges.add(e);

        }

        public void addEdge(E e, boolean start){
            addEdge(e);
            startOrEnd.put(e.index,start);

        }

        public  DeBruijnGraphNode(int index){
            this.index=index;
        }
        public E getEdge(int ind){
            return edges.get(ind);
        }

        public List<? extends E> getEdges() {
            return edges;
        }

        public int getIndex(){
            return index;
        }

        public void glueToNode(DeBruijnGraphNode n){
            if(this.equals(n))
                return;
            for(E edge:edges){
                edge.glueNodeToNode(n, startOrEnd.get(edge.getIndex()));
            }
        }

        public boolean getStartOrEnd(Edge e){
            return startOrEnd.get(e.getIndex());
        }
    }

    /**
     * A DeBruijn implementation of the Edge interface.
     * Allows nodes to be glued together.
     */
    abstract class DeBruijnGraphEdge<N extends DeBruijnGraphNode> implements Edge<N>{
        private N starttNode;
        private N endNode;
        int index;
        public DeBruijnGraphEdge(N firstNode, N secondNode, int index){
            this.starttNode=firstNode;
            this.endNode = secondNode;
            this.index=index;
        }


        public List<N> getNodes() {
            List<N> nodes = new ArrayList<>();
            nodes.add(starttNode);
            nodes.add((endNode));
            return nodes;
        }
        public N getNode(int i){
            switch (i){
                case 0: return starttNode;
                case 1: return endNode;
                default: throw new IllegalArgumentException("node index can only be 0 or 1");

            }

        }
        public void glueNodeToNode(N n, boolean start){
            if(start){
                starttNode = n;
            } else {
                endNode = n;
            }
        }
        public int getIndex(){
            return index;
        }
    }


    //classes immediately used for solving problems

    protected class Square{
        private int top;
        private int bottom;
        private int left;
        private int right;
        private Map<String, Integer> colorToInt;

        public Square(Map<String,Integer> colorToInt, String top, String left, String bottom, String right){
            this.colorToInt = colorToInt;
            this.top=colorToInt.get(top);
            this.bottom=colorToInt.get(bottom);
            this.left=colorToInt.get(left);
            this.right=colorToInt.get(right);
        }
    }

    //classes related to inputting data

    /**
     * scanner to read data
     */
    class FastScanner {
        StringTokenizer tok = new StringTokenizer("");
        BufferedReader in;

        FastScanner() {
            in = new BufferedReader(new InputStreamReader(System.in));
        }

        String next() throws IOException {
            while (!tok.hasMoreElements())
                tok = new StringTokenizer(in.readLine());
            return tok.nextToken();
        }

        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }


    }

    /**
     * interface that allows me to input the data
     */
    private interface Inputter{
        public List<String> input(int numberOfInputs);
    }

    /**
     * implementation of Inputter that gets data from user
     */
    private class UserInput implements Inputter{
        /**
         * inputs 25 strings
         * @return an arrayList of 25 strings
         */
        public List<String> input(int numberOfInputs){
            List<String> inputs = new ArrayList<>();
            try {
                for (int i = 0; i < numberOfInputs; i++) {
                    inputs.add(scanner.next());

                }
            }catch(IOException e) {
                System.out.println(e);
            }
            return inputs;
        }

    }

    //main class, thead class and general properties

    private static class PuzzleThread implements Runnable{
        @Override
        public void run() {
            PuzzleAssembly pa = new PuzzleAssembly();
            pa.AssemblePuzzle(pa.new UserInput());
        }
    }

    public static void main(String[] args) {
        PuzzleThread pt = new PuzzleThread();
        pt.run();

    }


    FastScanner scanner;
    Inputter inputter;

    public PuzzleAssembly(){
        scanner = new FastScanner();
    }

}
