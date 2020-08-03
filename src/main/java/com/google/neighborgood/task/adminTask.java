package com.google.neighborgood.task;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;

public final class adminTask {
  private final String keyString;
  private final String detail;
  private final String date;
  private final String time;
  private final String owner;

  public adminTask(String keyString, String detail, String date, String time, String owner) {
    this.keyString = keyString;
    this.detail = detail;
    this.time = time;
    this.date = date;
    this.owner = owner;
  }

  public adminTask(Entity entity) {
    this.keyString = KeyFactory.keyToString(entity.getKey());
    this.detail = (String) entity.getProperty("detail");
    this.date = (String) entity.getProperty("date");
    this.time = (String) entity.getProperty("time");
    this.owner = (String) entity.getProperty("owner");
  }
}
