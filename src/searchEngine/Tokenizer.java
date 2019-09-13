package searchEngine;

// Snowball stemming algorithm
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

public class Tokenizer {

    private String path;
    private ArrayList<String> fileNames;
    private HashMap<String, Integer> dictionary;
    private HashMap<String, String> stoplist;
    private TreeMap<Tuple, ArrayList<Integer>> forwardIndex;
    int terms =0;

    public class Tuple implements Comparable{

        private int termID;
        private int docID;

        public Tuple(int termID, int docID){

            this.termID = termID;
            this.docID = docID;
        }
        public void setTuple(int termID, int docID){

            this.termID = termID;
            this.docID = docID;
        }


        @Override
        public int compareTo(Object obj){

            if (obj == null)
                return -2;

            if (this.termID == ((Tuple) obj).termID && this.docID == ((Tuple) obj).docID)
                return 0;

            if (this.termID > ((Tuple) obj).termID || this.docID > ((Tuple) obj).docID)
                return 1;

            if (this.termID < ((Tuple) obj).termID || this.docID < ((Tuple) obj).docID)
                return -1;

            return -2;

        }


        @Override
        public boolean equals(Object obj){
            if (this == obj)
                return true;

            if (!(obj instanceof Tuple))
                return false;

            return (this.termID == ((Tuple) obj).termID && this.docID == ((Tuple) obj).docID);
        }

        @Override
        public int hashCode(){
            return termID + (docID * 31);
        }

    }

    Tokenizer(String abs_path){

        path = abs_path;
        fileNames = FileHandler.getFileNamesFromDirectory(abs_path);
        dictionary = new HashMap<String, Integer>();
        stoplist = new HashMap<String, String>();
        forwardIndex = new TreeMap<Tuple, ArrayList<Integer>>();
        stoplist_reader();
    }

    void stoplist_reader(){

        try{
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\stoplist.txt"));

            String word = reader.readLine();

            while (word != null){
                stoplist.put(word, null);
                word = reader.readLine();
            }

        }
        catch (IOException e){
            System.out.println("stoplist_reader() error");
        }

    }

    public void tokenize_and_write(){

        BufferedWriter termID_writer =null;
        BufferedWriter docID_writer =null;
        BufferedWriter forwardIndex_writer =null;
        EnglishStemmer stemmer = new EnglishStemmer();


        try{
            termID_writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\searchEngine\\termids.txt"));

            docID_writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\searchEngine\\docids.txt"));

            forwardIndex_writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\searchEngine\\doc_index.txt"));

        }
        catch (IOException e){
            System.out.println("tokenize_and_write() error");
        }


        int current_docID =1;
        for (int i=0; i< fileNames.size(); i++){

            System.out.println("Processing Document#: " + (i + 1) + ".(" + fileNames.get(i) + ")");

            String extract = FileHandler.fetchString_http(path + "\\" + fileNames.get(i));


            if (extract == null)
                continue;

            String[] tokens = extract.split("[\\p{Punct}\\s]+");

            int termID =1;
            int current_position =1;
            boolean not_in_forwardIndex = false;
            for (int j=0; j <tokens.length; j++){

                tokens[j] = Normalizer.normalize(tokens[j], Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "").toLowerCase();

                if (tokens[j].equals("") || tokens[j].length() < 2)
                    continue;

                stemmer.setCurrent(tokens[j]);
                stemmer.stem();

                if (stoplist.containsKey(tokens[j]) || stoplist.containsKey(stemmer.getCurrent()))
                    continue;

                if (!(dictionary.containsKey(stemmer.getCurrent()))){
                    try{
                        termID_writer.write((termID) + "\t" + stemmer.getCurrent() + "\r\n");
                        dictionary.put(stemmer.getCurrent(), termID);
                        termID++;
                        terms++;

                    }
                    catch (IOException e){
                        System.out.println("Error in the file mapping of termsid.txt");
                    }

                }

                Tuple tuple = new Tuple(current_docID, dictionary.get(stemmer.getCurrent()));

                if (!(forwardIndex.containsKey(tuple))){
                    not_in_forwardIndex = true;
                    forwardIndex.put(tuple, new ArrayList<Integer>());
                }

                forwardIndex.get(tuple).add(current_position);

            }

            if (not_in_forwardIndex) {

                try {
                    docID_writer.write(current_docID + "\t" + fileNames.get(i) + (i == fileNames.size() - 1 ? "" : "\r\n"));
                    current_docID++;
                    not_in_forwardIndex = false;
                } catch (IOException e) {
                    System.out.println("Error in the file mapping of docids.txt ");
                }
            }

                Iterator iterator = forwardIndex.entrySet().iterator();

                while (iterator.hasNext()){

                    Map.Entry tup = (Map.Entry) iterator.next();
                    Tuple index_tuple = (Tuple) tup.getKey();

                    ArrayList<Integer> val = (ArrayList<Integer>) tup.getValue();

                    try {

                        forwardIndex_writer.write(index_tuple.docID + "\t" + index_tuple.termID);

                        for (int k=0; k < val.size(); k++)
                            forwardIndex_writer.write("\t" + val.get(k));

                        iterator.remove();

                        if (iterator.hasNext() == false && i == fileNames.size() -1 )
                            break;

                        forwardIndex_writer.write("\r\n");
                    }
                    catch (IOException e){
                        System.out.println("Error in file mapping of doc_index.txt");
                    }
                }
            }

            try{

                BufferedWriter count_writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\searchEngine\\counter.txt"));
                count_writer.write(terms + "\t" + (current_docID - 1));
                docID_writer.close();
                termID_writer.close();
                forwardIndex_writer.close();
                count_writer.close();

        }
        catch (IOException e){
                e.printStackTrace();
        }


    }




}
