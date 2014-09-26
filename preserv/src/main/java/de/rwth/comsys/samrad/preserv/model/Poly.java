package de.rwth.comsys.samrad.preserv.model;

/**
 * Poly class holds name of polygon
 * and a list of its vertices.
 */

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class Poly {

    private String name;
    private List<LatLng> vertexList;

    public Poly(String name) {
        this.name = name;
        vertexList = new ArrayList<LatLng>();
    }

    public String getName() {
        return this.name;
    }

    public void addVertex(LatLng v) {
        vertexList.add(v);
    }

    public List<LatLng> getVertexList() {
        return vertexList;
    }
}
