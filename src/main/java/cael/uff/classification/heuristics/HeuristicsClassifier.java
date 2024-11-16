package cael.uff.classification.heuristics;

import cael.uff.classification.FunctionInfo;
import cael.uff.classification.framework.PhaseInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HeuristicsClassifier {
    protected ArrayList<KeywordsInfo> keywords = new ArrayList<>();
    protected HashMap<String, List<KeywordsInfo>> results = new HashMap<>();
    protected String undefinedKeyword = "undefined";

    HeuristicsClassifier(Path keywordFile){
        if (!Files.exists(keywordFile)){
            System.err.println("The keywords file does not exist.");
            System.exit(1);
        }

        ObjectMapper mapper = new ObjectMapper();
        try{
            this.keywords = mapper.readValue(keywordFile.toAbsolutePath().toString(), new TypeReference<ArrayList<KeywordsInfo>>(){});
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        this.results.put("undefined", new ArrayList<>());
        for(KeywordsInfo i : this.keywords){
            this.results.put(i.phase(), new ArrayList<>());
        }
    }
}
