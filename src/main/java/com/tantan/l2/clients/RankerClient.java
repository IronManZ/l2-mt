package com.tantan.l2.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tantan.l2.models.*;
import com.tantan.l2.utils.JacksonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class RankerClient {
  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  /**
   * This method will get a user from id
   *
   * @param id - user id
   * @return
   */
//  private final static String url_link =
//      "http://localhost:8008/ranker?id=1&candidateIds=8,2,3,4&modelId=0&linearModelParameter=popularity:0!type:0.5!distance:0.5";
  private final static String url_link =
      "http://localhost:8008/ranker";

  @Async
  public CompletableFuture<List<User>> getRankerList(Long id, List<User> inputUserList, String linearModelParameter) {

    // URI (URL) parameters
    Map<String, Object> uriParams = new HashMap<>();
    List<Long> candidateIds = new ArrayList<>();
    Map<Long, User> userMap = new HashMap<>();
    for (User user : inputUserList) {
      candidateIds.add(user.getId());
      userMap.put(user.getId(), user);
    }
    String listOfIds = candidateIds.stream().map(Object::toString).collect(Collectors.joining(","));
    // Query parameters
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url_link)
                                       .queryParam("candidateIds", listOfIds)
                                       .queryParam("id", id)
                                       .queryParam("modelId", 0)
                                       .queryParam("linearModelParameter", linearModelParameter);

    LOGGER.info("Ranking URL from url builder: " + builder.buildAndExpand(uriParams).toUri());

    //Get from ranker
    String url = null;
    try {
      url = builder.buildAndExpand(uriParams).toUri().toURL().toString();
    } catch (MalformedURLException e) {
      LOGGER.info("Error in building url for ranking id " + id, e);
      e.printStackTrace();
    }
    //convert json to java object
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getMessageConverters().add(new JacksonConverter());
    List<Object> userIdList = restTemplate.getForObject(url, List.class);
    LOGGER.info("userIdList data is :  " + userIdList);
    List<User> outputUserList = new ArrayList<>();
    for (Object userIdObject: userIdList) {
      outputUserList.add(userMap.get(Long.parseLong(userIdObject.toString())));
    }
    return CompletableFuture.completedFuture(outputUserList);
  }
}