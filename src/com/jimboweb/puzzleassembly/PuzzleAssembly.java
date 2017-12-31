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
        int squareCount = 0;
        for(List<String> colors:brokenUpInputs){
            squares.add(new Square(colorToInt,colors.get(0),colors.get(1),colors.get(2),colors.get(3), squareCount));
            squareCount++;
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
        int nodeCount = 0;
        int edgeCount = 0;
        boolean topLeftNodeExists = false;
        boolean bottomRightNodeExists = false;
        public DeBruinSquareGraph(List<Square> squares) {
            makeNodesAndEdges(squares);
        }

        private void makeNodesAndEdges(List<Square> squares){
            int nodeCount = 0;
            // TODO: 12/30/17 make these into separate methods
            for(Square square:squares){
                for (Square othersquare:squares) {
                    //this should mean each square is compared only once with each other square
                    //reducing time and preventing duplicate nodes
                    if(othersquare.getIndex()>square.getIndex()) {
                        List<DeBruijnSquareNode> newNodes = new ArrayList<>();
                        for (int s : othersquare.sideColor) {
                            DeBruijnSquareNode newNode = addNodeToMatchingSides(square, othersquare, s);
                            if (newNode != null) {
                                newNodes.add(newNode);
                                nodeCount++;
                                square.addAssociatedNode(newNode, 3 - s);
                                othersquare.addAssociatedNode(newNode, s);
                            }
                        }
                        if (!newNodes.isEmpty()) {
                            nodes.addAll(newNodes);
                        }
                    }
                }
                for(Map.Entry<DeBruijnSquareNode,Integer> entry:square.associatedNodes.entrySet()){
                    for(Map.Entry<DeBruijnSquareNode,Integer> otherEntry:square.associatedNodes.entrySet()){
                        if(otherEntry.getKey().index>entry.getKey().index) {
                            if (entry.getValue() + otherEntry.getValue() == 3) {
                                boolean vertical = (entry.getValue() & 1) != 1;
                                DeBruijnSquareEdge newEdge = new DeBruijnSquareEdge(entry.getKey(),
                                        otherEntry.getKey(),
                                        edgeCount,
                                        vertical);
                                addEdge(newEdge);
                                square.addAssociatedEdges(newEdge);
                                edgeCount++;
                            }  
                        }
                    }
                }

            }

        }

        /**
         * <p>this will add a node if:</p>
         * <ol>
         *     <li>top matches bottom and horizontal position is the same</li>
         *     <li>left matches right and vertical position is the same</li>
         * </ol>
         * @param firstSquare the first square
         * @param secondSquare the second square
         * @param secondSquareSide the side the second square is trying to match
         */
        private DeBruijnSquareNode addNodeToMatchingSides(Square firstSquare, Square secondSquare, int secondSquareSide){
            int firstSquareSide = 3 - secondSquareSide;
            // TODO: 12/31/17 break this into separate methods
            if(secondSquare.isCornerSide(secondSquareSide)){
                if(secondSquareSide == Square.Sides.TOP || secondSquareSide == Square.Sides.LEFT){
                    if(!topLeftNodeExists) {
                        topLeftNodeExists=true;
                        return new DeBruijnSquareNode(nodeCount, 0, secondSquare, Square.Sides.TOP, secondSquare, Square.Sides.LEFT);
                    }
                    return null;
                } else if (secondSquareSide == Square.Sides.BOTTOM || secondSquareSide == Square.Sides.RIGHT){
                    if(!bottomRightNodeExists) {
                        bottomRightNodeExists=true;
                        return new DeBruijnSquareNode(nodeCount, 0, secondSquare, Square.Sides.BOTTOM, secondSquare, Square.Sides.RIGHT);
                    }
                    return null;
                } else {
                    return null;
                }


           }


            //I know I could do this with one huge conditional but it would just be confusing and
            //it will all compile to the same thing anyway
            if(secondSquareSide == Square.Sides.TOP || secondSquareSide == Square.Sides.BOTTOM &&
                    firstSquare.horizontalLocation != secondSquare.horizontalLocation){
                return null;
            } else if (secondSquareSide == Square.Sides.LEFT || secondSquareSide == Square.Sides.RIGHT &&
                    firstSquare.verticalLocation != secondSquare.verticalLocation){
                return null;
            }
            if(firstSquare.sideColor[firstSquareSide]==secondSquare.sideColor[secondSquareSide]){
                return new DeBruijnSquareNode(nodeCount,secondSquare.sideColor[secondSquareSide], firstSquare,firstSquareSide,secondSquare,secondSquareSide);
            }
            return null;
        }
    }

    class DeBruijnSquareNode extends DeBruijnGraphNode<DeBruijnSquareEdge>{
        int color;
        Map<Square,Integer> squareReferences;
        public DeBruijnSquareNode(int index, int color, Square firstSquareRef, int firstSquareSide, Square secondSquareRef, int secondSquareSide){
            super(index);
            squareReferences.put(firstSquareRef,firstSquareSide);
            squareReferences.put(secondSquareRef,secondSquareSide);
        }

    }

    /**
     * Square version of DeBruijnGraphEdge
     */
    class DeBruijnSquareEdge extends DeBruijnGraphEdge<DeBruijnSquareNode>{
        private int squareRef;
        private boolean isVertical;

        /**
         * creates a new edge going through a square
         * @param firsteNode first node
         * @param secondNode second node
         * @param i index of edge (NOT the square it refers to)
         * @param vertical true if vertical, otherwise horizontal
         */
        public DeBruijnSquareEdge(DeBruijnSquareNode firsteNode, DeBruijnSquareNode secondNode, int i, boolean vertical){
            super(firsteNode,secondNode,i);
            this.squareRef=i;
            this.isVertical = vertical;
            this.index = i;
        }

        /**
         * use this, NOT index, to find what square an edge refers to
         * @return the number of the square
         */
        public int getSquareRef() {
            return squareRef;
        }

        /**
         *
         * @return true if the square is vertical, false if horizontal
         */
        public boolean isVertical() {
            return isVertical;
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
        protected List<E> edges;
        protected List<N> nodes;
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
        private final int[] sideColor = new int[4];
        private final int verticalLocation;
        private final int horizontalLocation;
        private Map<DeBruijnSquareNode, Integer> associatedNodes;
        private List<DeBruijnSquareEdge> associatedEdges;
        private int index;
        public Square(Map<String,Integer> colorToInt, String top, String left, String bottom, String right, int index){
            associatedEdges = new ArrayList<>();
            associatedNodes = new HashMap<>();
            sideColor[Sides.TOP]=colorToInt.get(top);
            sideColor[Sides.BOTTOM]=colorToInt.get(bottom);
            sideColor[Sides.LEFT]=colorToInt.get(left);
            sideColor[Sides.RIGHT]=colorToInt.get(right);
            this.index=index;

            if(sideColor[Sides.TOP]==0){
                verticalLocation=VerticalLocations.TOP;
            } else if (sideColor[Sides.BOTTOM]==0) {
                verticalLocation = VerticalLocations.BOTTOM;
            } else {
                verticalLocation = VerticalLocations.CENTER;
            }

            if(sideColor[Sides.LEFT]==0){
                horizontalLocation = HorizontalLocations.LEFT;
            } else if (sideColor[Sides.RIGHT] == 0) {
                horizontalLocation = HorizontalLocations.RIGHT;
            } else {
                horizontalLocation = HorizontalLocations.CENTER;
            }
        }

        public int getIndex() {
            return index;
        }

        public boolean isCornerSide(int s){
            return (isUpperLeftCorner() && (s == Sides.TOP || s == Sides.LEFT)) ||
                    (isBottomRightCorner() && (s == Sides.BOTTOM || s == Sides.RIGHT));
        }

        public boolean isUpperLeftCorner(){
            return verticalLocation == VerticalLocations.TOP && horizontalLocation == HorizontalLocations.LEFT;
        }

        public boolean isBottomRightCorner(){
            return verticalLocation== VerticalLocations.BOTTOM && horizontalLocation == HorizontalLocations.RIGHT;
        }

        public int getSide(int s){
            return sideColor[s];
        }

        public int[] getSideColor() {
            return sideColor;
        }

        public int getHorizontalLocation() {
            return horizontalLocation;
        }

        public int getVerticalLocation() {
            return verticalLocation;
        }

        public void addAssociatedNode(DeBruijnSquareNode n, int side){
            associatedNodes.put(n,side);
        }

        public Map<DeBruijnSquareNode, Integer> getAssociatedNodes() {
            return associatedNodes;
        }

        public List<DeBruijnSquareEdge> getAssociatedEdges() {
            return associatedEdges;
        }

        public void addAssociatedEdges(DeBruijnSquareEdge e){
            associatedEdges.add(e);
        }

        /* no-op */
        class Sides{
            public static final int TOP = 0;
            public static final int LEFT = 1;
            public static final int BOTTOM = 2;
            public static final int RIGHT = 3;
        }

        class VerticalLocations{
            public static final int TOP = 0;
            public static final int CENTER = 1;
            public static final int BOTTOM = 2;
        }

        class HorizontalLocations{
            public static final int LEFT = 0;
            public static final int CENTER = 1;
            public static final int RIGHT = 2;
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
