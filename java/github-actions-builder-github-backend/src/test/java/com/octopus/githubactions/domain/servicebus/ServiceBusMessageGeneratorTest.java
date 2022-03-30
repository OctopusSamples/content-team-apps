package com.octopus.githubactions.domain.servicebus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octopus.githubactions.domain.TestingProfile;
import com.octopus.githubactions.domain.entities.GithubUserLoggedInForFreeToolsEventV1;
import com.octopus.githubactions.infrastructure.client.ServiceBusProxyClient;
import com.octopus.oauth.OauthClientCredsAccessor;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.vavr.control.Try;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * This test verifies the service bus messages are sent to the service bus proxy as expected.
 */
@QuarkusTest
@TestProfile(TestingProfile.class)
public class ServiceBusMessageGeneratorTest {

  private static final String XRAY = "xray";
  private static final String ROUTING = "routing";
  private static final String PARTITION = "partition";
  private static final String AUTH = "auth";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @RestClient
  @InjectMock
  ServiceBusProxyClient serviceBusProxyClient;

  @InjectMock
  OauthClientCredsAccessor oauthClientCredsAccessor;

  @Inject
  ServiceBusMessageGenerator serviceBusMessageGenerator;

  @BeforeEach
  public void setup() {
    Mockito.when(oauthClientCredsAccessor.getAccessToken(any())).thenReturn(Try.of(() -> "token"));
    doAnswer(invocation -> {
      final String message = invocation.getArgument(0);
      final String xray = invocation.getArgument(1);
      final String routing = invocation.getArgument(2);
      final String partition = invocation.getArgument(3);
      final String auth = invocation.getArgument(4);

      assertDoesNotThrow(() -> OBJECT_MAPPER.readValue(message, Map.class));

      assertEquals(XRAY, xray);
      assertEquals(ROUTING, routing);
      assertEquals(PARTITION, partition);
      assertEquals(AUTH, auth);

      return new Response() {
        @Override
        public int getStatus() {
          return 200;
        }

        @Override
        public StatusType getStatusInfo() {
          return null;
        }

        @Override
        public Object getEntity() {
          return null;
        }

        @Override
        public <T> T readEntity(Class<T> entityType) {
          return null;
        }

        @Override
        public <T> T readEntity(GenericType<T> entityType) {
          return null;
        }

        @Override
        public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
          return null;
        }

        @Override
        public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
          return null;
        }

        @Override
        public boolean hasEntity() {
          return false;
        }

        @Override
        public boolean bufferEntity() {
          return false;
        }

        @Override
        public void close() {

        }

        @Override
        public MediaType getMediaType() {
          return null;
        }

        @Override
        public Locale getLanguage() {
          return null;
        }

        @Override
        public int getLength() {
          return 0;
        }

        @Override
        public Set<String> getAllowedMethods() {
          return null;
        }

        @Override
        public Map<String, NewCookie> getCookies() {
          return null;
        }

        @Override
        public EntityTag getEntityTag() {
          return null;
        }

        @Override
        public Date getDate() {
          return null;
        }

        @Override
        public Date getLastModified() {
          return null;
        }

        @Override
        public URI getLocation() {
          return null;
        }

        @Override
        public Set<Link> getLinks() {
          return null;
        }

        @Override
        public boolean hasLink(String relation) {
          return false;
        }

        @Override
        public Link getLink(String relation) {
          return null;
        }

        @Override
        public Builder getLinkBuilder(String relation) {
          return null;
        }

        @Override
        public MultivaluedMap<String, Object> getMetadata() {
          return null;
        }

        @Override
        public MultivaluedMap<String, String> getStringHeaders() {
          return null;
        }

        @Override
        public String getHeaderString(String name) {
          return null;
        }
      };
    })
        .when(serviceBusProxyClient)
        .createLoginMessage(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  public void sendServiceMessageTest() {
    serviceBusMessageGenerator.sendLoginMessage(
        GithubUserLoggedInForFreeToolsEventV1.builder().build(),
        XRAY,
        ROUTING,
        PARTITION,
        AUTH);
  }

  @Test
  public void sendServiceMessageNullParamsTest() {
    assertThrows(NullPointerException.class, () -> serviceBusMessageGenerator.sendLoginMessage(
        null,
        XRAY,
        ROUTING,
        PARTITION,
        AUTH));

    assertThrows(NullPointerException.class, () -> serviceBusMessageGenerator.sendLoginMessage(
        GithubUserLoggedInForFreeToolsEventV1.builder().build(),
        XRAY,
        null,
        PARTITION,
        AUTH));

    assertThrows(NullPointerException.class, () -> serviceBusMessageGenerator.sendLoginMessage(
        GithubUserLoggedInForFreeToolsEventV1.builder().build(),
        XRAY,
        ROUTING,
        null,
        AUTH));

    assertThrows(NullPointerException.class, () -> serviceBusMessageGenerator.sendLoginMessage(
        GithubUserLoggedInForFreeToolsEventV1.builder().build(),
        XRAY,
        ROUTING,
        PARTITION,
        null));
  }
}
