# 安装

## 普通安装

1.下载

```
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.4.3.tar.gz
```

2.解压到`/usr/local`目录下

```
tar -zxvf elasticsearch-6.4.3.tar.gz -C /usr/local/
```

3.在`elasticsearch`根目录下创建data目录，用于存放数据

```
mkdir data
```

4.修改`elasticsearch.yml`配置文件

```sh
# 集群名称
cluster.name: imooc-elasticsearch
# 节点名称
node.name: es-node1
# 数据存放路径
path.data: /usr/local/elasticsearch-6.4.3/data
# 日志存放路径
path.logs: /usr/local/elasticsearch-6.4.3/logs
# 开放网络host
network.host: 0.0.0.0
```

5.设置内核参数`vi /etc/sysctl.conf`

```
vm.max_map_count=655360
```

6.配置生效

```
sysctl -p
```

7.设置资源参数`vi /etc/security/limits.conf`

```
* soft nofile 65536

* hard nofile 131072

* soft nproc 65536

* hard nproc 131072
```

8.创建esuser

> es禁止root运行

```
# 创建用户
useradd esuser
# 设置密码
passwd 12345678
# 创建用户组
groupadd esuser
# 将当前目录修改成esuser
chown -R esuser:esuser ./
```

9.运行es

```
# 切换esuser
su esuser
# 后台启动es
./bin/elasticsearch -d
```

## docker安装

## 中文分词器安装

**方式一**

1.下载相应的版本`https://github.com/medcl/elasticsearch-analysis-ik/releases`

> 比如：我的es版本6.4.3，就下载对应的分词器6.4.3版本

2.解压并放入es的plugin目录下

```
unzip elasticsearch-analysis-ik-6.3.0.zip -d /usr/local/elasticsearch/plugin/ik
```

3.重启es

```
# 查找es pid
jps |grep elastic -i
# 关闭进程
kill -9 PID
```

**方式二**

使用plugin install来安装

```
./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v6.3.0/elasticsearch-analysis-ik-6.3.0.zip
```

3.重启es

```
# 查找es pid
jps |grep elastic -i
# 关闭进程
kill -9 PID
```

### 自定义中文词库

1.修改`IKAnalyzer.cfg.xml`

```xml
vim {plugins}/elasticsearch-analysis-ik-*/config/IKAnalyzer.cfg.xml

# 修改内容如下：
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
	<comment>IK Analyzer 扩展配置</comment>
	<!--用户可以在这里配置自己的扩展字典 -->
	<entry key="ext_dict">custom.dic;custom/single_word_low_freq.dic</entry>
	 <!--用户可以在这里配置自己的扩展停止词字典-->
	<entry key="ext_stopwords">custom/ext_stopword.dic</entry>
 	<!--用户可以在这里配置远程扩展字典 -->
	<entry key="remote_ext_dict">location</entry>
 	<!--用户可以在这里配置远程扩展停止词字典-->
	<entry key="remote_ext_stopwords">http://xxx.com/xxx.dic</entry>
</properties>
```

2.在同级目录下添加`custom.dic`，里面录入自定义的词汇

3.重启es

# 使用

## 创建索引

```json
PUT /myindex #创建myindex索引
{
  "settings": {
    "number_of_shards": 2, #分片数2
    "number_of_replicas": 0 #副本数0
  },
  "mappings": {
    "_doc": { # 指定_doc索引，es7之前的写法
      "properties": {
        "realname": { #指定field为realname
          "type": "text", #类型为text
          "index": true #是否索引
        },
        "username": {
          "type": "keyword",#类型keyword（精确搜索）
          "index": false
        }
      }
    }
  }
}
```

> 这个是es7之前的写法，ex7去掉指定索引，变成如下：
>
> ```json
> {
> "settings": {
>  "number_of_shards": 2,
>  "number_of_replicas": 0
> },
> "mappings": {
>    "properties": {
>      "realname": {
>        "type": "text",
>        "index": true
>      },
>      "username": {
>        "type": "keyword", 
>        "index": false
>      }
>    }
> }
> }
> ```

## 删除索引

```
DELETE /myindex
```

## 新增mapping

```
PUT /myindex/_mapping/type
{
  "properties": {
       "new_field_name": {
           "type":  "text"
       }
   }
}
```

**es7.x写法**

在原有的基础上，新增新的字段

