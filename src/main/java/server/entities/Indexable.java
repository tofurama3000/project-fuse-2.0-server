package server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * Created by tofurama on 12/16/17.
 * Describes an Elasticsearch document
 */
public interface Indexable {
  // ElasticSearch takes a Map of strings and objects
  @JsonIgnore
  Map<String, Object> getEsJson();

  // The name of the Elasticsearch Index to use
  @JsonIgnore
  String getEsIndex();

  // The type of document in the Elasticsearch Index
  @JsonIgnore
  String getEsType();

  // The ID of the document
  @JsonIgnore
  String getEsId();
}
