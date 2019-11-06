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

import com.google.auto.value.AutoValue;
import java.util.function.Consumer;

@AutoValue
public abstract class TopicSubscriber {
  public static TopicSubscriber topicSubscriber(String topic, Consumer<EventMessage> consumer) {
    return new AutoValue_TopicSubscriber(topic, consumer);
  }

  public abstract String topic();

  public abstract Consumer<EventMessage> consumer();
}
