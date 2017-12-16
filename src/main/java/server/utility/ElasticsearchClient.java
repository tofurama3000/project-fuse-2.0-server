package server.utility;



import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import server.entities.Indexable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

/**
 * Created by tofurama on 12/16/17.
 */
public class ElasticsearchClient {

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

  public static ElasticsearchClient instance(){
    // Try to create an instance if one isn't present
    if(inst == null) {
      try {
        inst = new ElasticsearchClient();
      } catch (UnknownHostException | InvalidObjectException e) {
        // If there was an error, print the stack trace and return null
        e.printStackTrace();
        inst = null;
      }
    }
    return inst;
  }

  // Create an index request for an indexable document
  private IndexRequest getIndexRequst(Indexable doc){
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

  // This is the default async handler, currently it just prints to the console the result
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

  // Perform a synchronous index of a document
  public IndexResponse index(Indexable doc) throws IOException {
    return elasticsearch_client.index(getIndexRequst(doc));
  }

  // Perform an asynchronous index of a document
  public void indexAsync(Indexable doc) {
    elasticsearch_client.indexAsync(getIndexRequst(doc), getDefaultAsyncHandler());
  }

  private static ElasticsearchClient inst = null;
  private RestHighLevelClient elasticsearch_client;

  private static String es_server;
}
