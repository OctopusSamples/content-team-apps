package com.octopus.audits;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.handlers.AuditsHandler;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;
import lombok.NonNull;

public class BaseTest {
  protected Audit createAudit(@NonNull final String subject) {
    return createAudit(subject, null);
  }

  protected Audit createAudit(@NonNull final String subject, final String partition) {
    final Audit audit = new Audit();
    audit.setSubject(subject);
    audit.setObject("object");
    audit.setAction("action");
    audit.setTime(new Timestamp(System.currentTimeMillis()));
    audit.setDataPartition(partition);
    return audit;
  }

  protected Audit createAudit(
      @NonNull final AuditsHandler auditsHandler,
      @NonNull final ResourceConverter resourceConverter,
      @NonNull final String partition)
      throws DocumentSerializationException {
    final Audit audit = createAudit("myname");
    final String result =
        auditsHandler.create(
            auditToResourceDocument(resourceConverter, audit),
            List.of(partition),
            null, null);
    final Audit resultObject = getAuditFromDocument(resourceConverter, result);
    return resultObject;
  }

  protected Audit getAuditFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<Audit> resourceDocument =
        resourceConverter.readDocument(document.getBytes(StandardCharsets.UTF_8), Audit.class);
    final Audit audit = resourceDocument.get();
    return audit;
  }

  protected List<Audit> getAuditsFromDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final String document) {
    final JSONAPIDocument<List<Audit>> resourceDocument =
        resourceConverter.readDocumentCollection(
            document.getBytes(StandardCharsets.UTF_8), Audit.class);
    final List<Audit> audits = resourceDocument.get();
    return audits;
  }

  protected String auditToResourceDocument(
      @NonNull final ResourceConverter resourceConverter, @NonNull final Audit audit)
      throws DocumentSerializationException {
    final JSONAPIDocument<Audit> document = new JSONAPIDocument<Audit>(audit);
    return new String(resourceConverter.writeDocument(document));
  }
}
