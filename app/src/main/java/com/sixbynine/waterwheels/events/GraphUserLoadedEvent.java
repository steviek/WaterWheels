package com.sixbynine.waterwheels.events;

import com.sixbynine.waterwheels.model.GraphUser;

public final class GraphUserLoadedEvent {

  private final GraphUser graphUser;

  public GraphUserLoadedEvent(GraphUser graphUser) {
    this.graphUser = graphUser;
  }

  public GraphUser getGraphUser() {
    return graphUser;
  }
}
