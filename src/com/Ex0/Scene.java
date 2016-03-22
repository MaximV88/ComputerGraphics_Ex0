
package com.Ex0;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;

class Scene {

    /**
     * Creates an empty scene.
     */
    Scene() { }

    /**
     * Creates a scene from a formatted scene string.
     * @param scene The formatted scene contents to load.
     */
    Scene(String scene) {

        java.util.List<Point> all_points = new java.util.ArrayList<>();
        java.util.List<Point> all_edges = new java.util.ArrayList<>();

        //Load all of the points and edges from the input
        BufferedReader bufReader = new BufferedReader(new StringReader(scene));

        /*
         * The process is:
         * 1) Read line that contains the number of points
         * 2) Read all of the points
         * 3) Read line that contains the number of edges
         * 4) Read all of the edges
         * 5) Combine all of the polygons via Scene methods
         */

        //Number of vertices
        int limit = 0;

        try { limit = Integer.parseInt(bufReader.readLine()); }
        catch (IOException e) { e.printStackTrace(); }

        for (int index = 0; index < limit; index++) {

            try {

                String[] raw_point = bufReader.readLine().split(" ");
                int pos_x = Integer.parseInt(raw_point[0]);
                int pos_y = Integer.parseInt(raw_point[1]);

                //Extracted point
                all_points.add(new Point(pos_x, pos_y));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //Number of edges
        try { limit = Integer.parseInt(bufReader.readLine()); }
        catch (IOException e) { e.printStackTrace(); }

        for (int index = 0; index < limit; index++) {

            try {

                String[] raw_edge = bufReader.readLine().split(" ");
                int start = Integer.parseInt(raw_edge[0]);
                int end = Integer.parseInt(raw_edge[1]);

                //Extracted point
                all_edges.add(new Point(start, end));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        /*
         * The edges contain instructions as to how to parse the
         * polygon but the class can identify shape only by using
         * setPolylineSection and endPolylineSection, as long as
         * the final shape in contents is the polyline.
         */
        for (Point p : all_edges) {

            //X contains the index of start point
            setPolylineSection(all_points.get(p.x));
            endPolylineSection();

            //Y contains the index of end point
            setPolylineSection(all_points.get(p.y));
            endPolylineSection();

        }

    }

    /**
     * Returns all of the polygons stored in the scene.
     *
     * @return A list of polygons.
     */
    public java.util.List<Polygon> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }

    /**
     * Returns a polyline that is stored in the manager.
     *
     * @return A list of points that represent a polyline.
     */
    public java.util.List<Point> getPolyline() {
        return Collections.unmodifiableList(points);
    }

    /**
     * Creates (in case no section present) or moves the last section of the polyline.
     *
     * @param p Position of the lastest section of the polyline.
     */
    public void setPolylineSection(Point p) {

        /*
         * If this is the start of a new section, add the new point to extend the shape
         * and store it for later update. Otherwise update the latest point to the input.
         */
        if (current == null) {

            current = (Point)p.clone();
            points.add(current);

        }
        else {

            current.setLocation(p);

        }

        //If near the start section update coordinates to exact location
        if (isNearStartPolyline(p))
            current.setLocation(points.get(0));

    }

    /**
     * Indicates to the manager that it should end the current section
     * in the polyline, and creates a new polygon from the polyline in
     * case the end point is near the start section.
     */
    public void endPolylineSection() {

        //If the ending point is the same as the last one don't accept it (prevent double clicking)
        if (points.size() >= 2 && points.get(points.size() - 2).equals(points.get(points.size() - 1))) {
            points.remove(points.size() - 1);
            current = null;
            return;
        }

        if (isValidPolygon(points)) {

            //Remove the last marker point
            points.remove(points.size() - 1);

            //Create the polygon and return it
            Polygon poly = new Polygon();

            for (Point point : points)
                poly.addPoint(point.x, point.y);

            //Clear the gathered points to make room for a new polyline
            points.clear();
            current = null;

            //Add the new polygon to the stored list to be drawn
            polygons.add(poly);

        }
        else {

            /*
              There are 2 reasons why the polyline is not a valid polygon:
              1) The ending point is not near the start location.
              2) The ending point is near the start location, but the shape is not valid.

             The first case is interpreted as a continuation of the polyline (adding another section)
             and should add another point.
             The second case should not add another point to the polyline, and have the last point repositioned
             when possible.
             */

            //First case - nullify current point to add another section in next mouse press
            if (points.size() == 1 || !isNearStartPolyline(points.get(points.size() - 1)))
                current = null;

            //Second case - don't do anything

        }

    }

    /**
     * Encodes the scene into a format.
     * @return formatted scene string.
     */
    @Override
    public String toString() {

        String result = "";

        /*
         * Add polygon details to result for every one that exists,
         * and add the edges to a separate array that stores them
         */
        java.util.List<Point> all_points = new java.util.ArrayList<>();
        java.util.List<Point> all_edges = new java.util.ArrayList<>();
        int edge_index = 0;

        for (Polygon poly : getPolygons()) {

            int points_x[] = poly.xpoints;
            int points_y[] = poly.ypoints;

            for (int index = 0 ; index < poly.npoints ; index++) {

                //The current point that makes up the Polygon
                all_points.add(new Point(points_x[index], points_y[index]));

                /*
                 * The Polygon is constructed from edges starting from 0 -> 1, 1 -> 2 ... n-1 -> n.
                 * To make up this order we need to start from the second point and construct from first
                 * and second point an edge (i.e. 0 -> 1), and continue in this fashion until the last point
                 * (i.e. n-1 -> n). Afterwards update global edge count to avoid collision with former data.
                 */
                if (index != 0)
                    all_edges.add(new Point(index + edge_index - 1, index + edge_index));

            }

            //Add the closing edge
            all_edges.add(new Point(edge_index + poly.npoints - 1, edge_index));

            edge_index += poly.npoints;

        }

        /*
         * Now that we have all the data, return it as required in the format:
         * # Vectices
         * X0 Y0
         * X1 Y1
         * ...
         *
         * # Edges
         * V0 V1
         * ...
         */
        result += Integer.toString(all_points.size()) + '\n';

        for (Point p : all_points)
            result += Integer.toString(p.x) + ' ' + Integer.toString(p.y) + '\n';

        result += Integer.toString(all_edges.size()) + '\n';

        for (Point e : all_edges)
            result += Integer.toString(e.x) + ' ' + Integer.toString(e.y) + '\n';

        return result;

    }

    /**
     * Checks if the input point is within 5 pixels of the start of polyline.
     *
     * @param p The point to check against.
     * @return True if the input point is within 5 pixels of polyline start.
     */
    private boolean isNearStartPolyline(Point p) {

        return (!points.isEmpty() &&
                (Math.sqrt(Math.pow(points.get(0).x - p.x, 2)
                        + Math.pow(points.get(0).y - p.y, 2)) <= 5));

    }

    /**
     * Checks if the input polyline is a valid polygon.
     *
     * @param data The points that make up the polyline.
     * @return True if polyline is a valid polygon, false otherwise.
     */
    private boolean isValidPolygon(java.util.List<Point> data) {

        /*
         * Check that the polyline is a valid shape: A start and end point in
         * the same location, with at least 2 points in between them.
         */
        return (points.size() >= 4 && isNearStartPolyline(data.get(data.size() - 1)));

    }

    /** Stores all of the created polygons */
    private java.util.List<Polygon> polygons = new ArrayList<>();

    /** Stores the points that make up the current polyline. */
    private java.util.List<Point> points = new ArrayList<>();

    /** Stores the latest section's position of the polyline. */
    private Point current = null;

}