package com.jimboweb.puzzleassembly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class PuzzleAssembly {

    private final int numberOfInputs = 4;

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
        PrintCircuit pc = new PrintCircuit();
        outputter.outputLine("Nothing to show yet");
    }

    private Deque<Integer>[] makeHierholzerInput(){
        int arraySize = 0;
        Deque<Integer>[] rtrn = new ArrayDeque[arraySize];
        return rtrn;
    }

    /**
     * creates the Square objects
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


    //classes immediately used for solving problems

    protected class Square{
        private String topColor, leftColor, bottomColor, rightColor;
        private final int[] sideColor = new int[4];
        private final int verticalLocation;
        private final int horizontalLocation;
        private int index;
        public Square(Map<String,Integer> colorToInt, String top, String left, String bottom, String right, int index){
            topColor=top;
            bottomColor = bottom;
            leftColor = left;
            rightColor = right;
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

        public boolean isOppositeOrSameEnd(Square secondSquare, boolean horizontal){
            int firstSquareLocRef = horizontal?this.horizontalLocation:this.verticalLocation;
            int secondSquareLocRef = horizontal?secondSquare.horizontalLocation:secondSquare.verticalLocation;
            //if they are opposite or the same they'll both be even
            return ((firstSquareLocRef | secondSquareLocRef) & 1) == 0;

        }

        public boolean sideIsTopOrLeft(int sideNumber){
            return sideNumber < Square.Sides.BOTTOM;
        }

        public boolean sideIsHorizontal(int sideNumber){
            return (sideNumber & 1) == 0;
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

class PrintCircuit{
    int loopCount = 0; //debug
    /**
     *
     * @param edges list of adjacent vertices for current edges
     * @return circuit in deque list
     */
    Deque<Integer> makeEulerianCircuit(Deque<Integer>[] edges, int numberOfNodes)
    {

        // return empty list for empty graph
        if (edges.length==0)
            return new LinkedList<>(); //empty graph

        // Stack for the path in the current iteration
        Deque<Integer> currentPath = new ArrayDeque<>();

        // queue for the final circuit
        Deque<Integer> circuit = new ArrayDeque<>();

        // start from any vertex
        currentPath.push(0);
        int currentVertexNumber = 0; // Current vertex

        while (!currentPath.isEmpty())
        {
            loopCount++; //debug
            //if there are outgoing edges
            if (edges[currentVertexNumber].size() > 0)
            {
                currentPath.push(currentVertexNumber);
                currentVertexNumber = edges[currentVertexNumber].pop();
            }

            // otherwise step back
            else
            {
                circuit.add(currentVertexNumber);
                currentVertexNumber = currentPath.pop();
            }
        }

        return circuit;

    }


    /**
     * Get the input, check to make sure the graph is even and run the printCircuit function
     */
    private void inputAndPrintCircuit(){
        Scanner scanner = new Scanner(System.in);
        int[] in = new int[2];
        in[0] = scanner.nextInt();
        in[1] = scanner.nextInt();
        Deque<Integer>[] edges = new Deque[in[0]];
        for(int i=0;i<in[0];i++)
        {
            edges[i] = new ArrayDeque<>();
            loopCount++; //debug
        }

        // evenChecker is a Nx2 array where [0] = incoming edges and [1] = outgoing edges
        //should be equal or graph isn't Eulerian
        int[][] evenChecker = new int[in[0]][2];
        for (int i = 0; i < in[1]; i++) {
            int from = scanner.nextInt()-1;
            int to = scanner.nextInt()-1;
            evenChecker[from][0]++;
            evenChecker[to][1]++;
            edges[from].push(to);

        }
        if(!isGraphEven(evenChecker)){
            System.out.println("0");
            System.exit(0);
        } else {
            System.out.println("1");
        }
        Deque<Integer> circuit = makeEulerianCircuit(edges, in[0]);
        while(circuit.size()>1){
            int nextNode = circuit.pollLast()+1;
            System.out.print(nextNode + " ");
            loopCount++; //debug
        }
        System.out.println();
    }

    /**
     * checks to make sure that incoming edges = outgoing edges
     * @param evenChecker a Nx2 array where [0] = incoming edges and [1] = outgoing edges
     * @return true if incoming eges = outgoing edges, false otherwise
     */
    public boolean isGraphEven(int[][] evenChecker){
        for(int[] evenCheck:evenChecker){
            if(evenCheck[0]!=evenCheck[1]){
                loopCount++; //debug
                return false;
            }
        }
        return true;
    }



}
