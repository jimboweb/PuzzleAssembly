package com.jimboweb.puzzleassembly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class PuzzleAssembly {

    FastScanner scanner;

    public PuzzleAssembly(){
        scanner = new FastScanner();
    }

    private static class PuzzleThread implements Runnable{
        @Override
        public void run() {
            AssemblePuzzle();
        }
    }

    public static void main(String[] args) {
        PuzzleThread pt = new PuzzleThread();
        pt.run();

    }

    private void AssemblePuzzle(){
        List<String> inputs = new ArrayList<>();
        for(int i=0;i<25;i++){
            inputs.add(scanner.next());

        }
    }

    private void input(){

    }

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

}
