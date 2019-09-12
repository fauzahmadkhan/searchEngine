package searchEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class InvertedIndex {

    private class IndexItem {


        ArrayList<DocIDandOffset> docIDandOffset;
        int times_term_in_documents =0;

        public IndexItem(){

            docIDandOffset = new ArrayList<DocIDandOffset>();
        }

        public class DocIDandOffset{

            int item_docID;
            int item_offset;

            public DocIDandOffset(int docID, int offset){

                item_docID = docID;
                item_docID = offset;
            }
        }

    }

    private IndexItem []indexItems;
    private int total_docs =0;
    private int total_terms =0;


    InvertedIndex(){

        try {

            BufferedReader counter_reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\counter.txt"));
            String counter_data;
            counter_data = counter_reader.readLine();

            total_terms = Integer.parseInt(counter_data.split("\t")[0]);
            total_docs = Integer.parseInt(counter_data.split("\t")[1]);

            counter_reader.close();

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
