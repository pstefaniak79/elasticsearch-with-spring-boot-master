package elasticsearch.example.demo.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import elasticsearch.example.demo.properties.ElasticsearchProperties;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfiguration extends AbstractElasticsearchConfiguration {

    private final ElasticsearchProperties elasticsearchProperties;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticsearchProperties.getHostAndPort())
                .withConnectTimeout(elasticsearchProperties.getConnectTimeout())
                .withSocketTimeout(elasticsearchProperties.getSocketTimeout())
                .build();
        return RestClients.create(clientConfiguration).rest();
    }


    @Bean
    public ElasticsearchClient elasticsearchClientJavaApi() {
        RestClient restClient = RestClient.builder(
                new HttpHost("192.168.99.100", 9200)).build();

        JacksonJsonpMapper jacksonJsonpMapper = new JacksonJsonpMapper();
        jacksonJsonpMapper.ignoreUnknownFields();
        jacksonJsonpMapper.objectMapper().configure(JsonParser.Feature.IGNORE_UNDEFINED, true);


        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,jacksonJsonpMapper );

        // And create the API client
        ElasticsearchClient client = new ElasticsearchClient(transport);
        return client;
    }


}
