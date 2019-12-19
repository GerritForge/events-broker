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

import com.gerritforge.gerrit.eventbroker.EventMessage.Header;
import com.google.gerrit.server.events.Event;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/** API for sending/receiving events through a message Broker. */
public interface BrokerApi {

  /**
   * Creates a {@link EventMessage} for an event
   *
   * @param event
   * @return {@link EventMessage} object
   */
  default EventMessage newMessage(UUID instanceId, Event event) {

    return new EventMessage(new Header(UUID.randomUUID(), instanceId), event);
  }

  /**
   * Send an message to a topic.
   *
   * @param topic topic name
   * @param message to be send to the topic
   * @return true if the message was successfully sent. False otherwise.
   */
  boolean send(String topic, EventMessage message);

  /**
   * Receive asynchronously a message from a topic.
   *
   * @param topic topic name
   * @param consumer an operation that accepts and process a single message
   */
  void receiveAsync(String topic, Consumer<EventMessage> consumer);

  /**
   * Get the active subscribers
   *
   * @return {@link Set} of the topics subscribers
   */
  Set<TopicSubscriber> topicSubscribers();

  /** Disconnect from broker and cancel all active consumers */
  void disconnect();

  /**
   * Redeliver all stored messages for specified topic
   *
   * @param topic topic name
   */
  void replayAllEvents(String topic);
}