```
POST /myindex/_mapping
{
  "properties": {
       "new_field_name": {
           "type":  "text"
       }
   }
}
```

> elasticsearch不支持修改mapping，所以只能删除后新增

## 新增文档

```
POST /myindex/type #后面可以指定id，如果不加则es使用uuid
{
  "id": 1009,
  "name": "imooc-9",
  "desc": "在慕课网学习很久~！",
  "create_date": "2020-01-01"
}
```

## 删除文档

```
DELETE /myindex/type/_id
```

## 更新文档

### 方式一

```
POST /myindex/type/_id/_update
{
  "doc": {
    "name": "我是慕课网"
  }
}
```

### 方式二

```
PUT /myindex/type/_id
{
  "id": 1001,
  "name": "update-imooc",
  "desc": "慕课网很强大！",
  "create_date": "2019-12-25"
}
```

> 注意，这种方式是全量更新，如果已存在未填写的数据则会置为空

## 查询文档

**简单查询**

```
GET /myindex/type/_id
# 定制查询
GET /myindex/type/_id?_source=id,name
```

**queryString**

```
GET /myindex/type/_search?q=key:value
```

**查询全部**

```
GET /myindex/type/_search
```

**判断文档是否存在**

```
HEAD /myindex/type/_id
```

### **DSL查询**

#### match

匹配查询，全文检索，如果文本中含有分词后的信息，如下面例子：慕课网分词后，`慕课，课网，慕`，这样es会查询匹配的数据。

`operator`：操作符，包含：`and`，`or`。有点类似于sql语句中的`AND`，`OR`。

`minimum_should_match`：最小匹配百分比。例子：60%（满足60%以上词汇），2（满足2个以上词汇）。表示用户输入的关键词里面需要按指定百分比满足的查询出来。

```
POST /myindex/type/_search

# 查询desc字段含有“慕课网”
{
    "query":{
        "match":{
            "desc":"慕课网"
        }
    }
    "operator":{},
    ""
}
```

#### **match_phrase**

匹配短语查询，即按顺序检索相应的短语。如下面的例子：`大学 研究生`会按顺序检索是否包含这2个词汇。

`slop`：表示允许跳过的位置。

```
{
    "query": {
        "match_phrase": {
            "desc":{
                "query":"大学 研究生 ",
                "slop":9
            }
        }
    }
}
```

#### **ids**

查询多个id

```
{
    "query": {
        "ids":{
            "type":"_doc",
            "values":["1001","1003","10111"]
        }
    }
}
```

#### **multi_match**

多字段匹配。

使用`^`加在字段后方可以提高对应的权重值。

```json
{
    "query": {
        "multi_match": {
            "query": "皮特帕克慕课网",
            "fields": [
                "desc",
                "nickname^10"
            ]
        }
    }
}
```

#### **bool**

多条件组合查询。

`must`：必须存在的条件

`must_not`：不存在的条件

`should`：满足其中之一的条件

```json
{
    "query": {
        "bool": {
            "must": [
                {
                    "match": {
                        "desc": "慕"
                        "boost":2 # 加权
                    }
                },
                {
                    "match": {
                        "nickname": "慕"
                    }
                }
            ],
            "should": [
                {
                    "match": {
                        "sex": 1
                    }
                }
            ],
            "must_not": [
                {
                    "term": {
                        "birthday": "1992-12-24"
                    }
                }
            ]
        }
    }
}
```

#### post_filter

查询数据后的后置过滤器。

```json
{
    "query": {
        "match": {
            "desc": "慕课网游戏"
        }
    },
    "post_filter": {
        "range": {
            "money": {
                "gt": 60,
                "lt": 1000
            }
        }
    }
}
```

#### sort

排序，包含：降序`desc`，升序`asc`。

```json
{
    "query": {
        "match": {
            "desc": "慕课网游戏"
        }
    },
    "sort": [
        {
            "age": "desc"
        }
    ]
}
```

#### highlight

高亮展示。

```json
{
    "query": {
        "match": {
            "desc": "慕课网游戏"
        }
    },
    "highlight": {
        "pre_tags": [
            "<span>"
        ],
        "post_tags": [
            "</span>"
        ],
        "fields": {
            "desc": {}
        }
    }
}
```

#### 分页

`from`：从第几行开始

`size`：一页显示多少条

