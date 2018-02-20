package server.entities.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "link")
@Data
public class Link {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "link")
  private String link;

  @Column(name = "referenced_type")
  private String referencedType;

  @Column(name = "referenced_Id")
  private Long referencedId;
}