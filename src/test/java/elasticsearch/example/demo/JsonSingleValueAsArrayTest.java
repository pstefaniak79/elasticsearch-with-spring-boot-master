package elasticsearch.example.demo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonSingleValueAsArrayTest {


    @ParameterizedTest
    @ValueSource(strings = {
        "{\"color\":\"black\"}"
       ,"{\"color\":[\"black\",\"green\"]}"
       ,"{\"color\":\"black\" ,\"wheels\":4 }"
    })

    public void singleValueAsArray(String autoJson) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Auto auto = mapper.readValue(autoJson, Auto.class);
        System.out.println(auto);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{ \"process\": { \"parent\": { \"name\": \"powershell.exe\", \"entity_id\": \"{42FC7E13-C11D-5C05-0000-0010C6E90401}\", \"executable\": \"C:\\\\Windows\\\\System32\\\\WindowsPowerShell\\\\v1.0\\\\powershell.exe\" }, \"name\": \"cmd.exe\", \"pid\": 2012, \"entity_id\": \"{42FC7E13-CB3E-5C05-0000-0010A0125101}\", \"command_line\": \"\\\"C:\\\\WINDOWS\\\\system32\\\\cmd.exe\\\" /c \\\"for /R c: %%f in (*.docx) do copy %%f c:\\\\temp\\\\\\\"\", \"executable\": \"C:\\\\Windows\\\\System32\\\\cmd.exe\", \"ppid\": 7036 }, \"logon_id\": 217055, \"@timestamp\": 131883571822010000, \"event\": { \"category\": \"process\", \"type\": \"creation\" }, \"user\": { \"full_name\": \"bob\", \"domain\": \"ART-DESKTOP\", \"id\": \"ART-DESKTOP\\\\bob\" } }"
    })
    public void otherSerializationTypes(String autoJson) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        JsonNode jsonNode = mapper.readTree(autoJson);
        System.out.println(jsonNode);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    static class Auto {
        // @JsonDeserialize(using = StringListDeserializer.class)
        //@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<String> color;
        Integer wheels;



    }
}

class StringListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        List<String> ret = new ArrayList<>();

        ObjectCodec codec = parser.getCodec();
        TreeNode node = codec.readTree(parser);

        if (node.isArray()){
            for (JsonNode n : (ArrayNode)node){
                ret.add(n.asText());
            }
        } else if (node.isValueNode()){
            ret.add( ((JsonNode)node).asText() );
        }
        return ret;
    }
}
