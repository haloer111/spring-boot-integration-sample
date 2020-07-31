package com.gexiao.sample;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gexiao.sample.entity.ElasticEntity;
import com.gexiao.sample.entity.Stu;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootElasticsearchSampleApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Qualifier("client")
    @Autowired
    RestHighLevelClient restHighLevelClient;


    // 不推荐使用restHighLevelClient对索引进行管理操作（创建索引，更新映射，删除索引）
    // 索引就像是数据库或数据库中的表，我们平时是不会通过java代码频繁的去创建修改删除数据库或表结构的。
    // 我们只会针对数据库做CRUD的操作
    // 在es中也是同理，我们尽量使用restHighLevelClient对文档数据进行CRUD操作。

    /**
     * 创建名为stu的索引
     * mapping为：
     * {
     * "properties": {
     * "id":{
     * "type":"integer"
     * },
     * "bookId":{
     * "type":"integer"
     * },
     * "name":{
     * "type":"text",
     * "analyzer": "ik_max_word",
     * "search_analyzer": "ik_smart"
     * }
     * }
     * }
     */
    @Test
    public void createIndexStu() {
        try {
            //判断索引是否存在
            if (restHighLevelClient.indices().exists(new GetIndexRequest("stu"), RequestOptions.DEFAULT)) {
                return;
            }
            // 创建名为stu的索引
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("stu");
            createIndexRequest.settings(
                    Settings.builder().put("index.number_of_shards", 3)
                            .put("index.number_of_replicas", 2)
            );
            // 构建mapping
            createIndexRequest.mapping(readFile(), XContentType.JSON);

            CreateIndexResponse resp = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (!resp.isAcknowledged()) {
                throw new RuntimeException("构建失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建或更新文档
     */
    @Test
    public void insertOrUpdateOne() {
        // 构建index的名称
        IndexRequest request = new IndexRequest("stu");
        ElasticEntity<Stu> entity = new ElasticEntity<>();
        entity.setId("2");
        Map<String, Object> map = new HashMap<>();
        map.put("id", 2);
        map.put("bookId", 1);
        map.put("name", "方大同2");
        entity.setData(map);
        // 设置_id，文档内容
        request.id(entity.getId())
                .source(JSON.toJSONString(entity.getData()), XContentType.JSON);
        try {
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除index
     *
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:13
     * @since
     */
    @Test
    public void deleteIndex() {
        try {

            restHighLevelClient.indices().delete(new DeleteIndexRequest("stu"), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询文档
     */
    @Test
    public void selectDoc() {
        // index名称
        SearchRequest request = new SearchRequest("stu");
        // dsl构建
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchQuery("name", "方大同"));
        // SearchSourceBuilder
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // sort排序
        SortBuilder sortBuilder = new FieldSortBuilder("id");
        sortBuilder.order(SortOrder.ASC);
        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("name").preTags("<span>").postTags("</span>");
        builder.size(10)
                .query(boolQueryBuilder)
                .highlighter(highlightBuilder)
                .sort(sortBuilder)
        ;
        request.source(builder);
        // 执行查询
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<Stu> res = new ArrayList<>(hits.length);
            for (SearchHit hit : hits) {
                // source内的结果
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                // 高亮的结果
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField name = highlightFields.get("name");
                if (name != null) {
                    StringBuilder str = new StringBuilder();
                    Text[] fragments = name.getFragments();
                    for (Text fragment : fragments) {
                        str.append(fragment);
                    }
                    sourceAsMap.put("name", str.toString());
                }
                res.add(JSONObject.parseObject(JSONObject.toJSONString(sourceAsMap), Stu.class));

            }
            System.out.println("res = " + res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文档
     */
    @Test
    public void deleteDoc() {
        DeleteByQueryRequest request = new DeleteByQueryRequest("stu");
        // dsl构建
        TermsQueryBuilder builder = new TermsQueryBuilder("id", "1");
        request.setQuery(builder);
        //设置批量操作数量,最大为10000
        request.setBatchSize(10000);
        request.setConflicts("proceed");
        try {
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile() {
        File file = new File("src/main/resources/es.txt");
        try (FileReader reader = new FileReader(file);
             BufferedReader br = new BufferedReader(reader);
        ) {
            StringBuilder sb = new StringBuilder();
            String s = "";
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.getCause();
        }
        return null;
    }

}