```
{
    "query": {
        "match": {
            "desc": "慕课网游戏"
        }
    },
    "from": 1,
    "size": 10
}
```

#### **mget**

批量查询

```
POST /myindex/type/_mget

{
    "ids": [
        "1001",
        "1002"
    ]
}
```

#### bulk

批量操作。

语法格式

```
{"action":{}}\n
{"request":{}}\n
{"action":{}}\n
{"request":{}}\n
```

> action的可选项有：
>
> - create：创建
> - index：_id没有则创建，有则覆盖
> - update：更新
> - delete：删除

示例：

```
POST /myindex/type/_bulk

# create/index
{"create":{"_index":"my_doc","_type":"_doc","_id":"1001"}}
{"id":"2001","name":"name-2001"}
{"index":{"_index":"my_doc","_type":"_doc","_id":"1002"}}
{"id":"2002","name":"name-2002"}

# update
{"update":{"_index":"my_doc","_type":"_doc","_id":"1001"}}
{"doc":{"id":"2001","name":"name-1111"}}
{"update":{"_index":"my_doc","_type":"_doc","_id":"1003"}}
{"doc":{"id":"2003","name":"name-2003"}}

# delete
{"delete":{"_index":"my_doc","_type":"_doc","_id":"1001"}}
{"delete":{"_index":"my_doc","_type":"_doc","_id":"1003"}}


```



# 分词器

```
GET /_analyze
{
  "analyzer": "standard", #分词器类型有：standard/whitespace/simple/stop/keyword
  "text": "我在慕课网学习"
}
```

`standard`：默认分词，单词会被拆分，大小写会转换成小写

`whitespace`：根据空格来分割，忽略大小写

`simple`：按照非字母分词，大写转小写

`stop`：去掉英文中无意义的词，如：is/the/a/an

`keyword`：不做分词，把整个文本作为一个独立的关键词

# es集群

## 前期准备

- 服务器规划
  - es-node1
    - 192.168.2.70
  - es-node2
    - 192.168.2.71
  - es-node3
    - 192.168.2.72

- 保证三台主机之间能ping通
- 按照普通安装的方式安装，保证都能单独启动

## 节点配置

在`es-node1`，`es-node2`，`es-node3`上分别配置`elasticsearch.yml`文件，修改内容如下：

```yml
# 集群名字，在同一集群中保持一致
cluster.name: imooc-elasticsearch
# 节点id，保证唯一
node.name: es-node1
# 数据存放位置
path.data: /usr/local/elasticsearch-6.4.3/data
# 日志存放位置
path.logs: /usr/local/elasticsearch-6.4.3/logs
# 开放访问端口，0.0.0.0表示全部ip都可以访问
network.host: 0.0.0.0
# 允许跨域请求
http.cors.enabled: true
http.cors.allow-origin: "*"
# 表示支持成为master节点
node.master: true
# 表示支持成为副本节点
node.data: true
# 集群节点构建，如果端口不为默认9200，则需要配置成，如：192.168.2.70:9211
discovery.zen.ping.unicast.hosts: ["192.168.2.70", "192.168.2.71","192.168.2.72"]
# 最小master投票人数，在es7以下的版本需要配置。表示开放node.master: true参数的节点进行投票。
# 设置数规则为：（N/2）+1。N为开放node.master: true参数的节点
# 如果使用默认值1则容易出现脑裂现象
discovery.zen.minimum_master_nodes: 1
```

> 集群脑裂
>
> 例如有三个节点：node1，node2，node3。假设node1为master节点，当node1遇到网络故障的时候，node1会被踢出集群。node2重新成为master节点。因为默认投票人数为1，当node1重新恢复的时候可能会重新成为master节点。这样会被划分为2个集群。node1的集群为不完整集群。这样是不合理的。为了防止这种情况发生，我们需要将投票人数设置为`（N/2）+1`。N为参与选举master的节点数。

## 集群文档写原理

当客户端访问集群时候，会默认的选取其中一个节点。这个节点称为**协调节点（coordinating node）**，由它来决定来写到哪个主分片的node节点（根据hash算法），随后主分片同步数据到副本分片。最后，文档写完毕后返回给协调节点，然后返回给客户端。

## 集群文档读原理

当客户端访问集群时候，会默认的选取其中一个节点。这个节点称为**协调节点（coordinating node）**，由它来决定来读到哪个node节点（根据hash算法），

