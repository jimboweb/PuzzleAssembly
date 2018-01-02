package com.jimboweb.puzzleassembly;

import org.jetbrains.annotations.Nullable;

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
    public void AssemblePuzzle(Inputter inputter, Outputter outputter){
        //haha look at me using inversion of control
        this.inputter = inputter;
        this.outputter = outputter;
        List<String> inputs = (ArrayList<String>)inputter.input(numberOfInputs);
        Map<String, Integer> colorToInt;
        List<String> colorsUsed = new ArrayList<>();
        List<List<String>> brokenUpInputs = breakupInputs(inputs);
        colorToInt = makeColorToIntMap(brokenUpInputs);
        List<Square> squares = makeSquareList(colorToInt,brokenUpInputs);
        DeBruinSquareGraph gr = new DeBruinSquareGraph(squares);
        outputter.outputLine("Nothing to show yet");
    }

    /**
     * creates the Square objects - TESTED AND WORKS
     * @param colorToInt a map matching each color to an integer
     * @param brokenUpInputs list of list of colors
     * @return List of square objects
     *
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
            input = input.substring(input.indexOf("(")+1,input.indexOf(")"));
            String[] nextInput = input.split(",");
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

    /**
     * version of DeBruijnGraph for squares problem
     */
    class DeBruinSquareGraph extends DeBruijnGraph<DeBruijnSquareEdge,DeBruijnSquareNode>{
        int nodeCount = 0;
        int edgeCount = 0;
        boolean topLeftNodeExists = false;
        boolean bottomRightNodeExists = false;
        public DeBruinSquareGraph(List<Square> squares) {
            makeNodesAndEdges(squares);
        }

        /**
         * goes through all the squares and finds places where they make a node
         * TESTED - No exceptions but not the right outcome. Should be 18 nodes but there are only 6.
         * @param squares list of squares from the method that makes the squares from input
         */
        private void makeNodesAndEdges(List<Square> squares){
            int nodeCount = 0;
            for(Square square:squares){
                for (Square othersquare:squares) {
                    addNodesToSquares(square,othersquare);
                }
                addEdgesToSquare(square);
            }

        }

        /**
         * finds all the internal edges in a single square
         * @param square a single square from the list
         */
        private void addEdgesToSquare(Square square){
            for(Map.Entry<DeBruijnSquareNode,Integer> entry:square.associatedNodes.entrySet()){
                for(Map.Entry<DeBruijnSquareNode,Integer> otherEntry:square.associatedNodes.entrySet()){
                    if(otherEntry.getKey().index>entry.getKey().index) {
                        if (entry.getValue() + otherEntry.getValue() == 3) {
                            addEdgeToSquare(entry,otherEntry,square);
                        }
                    }
                }
            }
        }

        /**
         * adds a particular edge from a square
         * @param entry one of the edges in associatedNodes
         * @param otherEntry another edge in associatedNodes
         * @param square the square we're looking at
         */
        private void addEdgeToSquare(Map.Entry<DeBruijnSquareNode,Integer> entry, Map.Entry<DeBruijnSquareNode,Integer> otherEntry, Square square){
            boolean vertical = (entry.getValue() & 1) != 1;
            DeBruijnSquareEdge newEdge = new DeBruijnSquareEdge(entry.getKey(),
                    otherEntry.getKey(),
                    edgeCount,
                    vertical);
            addEdge(newEdge);
            square.addAssociatedEdges(newEdge);
            edgeCount++;

        }

        /**
         * compares two squares and adds all the possible nodes between them
         * @param square
         * @param othersquare
         */
        private void addNodesToSquares(Square square, Square othersquare){
            //this should mean each square is compared only once with each other square
            //reducing time and preventing duplicate nodes
            if(!(othersquare.getIndex()>square.getIndex())) {
                return;
            }
            List<DeBruijnSquareNode> newNodes = new ArrayList<>();
            for (int s = 0; s < othersquare.sideColor.length; s++) {
                DeBruijnSquareNode newNode = addNodeToMatchingSides(square, othersquare, s);
                if (newNode != null) {
                    addNewNode(newNodes,newNode, square, othersquare,s);
                }
            }
            if (!newNodes.isEmpty()) {
                nodes.addAll(newNodes);
            }
        }

        /**
         * adds a node to the newNodes list and to the square's associated nodes
         * @param newNodes
         * @param newNode
         * @param square
         * @param othersquare
         */
        private void addNewNode(List<DeBruijnSquareNode> newNodes, DeBruijnSquareNode newNode, Square square, Square othersquare, int s){
            newNodes.add(newNode);
            nodeCount++;
            square.addAssociatedNode(newNode, 3 - s);
            othersquare.addAssociatedNode(newNode, s);

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
            if(secondSquare.isCornerSide(secondSquareSide)){
                return addNodesToCornerSide(secondSquareSide, firstSquare,secondSquare);
           }
            //I know I could do this with one huge conditional but it would just be confusing and
            //it will all compile to the same thing anyway
            if((secondSquareSide == Square.Sides.TOP || secondSquareSide == Square.Sides.BOTTOM) &&
                    firstSquare.horizontalLocation != secondSquare.horizontalLocation){
                return null;
            } else if ((secondSquareSide == Square.Sides.LEFT || secondSquareSide == Square.Sides.RIGHT) &&
                    firstSquare.verticalLocation != secondSquare.verticalLocation){
                return null;
            }
            if(firstSquare.sideColor[firstSquareSide]==secondSquare.sideColor[secondSquareSide]){
                return new DeBruijnSquareNode(nodeCount,secondSquare.sideColor[secondSquareSide], firstSquare,firstSquareSide,secondSquare,secondSquareSide);
            }
            return null;
        }
        /**
         * This is for the upper left hand and lower right hand corners
         * @param secondSquareSide the side that might be an ul or lr corner
         * @param firstSquare
         * @param secondSquare
         * @return a corner node if one doesn't already exist
         */
        @Nullable
        private DeBruijnSquareNode addNodesToCornerSide(int secondSquareSide, Square firstSquare, Square secondSquare){
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
    }


    /**
     * version of DeBruijnNode for square problem
     */
    class DeBruijnSquareNode extends DeBruijnGraphNode<DeBruijnSquareEdge>{
        int color;
        List<SquareReference> squareReferences = new ArrayList<>();
        public DeBruijnSquareNode(int index, int color, Square firstSquareRef, int firstSquareSide, Square secondSquareRef, int secondSquareSide){
            super(index);
            squareReferences.add(new SquareReference(firstSquareRef,firstSquareSide));
            squareReferences.add(new SquareReference(secondSquareRef,secondSquareSide));
        }

        private class SquareReference{
            Square square;
            int location;

            public SquareReference(Square square, int location) {
                this.square = square;
                this.location = location;
            }

            @Override
            public String toString() {
                // TODO: 12/31/17 make it so it lists the sides by side 
                return "SquareReference{" +
                        "square=" + square +
                        ", location=" + location +
                        '}';
            }
        }

        @Override
        public String toString() {
            //TODO: change this so it lists the color by color if possible
            return "DeBruijnSquareNode{" +
                    "color=" + color +
                    ", squareReferences=" + squareReferences +
                    '}';
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
        private String topColor, leftColor, bottomColor, rightColor;
        private final int[] sideColor = new int[4];
        private final int verticalLocation;
        private final int horizontalLocation;
        private Map<DeBruijnSquareNode, Integer> associatedNodes;
        private List<DeBruijnSquareEdge> associatedEdges;
        private int index;
        public Square(Map<String,Integer> colorToInt, String top, String left, String bottom, String right, int index){
            topColor=top;
            bottomColor = bottom;
            leftColor = left;
            rightColor = right;
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

        @Override
        public String toString() {
            return "(" + topColor + ", " + leftColor + ", " + bottomColor + ", " + rightColor + ")";
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
    public interface Inputter{
        public List<String> input(int numberOfInputs);
    }

    public class TestInput implements Inputter{
        private List<String> input;

        public void setInput(List<String> input){
            this.input=input;
        }

        @Override
        public List<String> input(int numberOfInputs) {
            return input;
        }
    }

    /**
     * implementation of Inputter that gets data from user
     */
    private class UserInputter implements Inputter{
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

    public interface Outputter{
        public void output(String s);
        public void outputLine(String s);
    }

    public class TestOutputter implements Outputter {
        private String output = "";

        @Override
        public void output(String s) {
            output += s;
        }

        @Override
        public void outputLine(String s) {
            output += s + "\n";
        }

        public String getOutput(){
            return output;
        }
    }

    private class ConsoleOutputter implements Outputter{

        @Override
        public void output(String s) {
            System.out.print(s);
        }

        @Override
        public void outputLine(String s) {
            System.out.println(s);
        }
    }

    //main class, thead class and general properties
    private static class PuzzleThread implements Runnable{
        @Override
        public void run() {
            PuzzleAssembly pa = new PuzzleAssembly();
            pa.AssemblePuzzle(pa.new UserInputter(), pa.new ConsoleOutputter());
        }
    }

    public static void main(String[] args) {
        PuzzleThread pt = new PuzzleThread();
        pt.run();

    }


    FastScanner scanner;
    public Inputter inputter;
    public Outputter outputter;

    public PuzzleAssembly(){
        scanner = new FastScanner();
    }

}
