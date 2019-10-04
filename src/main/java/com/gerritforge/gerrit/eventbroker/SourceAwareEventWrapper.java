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

import static java.util.Objects.requireNonNull;

import com.google.gerrit.server.events.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.UUID;

/**
 * Purpose of this class is to enrich an existing {@link Event} object with the information about
 * the source that produced it, including the Gerrit server instance id. Additionally this class
 * contains an event-id, event-type and event-created-on fields.
 */
public class SourceAwareEventWrapper {

  private final EventHeader header;
  private final JsonObject body;

  public EventHeader getHeader() {
    return header;
  }

  /**
   * Returns Json representation of an underlaying {@code Event} object.
   *
   * @return {@code Event} json representation
   */
  public JsonObject getBody() {
    return body;
  }

  /**
   * Returns deserialized {@code Event} object from the Json representation
   *
   * @param gson Json serializer/deserializer
   * @return {@code Event} class instance
   */
  public Event getEventBody(Gson gson) {
    return gson.fromJson(this.body, Event.class);
  }

  /** Contains all additional information required to successfully send an {@code Event} object. */
  public static class EventHeader {

    private final UUID eventId;
    private final String eventType;
    private final UUID sourceInstanceId;
    private final Long eventCreatedOn;

    public EventHeader(UUID eventId, String eventType, UUID sourceInstanceId, Long eventCreatedOn) {
      this.eventId = eventId;
      this.eventType = eventType;
      this.sourceInstanceId = sourceInstanceId;
      this.eventCreatedOn = eventCreatedOn;
    }
    /** Unique event id. */
    public UUID getEventId() {
      return eventId;
    }

    /** Type of an underlaying {@code Event} object. */
    public String getEventType() {
      return eventType;
    }

    /** Gerrit server instance id from which event was sent. */
    public UUID getSourceInstanceId() {
      return sourceInstanceId;
    }
    /** Underlaying event creation time in seconds. */
    public Long getEventCreatedOn() {
      return eventCreatedOn;
    }

    /** Validate if all required header fields are not null. */
    public void validate() {
      requireNonNull(eventId, "EventId cannot be null");
      requireNonNull(eventType, "EventType cannot be null");
      requireNonNull(sourceInstanceId, "Source Instance ID cannot be null");
    }

    @Override
    public String toString() {
      return "{"
          + "eventId="
          + eventId
          + ", eventType='"
          + eventType
          + '\''
          + ", sourceInstanceId="
          + sourceInstanceId
          + ", eventCreatedOn="
          + eventCreatedOn
          + '}';
    }
  }

  /**
   * Creates a new instance which can be send as a message via {@link BrokerApi}.
   *
   * @param header message header object, contains all additional information required to properly
   *     send the message
   * @param body Json representation of an {@link Event} object
   */
  public SourceAwareEventWrapper(EventHeader header, JsonObject body) {
    this.header = header;
    this.body = body;
  }
  /** Validate if all required fields are not null. */
  public void validate() {
    requireNonNull(header, "Header cannot be null");
    requireNonNull(body, "Body cannot be null");
    header.validate();
  }
}
