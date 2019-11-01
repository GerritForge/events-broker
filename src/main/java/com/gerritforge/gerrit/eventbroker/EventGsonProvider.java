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
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventDeserializer;
import com.google.gerrit.server.events.EventTypes;
import com.google.gerrit.server.events.ProjectNameKeySerializer;
import com.google.gerrit.server.events.SupplierDeserializer;
import com.google.gerrit.server.events.SupplierSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
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

  public class ProjectNameKeyDeserializer implements JsonDeserializer<Project.NameKey> {

    @Override
    public Project.NameKey deserialize(
        JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
      if (!json.isJsonPrimitive()) {
        throw new JsonParseException("Not a primitive type");
      }

      JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
      if (!jsonPrimitive.isString()) {
        throw new JsonParseException("Not a string");
      }

      return Project.nameKey(jsonPrimitive.getAsString());
    }
  }

  @Override
  public Gson get() {
    return new GsonBuilder()
        .registerTypeAdapter(Event.class, new EventDeserializer())
        .registerTypeAdapter(Event.class, new EventSerializer())
        .registerTypeAdapter(Supplier.class, new SupplierSerializer())
        .registerTypeAdapter(Supplier.class, new SupplierDeserializer())
        .registerTypeAdapter(Project.NameKey.class, new ProjectNameKeySerializer())
        .registerTypeAdapter(Project.NameKey.class, new ProjectNameKeyDeserializer())
        .create();
  }
}
