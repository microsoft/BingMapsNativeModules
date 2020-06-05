package com.microsoft.maps;

import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementCollection;
import com.microsoft.maps.MapElementLayer;

import java.util.ArrayList;

public class MockMapElementCollection extends MapElementCollection {

  private ArrayList<MapElement> mElements;

  public MockMapElementCollection(MapElementLayer layer) {
    super(layer);
    mElements = new ArrayList<>();
  }

  @Override
  public boolean add(MapElement element) {
    return mElements.add(element);
  }

  public ArrayList<MapElement> getElements() {
    return mElements;
  }


}
