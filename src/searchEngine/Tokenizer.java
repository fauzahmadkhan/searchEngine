package searchEngine;

// Snowball stemming algorithm
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

public class Tokenizer {

    private String path;
    private ArrayList<String> fileNames;
    private HashMap<String, Integer> dictionary;    // also contains mapping to termids
    private HashMap<String, String> stoplist;
    private TreeMap<Tuple, ArrayList<Integer>> forwardIndex;        // without hashMap
    int terms =0;

    // Tuple <termID, docID> implements the Comparable interface
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
            // reading stoplis.txt file
            BufferedReader stoplist_reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\stoplist.txt"));

            String word = stoplist_reader.readLine();

            // creating hashMap of the stoplist
            while (word != null){
                stoplist.put(word, null);     // terminating each entry with null
                word = stoplist_reader.readLine();
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



        // creating termids.txt, docids.txt & doc_index.txt files
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

            // reading the document
            String extract = FileHandler.fetchString_http(path + "\\" + fileNames.get(i));


            if (extract == null)
                continue;

            // tokenizing based on this delimiting regular expression [\\p{Punct}\\s]+ with no limit
            String[] tokens = extract.split("[\\p{Punct}\\s]+");

            int termID =1;
            int current_position =1;
            boolean not_in_forwardIndex = false;
            for (int j=0; j <tokens.length; j++){

                // removing non-ASCII characters with this delimiting expression [^\\x00-\\x7F] & changing to lower case
                tokens[j] = Normalizer.normalize(tokens[j], Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "").toLowerCase();

                if (tokens[j].equals("") || tokens[j].length() < 2)
                    continue;

                // applying the stemmer
                stemmer.setCurrent(tokens[j]);
                stemmer.stem();

                // removing stop words
                if (stoplist.containsKey(tokens[j]) || stoplist.containsKey(stemmer.getCurrent()))
                    continue;


                // if the stemmed term doesn't exist in the dictionary then add it in the dictionary
                // as well as in termids.txt
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

                // <docID, termID> tuple
                Tuple tuple = new Tuple(current_docID, dictionary.get(stemmer.getCurrent()));

                // adding the tuple to forwardIndex, if the forwardIndex doesn't contain the tuple
                if (!(forwardIndex.containsKey(tuple))){
                    not_in_forwardIndex = true;
                    forwardIndex.put(tuple, new ArrayList<Integer>());
                }

                forwardIndex.get(tuple).add(current_position);

            }

            // also writing current_docID to docids.txt
            if (not_in_forwardIndex) {

                try {
                    docID_writer.write(current_docID + "\t" + fileNames.get(i) + (i == fileNames.size() - 1 ? "" : "\r\n"));
                    current_docID++;
                    not_in_forwardIndex = false;
                } catch (IOException e) {
                    System.out.println("Error in the file mapping of docids.txt ");
                }
            }


            // writing forwardIndex to disk

            Iterator forwardIndex_iterator = forwardIndex.entrySet().iterator();

            while (forwardIndex_iterator.hasNext()){

                    Map.Entry map_entry = (Map.Entry) forwardIndex_iterator.next();
                    Tuple index_tuple = (Tuple) map_entry.getKey();

                    ArrayList<Integer> val = (ArrayList<Integer>) map_entry.getValue();

                    try {

                        forwardIndex_writer.write(index_tuple.docID + "\t" + index_tuple.termID);

                        for (int k=0; k < val.size(); k++)
                            forwardIndex_writer.write("\t" + val.get(k));

                        forwardIndex_iterator.remove(); // avoids a ConcurrentModificationException

                        if (forwardIndex_iterator.hasNext() == false && i == fileNames.size() -1 ) // preventing the new line at the end of file
                            break;

                        forwardIndex_writer.write("\r\n");
                    }
                    catch (IOException e){
                        System.out.println("Error in file mapping of doc_index.txt");
                    }
                }

        }   // end for loop

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
