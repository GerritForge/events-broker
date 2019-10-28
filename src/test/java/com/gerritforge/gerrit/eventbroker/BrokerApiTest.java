// Copyright (C) 2019 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.gerritforge.gerrit.eventbroker;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BrokerApiTest {

  @Captor ArgumentCaptor<SourceAwareEventWrapper> eventCaptor;
  Consumer<SourceAwareEventWrapper> eventConsumer;

  BrokerApi brokerApiUnderTest;
  UUID instanceId = UUID.randomUUID();
  private Gson gson = new Gson();

  @Before
  public void setup() {
    brokerApiUnderTest = new InProcessBrokerApi(instanceId);
    eventConsumer = mockEventConsumer();
  }

  @Test
  public void shouldSendEvent() {
    ProjectCreatedEvent event = new ProjectCreatedEvent();

    brokerApiUnderTest.receiveAsync("topic", eventConsumer);

    assertThat(brokerApiUnderTest.send("topic", event)).isTrue();
    compareWithExpectedEvent(eventConsumer, eventCaptor, toSourceAwareEvent(event));
  }

  @Test
  public void shouldRegisterConsumerPerTopic() {
    Consumer<SourceAwareEventWrapper> secondConsumer = mockEventConsumer();
    ArgumentCaptor<SourceAwareEventWrapper> secondArgCaptor =
        ArgumentCaptor.forClass(SourceAwareEventWrapper.class);

    ProjectCreatedEvent eventForTopic = testProjectCreatedEvent("Project name");
    ProjectCreatedEvent eventForTopic2 = testProjectCreatedEvent("Project name 2");

    brokerApiUnderTest.receiveAsync("topic", eventConsumer);
    brokerApiUnderTest.receiveAsync("topic2", secondConsumer);
    brokerApiUnderTest.send("topic", eventForTopic);
    brokerApiUnderTest.send("topic2", eventForTopic2);

    compareWithExpectedEvent(eventConsumer, eventCaptor, toSourceAwareEvent(eventForTopic));
    compareWithExpectedEvent(secondConsumer, secondArgCaptor, toSourceAwareEvent(eventForTopic2));
  }

  @Test
  public void shouldDeliverEventToAllRegisteredConsumers() {
    Consumer<SourceAwareEventWrapper> secondConsumer = mockEventConsumer();
    ArgumentCaptor<SourceAwareEventWrapper> secondArgCaptor =
        ArgumentCaptor.forClass(SourceAwareEventWrapper.class);

    ProjectCreatedEvent event = testProjectCreatedEvent("Project name");

    brokerApiUnderTest.receiveAsync("topic", eventConsumer);
    brokerApiUnderTest.receiveAsync("topic", secondConsumer);
    brokerApiUnderTest.send("topic", event);

    compareWithExpectedEvent(eventConsumer, eventCaptor, toSourceAwareEvent(event));
    compareWithExpectedEvent(secondConsumer, secondArgCaptor, toSourceAwareEvent(event));
  }

  @Test
  public void shouldReceiveEventsOnlyFromRegisteredTopic() {

    ProjectCreatedEvent eventForTopic = testProjectCreatedEvent("Project name");

    ProjectCreatedEvent eventForTopic2 = testProjectCreatedEvent("Project name 2");

    brokerApiUnderTest.receiveAsync("topic", eventConsumer);
    brokerApiUnderTest.send("topic", eventForTopic);
    brokerApiUnderTest.send("topic2", eventForTopic2);

    compareWithExpectedEvent(eventConsumer, eventCaptor, toSourceAwareEvent(eventForTopic));
  }

  @Test
  public void shouldNotRegisterTheSameConsumerTwicePerTopic() {
    ProjectCreatedEvent event = new ProjectCreatedEvent();

    brokerApiUnderTest.receiveAsync("topic", eventConsumer);
    brokerApiUnderTest.receiveAsync("topic", eventConsumer);
    brokerApiUnderTest.send("topic", event);

    compareWithExpectedEvent(eventConsumer, eventCaptor, toSourceAwareEvent(event));
  }

  @Test
  public void shouldReconnectConsumers() {
    ArgumentCaptor<SourceAwareEventWrapper> newConsumerArgCaptor =
        ArgumentCaptor.forClass(SourceAwareEventWrapper.class);

    ProjectCreatedEvent eventForTopic = testProjectCreatedEvent("Project name");
    SourceAwareEventWrapper expectedEventForTopic = toSourceAwareEvent(eventForTopic);

    brokerApiUnderTest.receiveAsync("topic", eventConsumer);
    brokerApiUnderTest.send("topic", eventForTopic);

    compareWithExpectedEvent(eventConsumer, eventCaptor, expectedEventForTopic);

    Consumer<SourceAwareEventWrapper> newConsumer = mockEventConsumer();
    TopicSubscriber consumer =
        new TopicSubscriber() {
          @Override
          public String getTopic() {
            return "topic";
          }

          @Override
          public Consumer<SourceAwareEventWrapper> getConsumer() {
            return newConsumer;
          }
        };

    clearInvocations(eventConsumer);

    brokerApiUnderTest.disconnect();
    brokerApiUnderTest.connect(Lists.newArrayList(consumer));
    brokerApiUnderTest.send("topic", eventForTopic);

    compareWithExpectedEvent(newConsumer, newConsumerArgCaptor, expectedEventForTopic);
    verify(eventConsumer, never()).accept(eventCaptor.capture());
  }

  @Test
  public void shouldDisconnectSubscribers() {
    ProjectCreatedEvent eventForTopic = testProjectCreatedEvent("Project name");

    brokerApiUnderTest.receiveAsync("topic", eventConsumer);
    brokerApiUnderTest.disconnect();

    brokerApiUnderTest.send("topic", eventForTopic);

    verify(eventConsumer, never()).accept(eventCaptor.capture());
  }

  @Test
  public void shouldBeAbleToSwitchBrokerAndReconnectSubscribers() {
    ArgumentCaptor<SourceAwareEventWrapper> newConsumerArgCaptor =
        ArgumentCaptor.forClass(SourceAwareEventWrapper.class);

    ProjectCreatedEvent eventForTopic = testProjectCreatedEvent("Project name");
    TopicSubscriber subscriber = brokerApiUnderTest.receiveAsync("topic", eventConsumer);

    BrokerApi secondaryBroker = new InProcessBrokerApi(instanceId);
    brokerApiUnderTest.disconnect();
    secondaryBroker.connect(Arrays.asList(subscriber));

    clearInvocations(eventConsumer);

    brokerApiUnderTest.send("topic", eventForTopic);
    verify(eventConsumer, never()).accept(eventCaptor.capture());

    clearInvocations(eventConsumer);
    secondaryBroker.send("topic", eventForTopic);

    compareWithExpectedEvent(
        eventConsumer, newConsumerArgCaptor, toSourceAwareEvent(eventForTopic));
  }

  private ProjectCreatedEvent testProjectCreatedEvent(String s) {
    ProjectCreatedEvent eventForTopic = new ProjectCreatedEvent();
    eventForTopic.projectName = s;
    return eventForTopic;
  }

  private interface Subscriber extends Consumer<SourceAwareEventWrapper> {

    @Override
    @Subscribe
    void accept(SourceAwareEventWrapper sourceAwareEventWrapper);
  }

  @SuppressWarnings("unchecked")
  private <T> Consumer<T> mockEventConsumer() {
    return (Consumer<T>) Mockito.mock(Subscriber.class);
  }

  private void compareWithExpectedEvent(
      Consumer<SourceAwareEventWrapper> eventConsumer,
      ArgumentCaptor<SourceAwareEventWrapper> eventCaptor,
      SourceAwareEventWrapper expectedEvent) {
    verify(eventConsumer, times(1)).accept(eventCaptor.capture());
    assertThat(eventCaptor.getValue().getHeader().toString())
        .isEqualTo(expectedEvent.getHeader().toString());
    assertThat(eventCaptor.getValue().getBody()).isEqualTo(expectedEvent.getBody());
  }

  private JsonObject eventToJson(Event event) {
    return gson.toJsonTree(event).getAsJsonObject();
  }

  protected SourceAwareEventWrapper toSourceAwareEvent(Event event) {
    JsonObject body = eventToJson(event);
    return new SourceAwareEventWrapper(
        new SourceAwareEventWrapper.EventHeader(
            instanceId, event.getType(), instanceId, event.eventCreatedOn),
        body);
  }
}