因为会存在主/副分片。所以它会轮询的方式选择node节点。返回将数据返回给协调节点，然后返回给客户端。

# springboot整合

**1.maven的引入依赖**

```
        <!--elasticsearch client-->
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.8.0</version>
        </dependency>
        <!--elasticsearch-->
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch</artifactId>
            <version>7.8.0</version>
        </dependency>
```

> 注意，springdata的elasticsearch版本太低了，这里采用的方式是自行引入es，注意版本要和服务器上的保持一致
>
> **查看版本**：`http://<your_host>:9200/`

**2.配置`properties.yml`**

我这里是单机版本，如果是集群版本要修改。

```
# elasticsearch
es:
  host: 192.168.2.56
  port: 9200
  scheme: http
```

**3.配置`ElasticConfig.java`**

```
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
```

**4.配置核心操作类**

```java
@Slf4j
@Component
public class BaseElasticService {


    @Qualifier("client")
    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * @param idxName 索引名称
     * @param idxSQL  索引描述信息 </p>
     *                e.g.</p>
     *                {
     *                  "properties": {
     *                    "id":{
     *                        "type":"integer"
     *                    },
     *                    "bookId":{
     *                        "type":"integer"
     *                    },
     *                    "name":{
     *                        "type":"text",
     *                        "analyzer": "ik_max_word",
     *                        "search_analyzer": "ik_smart"
     *                    }
     *                  }
     *                }
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:30
     * @since
     */
    public void createIndex(String idxName, String idxSQL) {
        try {
            if (!this.indexExist(idxName)) {
                log.error(" idxName={} 已经存在,idxSql={}", idxName, idxSQL);
                return;
            }
            CreateIndexRequest request = new CreateIndexRequest(idxName);
            buildSetting(request);
            request.mapping(idxSQL, XContentType.JSON);
//            request.settings() 手工指定Setting
            CreateIndexResponse res = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            if (!res.isAcknowledged()) {
                throw new RuntimeException("初始化失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * 制定配置项的判断索引是否存在，注意与 isExistsIndex 区别
     *
     * @param idxName index名
     * @return boolean
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:27
     * @since
     */
    public boolean indexExist(String idxName) throws Exception {
        GetIndexRequest request = new GetIndexRequest(idxName);
        //TRUE-返回本地信息检索状态，FALSE-还是从主节点检索状态
        request.local(false);
        //是否适应被人可读的格式返回
        request.humanReadable(true);
        //是否为每个索引返回所有默认设置
        request.includeDefaults(false);
        //控制如何解决不可用的索引以及如何扩展通配符表达式,忽略不可用索引的索引选项，仅将通配符扩展为开放索引，并且不允许从通配符表达式解析任何索引
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 断某个index是否存在
     *
     * @param idxName index名
     * @return boolean
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:27
     * @since
     */
    public boolean isExistsIndex(String idxName) throws Exception {
        return restHighLevelClient.indices().exists(new GetIndexRequest(idxName), RequestOptions.DEFAULT);
    }

    /**
     * 设置分片
     *
     * @param request
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 19:27
     * @since
     */
    public void buildSetting(CreateIndexRequest request) {
        request.settings(Settings.builder().put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2));
    }

    /**
     * @param idxName index
     * @param entity  对象
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:27
     * @since
     */
    public void insertOrUpdateOne(String idxName, ElasticEntity entity) {
        IndexRequest request = new IndexRequest(idxName);
        log.error("Data : id={},entity={}", entity.getId(), JSON.toJSONString(entity.getData()));
        request.id(entity.getId());
//        request.source(entity.getData(), XContentType.JSON);
        request.source(JSON.toJSONString(entity.getData()), XContentType.JSON);
        try {
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param idxName index
     * @param entity  对象
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:27
     * @since
     */
    public void deleteOne(String idxName, ElasticEntity entity) {
        DeleteRequest request = new DeleteRequest(idxName);
        request.id(entity.getId());
        try {
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量插入数据
     *
     * @param idxName index
     * @param list    带插入列表
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:26
     * @since
     */
    public void insertBatch(String idxName, List<ElasticEntity> list) {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(new IndexRequest(idxName).id(item.getId())
                .source(JSON.toJSONString(item.getData()), XContentType.JSON)));
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量插入数据
     *
     * @param idxName index
     * @param list    带插入列表
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:26
     * @since
     */
    public void insertBatchTrueObj(String idxName, List<ElasticEntity> list) {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(new IndexRequest(idxName).id(item.getId())
                .source(item.getData(), XContentType.JSON)));
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量删除
     *
     * @param idxName index
     * @param idList  待删除列表
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:14
     * @since
     */
    public <T> void deleteBatch(String idxName, Collection<T> idList) {
        BulkRequest request = new BulkRequest();
        idList.forEach(item -> request.add(new DeleteRequest(idxName, item.toString())));
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param idxName index
     * @param builder 查询参数
     * @param c       结果类对象
     * @return java.util.List<T>
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:14
     * @since
     */
    public <T> List<T> search(String idxName, SearchSourceBuilder builder, Class<T> c) {
        SearchRequest request = new SearchRequest(idxName);
        request.source(builder);
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<T> res = new ArrayList<>(hits.length);
            for (SearchHit hit : hits) {
                res.add(JSON.parseObject(hit.getSourceAsString(), c));
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除index
     *
     * @param idxName
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:13
     * @since
     */
    public void deleteIndex(String idxName) {
        try {
            if (!this.indexExist(idxName)) {
                log.error(" idxName={} 已经存在", idxName);
                return;
            }
            restHighLevelClient.indices().delete(new DeleteIndexRequest(idxName), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @param idxName
     * @param builder
     * @return void
     * @throws
     * @author WCNGS@QQ.COM
     * @See
     * @date 2019/10/17 17:13
     * @since
     */
    public void deleteByQuery(String idxName, QueryBuilder builder) {

        DeleteByQueryRequest request = new DeleteByQueryRequest(idxName);
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
}
```

