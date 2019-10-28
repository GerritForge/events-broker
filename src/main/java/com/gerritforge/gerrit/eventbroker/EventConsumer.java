package com.gerritforge.gerrit.eventbroker;

import java.util.function.Consumer;

/** Interface describing {@link SourceAwareEventWrapper} consumer */
public interface EventConsumer {

  /**
   * Event consumer topic to subscribe to
   *
   * @return topic name
   */
  String getTopic();

  /**
   * Event consumer to handle {@code SourceAwareEventWrapper}
   *
   * @return {@code Consumer} function to handle the {@code SourceAwareEventWrapper}
   */
  Consumer<SourceAwareEventWrapper> getConsumer();
}
