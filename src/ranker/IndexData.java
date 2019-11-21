package ranker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class IndexData {

    public class TermFreqInDoc {

        int termID;
        int docID;
        int tf;

        TermFreqInDoc() {
            termID = docID = tf = 0;
        }
    }

    public class TermInfo {

        long offset;
        long tf_corpus;
        long df;

        TermInfo(long offset, long tf_corpus, long df) {

            this.offset = offset;
            this.tf_corpus = tf_corpus;
            this.df = df;
        }
    }


    public static final double RELEVANT_DOCS_SIZE_IN_QUERY_RANKLIST = 250;

    public HashMap<Integer, HashMap<Integer, Integer>> docIndex;
    public HashMap<Integer, String> docNames;
    public HashMap<Integer, TermInfo> termInfo;

    public double terms_tf_corpus_sum = 0;
    public int total_docs = 0;

    double[] docs_length;
    double avgDocLength = 0;

    IndexData() {

        total_docs = 0;
        docNames = new HashMap<>();
        termInfo = new HashMap<>();
        docIndex = new HashMap<>();

        readDocNames();

        total_docs = docNames.size();
        docs_length = new double[docNames.size()];

        readTermInfo();
        find_allDocs_length();

    }


    public ArrayList<TermFreqInDoc> findTermFreqInTermOccurredDocs(int termID) {

        ArrayList<TermFreqInDoc> termFreqInTermOccurredDocs = new ArrayList<>();

        TermFreqInDoc termFreqInDoc;

        long offset = this.termInfo.get(termID).offset;

        BufferedReader termIndex_reader;

        try {

            termIndex_reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\ranker\\input files\\term_index.txt"));

            //System.out.println(offset);
            termIndex_reader.skip(offset);

            String line = termIndex_reader.readLine();
            String data[] = line.split("\t");


            int docID = Integer.parseInt(data[1].split(",")[0]);
            int tf = 1;

            for (int i = 2; i < data.length; i++) {

                if (Integer.parseInt(data[i].split(",")[0]) == 0)
                    tf++;
                else {

                    termFreqInDoc = new TermFreqInDoc();

                    termFreqInDoc.docID = docID;

                    termFreqInDoc.termID = termID;

                    termFreqInDoc.tf = tf;

                    termFreqInTermOccurredDocs.add(termFreqInDoc);

                    tf = 1;

                    docID = docID + Integer.parseInt(data[i].split(",")[0]);
                }
            }

            // adding last piece of data
            termFreqInDoc = new TermFreqInDoc();

            termFreqInDoc.docID = docID;

            termFreqInDoc.termID = termID;

            termFreqInDoc.tf = tf;

            termFreqInTermOccurredDocs.add(termFreqInDoc);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return termFreqInTermOccurredDocs;
    }


    public void find_allDocs_length() {

        try {

            BufferedReader docIndex_reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\ranker\\input files\\doc_index.txt"));

            long docLength = 0;

            String line = docIndex_reader.readLine();

            String data[] = line.split("\t");

            int docID = Integer.parseInt(data[0]);

            docLength = docLength + data.length - 2;

            HashMap<Integer, Integer> docTermsAndFreq = new HashMap<>();

            docTermsAndFreq.put(Integer.parseInt(data[1]), data.length - 2);


            while ((line = docIndex_reader.readLine()) != null) {

                data = line.split("\t");

                if (Integer.parseInt(data[0]) == docID) {

                    docLength = docLength + data.length - 2;

                    docTermsAndFreq.put(Integer.parseInt(data[1]), data.length - 2);
                } else {

                    docIndex.put(docID, docTermsAndFreq);

                    avgDocLength = avgDocLength + docLength;

                    if (docID < docs_length.length)
                        docs_length[docID - 1] = docLength;

                    docID = Integer.parseInt(data[0]);

                    docTermsAndFreq = new HashMap<>();
                    docTermsAndFreq.put(Integer.parseInt(data[1]), data.length - 2);

                    docLength = data.length - 2;
                }
            }

            avgDocLength = avgDocLength + docLength;

            if (docID < docs_length.length)
                docs_length[docID - 1] = docLength;

            docIndex.put(docID, docTermsAndFreq);

            avgDocLength = avgDocLength / docNames.size();

            docIndex_reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readTermInfo() {

        try {

            BufferedReader termInfo_reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\ranker\\input files\\term_info.txt"));

            String line = termInfo_reader.readLine();

            while (line != null) {

                String data[] = line.split("\t");
                termInfo.put(Integer.parseInt(data[0]), new TermInfo(Long.parseLong(data[1]), Long.parseLong(data[2]), Long.parseLong(data[3])));

                terms_tf_corpus_sum = terms_tf_corpus_sum + Long.parseLong(data[2]);

                line = termInfo_reader.readLine();
            }

            termInfo_reader.close();

        } catch (IOException e) {
            System.out.println("An IO Exception occurred in getDocID function while reading term_info.txt");
        }
    }


    private void readDocNames() {

        try {

            BufferedReader docIDs_reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\ranker\\input files\\docids.txt"));

            String line = docIDs_reader.readLine();

            while (line != null) {

                String data[] = line.split("\t");
                docNames.put(Integer.parseInt(data[0]), data[1]);

                line = docIDs_reader.readLine();
            }

            docIDs_reader.close();

        } catch (IOException e) {
            System.out.println("An IO Exception occurred in getDocID function while reading docids.txt");
        }


    }

}


