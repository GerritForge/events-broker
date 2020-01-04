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

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Set;
import java.util.function.Consumer;

public class BrokerApiNoOp implements BrokerApi {
  private final Set<TopicSubscriber> topicSubscribers;

  @Inject
  public BrokerApiNoOp() {
    topicSubscribers = Sets.newHashSet();
  }

  @Override
  public boolean send(String topic, EventMessage event) {
    return true;
  }

  @Override
  public void receiveAsync(String topic, Consumer<EventMessage> eventConsumer) {
    topicSubscribers.add(TopicSubscriber.topicSubscriber(topic, eventConsumer));
  }

  @Override
  public void disconnect() {
    topicSubscribers.clear();
  }

  @Override
  public void replayAllEvents(String topic) {}

  @Override
  public Set<TopicSubscriber> topicSubscribers() {
    return topicSubscribers;
  }
}