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

import com.google.common.base.Supplier;
import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.Project;
import com.google.gerrit.server.change.ChangeKeyAdapter;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventDeserializer;
import com.google.gerrit.server.events.EventTypes;
import com.google.gerrit.server.events.ProjectNameKeyAdapter;
import com.google.gerrit.server.events.SupplierDeserializer;
import com.google.gerrit.server.events.SupplierSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Provider;
import java.lang.reflect.Type;

public class EventGsonProvider implements Provider<Gson> {

  public static class EventSerializer implements JsonSerializer<Event> {

    @Override
    public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
      String type = src.getType();

      Class<?> cls = EventTypes.getClass(type);
      if (cls == null) {
        throw new JsonParseException("Unknown event type: " + type);
      }

      return context.serialize(src, cls);
    }
  }

  @Override
  public Gson get() {
    return new GsonBuilder()
        .registerTypeAdapter(Event.class, new EventDeserializer())
        .registerTypeAdapter(Event.class, new EventSerializer())
        .registerTypeAdapter(Supplier.class, new SupplierSerializer())
        .registerTypeAdapter(Supplier.class, new SupplierDeserializer())
        .registerTypeAdapter(Change.Key.class, new ChangeKeyAdapter())
        .registerTypeAdapter(Project.NameKey.class, new ProjectNameKeyAdapter())
        .create();
  }
}
