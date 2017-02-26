package com.dryadandnaiad.sethlans.gui;

import com.dryadandnaiad.sethlans.utils.SethlansConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class SethlansGUI extends Application {
    private SethlansConfiguration config;

    @Override
    public void start(Stage stage) throws Exception {
        config = SethlansConfiguration.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("Sethlans");
        stage.setScene(scene);
        stage.show();
    }
}
