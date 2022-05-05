package com.octopus.auditreporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Main {


  public static void main(final String[] args) {
    try {
      final RsaCryptoUtilsDecryptor rsaCryptoUtilsDecryptor = new RsaCryptoUtilsDecryptor();
      final String bearer = args[0];
      final String privateKeyBase64 = args[1];

      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpGet httpget = new HttpGet(
          "https://dodwbeqe6g.execute-api.us-west-1.amazonaws.com/Production/api/audits?page[limit]=100000");
      httpget.addHeader("Authorization", "Bearer " + bearer);
      final HttpResponse response = httpclient.execute(httpget);

      HttpEntity entity = response.getEntity();
      String responseString = EntityUtils.toString(entity, "UTF-8");

      ObjectMapper mapper = new ObjectMapper();
      Map audits = mapper.readValue(responseString, Map.class);

      List data = ((List) audits.get("data"));
      Set<String> emails = new HashSet<String>();
      int githubcount = 0;
      int jenkinscount = 0;

      HashMap<String, Integer> languages = new HashMap<>();

      System.out.println("Accesses " + data.size());

      for (Object audit : data) {
        String emailEncrypted = ((Map) ((Map) audit).get("attributes")).get("object").toString();
        String action = ((Map) ((Map) audit).get("attributes")).get("action").toString();
        String subject = ((Map) ((Map) audit).get("attributes")).get("subject").toString();
        String object = ((Map) ((Map) audit).get("attributes")).get("object").toString();

        if (action.equals("CreateTemplateFor")) {
          emails.add(rsaCryptoUtilsDecryptor.decrypt(emailEncrypted, privateKeyBase64));
        }

        if (action.equals("CreateTemplateUsing")) {
          if (subject.equals("GithubActionWorkflowBuilder")) {
            ++githubcount;
          } else {
            ++jenkinscount;
          }

          if (!languages.containsKey(object)) {
            languages.put(object, 1);
          } else {
            languages.put(object, languages.get(object) + 1);
          }
        }
      }

      for (final String language : languages.keySet()) {
        System.out.println(language + " " + languages.get(language));
      }

      System.out.println("GitHub Count " + githubcount);
      System.out.println("Jenkins Count " + jenkinscount);

      System.out.println("Private Accesses " + emails.size());
      System.out.println("No Reply Private Accesses " + (int) emails.stream()
          .filter(e -> !e.endsWith("users.noreply.github.com")).count());

      for (String email : emails) {
        System.out.println(email);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
