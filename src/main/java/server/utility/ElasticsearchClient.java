package server.utility;



import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import server.entities.Indexable;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by tofurama on 12/16/17.
 */
public class ElasticsearchClient {

  private ElasticsearchClient() throws UnknownHostException, InvalidObjectException {
    this.elasticsearch_client = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost("localhost", 9200, "http"),
                    new HttpHost("localhost", 9201, "http")));
    if(this.elasticsearch_client == null)
      throw new InvalidObjectException("Unable to connect to Elasticsearch. Verify Elasticsearch is running!");
  }

  public static ElasticsearchClient instance(){
    if(inst == null) {
      try {
        inst = new ElasticsearchClient();
      } catch (UnknownHostException | InvalidObjectException e) {
        e.printStackTrace();
        inst = null;
      }
    }
    return inst;
  }

  private IndexRequest getIndexRequst(Indexable doc){
//    return new IndexRequest("test","index_test", "1").source(doc.getEsJson());
    String index = doc.getEsIndex(), type = doc.getEsType(), id = doc.getEsId();
    IndexRequest req;
    if(doc.getEsId() != null) {
      req = new IndexRequest(doc.getEsIndex(), doc.getEsType(), doc.getEsId())
              .source(doc.getEsJson());
    }
    else {
      req = new IndexRequest(doc.getEsIndex(), doc.getEsType())
              .source(doc.getEsJson());
    }
    return req;
  }


  private static <T> ActionListener<T> getDefaultAsyncHandler(){
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
        System.err.print(e.getStackTrace());
      }
    };
  }

  public IndexResponse index(Indexable doc) throws IOException {
    return elasticsearch_client.index(getIndexRequst(doc));
  }

  public void indexAsync(Indexable doc) {
    elasticsearch_client.indexAsync(getIndexRequst(doc), getDefaultAsyncHandler());
  }

  private static ElasticsearchClient inst = null;
  private RestHighLevelClient elasticsearch_client;

  private static String es_server;
}
