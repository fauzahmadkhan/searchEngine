package searchEngine;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileHandler {


    // to read & return all data from the given file, in the standard charsets utf_8
    public static String readFile (String file){

        try{
            String fileData = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
            return fileData;
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }


    // extracting the document text with the HTML parsing library Jsoup.parse(html).text
    // ignoring the headers at the beginning of the file & all HTML tags
    public static String fetchString_http (String file_path){

        String http = readFile(file_path);
        String html = null;

        try{
            // first converting all the data in the http string to lower case & then ignoring all the headers
            html = http.substring(http.toLowerCase().indexOf("<!doctype>"));
        }
        catch (StringIndexOutOfBoundsException e){

            try{
                // if headers do not exist then
                // first converting all the data in the http string to lower case & then ignoring all HTML tags
                html = http.substring(http.toLowerCase().indexOf("<html>"));
            }
            catch (StringIndexOutOfBoundsException s){
                html = null;
            }

        }

        if (html == null)
            return null;

        return  Jsoup.parse(html).text();   // parsing the html string, with the help of the parsing library
    }

    // to get all the file names from the directory & returning them in the form of ArrayList<String>
    public static ArrayList<String> getFileNamesFromDirectory (String directory){

        File folder = new File(directory);  // folder (path) containing all the files
        File[] listOfFiles = folder.listFiles();

        ArrayList<String> fileNames = new ArrayList<String>();

        for (int i=0; i< listOfFiles.length; i++){

            // filtering valid files
            if (listOfFiles[i].isFile()){

                fileNames.add(listOfFiles[i].getName());
            }


        }

        return fileNames;
    }


}
