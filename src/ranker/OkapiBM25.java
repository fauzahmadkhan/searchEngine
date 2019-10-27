package ranker;

import java.util.ArrayList;
import java.util.HashMap;

public class OkapiBM25 {

    public class DocumentScore {

        public int docID;
        public double score;

        DocumentScore() {
            docID = 0;
            score = 0;
        }
    }

    DocumentScore[] documentScores;

    IndexData indexData;

    ArrayList<Integer> queryTermIDs;
    double avg_query_length = 0;

    public static final double K1 = 1.2;
    public static final double K2 = 300;
    public static final double B = 0.75;

    public OkapiBM25(IndexData indexData, ArrayList<Integer> queryTermIDs, double avg_query_length) {

        this.indexData = indexData;
        this.queryTermIDs = queryTermIDs;
        this.documentScores = new DocumentScore[indexData.docNames.size()];

        for (int i = 0; i < indexData.docNames.size(); i++)
            this.documentScores[i] = new DocumentScore();

    }

    public void computeScores() {

        HashMap<Integer, ArrayList<IndexData.TermFreqInDoc>> queryTermsFreq_docs = new HashMap<>();

        for (int i = 0; i < queryTermIDs.size(); i++) {

            ArrayList<IndexData.TermFreqInDoc> termFreqInDocs = indexData.findTermFreqInTermOccurredDocs(queryTermIDs.get(i));
            queryTermsFreq_docs.put(queryTermIDs.get(i), termFreqInDocs);
        }

        for (int i = 0; i < indexData.total_docs; i++) {

            this.documentScores[i].docID = i + 1;

            for (int j = 0; j < queryTermIDs.size(); j++) {

                double df = indexData.termInfo.get(queryTermIDs.get(j)).df;
                double tf = 0;

                int currentTermID = queryTermIDs.get(j);

                ArrayList<IndexData.TermFreqInDoc> termFreqInDocs = queryTermsFreq_docs.get(currentTermID);

                for (int k = 0; k < termFreqInDocs.size(); k++)
                    if (termFreqInDocs.get(k).docID == i + 1)
                        tf = termFreqInDocs.get(k).tf;

                double K = K1 * ((1 - B) + B * (indexData.docs_length[i]) / indexData.avgDocLength);

                double formula_firstPart = Math.log((indexData.total_docs + 0.5) / (df + 0.5));

                double formula_secondPart = ((1 + K1) * tf) / (K + tf);

                int termFreqInQuery = 0;

                for (int k = 0; k < queryTermIDs.size(); k++)
                    if (queryTermIDs.get(k) == queryTermIDs.get(j))
                        termFreqInQuery++;

                double formula_thirdPart = ((1 + K2) * termFreqInQuery) / (K2 + termFreqInQuery);

                this.documentScores[i].score = this.documentScores[i].score + formula_firstPart * formula_secondPart * formula_thirdPart;

            }
        }

        Sort.bubbleSort(documentScores);


    }
}
