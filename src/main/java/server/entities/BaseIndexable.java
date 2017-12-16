package server.entities;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.rest.RestStatus;
import server.utility.ElasticsearchClient;

import java.io.IOException;

/**
 * Created by tofurama on 12/16/17.
 */
public abstract class BaseIndexable implements Indexable {
  public boolean tryToIndex(){
    try {
      IndexResponse res = ElasticsearchClient.instance().index(this);
      if(res.status() == RestStatus.CREATED)
        return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void indexAsync(){
    ElasticsearchClient.instance().indexAsync(this);
  }
}
