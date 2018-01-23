package server.entities;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.rest.RestStatus;
import server.utility.ElasticsearchClient;

import java.io.IOException;

/**
 * Created by tofurama on 12/16/17.
 */
public abstract class BaseIndexable implements Indexable {
  public boolean tryToIndex() {
    ElasticsearchClient es_client = ElasticsearchClient.instance();
    if (es_client == null) return false;
    try {
      DocWriteResponse res = es_client.index(this);
      if (res.status() == RestStatus.CREATED || res.status() == RestStatus.OK)
        return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void indexAsync() {
    ElasticsearchClient es_client = ElasticsearchClient.instance();
    if (es_client == null) return;
    ElasticsearchClient.instance().indexAsync(this);
  }
}
