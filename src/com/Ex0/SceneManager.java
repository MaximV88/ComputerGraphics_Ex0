
package com.Ex0;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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