**5.es通用实体类**

```java
/**
 * @ClassName ElasticEntity
 * @Description  数据存储对象
 * @author WCNGS@QQ.COM
 * @Github <a>https://github.com/rothschil</a>
 * @date 2019/11/21 9:10
 * @Version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticEntity<T> {

    /**
     * 主键标识，用户ES持久化
     */
    private String id;

    /**
     * JSON对象，实际存储数据
     */
    private Map data;
}
```

# Logstash数据同步

以id或update_time作为同步边界。

## 安装

1.解压到`/usr/local`目录下

```
tar -zxvf logstash-7.8.0.tar.gz -C /usr/local
```

2.在logstash根目录下创建同步目录

```
mkdir sync
```

3.在`sync`目录下创建`logstash-db-sync.conf`

```
touch logstash-db-sync.conf
```

4.将`mysql-connect`的`jar`放在sync目录下

```
注意:`mysql-connect`的版本根据你连接数据库的版本作选择，我这里是8.x
```

5.准备好sql文件和配置文件

`logstash-db-sync.conf`，内容如下：

```
input {
  jdbc {
    # 连接的数据库地址和哪一个数据库，指定编码格式，禁用SSL协议，设定自动重连
    jdbc_connection_string => "jdbc:mysql://192.168.2.55:3306/foodie?characterEncoding=UTF-8&useSSL=false&autoReconnect=true"
    jdbc_user => "root"
    jdbc_password => "root"
    # 下载连接数据库的驱动包，建议使用绝对地址
    jdbc_driver_library => "/usr/local/logstash-7.8.0/sync/mysql-connector-java-8.0.15.jar"
    jdbc_driver_class => "com.mysql.jdbc.Driver"
    # 开启分页
    jdbc_paging_enabled => "true"
    jdbc_page_size => "10000"
    codec => plain { charset => "UTF-8"}
    # 使用其它字段追踪，而不是用时间
    # 这里如果是用时间追踪比如：数据的更新时间或创建时间等和时间有关的这里一定不能是true, 切记切记切记，我是用update_time来追踪的
    use_column_value => true   
    # 追踪的字段
    tracking_column => updated_time
    tracking_column_type => "timestamp"
    record_last_run => true
    # 上一个sql_last_value值的存放文件路径, 必须要在文件中指定字段的初始值  这里说是必须指定初始值，我没指定默认是1970-01-01 08：00：00
    last_run_metadata_path => "/usr/local/logstash-7.8.0/sync/track_time"
    # 设置时区
    jdbc_default_timezone => "Asia/Shanghai"
    # statement => SELECT * FROM goods  WHERE update_time > :last_sql_value
    # 这里要说明一下如果直接写sql语句，前面这种写法肯定不对的，加上引号也试过也不对，所以我直接写在jdbc.sql文件中  
    statement_filepath => "/usr/local/logstash-7.8.0/sync/foodie-items.sql"
    # 是否清除 last_run_metadata_path 的记录,如果为真那么每次都相当于从头开始查询所有的数据库记录
    clean_run => false
    # 这是控制定时的，重复执行导入任务的时间间隔，第一位是分钟 不设置就是1分钟执行一次
    schedule => "* * * * *"
    type => "_doc"
    # 不进行数据库列名的大小写转换
    "lowercase_column_names" => false
  }
}

# 还不清楚干嘛的
#filter {
#
#  json {
#
#    source => "message"
#
#    remove_field => ["message"]
#
#  }
#
#}

output {
    elasticsearch {
        # 要导入到的Elasticsearch所在的主机
        hosts => ["192.168.2.56:9200"]
        # 要导入到的Elasticsearch的索引的名称
        index => "foodie-items"
        # 设置_id = 你sql语句中的id列，因为这个sql文件主键id as itemId所以要改
        #document_id => "%{id}"
        document_id => "%{itemId}"
    }
    # 日志输出
    stdout {
        # JSON格式输出
        codec => json_lines
    }
}
```

