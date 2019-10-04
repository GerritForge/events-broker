package com.gerritforge.gerrit.eventbroker;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.server.events.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.Ignore;

@Ignore
public class FakeBrokerApi implements BrokerApi {

  private final FluentLogger log = FluentLogger.forEnclosingClass();

  private UUID instanceId;
  private Gson gson;
  private Map<String, Set<Consumer<SourceAwareEventWrapper>>> eventConsumers;

  public FakeBrokerApi(UUID instanceId) {
    this.instanceId = instanceId;
    this.gson = new Gson();
    this.eventConsumers = new MapMaker().concurrencyLevel(1).makeMap();
  }

  @Override
  public boolean send(String topic, Event event) {
    SourceAwareEventWrapper sourceAwareEvent = toSourceAwareEvent(event);

    Set<Consumer<SourceAwareEventWrapper>> topicEventConsumers = eventConsumers.get(topic);
    try {
      if (topicEventConsumers != null) {
        topicEventConsumers.stream()
            .forEach(eventsConsumer -> eventsConsumer.accept(sourceAwareEvent));
      }
    } catch (RuntimeException e) {
      log.atSevere().withCause(e).log();
      return false;
    }

    return true;
  }

  @Override
  public void receiveAsync(String topic, Consumer<SourceAwareEventWrapper> eventConsumer) {
    Set<Consumer<SourceAwareEventWrapper>> topicEventConsumers = eventConsumers.get(topic);
    if (topicEventConsumers == null) {
      topicEventConsumers = Sets.newConcurrentHashSet();
      eventConsumers.put(topic, topicEventConsumers);
    }

    topicEventConsumers.add(eventConsumer);
  }

  private JsonObject eventToJson(Event event) {
    return gson.toJsonTree(event).getAsJsonObject();
  }

  protected SourceAwareEventWrapper toSourceAwareEvent(Event event) {
    JsonObject body = eventToJson(event);
    return new SourceAwareEventWrapper(
        new SourceAwareEventWrapper.EventHeader(
            instanceId,
            event.getType(),
            instanceId,
            event.eventCreatedOn),
        body);
  }
}
