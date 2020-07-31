package com.gexiao.sample.config;


import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Value("${es.host}")
//public String host;
//@Value("${es.port}")
//public int port;
//@Value("${es.scheme}")
//public String scheme;
@Configuration
public class ElasticConfig {

    @Value("${es.host}")
    public String host;
    @Value("${es.port}")
    public int port;
    @Value("${es.scheme}")
    public String scheme;

    /**
     * Creates a Elasticsearch client from config
     *
     * @return Elasticsearch client
     */
    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme)));
    }
}