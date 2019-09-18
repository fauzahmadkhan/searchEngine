package searchEngine;

import java.io.*;
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

    public void createInvertedIndex_inMemory(){

        indexItems = new IndexItem[total_terms + 1];

        for (int i=1; i < total_terms + 1; i++)
            this.indexItems[i] = new IndexItem();


        try (BufferedReader buf = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\doc_index.txt")))
        {
            String line;
            int lineNum =1;

            System.out.println("Indexer, processing lines in the doc_index.txt file");
            while ((line = buf.readLine()) != null){

                String data[] = line.split("\t");

                int termID = Integer.parseInt(data[1]);
                int docID = Integer.parseInt(data[0]);

                System.out.println("Processing line#: " + (lineNum++));

                this.indexItems[termID].times_term_in_documents++;

                ArrayList<IndexItem.DocIDandOffset> docIDandOffsetArrayList = this.indexItems[termID].docIDandOffset;

                int previous_docID =0;
                for (int i=0; i < docIDandOffsetArrayList.size(); i++)
                    previous_docID = previous_docID + docIDandOffsetArrayList.get(i).item_docID;


                // document delta
                int delta_docID = docID - previous_docID;

                // adding the document delta
                docIDandOffsetArrayList.add(indexItems[termID].new DocIDandOffset(delta_docID, Integer.parseInt(data[2])));
                int termOffset_aggregate = Integer.parseInt(data[2]);

                for (int i=3; i < data.length; i++)
                {
                    int delta_offset = Integer.parseInt(data[i]) - termOffset_aggregate;

                    docIDandOffsetArrayList.add(indexItems[termID].new DocIDandOffset(0, delta_offset));
                    termOffset_aggregate = termOffset_aggregate + delta_offset;
                }

            }

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeInvertedIndex_toFile(){

        try
        {
            BufferedWriter termIndexWriter = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\searchEngine\\term_index.txt"));

            BufferedWriter termInfoWriter = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\searchEngine\\term_info.txt"));

            long offset =0;
            for (int i=0; i < total_terms +1; i++){

                termInfoWriter.write(i + "\t" + offset + "\t" + this.indexItems[i].docIDandOffset.size() + "\t" + this.indexItems[i].times_term_in_documents);

                termIndexWriter.write(i + "");

                offset = offset + String.valueOf(i).length();

                for (int j=0; j < this.indexItems[i].docIDandOffset.size(); j++){

                    offset = offset + 1 + String.valueOf(this.indexItems[i].docIDandOffset.get(i).item_docID).length() + 1 + String.valueOf(this.indexItems[i].docIDandOffset.get(j).item_offset).length();

                    termIndexWriter.write("\t" + this.indexItems[i].docIDandOffset.get(j).item_docID + ":" + this.indexItems[i].docIDandOffset.get(j).item_offset);

                }

                if ( i < total_terms){

                    offset++;
                    termIndexWriter.write("\n");
                    termInfoWriter.write("\n");
                }


            }

            termIndexWriter.close();
            termInfoWriter.close();



        }
        catch (IOException e){
            System.out.println("writeInvertedIndex_toFile() error");
        }
    }
}
