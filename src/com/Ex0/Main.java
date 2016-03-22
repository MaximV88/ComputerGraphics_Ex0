
package com.Ex0;

import javafx.util.Pair;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.swing.JFileChooser;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.StringReader;

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

class MyCanvas extends Canvas implements MouseListener, MouseMotionListener, KeyListener {

    public MyCanvas() {

        setSize(600, 480);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

    }

    @Override
    public void paint(Graphics g) {

        drawPolyline(g, scene.getPolyline());
        drawPolygons(g, scene.getPolygons());

        if (fill_polygons)
            fillPolygons(g, scene.getPolygons());

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

    private void fillPolygons(Graphics g, java.util.List<Polygon> polys) {

        /*
         * For every polygon we find the minimum enclosing rectangle.
         * Afterwards, iterate Ymax - Ymin + 1 times which is the number of
         * scan lines. For each such scan line we obtain the intersection points
         * with the polygon edges. We collect the intersection points and then
         * apply the scan line algorithm.
         */
        java.util.List<Point> intersections = new java.util.ArrayList<>();

        for (Polygon poly : polys) {

            //Minimum enclosing rectangle
            Rectangle bounds = poly.getBounds();

            int scan_lines = (int)(bounds.getMaxY() - bounds.getMinY() + 1);
            for (int index = 0 ; index < scan_lines ; index++) {

                //For each scan line find intersection point with polygon edges
                int y_coord = (int)(bounds.getMinY() + index);
                boolean inside_polygon = false;
                for (int x_coord = (int)bounds.getMinX(); x_coord <= bounds.getMaxX() ; x_coord++) {

                    //Find the intersection points via iterating each coordinate until hitting the edge
                    if (inside_polygon) {

                        if (!poly.contains(x_coord, y_coord)) {

                            //The real intersection has happened one step prior
                            intersections.add(new Point(x_coord - 1, y_coord));
                            inside_polygon = false;
                        }

                    } else {

                        if (poly.contains(x_coord, y_coord)) {
                            intersections.add(new Point(x_coord, y_coord));
                            inside_polygon = true;
                        }

                    }

                }

            }

        }

        //Sort the intersection points by y values followed by x values
        intersections.sort(new Comparator<Point>() {

            @Override
            public int compare(Point p1, Point p2) {

                //First argument vs second argument
                if (p1.y == p2.y)   return Integer.compare(p1.x, p2.x);
                else                return Integer.compare(p1.y, p2.y);

            }
        });

        //Form pairs of intersections from the list and draw them
        for (int pair_index = 0 ; pair_index < intersections.size() ; pair_index++) {

            if (pair_index % 2 == 1) {

                Point first = intersections.get(pair_index - 1);
                Point second = intersections.get(pair_index);

                g.drawLine(first.x, first.y, second.x, second.y);

            }

        }

    }

    private void loadScene(Scene scene_to_load) {

        scene = scene_to_load;
        fill_polygons = false;

        //Redraw the loaded scene to update canvas
        this.repaint();

    }
    @Override
    public void mouseReleased(MouseEvent e) {

        //Prevent interaction while polygons are filled
        if (fill_polygons) return;

        scene.setPolylineSection(e.getPoint());
        scene.endPolylineSection();
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

        //Prevent interaction while polygons are filled
        if (fill_polygons) return;

        scene.setPolylineSection(e.getPoint());
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

        //Prevent interaction while polygons are filled
        if (fill_polygons) return;

        scene.setPolylineSection(e.getPoint());
        this.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {

            //If L key was hit, we load new *.scn file.
            case KeyEvent.VK_L: {

                SceneManager manager = new SceneManager();
                Scene newScene = manager.loadSceneWithMenu();

                if (newScene != null)
                    loadScene(newScene);

                break;
            }

            //If S key was hit, save the scene
            case KeyEvent.VK_S: {

                //Don't allow saving of scene when there are unfinished shapes
                if (!scene.getPolyline().isEmpty())
                    return;

                SceneManager manager = new SceneManager();
                manager.saveSceneWithMenu(scene);

                break;
            }
            case KeyEvent.VK_F: {

                //Don't allow filling of polygons when there are unfinished shapes
                if (!scene.getPolyline().isEmpty())
                    return;

                fill_polygons = !fill_polygons;

                //Redraw the loaded scene to update canvas
                this.repaint();

                break;
            }
            default: //Do nothing
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
    }

    private static final long serialVersionUID = 1L;

    /** The manager that handles the polygon/polyline creation and storage. */
    private Scene scene = new Scene();

    /** Flag that decides whether to fill polygons. */
    private boolean fill_polygons = false;

}