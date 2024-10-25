package cael.uff.serialization;

import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JsonTest {
    @Test
    void should_deserialize(){
        String testString = "[{\"repo\":\"test/test\", \"uses\":[\"junit\"]}]";
        RepoInfo baseRepo =  new RepoInfo("test/test", new String[]{"junit"});
        ArrayList<RepoInfo> result = Json.deserialize(testString);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).name(), baseRepo.name());
        assertEquals(result.get(0).testFrameworks()[0], baseRepo.testFrameworks()[0]);
    }
}