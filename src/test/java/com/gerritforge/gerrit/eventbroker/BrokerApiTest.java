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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BrokerApiTest {

  @Mock Consumer<SourceAwareEventWrapper> eventConsumer;
  @Captor ArgumentCaptor<SourceAwareEventWrapper> eventCaptor;

  BrokerApi objectUnderTest;
  UUID instanceId = UUID.randomUUID();
  private Gson gson = new Gson();

  @Before
  public void setup() {
    objectUnderTest = new FakeBrokerApi(instanceId);
  }

  @Test
  public void shouldSendEvent() {
    ProjectCreatedEvent event = new ProjectCreatedEvent();

    SourceAwareEventWrapper expectedEvent = toSourceAwareEvent(event);

    objectUnderTest.receiveAsync("topic", eventConsumer);

    assertThat(objectUnderTest.send("topic", event)).isTrue();
    compareWithExpectedEvent(eventConsumer, eventCaptor, expectedEvent);
  }

  @Test
  public void shouldRegisterConsumerPerTopic() {
    Consumer<SourceAwareEventWrapper> secondConsumer = mockEventConsumer();
    ArgumentCaptor<SourceAwareEventWrapper> secondArgCaptor =
        ArgumentCaptor.forClass(SourceAwareEventWrapper.class);

    ProjectCreatedEvent eventForTopic = new ProjectCreatedEvent();
    eventForTopic.projectName = "Project name";
    SourceAwareEventWrapper expectedEventForTopic = toSourceAwareEvent(eventForTopic);

    ProjectCreatedEvent eventForTopic2 = new ProjectCreatedEvent();
    eventForTopic2.projectName = "Project name 2";
    SourceAwareEventWrapper expectedEventForTopic2 = toSourceAwareEvent(eventForTopic2);

    objectUnderTest.receiveAsync("topic", eventConsumer);
    objectUnderTest.receiveAsync("topic2", secondConsumer);
    objectUnderTest.send("topic", eventForTopic);
    objectUnderTest.send("topic2", eventForTopic2);

    compareWithExpectedEvent(eventConsumer, eventCaptor, expectedEventForTopic);
    compareWithExpectedEvent(secondConsumer, secondArgCaptor, expectedEventForTopic2);
  }

  @Test
  public void shouldDeliverEventToAllRegisteredConsumers() {
    Consumer<SourceAwareEventWrapper> secondConsumer = mockEventConsumer();
    ArgumentCaptor<SourceAwareEventWrapper> secondArgCaptor =
        ArgumentCaptor.forClass(SourceAwareEventWrapper.class);

    ProjectCreatedEvent event = new ProjectCreatedEvent();
    event.projectName = "Project name";
    SourceAwareEventWrapper expectedEvent = toSourceAwareEvent(event);

    objectUnderTest.receiveAsync("topic", eventConsumer);
    objectUnderTest.receiveAsync("topic", secondConsumer);
    objectUnderTest.send("topic", event);

    compareWithExpectedEvent(eventConsumer, eventCaptor, expectedEvent);
    compareWithExpectedEvent(secondConsumer, secondArgCaptor, expectedEvent);
  }

  @Test
  public void shouldReceiveEventsOnlyFromRegisteredTopic() {

    ProjectCreatedEvent eventForTopic = new ProjectCreatedEvent();
    eventForTopic.projectName = "Project name";
    SourceAwareEventWrapper expectedEventForTopic = toSourceAwareEvent(eventForTopic);

    ProjectCreatedEvent eventForTopic2 = new ProjectCreatedEvent();
    eventForTopic2.projectName = "Project name 2";

    objectUnderTest.receiveAsync("topic", eventConsumer);
    objectUnderTest.send("topic", eventForTopic);
    objectUnderTest.send("topic2", eventForTopic2);

    compareWithExpectedEvent(eventConsumer, eventCaptor, expectedEventForTopic);
  }

  @Test
  public void shouldNotRegisterTheSameConsumerTwicePerTopic() {
    ProjectCreatedEvent event = new ProjectCreatedEvent();

    SourceAwareEventWrapper expectedEvent = toSourceAwareEvent(event);

    objectUnderTest.receiveAsync("topic", eventConsumer);
    objectUnderTest.receiveAsync("topic", eventConsumer);
    objectUnderTest.send("topic", event);

    compareWithExpectedEvent(eventConsumer, eventCaptor, expectedEvent);
  }

  @SuppressWarnings("unchecked")
  private <T> Consumer<T> mockEventConsumer() {
    return Mockito.mock(Consumer.class);
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
