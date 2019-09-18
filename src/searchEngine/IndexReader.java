package searchEngine;

import org.tartarus.snowball.ext.EnglishStemmer;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class IndexReader {

    private static class TermMetaData{

        long offset;
        int frequency;
        int times_term_in_documents =0;

        public TermMetaData(long offset, int frequency, int times_term_in_documents){

            this.offset = offset;
            this.frequency = frequency;
            this.times_term_in_documents = times_term_in_documents;
        }
    }

    private static class DocMetaData{

        int unique_terms =0;
        int total_terms =0;
    }

    private static class TermDocMetaData{

        int term_frequency_in_doc =0;
        ArrayList<Integer> positions = new ArrayList<Integer>();
    }


    private static EnglishStemmer englishStemmer = new EnglishStemmer();


    private static int getTermID(String term){

        int retValue = -1;

        try
        {
            BufferedReader termID_docReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\termids.txt"));

            String line = termID_docReader.readLine();

            while (line != null){

                String data[] = line.split("\t");

                englishStemmer.setCurrent(term.toLowerCase());
                englishStemmer.stem();

                if (englishStemmer.getCurrent().equals(data[1])){

                    retValue = Integer.parseInt(data[0]);
                    break;
                }

                line = termID_docReader.readLine();
            }

            termID_docReader.close();
        }
        catch (IOException e){
            System.out.println("getTermID error while reading termids.txt");
        }


        return retValue;
    }


    private static int getDocID(String doc){

        int retValue = -1;

        try
        {
            BufferedReader docID_Reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\docids.txt"));

            String line = docID_Reader.readLine();

            while (line != null){

                String data[] = line.split("\t");

                if (doc.equals(data[1])){

                    retValue = Integer.parseInt(data[0]);
                    break;
                }

                line = docID_Reader.readLine();
            }

            docID_Reader.close();

        }
        catch (IOException e){
            System.out.println("getDocID error while reading docids.txt");
        }

        return retValue;

    }

    //get document information like total terms & unique terms, using doc_index.txt
    public static DocMetaData getDocMetaData(int docID){

        DocMetaData docMetaData = new DocMetaData();

        try
        {
           BufferedReader docIndexReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\doc_index.txt"));

           String line = null;

           while ((line = docIndexReader.readLine()) != null){


               String data[] = line.split("\t");

               if (Integer.parseInt(data[0]) == docID){

                   while (Integer.parseInt(data[0]) == docID){

                       docMetaData.unique_terms++;
                       docMetaData.total_terms = docMetaData.total_terms + data.length - 2;
                       data = docIndexReader.readLine().split("\t");
                   }
                   break;
               }
           }

           docIndexReader.close();

           return docMetaData;
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return docMetaData;
    }


    private static long getTermOffset(int termID){

        long offset = -1;
        BufferedReader termInfoReader;

        try
        {
            termInfoReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\term_info.txt"));

            String line = termInfoReader.readLine();

            while(line != null){

                String data[] = line.split("\t");

                if (Integer.parseInt(data[0]) == termID)
                    offset = Long.parseLong(data[1]);

                line = termInfoReader.readLine();
            }

            termInfoReader.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return offset;
    }


    private static TermDocMetaData getTermDocMetaData(int termID, int docID){

        TermDocMetaData termDocMetaData = new TermDocMetaData();

        long term_offset = getTermOffset(termID);

        try
        {
            BufferedReader termIndexReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\term_index.txt"));

            termIndexReader.skip(term_offset);

            String line = termIndexReader.readLine();
            termIndexReader.close();

            String data[] = line.split("\t");

            for (int i=1; i < data.length; i++){

                int docID_in_index = Integer.parseInt(data[i].split(":")[0]);

                while (docID_in_index != docID){

                    if (docID_in_index > docID)
                        return null;

                    docID_in_index = docID_in_index + Integer.parseInt(data[i].split(":")[0]);
                    i++;
                }

                int next_offset = Integer.parseInt(data[i].split(":")[1]);
                termDocMetaData.term_frequency_in_doc++;
                termDocMetaData.positions.add(next_offset);

                for (int j = i + 1; Integer.parseInt(data[j].split(":")[0]) == 0; j++){

                    next_offset = next_offset + Integer.parseInt(data[j].split(":")[1]);
                    termDocMetaData.term_frequency_in_doc++;
                    termDocMetaData.positions.add(next_offset);
                }

                return termDocMetaData;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }


    // function to find metadata of a term using term_index.txt
    private static TermMetaData getTermMetaData_by_termIndex(int termID){

        TermMetaData termMetaData =null;
        long offset =0;
        BufferedReader termIndexReader =null;

        try
        {
            termIndexReader =  new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\searchEngine\\term_index.txt"));

            offset = getTermOffset(termID);
            termIndexReader.skip(offset);

            String []requiredLine = termIndexReader.readLine().split("\t");

            int total_docs =0;
            int frequency =0;

            frequency = frequency + requiredLine.length -1;

            // finding total number of documents in which the term appeared
            for (int i=1; i < requiredLine.length; i++){

                int next_docID = Integer.parseInt(requiredLine[i].split(":")[0]);

                if (next_docID != 0)
                    total_docs++;
            }

            termMetaData = new TermMetaData(offset, frequency, total_docs);
            termIndexReader.close();

        }
        catch (IOException e){
            System.out.println("getTermMetaData_by_termIndex() error");
        }


        return termMetaData;
    }

    public static void main(String []args){

        if (args.length < 2)
            return;

        if (args[0].equals("--term") && args.length == 4){

            int termID = getTermID(args[1]);
            int docID = getDocID(args[3]);

            if (termID == -1){
                System.out.println("The term doesn't exist in the corpus.");
                return;
            }


            if (docID == -1){
                System.out.println("The document doesn't exist in the corpus.");
                return;
            }

            System.out.println("Inverted list for term: " + args[1]);
            System.out.println("In document: " + args[3]);
            System.out.println("TermID: " + termID);
            System.out.println("DocID: " + docID);

            TermDocMetaData termDocMetaData = getTermDocMetaData(termID, docID);

            if (termDocMetaData == null){
                System.out.println("The term doesn't exist in the document");
                return;
            }

            System.out.println("Term frequency in Doc: " + termDocMetaData.term_frequency_in_doc);

            System.out.println("Positions: ");
            for (int i=0; i < termDocMetaData.positions.size(); i++){

                System.out.println(termDocMetaData.positions.get(i));

                if (i < termDocMetaData.positions.size() -1)
                    System.out.print(", ");
            }

        }

        else if (args[0].equals("--doc")){

            System.out.println("Listing for Document: " + args[1]);
            int docID = getDocID(args[1]);

            if (docID == -1){
                System.out.println("The document name doesn't exist in the corpus.");
                return;
            }

            System.out.println("DocID: " + docID);

            DocMetaData docMetaData = getDocMetaData(docID);

            System.out.println("Unique Terms: " + docMetaData.unique_terms);
            System.out.println("Total Terms: " + docMetaData.total_terms);
        }

        else if (args[0].equals("--term") && args.length == 2){

            System.out.println("Inverted Index list for Term: " + args[1]);

            int termID = getTermID(args[1]);

            if (termID == -1){
                System.out.println("The term doesn't exist in the corpus.");
                return;
            }
            else {

                System.out.println("TermID: " + termID);

                TermMetaData termMetaData = getTermMetaData_by_termIndex(termID);

                if (termMetaData == null)
                    System.out.println("Invalid termID");

                else {

                    System.out.println("No. of documents containing term: " + termMetaData.times_term_in_documents);
                    System.out.println("Term frequency in the corpus: " + termMetaData.frequency);
                    System.out.println("Inverted Index list offset: " + termMetaData.offset);

                }



            }
        }
    }

}
