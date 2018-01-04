package com.jimboweb.puzzleassembly;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Test the PuzzleAssembly class
 */
public class PuzzleAssemblyTest {

    /**
     * Test assemblePuzzle method
     */
    @Test
    public void testAssemblePuzzle(){
        PuzzleAssembly pa = new PuzzleAssembly();
        PuzzleAssembly.TestInput ti = pa.new TestInput();
        PuzzleAssembly.TestOutputter to = pa.new TestOutputter();
        List<String> inputStrings = new ArrayList<>();
        String inputString = "(yellow,black,black,blue)\n" + //0
                "(blue,blue,black,yellow)\n" + //1
                "(orange,yellow,black,black)\n" + //2
                "(red,black,yellow,green)\n" + //3
                "(orange,green,blue,blue)\n" + //4
                "(green,blue,orange,black)\n" + //5
                "(black,black,red,red)\n" + //6
                "(black,red,orange,purple)\n" + //7
                "(black,purple,green,black)"; //8
        inputStrings = new ArrayList<String>(Arrays.asList(inputString.split("\n")));
        ti.setInput(inputStrings);
        pa.AssemblePuzzle(ti,to);
        String expectedOutput = "(black,black,red,red);(black,red,orange,purple);(black,purple,green,black)\n" +
                "(red,black,yellow,green);(orange,green,blue,blue);(green,blue,orange,black)\n" +
                "(yellow,black,black,blue);(blue,blue,black,yellow);(orange,yellow,black,black)";
        assertEquals(to.getOutput(),expectedOutput);
    }
}
