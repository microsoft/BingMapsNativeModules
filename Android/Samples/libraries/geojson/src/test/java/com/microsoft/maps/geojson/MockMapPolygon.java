package com.microsoft.maps.geojson;

import static org.mockito.Mockito.doAnswer;

import com.microsoft.maps.MapPolygon;
import java.util.concurrent.atomic.AtomicReference;
import org.mockito.Mockito;

class MockMapPolygon {

  MockMapPolygon() {
    MapPolygon polygon = Mockito.mock(MapPolygon.class);

    // fillColor must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to fillColor
    final AtomicReference<Integer> fillColor = new AtomicReference();
    fillColor.set(0xff0000ff);
    doAnswer(
            invocation -> {
              fillColor.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setFillColor(Mockito.anyInt());

    doAnswer(invocation -> fillColor.get()).when(polygon).getFillColor();
  }
}
