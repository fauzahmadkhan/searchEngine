package ranker;

import java.util.ArrayList;
import java.util.HashMap;

public class DirichletSmoothing {

    double N = 0;
    double mu = 0;
    double LAMBDA = 0;
    DocumentScore[] documentScores;
    IndexData indexData;
    ArrayList<Integer> queryTermIDs;

    public class DocumentScore {

        public int docID;
        public double score;

        DocumentScore() {

            docID = 0;
            score = 1;
        }
    }


    public void bubbleSort(DocumentScore arr[]) {

        int n = arr.length;
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (arr[j].score < arr[j + 1].score) {

                    DocumentScore temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
    }

    DirichletSmoothing(IndexData indexData, ArrayList<Integer> queryTermIDs) {

        this.indexData = indexData;
        this.queryTermIDs = queryTermIDs;

        this.documentScores = new DocumentScore[indexData.docNames.size()];

        N = indexData.total_docs;
        mu = indexData.avgDocLength;

        LAMBDA = N / (N + mu);

        for (int i = 0; i < indexData.docNames.size(); i++)
            documentScores[i] = new DocumentScore();

    }

    public void computeScores() {

        HashMap<Integer, ArrayList<IndexData.TermFreqInDoc>> queryTermFreq_docs = new HashMap<>();

        for (int i = 0; i < queryTermIDs.size(); i++) {

            ArrayList<IndexData.TermFreqInDoc> termFreqInDocs = indexData.findTermFreqInTermOccurredDocs(queryTermIDs.get(i));
            queryTermFreq_docs.put(queryTermIDs.get(i), termFreqInDocs);
        }

        for (int i = 0; i < documentScores.length; i++) {

            for (int j = 0; j < queryTermIDs.size(); j++) {

                double documentWeightScore = 0;

                int currentTermID = queryTermIDs.get(j);

                ArrayList<IndexData.TermFreqInDoc> termFreqInDocs = queryTermFreq_docs.get(currentTermID);

                for (int k = 0; k < termFreqInDocs.size(); k++) {

                    if (termFreqInDocs.get(k).docID == i + 1)
                        documentWeightScore = LAMBDA * (termFreqInDocs.get(k).tf / indexData.docs_length[i]);
                }

                double corpusWeightScore = (1 - LAMBDA) * (indexData.termInfo.get(currentTermID).tf_corpus / indexData.terms_tf_corpus_sum);


                double total_probability_of_currentTerm = documentWeightScore + corpusWeightScore;
                documentScores[i].docID = i + 1;
                documentScores[i].score = documentScores[i].score * total_probability_of_currentTerm;

            }


        }

        bubbleSort(documentScores);
    }


}
