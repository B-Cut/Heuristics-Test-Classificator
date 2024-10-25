package cael.uff.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public abstract class Json {
    static ArrayList<RepoInfo> deserialize(String value){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(value, new TypeReference<ArrayList<RepoInfo>>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
