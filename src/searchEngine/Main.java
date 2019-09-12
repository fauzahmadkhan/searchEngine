package searchEngine;

public class Main {

    public static void main(String args[]){

        if (args.length != 0){

            System.out.println(args[0]);
            Tokenizer tokenizer = new Tokenizer(args[0]);
            tokenizer.tokenize_and_write();

            System.gc();    // garbage collector


        }
        else
            System.out.println("Please give a directory name!");


    }
}
