package ranker;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Evaluation {


    public class DocInfo {

        public String docID;
        public int rank;
        public double precision;
        public int relevance;

        DocInfo() {
            rank = 0;
            precision = 0;
            relevance = 0;
        }

        DocInfo(String docID, int relevance) {
            this.docID = docID;
            this.relevance = relevance;
        }


        DocInfo(String docID, int rank, double precision) {
            this.docID = docID;
            this.rank = rank;
            this.precision = precision;
        }


    }

    public class RelevanceJudgment {

        public int queryID;
        //public HashMap<String, Integer> docIDandRank;
        public ArrayList<DocInfo> docInfo;
        public double queryPrecisionSum;
        public double avgPrecision;

        RelevanceJudgment() {
            queryID = 0;
            //docIDandRank = new HashMap<>();
            docInfo = new ArrayList<>();
            queryPrecisionSum = 0;
            avgPrecision = 0;
        }
    }

    ArrayList<RelevanceJudgment> relevanceJudgments = new ArrayList<>();
    ArrayList<RelevanceJudgment> scoringData = new ArrayList<>();
    public double queriesPrecisionSummation = 0;
    public double MAP = 0;


    public void getDataFromInputFiles() {

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\ranker\\input files\\relevance judgements.qrel"));

            String line = reader.readLine();
            String data[] = line.split(" ");


            RelevanceJudgment relevanceJudgment = new RelevanceJudgment();

            relevanceJudgment.queryID = Integer.parseInt(data[0]);
            relevanceJudgment.docInfo.add(new DocInfo(data[2], Integer.parseInt(data[3])));

            relevanceJudgments.add(relevanceJudgment);
            int index = 0;


            while ((line = reader.readLine()) != null) {

                data = line.split(" ");

                while (relevanceJudgments.get(index).queryID == Integer.parseInt(data[0]) && line != null) {

                    relevanceJudgments.get(index).docInfo.add(new DocInfo(data[2], Integer.parseInt(data[3])));
                    line = reader.readLine();
                    if (line != null)
                        data = line.split(" ");
                }

                if (line != null) {

                    RelevanceJudgment new_relevanceJudgment = new RelevanceJudgment();

                    new_relevanceJudgment.queryID = Integer.parseInt(data[0]);
                    new_relevanceJudgment.docInfo.add(new DocInfo(data[2], Integer.parseInt(data[3])));

                    relevanceJudgments.add(new_relevanceJudgment);
                    index++;
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }


        try {

            reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\ranker\\output files\\BM25_results.txt"));

            String line = reader.readLine();
            String data[] = line.split(" ");


            RelevanceJudgment okapiBm25_score = new RelevanceJudgment();

            okapiBm25_score.queryID = Integer.parseInt(data[0]);
            okapiBm25_score.docInfo.add(new DocInfo(data[2], Integer.parseInt(data[3]), Double.parseDouble(data[4])));

            scoringData.add(okapiBm25_score);
            int index = 0;


            while ((line = reader.readLine()) != null) {

                data = line.split(" ");


                while (scoringData.get(index).queryID == Integer.parseInt(data[0]) && line != null) {

                    scoringData.get(index).docInfo.add(new DocInfo(data[2], Integer.parseInt(data[3]), Double.parseDouble(data[4])));

                    line = reader.readLine();
                    if (line != null)
                        data = line.split(" ");
                }

                if (line != null) {


                    RelevanceJudgment new_okapiBm25_score = new RelevanceJudgment();

                    new_okapiBm25_score.queryID = Integer.parseInt(data[0]);
                    new_okapiBm25_score.docInfo.add(new DocInfo(data[2], Integer.parseInt(data[3]), Double.parseDouble(data[4])));

                    scoringData.add(new_okapiBm25_score);
                    index++;
                }


            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void matchDocsandAssignRelevance() {


        for (int i = 0; i < scoringData.size(); i++)

            for (int j = 0; j < relevanceJudgments.size(); j++)

                if (scoringData.get(i).queryID == relevanceJudgments.get(j).queryID)

                    for (int k = 0; k < scoringData.get(i).docInfo.size(); k++)

                        for (int l = 0; l < relevanceJudgments.get(j).docInfo.size(); l++)

                            if (scoringData.get(i).docInfo.get(k).docID.equals(relevanceJudgments.get(j).docInfo.get(l).docID))
                                scoringData.get(i).docInfo.get(k).relevance = relevanceJudgments.get(j).docInfo.get(l).relevance;


    }

    public void computePrecision() {

        System.out.println("Total Queries = " + scoringData.size());

        for (int i = 0; i < scoringData.size(); i++) {

            for (int j = 0; j < scoringData.get(i).docInfo.size(); j++) {

                if (scoringData.get(i).docInfo.get(j).relevance > 0)
                    scoringData.get(i).queryPrecisionSum += scoringData.get(i).docInfo.get(j).precision;

            }

            scoringData.get(i).avgPrecision = scoringData.get(i).queryPrecisionSum / (scoringData.get(i).docInfo.size());

            queriesPrecisionSummation += scoringData.get(i).queryPrecisionSum;

            System.out.println("-------------------------");
            System.out.println("QueryID: " + scoringData.get(i).queryID);
            System.out.println("Total docs: " + scoringData.get(i).docInfo.size());
//            System.out.println("Precision Summation: " + scoringData.get(i).queryPrecisionSum);
            System.out.println("P@5: " + (scoringData.get(i).queryPrecisionSum /5));
            System.out.println("P@10: " + (scoringData.get(i).queryPrecisionSum /10));
            System.out.println("P@20: " + (scoringData.get(i).queryPrecisionSum /20));
            System.out.println("P@30: " + (scoringData.get(i).queryPrecisionSum /30));
            System.out.println("Average precision: " + scoringData.get(i).avgPrecision + "\n\n");


        }

        MAP = queriesPrecisionSummation / (scoringData.size());


        System.out.println("P@5 = " + queriesPrecisionSummation / 5);
        System.out.println("P@10 = " + queriesPrecisionSummation / 10);
        System.out.println("P@20 = " + queriesPrecisionSummation / 20);
        System.out.println("P@30 = " + queriesPrecisionSummation / 30);
        System.out.println("MAP = " + MAP);


    }
}
