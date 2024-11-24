package cael.uff.classification.heuristics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Classifier {
    protected ArrayList<KeywordsInfo> keywords = new ArrayList<>();
    protected String undefinedKeyword = "undefined";

    Classifier(Path keywordFile){
        if (!Files.exists(keywordFile)){
            System.err.println("The keywords file does not exist.");
            System.exit(1);
        }

        ObjectMapper mapper = new ObjectMapper();
        try{
            this.keywords = mapper.readValue(new File(keywordFile.toAbsolutePath().toString()), new TypeReference<ArrayList<KeywordsInfo>>(){});
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
