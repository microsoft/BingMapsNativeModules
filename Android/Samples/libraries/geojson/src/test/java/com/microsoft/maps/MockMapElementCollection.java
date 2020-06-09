package com.microsoft.maps;

import java.util.ArrayList;

public class MockMapElementCollection extends MapElementCollection {

  private ArrayList<MapElement> mElements = new ArrayList<>();

  public MockMapElementCollection(MapElementLayer layer) {
    super(layer);
  }

  @Override
  public boolean add(MapElement element) {
    return mElements.add(element);
  }

  public ArrayList<MapElement> getElements() {
    return mElements;
  }

  @Override
  public boolean remove(MapElement element) {
    return mElements.remove(element);
  }
}
