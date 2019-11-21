package ranker;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.tartarus.snowball.ext.EnglishStemmer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;

public class QueryProcessor {

    private HashMap<String, String> stopWords;
    private HashMap<String, Integer> termIDs;
    private EnglishStemmer englishStemmer = new EnglishStemmer();

    public class QueryIdTextPair {

        public int queryID;
        public String query;
        public ArrayList<Integer> queryTermIDs;

        QueryIdTextPair(int queryID, String query) {

            this.queryID = queryID;
            this.query = query;
            this.queryTermIDs = new ArrayList<>();
        }
    }

    QueryProcessor() {
        readStopWords();
        readTermIDs();

    }


    public ArrayList<QueryIdTextPair> processQueries() {

        try {

            ArrayList<QueryIdTextPair> QueryIdTextPairs = new ArrayList<>();

            File file = new File(System.getProperty("user.dir") + "\\src\\ranker\\input files\\topics.xml");
            FileInputStream fin = new FileInputStream(file);

            Document doc = Jsoup.parse(fin, null, "", Parser.xmlParser());

            for (Element e : doc.select("topic")) {

                QueryIdTextPair QueryIdTextPair = new QueryIdTextPair(Integer.parseInt(e.attr("number")), e.select("query").text());

                String[] tokens = e.select("query").text().split("[\\p{Punct}\\s]+");

                for (int i = 0; i < tokens.length; i++) {

                    tokens[i] = Normalizer.normalize(tokens[i], Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "").toLowerCase();

                    if (tokens[i].equals("") || tokens[i].length() < 2)
                        continue;

                    englishStemmer.setCurrent(tokens[i]);
                    englishStemmer.stem();

                    if (stopWords.containsKey(tokens[i]) || stopWords.containsKey(englishStemmer.getCurrent()))
                        continue;

                    int termID = termIDs.get(englishStemmer.getCurrent());

                    QueryIdTextPair.queryTermIDs.add(termID);
                }

                QueryIdTextPairs.add(QueryIdTextPair);

            }

            fin.close();

            return QueryIdTextPairs;


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void readTermIDs() {

        BufferedReader termID_doc_reader;
        termIDs = new HashMap<>();

        try {
            termID_doc_reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\ranker\\input files\\termids.txt"));

            String line = termID_doc_reader.readLine();

            while (line != null) {

                termIDs.put(line.split("\t")[1], Integer.parseInt(line.split("\t")[0]));
                line = termID_doc_reader.readLine();
            }

            termID_doc_reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readStopWords() {

        try {
            BufferedReader stoplist_reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\ranker\\input files\\stoplist.txt"));

            stopWords = new HashMap<>();

            String word = stoplist_reader.readLine();

            while (word != null) {

                stopWords.put(word, null);
                word = stoplist_reader.readLine();
            }

            stoplist_reader.close();

        } catch (IOException e) {
            System.out.println("An IOException occurred in readStopWords function in QueryProcessor clas");
        }
    }


}
