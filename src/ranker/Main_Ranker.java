package ranker;


import java.io.*;
import java.util.ArrayList;

public class Main_Ranker {

    public static void main(String args[]) {

        IndexData indexData = new IndexData();

        QueryProcessor queryProcessor = new QueryProcessor();

        ArrayList<QueryProcessor.QueryIdTextPair> QueryIdTextPairArrayList = queryProcessor.processQueries();

        double queries_terms_length_sum = 0;

        for (int i = 0; i < QueryIdTextPairArrayList.size(); i++)
            queries_terms_length_sum = queries_terms_length_sum + QueryIdTextPairArrayList.get(i).queryTermIDs.size();


        // Okapi BM25 algorithm
        if (args[1].toUpperCase().equals("BM25")) {

            BufferedWriter writer = null;

            try {

                writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\ranker\\output files\\BM25_results.txt"));

            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < QueryIdTextPairArrayList.size(); i++) {

                System.out.println("Now processing Query#: " + (i + 1) + " : Query ID= " + QueryIdTextPairArrayList.get(i).queryID);
                OkapiBM25 okapiBM25 = new OkapiBM25(indexData, QueryIdTextPairArrayList.get(i).queryTermIDs);
                okapiBM25.computeScores();

                try {

                    for (int j = 0; j < IndexData.RELEVANT_DOCS_SIZE_IN_QUERY_RANKLIST; j++) {

                        writer.write(QueryIdTextPairArrayList.get(i).queryID + " " + 0 + " " + indexData.docNames.get(okapiBM25.documentScores[j].docID) + " " + (j + 1) + " " + okapiBM25.documentScores[j].score + " " + "run1" + "\n");

//                        if (i != QueryIdTextPairArrayList.size() - 1 && j != IndexData.RELEVANT_DOCS_SIZE_IN_QUERY_RANKLIST - 1) {
//                            writer.write("\n");
//                        }

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


        // Dirichlet Smoothing algorithm
        else if (args[1].toUpperCase().equals("DS")) {

            BufferedWriter writer = null;

            try {
                writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\ranker\\output files\\DS_results.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < QueryIdTextPairArrayList.size(); i++) {

                System.out.println("Now processing Query#: " + (i + 1) + " : Query ID= " + QueryIdTextPairArrayList.get(i).queryID);

                DirichletSmoothing dirichletSmoothing = new DirichletSmoothing(indexData, QueryIdTextPairArrayList.get(i).queryTermIDs);

                dirichletSmoothing.computeScores();

                try {

                    for (int j = 0; j < IndexData.RELEVANT_DOCS_SIZE_IN_QUERY_RANKLIST; j++) {

                        writer.write(QueryIdTextPairArrayList.get(i).queryID + "\t" + 0 + "\t" + indexData.docNames.get(dirichletSmoothing.documentScores[j].docID) + "\t" + (j + 1) + "\t" + dirichletSmoothing.documentScores[j].score + "\t" + "run1" + "\n");

//                        if (i != QueryIdTextPairArrayList.size() - 1 && j != IndexData.RELEVANT_DOCS_SIZE_IN_QUERY_RANKLIST - 1)
//                            writer.write("\n");
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


        } else if (args[1].toUpperCase().equals("EV")) {


            Evaluation evaluation = new Evaluation();

            evaluation.getDataFromInputFiles();
            evaluation.matchDocsandAssignRelevance();
            evaluation.computePrecision();

            /*
            BufferedWriter writer = null;

            try {

                writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\src\\ranker\\output files\\EVALUATION_results.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }


             */

        }


    }
}
