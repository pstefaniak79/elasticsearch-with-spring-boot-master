package elasticsearch.example.demo;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import com.google.common.collect.ImmutableMap;
import jakarta.json.spi.JsonProvider;
import org.apache.http.util.Asserts;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    ElasticsearchClient elasticsearchClientJavaApi;


    @Test
    void contextLoads() {
        Asserts.notNull(elasticsearchClientJavaApi, "");
    }

    @Test
    void searchQuery() throws IOException {
        SearchResponse<Log> search = elasticsearchClientJavaApi.search(s -> s
                        .index("log")
                        .query(q -> q
                                .term(t -> t
                                        .field("event.category")
                                        .value(v -> v.stringValue("library"))
                                )),
                Log.class);

        for (Hit<Log> hit : search.hits().hits()) {
            System.out.println(hit.source());
        }

    }

    @Test
    void boolQuery() throws IOException {
        SearchResponse<Log> search = elasticsearchClientJavaApi.search(s -> s
                        .index("log")
                        .query(q -> q
                                .bool(
                                        b -> {
                                            b.should(
                                                    t -> t.term(c -> c
                                                            .field("event.category")
                                                            .value(v -> v.stringValue("process"))
                                                    )
                                            );
                                            b.should(
                                                    t -> t.term(c -> c
                                                            .field("user.full_name")
                                                            .value(v -> v.stringValue("bob"))
                                                    )
                                            );
                                            return b;
                                        }

                                )),
                Log.class);

        for (Hit<Log> hit : search.hits().hits()) {
            System.out.println(hit.source());
        }

    }

    @Test
    void boolQueryDynamic() throws IOException {

        Map<String, String> conditions = ImmutableMap.of("event.category", "process", "user.full_name", "bob");


        SearchResponse<Log> search = elasticsearchClientJavaApi.search(s -> s
                        .index("log")
                        .query(q -> {
                            return q
                                    .bool(
                                            b -> {
                                                for (Map.Entry<String, String> conditon : conditions.entrySet()) {
                                                    b.must(
                                                            t -> t.term(c -> c
                                                                    .field(conditon.getKey())
                                                                    .value(v -> v.stringValue(conditon.getValue()))
                                                            )
                                                    );
                                                }
                                                b.should(
                                                        t -> t.term(c -> c
                                                                .field("user.full_name")
                                                                .value(v -> v.stringValue("bob"))
                                                        )
                                                );
                                                return b;
                                            }
                                    );
                        }),
                Log.class);

        for (Hit<Log> hit : search.hits().hits()) {
            System.out.println(hit.source());
        }

    }


    @Test
    void boolQueryDynamicAggr() throws IOException {
        Map<String, String> conditions = ImmutableMap.of("event.category", "process", "user.full_name", "bob");
        SearchResponse<Void> search = elasticsearchClientJavaApi.search(s -> s
                        .index("log")
                        .aggregations("category", a -> a.terms(ta -> ta.field("event.category.keyword"))),
                Void.class);


        ArrayList<StringTermsBucket> category = (ArrayList<StringTermsBucket>) ((StringTermsAggregate) search.aggregations().get("category")._get()).buckets()._get();
        System.out.println(category);
    }


    @Test
    void boolQueryDynamicAggr2() throws IOException {
        Map<String, String> conditions = ImmutableMap.of("event.category", "process", "user.full_name", "bob");

        SearchRequest.Builder s = new SearchRequest.Builder()
                .index("log")
                .aggregations("category", a -> a.terms(ta -> ta.field("event.category.keyword")))
                .query(q -> {
                    q
                            .bool(
                                    b -> {
                                        for (Map.Entry<String, String> conditon : conditions.entrySet()) {
                                            b.must(
                                                    t -> t.term(c -> c
                                                            .field(conditon.getKey())
                                                            .value(v -> v.stringValue(conditon.getValue()))
                                                    )
                                            );
                                        }
                                        b.should(
                                                t -> t.term(c -> c
                                                        .field("user.full_name")
                                                        .value(v -> v.stringValue("bob"))
                                                )
                                        );
                                        return b;
                                    }
                            );

                    return q;
                });

        SearchRequest searchRequest = s.build();

        System.out.println(searchRequest.toString());

        SearchResponse<Void> search = elasticsearchClientJavaApi.search(searchRequest, Void.class);


        ArrayList<StringTermsBucket> category = (ArrayList<StringTermsBucket>) ((StringTermsAggregate) search.aggregations().get("category")._get()).buckets()._get();
        System.out.println(category);
    }


    @Test
    void boolQueryDynamicAggr3() throws IOException {

        SearchRequest.Builder sb = new SearchRequest.Builder()
                .index("log")
                .size(10)
                .query(q -> q.term(t -> t.field("event.category")
                        .value("library")));

        SearchRequest request = sb.build();

        SearchResponse<Log> search = elasticsearchClientJavaApi.search(request, Log.class);
    }




    @Test
    void BulkInsert1() throws IOException {
        Path path = Paths.get("d:/work/log.json");
        List<String> jsons = Files.readAllLines(path);

        BulkRequest.Builder br = new BulkRequest.Builder();
        for(String jsonAsString : jsons) {
            InputStream inputStream = new ByteArrayInputStream(jsonAsString.getBytes());
            JsonData json = readJson(inputStream, elasticsearchClientJavaApi);
            br.operations(op -> op
                    .index(idx -> idx
                            .index("log")
                            .document(json)
                    )
            );
        }

        BulkResponse result = elasticsearchClientJavaApi.bulk(br.build());

        // Log errors, if any
        if (result.errors()) {

            for (BulkResponseItem item: result.items()) {
                if (item.error() != null) {
                    System.out.println(item.error().reason());
                }
            }
        }

    }

    public static JsonData readJson(InputStream input, ElasticsearchClient esClient) {
        JsonpMapper jsonpMapper = esClient._transport().jsonpMapper();
        JsonProvider jsonProvider = jsonpMapper.jsonProvider();

        return JsonData.from(jsonProvider.createParser(input), jsonpMapper);
    }




}
