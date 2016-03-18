package com.Ex0;

import javafx.scene.shape.Polyline;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;


/**
 * Created by Maxim on 15/03/2016.
 */
public class Main {

    /**
     * @param args
     */
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
    /**
     *
     */
    private static final long serialVersionUID = 1L;

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
        manager.addPoint(e.getPoint());
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
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

}

class PolygonManager {

    private java.util.List<Polygon> polygons = new ArrayList<Polygon>();
    private java.util.List<Point> points = new ArrayList<Point>();

    public void addPoint(Point p) {

        //Check to see that the new point should close the polygon
        if (!points.isEmpty() &&
                (Math.sqrt(Math.pow(points.get(0).x - p.x, 2)
                        + Math.pow(points.get(0).y - p.y, 2)) <= 5)) {

            //No addition of point for less than 3 to avoid artifacts
            if (points.size() >= 3) {

                //Create the polygon and return it
                Polygon poly = new Polygon();

                for (Point point : points)
                    poly.addPoint(point.x, point.y);

                //Clear the gathered points to make room for a new polyline
                points.clear();

                //Add the new polygon to the stored list to be drawn
                polygons.add(poly);

            }

            return;

        }

        //Add the new point to extend the shape if no polygon is created
        points.add(p);

    }

    public java.util.List<Polygon> getPolygons() {
    return Collections.unmodifiableList(polygons);
    }

    public java.util.List<Point> getPolyline() {
        return Collections.unmodifiableList(points);
    }
}