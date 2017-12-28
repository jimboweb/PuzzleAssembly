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

    private ArrayList<Square> makeSquareList(Map<String,Integer> colorToInt,List<List<String>> brokenUpInputs){
        List<Square> squares = new ArrayList<>();
        for(List<String> colors:brokenUpInputs){
            squares.add(new Square(colorToInt,colors.get(0),colors.get(1),colors.get(2),colors.get(3)));
        }
        return (ArrayList<Square>) squares;
    }


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




    //classes immediately used for solving problems

    protected class Square{
        private int top;
        private int bottom;
        private int left;
        private int right;
        private Map<String, Integer> colorToInt;

        public Square(Map<String,Integer> colorToInt, String top, String bottom, String left, String right){
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