`foodie-items.sql`，内容如下：

```
    SELECT
        i.id as itemId,
        i.item_name as itemName,
        i.sell_counts as sellCounts,
        ii.url as imgUrl,
        tempSpec.price_discount as price,
				i.updated_time as updated_time
    FROM
        items i
    LEFT JOIN
        items_img ii
    on
        i.id = ii.item_id
    LEFT JOIN
        (SELECT item_id,MIN(price_discount) as price_discount from items_spec GROUP BY item_id) tempSpec
    on
        i.id = tempSpec.item_id
    WHERE
        ii.is_main = 1
	and updated_time > :sql_last_value
```

6.执行logstash

```
./bin/logstash -f /usr/local/logstash.7.8.0/sync/logstash-db-sync.conf
```

### 什么时候会更新文件？

我们可以检查`/usr/local/logstash-7.8.0/sync/track_time`。

```
cat track_time 
--- !ruby/object:DateTime '2019-09-09 19:45:38.000000000 Z'
```

只有当`updated_time`大于`2019-09-09 19:45:38`时候才会触发更新。

### logstash自定义模板输出到elasticsearch

elasticsearch默认的模板默认对text类型未作分词处理，所以需要加上中文分词器。

```
# 默认模板查询
GET http://127.0.0.1:9200/_template/logstash
```

**配置**

1.先查询默认的模板，将其拷贝出来放`sync`目录下，同时命名为`logstash-ik.json`。其中最主要的修改部分如下：

```json
{
    "index_patterns": ["*"],
  "order" : 1,
  "version": 1,
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas":0
  },
    "mappings": {
      "date_detection": true,
      "numeric_detection": true,
            "dynamic_templates": [
                {
                    "string_fields": {
                        "match": "*",
                        "match_mapping_type": "string",
                        "mapping": {
                            "type": "text",
                            "norms": false,
                            "analyzer": "ik_max_word",
                            "fields": {
                                "keyword": {
                                    "type": "keyword"
                                }
                            }
                        }
                    }
                }
            ]
    }
}

```

注意以下几点

- `order`：越高优先级越高，默认es会为我们配置名为`logstash`的模板，我们可以通过步骤一查询得到是否已经默认启动有。这里填写比logstash级别+1，否则我们自定义的模板不会生效。
- `index_patterns`：设置匹配索引的模式。

2.配置`logstash-db-sync.conf`，其中最主要的需要配置如下：

```
output {
    elasticsearch {
        # 重写模板
        template_overwrite => true
        # 模板所在位置
        template => "/usr/local/logstash-7.8.0/sync/logstash-ik.json"
        # 模板名称
        template_name => "myik"
        # 默认为true，false关闭logstash自动管理模板功能，如果使用自定义模板，设置为false
        manage_template => true
    }
}
```

3.指定配置文件的方式启动logstash

```
.logstash -f /usr/local/logstash/sync/logstash-db-sync.conf
```

# 测试

详见`SpringBootElasticsearchSampleApplicationTests`的使用