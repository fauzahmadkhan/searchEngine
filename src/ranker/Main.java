package ranker;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String args[]) {

        IndexData indexData = new IndexData();

        QueryExtractor queryExtractor = new QueryExtractor();

        ArrayList<QueryExtractor.QueryIDStringPair> queryIDStringPairArrayList = queryExtractor.extractQueries();

        double query_length_sum = 0;
        double avg_query_length = 0;

        for (int i = 0; i < queryIDStringPairArrayList.size(); i++)
            query_length_sum = query_length_sum + queryIDStringPairArrayList.get(i).queryTermIDs.size();

        avg_query_length = query_length_sum / queryIDStringPairArrayList.size();

        if (args[1].toUpperCase().equals("BM25")) {

            BufferedWriter writer = null;

            try {

                writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\ranker\\output files\\BM25_results.txt"));

            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < queryIDStringPairArrayList.size(); i++) {

                System.out.println("Now processing Query#: " + -(i + 1) + " : Query ID= " + queryIDStringPairArrayList.get(i).queryID);
                OkapiBM25 okapiBM25 = new OkapiBM25(indexData, queryIDStringPairArrayList.get(i).queryTermIDs, avg_query_length);
                okapiBM25.computeScores();

                try {

                    for (int j = 0; j < IndexData.relevent_docs_size_in_rankList; j++) {

                        writer.write(queryIDStringPairArrayList.get(i).queryID + " " + 0 + " " + indexData.docNames.get(okapiBM25.documentScores[j].docID) + " " + (j + 1) + " " + okapiBM25.documentScores[j].score + " " + "RUN");

                        if (i != queryIDStringPairArrayList.size() - 1 && j != IndexData.relevent_docs_size_in_rankList - 1) {
                            writer.write("\n");
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if (args[1].toUpperCase().equals("DS")){



        }


    }
}
