package com.jimboweb.puzzleassembly;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class PuzzleAssemblyTest {

    @Test
    public void testAssemblePuzzle(){
        PuzzleAssembly pa = new PuzzleAssembly();
        PuzzleAssembly.TestInput ti = pa.new TestInput();
        PuzzleAssembly.TestOutputter to = pa.new TestOutputter();
        List<String> inputStrings = new ArrayList<>();
        String inputString = "(yellow,black,black,blue)\n" +
                "(blue,blue,black,yellow)\n" +
                "(orange,yellow,black,black)\n" +
                "(red,black,yellow,green)\n" +
                "(orange,green,blue,blue)\n" +
                "(green,blue,orange,black)\n" +
                "(black,black,red,red)\n" +
                "(black,red,orange,purple)\n" +
                "(black,purple,green,black)";
        inputStrings = new ArrayList<String>(Arrays.asList(inputString.split("\n")));
        ti.setInput(inputStrings);
        pa.AssemblePuzzle(ti,to);
        String expectedOutput = "(black,black,red,red);(black,red,orange,purple);(black,purple,green,black)\n" +
                "(red,black,yellow,green);(orange,green,blue,blue);(green,blue,orange,black)\n" +
                "(yellow,black,black,blue);(blue,blue,black,yellow);(orange,yellow,black,black)";
        assertEquals(to.getOutput(),expectedOutput);
    }
}
