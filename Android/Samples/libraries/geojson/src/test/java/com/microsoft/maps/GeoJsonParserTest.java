package com.microsoft.maps;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

//import com.microsoft.maps.BingMapsLoader;
import com.microsoft.maps.GeoJsonParseException;
import com.microsoft.maps.GeoJsonParser;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapFactories;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests to check the parser.
 */
//@RunWith(RobolectricTestRunner.class)
//@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class GeoJsonParserTest {

    private static MapElementLayer mLayer;
    private static MapIcon mIcon;
    private static MapPolyline mPolyline;
    private static MapPolygon mPolygon;
    @Mock private MapElementCollection mCollection;

    private static final MapFactories DEFAULT_MAP_FACTORIES = new MapFactories() {
        @Override
        public MapElementLayer createMapElementLayer() {
            return mLayer;
        }

        @Override
        public MapIcon createMapIcon() {
            mIcon = Mockito.mock(MapIcon.class);

            return mIcon;
        }

        @Override
        public MapPolyline createMapPolyline() {
            mPolyline = Mockito.mock(MapPolyline.class);
            return mPolyline;
        }

        @Override
        public MapPolygon createMapPolygon() {
            mPolygon = Mockito.mock(MapPolygon.class);
            return mPolygon;
        }
    };

    @Before
    public void setup(){
        BingMapsLoader.mockInitialize();
        MockitoAnnotations.initMocks(this);
        mLayer = Mockito.mock(MapElementLayer.class);
        mIcon = Mockito.mock(MapIcon.class);
        mPolygon = DEFAULT_MAP_FACTORIES.createMapPolygon();
        mPolyline = DEFAULT_MAP_FACTORIES.createMapPolyline();
        when(mLayer.getElements()).thenReturn(mCollection);

    }

    /**
     * Tests that parser creates a new Polygon with the correct coordinates and adds it to the map.
     */
    @Test
    public void parseSimplePolygon_isCorrect() throws GeoJsonParseException {
        String geojson = "{\n" +
                "    \"type\": \"Polygon\", \n" +
                "    \"coordinates\": [\n" +
                "        [[30, 10], [40, 40], [20, 40], [10, 20], [30, 10]]\n" +
                "    ]\n" +
                "}";

        final int[][] points = {
                {30, 10}, {40, 40}, {20, 40}, {10, 20}, {30, 10}
        };
        final int[] index = {0};

        Mockito.doAnswer(
                new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
                        ArrayList<Geopath> arg = invocation.getArgument(0);
                        assertEquals(1, arg.size());
                        for (Geoposition position : arg.get(0)) {
                            assertEquals(points[index[0]][0], position.getLongitude());
                            assertEquals(points[index[0]][1], position.getLatitude());
                            assertEquals(0, position.getAltitude());
                            index[0]++;
                        }

                        return true;
                    }
                })
                .when(mPolygon)
                .setPaths(Mockito.<Geopath>anyList());

        Mockito.doAnswer(
                new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
                        assertTrue(invocation.getArgument(0) instanceof MapPolygon);
                        return true;
                    }
                })
                .when(mCollection)
                .add(Mockito.any(MapElement.class));

        mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
        verify(mCollection, times(1)).add(Mockito.any(MapElement.class));
    }

    /**
     * Tests that parser creates a new Polygon with the correct coordinates and adds it to the map.
     */
    @Test
    public void parseMultiRingPolygon_isCorrect() throws GeoJsonParseException {
        String geojson = "{\n" +
                            "    \"type\": \"Polygon\", \n" +
                            "    \"coordinates\": [\n" +
                            "        [[35, 10], [45, 45], [15, 40], [10, 20], [35, 10]], \n" +
                            "        [[20, 30], [35, 35], [30, 20], [20, 30]]\n" +
                            "    ]\n" +
                            "}";

        final int[][][] points = {
                {{35, 10}, {45, 45}, {15, 40}, {10, 20}, {35, 10}},
                {{20, 30}, {35, 35}, {30, 20}, {20, 30}}
        };
        final int[] index = {0};

        Mockito.doAnswer(
                new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
                        ArrayList<Geopath> arg = invocation.getArgument(0);
                        assertEquals(2, arg.size());
                        for(int i = 0; i < 2; i++){
                            Geopath path = arg.get(i);
                            for (Geoposition position : path) {
                                assertEquals(points[i][index[0]][0], position.getLongitude());
                                assertEquals(points[i][index[0]][1], position.getLatitude());
                                assertEquals(5, position.getAltitude());

                            }
                        }

                        return true;
                    }
                })
                .when(mPolygon)
                .setPaths(Mockito.<Geopath>anyList());

        Mockito.doAnswer(
                new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
                        assertTrue(invocation.getArgument(0) instanceof MapPolygon);
                        return true;
                    }
                })
                .when(mCollection)
                .add(Mockito.any(MapElement.class));

        mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
        verify(mCollection, times(1)).add(Mockito.any(MapElement.class));
    }

    /**
     * Tests that parser creates a new Polyline with the correct coordinates and adds it
     * to the Maplayer.
     */
    @Test
    public void parsePolyline_isCorrect() throws GeoJsonParseException {
        String geojson = "{\n" +
                "    \"type\": \"LineString\", \n" +
                "    \"coordinates\": [\n" +
                "        [30, 10], [10, 30], [40, 40]\n" +
                "    ]\n" +
                "}";

        final int[][] points = {
                {30, 10}, {10, 30}, {10, 30}
        };
        final int[] index = {0};

        Mockito.doAnswer(
                new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
                        assertTrue(invocation.getArgument(0) instanceof Geopath);
                        Geopath arg = invocation.getArgument(0);
                        for (Geoposition position : arg) {
                            assertEquals(points[index[0]][0], position.getLongitude());
                            assertEquals(points[index[0]][1], position.getLatitude());
                            assertEquals(0, position.getAltitude());
                            index[0]++;
                        }

                        return true;
                    }
                })
                .when(mPolyline)
                .setPath(Mockito.any(Geopath.class));

        Mockito.doAnswer(
                new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
                        assertTrue(invocation.getArgument(0) instanceof MapPolyline);
                        return true;
                    }
                })
                .when(mCollection)
                .add(Mockito.any(MapElement.class));

        mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
        verify(mCollection, times(1)).add(Mockito.any(MapElement.class));

    }

    /**
     * Tests that parser creates a new Point.
     */
    @Test
    public void parsePoint_isCorrect() throws GeoJsonParseException {
        String geojson = "{ \"type\": \"Point\", \"coordinates\": [30, 10] }";

        ArgumentCaptor<Geopoint> valueCapture = ArgumentCaptor.forClass(Geopoint.class);
        doNothing().when(mIcon).setLocation(valueCapture.capture());

        Mockito.doAnswer(
                new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
                        assertTrue(invocation.getArgument(0) instanceof MapIcon);
                        return true;
                    }
                })
                .when(mCollection)
                .add(Mockito.any(MapElement.class));

        mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
        verify(mIcon, times(1)).setLocation(valueCapture.capture());
        Geoposition position = valueCapture.getValue().getPosition();
        assertEquals(30.0, position.getLongitude());
        assertEquals(10.0, position.getLatitude());
        assertEquals(0.0, position.getAltitude());
        verify(mCollection, times(1)).add(Mockito.any(MapElement.class));
    }

    /**
     * Tests that parser creates a new MultiPolygon.
     */
    @Test
    public void parseMultiPolygon_isCorrect(){

    }

    /**
     * Tests that parser creates a new MultiPolyline.
     */
    @Test
    public void parseMultiPolyline_isCorrect(){

    }

    /**
     * Tests that parser creates a new MultiPoint.
     */
    @Test
    public void parseMultiPoint_isCorrect() throws GeoJsonParseException {
        String geojson = "{\n" +
                "    \"type\": \"MultiPoint\", \n" +
                "    \"coordinates\": [\n" +
                "        [10, 40], [40, 30], [20, 20], [30, 10]\n" +
                "    ]\n" +
                "}";
        final double[][] points = {
                {10, 40}, {40, 30}, {20, 20}, {30, 10}
        };
        final int[] index = {0};





        Mockito.doAnswer(
                new Answer<Void>() {
                    public Void answer(InvocationOnMock invocation) throws Throwable{
                        assertTrue(invocation.getArgument(0) instanceof Geopoint);
                        Geopoint arg = invocation.getArgument(0);

                        assertEquals(10.0, arg.getPosition().getLongitude());
                        assertEquals(40.0, arg.getPosition().getLatitude());
                        assertEquals(0, arg.getPosition().getAltitude());
                        return null;
                    }
                })
                .when(mIcon)
                .setLocation(Mockito.any(Geopoint.class));

//        Mockito.doAnswer(
//                new Answer<Boolean>() {
//                    @NonNull
//                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
//                        assertTrue(invocation.getArgument(0) instanceof MapIcon);
//                        mIcon.getLocation().getPosition().getLatitude();
//                        icons.add(mIcon);
//                        return true;
//                    }
//                })
//                .when(mCollection)
//                .add(Mockito.any(MapElement.class));

        mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);

        verify(mCollection, times(4)).add(Mockito.any(MapElement.class));

    }

    /**
     * Tests that the parser creates the correct number of polygons for the countries.geojson file.
     */
    @Test
    public void countriesParser_isCorrect() {
        final ArrayList<MapElement> list = new ArrayList<>();

        Mockito.doAnswer(
                new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable{
                        list.add((MapElement)(invocation.getArgument(0)));
                        return true;
                    }
                })
                .when(mCollection)
                .add(Mockito.any(MapElement.class));


        String geojson;
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream("countries.geojson");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            geojson = new String(buffer, "UTF-8");

            mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
            assertEquals(4252, list.size());
        } catch (IOException | GeoJsonParseException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}