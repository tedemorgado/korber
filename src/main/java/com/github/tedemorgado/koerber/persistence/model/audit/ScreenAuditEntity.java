package com.github.tedemorgado.koerber.persistence.model.audit;

import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "screen_audit")
@Audited
public class ScreenAuditEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "revision_id")
   private Long revisionId;

   private UUID uuid;

   private String name;

   @Lob
   private String contentJson;

   public Long getId() {
      return this.id;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public Long getRevisionId() {
      return this.revisionId;
   }

   public void setRevisionId(final Long revisionId) {
      this.revisionId = revisionId;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public void setUuid(final UUID uuid) {
      this.uuid = uuid;
   }

   public String getName() {
      return this.name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public String getContentJson() {
      return this.contentJson;
   }

   public void setContentJson(final String contentJson) {
      this.contentJson = contentJson;
   }
}