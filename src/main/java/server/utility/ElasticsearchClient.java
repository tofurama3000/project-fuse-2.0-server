package server.utility;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import server.entities.Indexable;
import server.entities.dto.PagedResults;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by tofurama on 12/16/17.
 */
public class ElasticsearchClient {
  private static String use_elasticsearch = "true";

  private ElasticsearchClient() throws UnknownHostException, InvalidObjectException {
    // Spring is not initialized when this is ran, so we need to get properties ourselves
    String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    String appConfigPath = rootPath + "application.properties";

    Properties appProps = new Properties();
    String hostname = "localhost";
    String tcp_protocol = "http";
    Integer main_port = 9200;
    Integer secondary_port = 9201;
    try {
      appProps.load(new FileInputStream(appConfigPath));
      hostname = appProps.getProperty("fuse.elasticsearch_host", hostname);
      tcp_protocol = appProps.getProperty("fuse.elasticsearch_tcp_protocol", tcp_protocol);
      use_elasticsearch = appProps.getProperty("fuse.use_elasticsearch", use_elasticsearch);
      main_port = Integer.parseInt(appProps.getProperty("fuse.elasticsearch_port", main_port.toString()));
      secondary_port = Integer.parseInt(appProps.getProperty("fuse.elasticsearch_sec_port", secondary_port.toString()));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Now create the elasticsearch REST client
    this.elasticsearch_client = new RestHighLevelClient(
        RestClient.builder(
            new HttpHost(hostname, main_port, tcp_protocol),
            new HttpHost(hostname, secondary_port, tcp_protocol)));
  }

  public static ElasticsearchClient instance() {
    if (!ElasticsearchClient.use_elasticsearch.equals("true"))
      return null;
    // Try to create an instance if one isn't present
    if (inst == null) {
      try {
        inst = new ElasticsearchClient();
      } catch (UnknownHostException | InvalidObjectException e) {
        // If there was an error, print the stack trace and return null
        e.printStackTrace();
        inst = null;
      }
    }
    if (!ElasticsearchClient.use_elasticsearch.equals("true"))
      return null;
    return inst;
  }

  // Create an index request for an indexable document
  private IndexRequest getIndexRequest(Indexable doc) {
    return new IndexRequest(doc.getEsIndex(), doc.getEsType(), doc.getEsId())
        .source(doc.getEsJson());
  }

  // Create an index request for an indexable document
  private GetRequest getGetRequst(Indexable doc) {
    return new GetRequest(doc.getEsIndex(), doc.getEsType(), doc.getEsId());
  }

  // Create an index request for an indexable document
  private UpdateRequest getUpdateRequest(Indexable doc) {
    return new UpdateRequest(doc.getEsIndex(), doc.getEsType(), doc.getEsId())
        .doc(doc.getEsJson());
  }

  // This is the default async handler, currently it just prints to the console the result
  private static <T> ActionListener<T> getDefaultAsyncHandler() {
    return new ActionListener<T>() {
      @Override
      public void onResponse(T response) {
        // TODO: add logging
        System.out.println(response);
      }

      @Override
      public void onFailure(Exception e) {
        // TODO: add logging
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    };
  }

  // Perform a synchronous index of a document
  public DocWriteResponse index(Indexable doc) throws IOException {
    IndexRequest req = getIndexRequest(doc);
    return elasticsearch_client.index(getIndexRequest(doc));
  }

  // Perform an asynchronous index of a document
  public void indexAsync(Indexable doc) {
    elasticsearch_client.indexAsync(getIndexRequest(doc), getDefaultAsyncHandler());
  }

  private static ElasticsearchClient inst = null;
  private RestHighLevelClient elasticsearch_client;

  public PagedResults searchSimpleQuery(String[] indices, String[] types, String searchString) {
    return searchSimpleQuery(indices, types, searchString, 0, 15);
  }

  public PagedResults searchSimpleQuery(String[] indices, String[] types, String searchString, Integer page, Integer pageSize) {
    SearchRequest req = new SearchRequest(indices);
    req.types(types);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders
            .simpleQueryStringQuery(searchString)
            .analyzeWildcard(true)).from(page * pageSize).size(pageSize);
    req.source(searchSourceBuilder);
    try {
      SearchResponse resp = elasticsearch_client.search(req);
      PagedResults results = new PagedResults();
      results.setStart(page*pageSize);
      List<Object> resultItems = Arrays.stream(resp.getHits().getHits())
              .sorted((res1, res2) -> Float.compare(res2.getScore(), res1.getScore()))
              .map(res -> {
                Map<String, Object> map = res.getSourceAsMap();
                map.put("score", res.getScore());
                return map;
              })
              .collect(Collectors.toList());
      results.setEnd(page*pageSize + resultItems.size() - 1);
      results.setItems(resultItems);
      results.setTotalItems(resp.getHits().getTotalHits());
      results.setPageSize(pageSize);
      return results;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void ensureIndexTypeExists(String indexName, String typeName, String mapping) {
//    CreateIndexRequest request = new CreateIndexRequest(indexName);
//    request.mapping(typeName, mapping, XContentType.JSON);
//    elasticsearch_client.indices().createIndex(request);
  }
}
