package elasticsearch.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Log {
    @JsonProperty("process")
    Process process;
    @JsonProperty("dll")
    Dll ddl;
    @JsonProperty("event")
    Event event;
    @JsonProperty("@timestamp")
    Long timestamp;
    @JsonProperty("logon_id")
    Long logon_id;
    @JsonProperty("user")
    User user;

    Destination destination;
    Source source;
    Network network;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Destination {
    String address;
    String port;

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Source {
    String address;
    String port;

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Network {
    String direction;
    String protocol;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Process {
    String name;
    Long pid;
    String entity_id;
    String executable;

    @JsonProperty("parent")
    Parent parent;

    String command_line;
    Object ppid;

}
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Parent {
    String name;
    String entity_id;
    String executable;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Dll {
    String path;
    String name;
}
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Event {
    @JsonProperty("category")
    String category;
    @JsonProperty("type")
    String type;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class User {
    String full_name;
    String domain;
    String id;

}