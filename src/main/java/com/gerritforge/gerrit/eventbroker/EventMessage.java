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
import java.util.UUID;

/**
 * Enrich an existing {@link Event} object with the information about the source that produced it,
 * including the Gerrit server instance id. Additionally this class contains an event-id, event-type
 * and event-created-on fields.
 */
public class EventMessage {

  private final Header header;
  private final Event body;

  public Header getHeader() {
    return header;
  }

  /**
   * Returns deserialized {@code Event} object
   *
   * @return {@code Event} class instance
   */
  public Event getEvent() {
    return body;
  }

  /** Contains all additional information required to successfully send an {@code Event} object. */
  public static class Header {
    /** Unique event id. */
    public final UUID eventId;

    /** Gerrit server instance id from which event was sent. */
    public final UUID sourceInstanceId;

    /** @deprecated required for interoperability with older JSON wire protocols */
    public final String eventType;

    public Header(UUID eventId, UUID sourceInstanceId) {
      this.eventId = eventId;
      this.sourceInstanceId = sourceInstanceId;
      this.eventType = "";
    }

    /** Validate if all required header fields are not null. */
    public void validate() {
      requireNonNull(eventId, "EventId cannot be null");
      requireNonNull(sourceInstanceId, "Source Instance ID cannot be null");
    }

    @Override
    public String toString() {
      return "{" + "eventId=" + eventId + ", sourceInstanceId=" + sourceInstanceId + '}';
    }
  }

  /**
   * Creates a new instance which can be send as a message via {@link BrokerApi}.
   *
   * @param header message header object, contains all additional information required to properly
   *     send the message
   * @param event {@link Event} object
   */
  public EventMessage(Header header, Event event) {
    this.header = header;
    this.body = event;
  }

  /** Validate if all required fields are not null. */
  public void validate() {
    requireNonNull(header, "Header cannot be null");
    requireNonNull(body, "Event cannot be null");
    header.validate();
  }

  @Override
  public String toString() {
    return String.format("Header='%s', Body='%s'", header, body);
  }
}
