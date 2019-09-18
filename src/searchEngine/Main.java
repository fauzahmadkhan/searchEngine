package searchEngine;

import javax.imageio.IIOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String args[]){

        if (args.length != 0){

            System.out.println(args[0]);    // absolute path


            Tokenizer tokenizer = new Tokenizer(args[0]);
            tokenizer.tokenize_and_write();

            System.gc();    // garbage collector

            InvertedIndex invertedIndex = new InvertedIndex();

            invertedIndex.createInvertedIndex_inMemory();
            invertedIndex.writeInvertedIndex_toFile();


        }
        else
            System.out.println("Please give a directory name!");


    }
}
