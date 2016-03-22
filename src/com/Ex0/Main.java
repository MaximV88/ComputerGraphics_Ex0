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
            fillPolygons(g);

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

    public void fillPolygons(Graphics g) {

        /*
         * For every polygon we find the minimum enclosing rectangle.
         * Afterwards, iterate Ymax - Ymin + 1 times which is the number of
         * scan lines. For each such scan line we obtain the intersection points
         * with the polygon edges.
         */
        for (Polygon poly : scene.getPolygons()) {

            //Minimum enclosing rectangle
            Rectangle bounds = poly.getBounds();

            int scan_lines = (int)(bounds.getMaxY() - bounds.getMinY() + 1);
            for (int index = 0 ; index < scan_lines ; index++) {

                //For each scan line find intersection point with polygon edges
                int y_coord = (int)(bounds.getMinY() + index);
                java.util.List<Point> intersections = new java.util.ArrayList<>();
                boolean inside_polygon = false;
                for (int x_coord = (int)bounds.getMinX(); x_coord <= bounds.getMaxX() ; x_coord++)  {

                    //Find the intersection points via iterating each coordinate until hitting the edge
                    if (inside_polygon) {

                        if (!poly.contains(x_coord, y_coord)) {
                            intersections.add(new Point(x_coord, y_coord));
                            inside_polygon = false;
                        }

                    }
                    else {

                        if (poly.contains(x_coord, y_coord)) {
                            intersections.add(new Point(x_coord, y_coord));
                            inside_polygon = true;
                        }

                    }

                }

                //Form pairs of intersections from the list and draw them
                for (int pair_index = 0 ; pair_index < intersections.size() ; pair_index++) {

                    if (pair_index % 2 == 1) {

                        Point first = intersections.get(pair_index - 1);
                        Point second = intersections.get(pair_index);

                        g.drawLine(first.x, first.y, second.x, second.y);

                    }

                }

            }

        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
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

                if (newScene != null) {

                    scene = newScene;

                    //Redraw the loaded scene to update canvas
                    this.repaint();

                }

                break;
            }

            //If S key was hit, save the scene
            case KeyEvent.VK_S: {

                SceneManager manager = new SceneManager();
                manager.saveSceneWithMenu(scene);

                break;
            }
            case KeyEvent.VK_F: {

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
        ///Nothing to implement here.
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        //Nothing to implement here.
    }

    private static final long serialVersionUID = 1L;

    /** The manager that handles the polygon/polyline creation and storage. */
    private Scene scene = new Scene();

    /** Flag that decides whether to fill polygons. */
    private boolean fill_polygons = false;

}

class SceneManager {

    /**
     * Saves a Scene object with a file interface to
     * specify the file name and path.
     * @param scene The Scene to save.
     */
    public void saveSceneWithMenu(Scene scene) {

        //Open file chooser to load a new scene
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        switch (fileChooser.showSaveDialog(fileChooser)) {
            case JFileChooser.APPROVE_OPTION: {

                    saveScene(scene, fileChooser.getSelectedFile());
                    break;
            }
            default: //Do nothing
        }

    }

    /**
     * Saves a Scene into a file.
     * @param scene The scene to save.
     * @param file The file to save the scene into.
     */
    public void saveScene(Scene scene, File file) {

        try {

            //Create the file and write formatted scene to it
            FileWriter fw = new FileWriter(file + ".scn",true);
            fw.write(scene.toString());
            fw.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Opens a file chooser interface in order to select the file
     * that contains the required scene. Afterwards loads and returns
     * the selected scene.
     * @return Scene object that is contained in the selected file.
     */
    public Scene loadSceneWithMenu() {

        //Open file chooser to load a new scene
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        switch (fileChooser.showOpenDialog(fileChooser)) {
            case JFileChooser.APPROVE_OPTION: {

                try {

                    //Actual parsing and loading of file contents
                    byte[] encoded = Files.readAllBytes(Paths.get(fileChooser.getSelectedFile().getPath()));
                    return loadScene(new String(encoded, StandardCharsets.UTF_8));

                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
            default: //Do nothing
        }

        return null;

    }

    /**
     * Loads a Scene object from a given string input
     * @param scene Encoded scene contents
     * @return A scene object that is the parsed scene
     */
    public Scene loadScene(String scene) {
        return new Scene(scene);
    }

}

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

        try {
            limit = Integer.parseInt(bufReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        try {
            limit = Integer.parseInt(bufReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

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