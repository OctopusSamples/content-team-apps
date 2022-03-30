package com.octopus.jenkins.github.domain.entities;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents the UTM params that may be associated with a request.
 */
@Data
@Builder
public class Utms {

  private String source;
  private String medium;
  private String campaign;
  private String term;
  private String content;

  /**
   * Conver the object to a mpa.
   *
   * @return A map containing the utm params.
   */
  public Map<String, String> getMap() {
    final Map<String, String> utms = new HashMap<>();
    if (StringUtils.isNotBlank(source)) {
      utms.put("utm_source", source.trim());
    }
    if (StringUtils.isNotBlank(medium)) {
      utms.put("utm_medium", medium.trim());
    }
    if (StringUtils.isNotBlank(campaign)) {
      utms.put("utm_campaign", campaign.trim());
    }
    if (StringUtils.isNotBlank(term)) {
      utms.put("utm_term", term.trim());
    }
    if (StringUtils.isNotBlank(content)) {
      utms.put("utm_content", content.trim());
    }
    return utms;
  }
}
