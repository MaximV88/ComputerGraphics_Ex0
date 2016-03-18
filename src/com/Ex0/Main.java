package com.Ex0;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        Frame myFrame = new Frame("Exercise1");
        MyCanvas myCanvas = new MyCanvas();
        myFrame.add(myCanvas);

        WindowAdapter myWindowAdapter = new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };

        myFrame.addWindowListener(myWindowAdapter);
        myFrame.pack();
        myFrame.setVisible(true);

    }

}

class MyCanvas extends Canvas implements MouseListener,  MouseMotionListener {

    private static final long serialVersionUID = 1L;

    /** The manager that handles the polygon/polyline creation and storage. */
    private PolygonManager manager = new PolygonManager();

    public MyCanvas() {

        setSize(600, 480);
        addMouseListener(this);
        addMouseMotionListener(this);

    }

    @Override
    public void paint(Graphics g) {

        drawPolyline(g, manager.getPolyline());
        drawPolygons(g, manager.getPolygons());

    }


    /**
     * Draws all of the polygons to the supplied context.
     *
     * @param g The context to draw on.
     * @param polys The polygons to draw on the context.
     */
    private void drawPolygons(Graphics g, java.util.List<Polygon> polys) {

        for (Polygon poly : polys)
            g.drawPolygon(poly);

    }

    /**
     * Draws the polyline to the supplied context.
     *
     * @param g The context to draw on.
     * @param polyline_parts The polyline to draw on the context.
     */
    private void drawPolyline(Graphics g, java.util.List<Point> polyline_parts) {

        //Gather the points to draw the Polyline
        int total_size = polyline_parts.size();

        //Nothing to process
        if (total_size == 0)
            return;

        int [] loc_x = new int[total_size];
        int [] loc_y = new int[total_size];

        for (int index = 0 ; index < total_size ; index++) {
            loc_x[index] = polyline_parts.get(index).x;
            loc_y[index] = polyline_parts.get(index).y;
        }

        //Draw the polyline
        g.drawPolyline(loc_x, loc_y, total_size);

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        manager.setPolylineSection(e.getPoint());
        manager.endPolylineSection();
        this.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        manager.setPolylineSection(e.getPoint());
        this.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        manager.setPolylineSection(e.getPoint());
        this.repaint();
    }

}

class PolygonManager {

    /**
     * Returns all of the polygons stored in the manager.
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