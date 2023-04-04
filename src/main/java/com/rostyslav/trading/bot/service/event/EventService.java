package com.rostyslav.trading.bot.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.model.input.socket.Event;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventService {

  private final ObjectMapper objectMapper;

  public EventService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  public Event getDeserializedEvent(String event) {
    return objectMapper.readValue(event, Event.class);
  }

}
