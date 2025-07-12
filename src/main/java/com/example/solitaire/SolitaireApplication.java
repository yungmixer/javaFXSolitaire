package com.example.solitaire;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SolitaireApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SolitaireApplication.class.getResource("solitaire-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Пасьянс");
        stage.getIcons().add(new Image(System.getProperty("user.dir") + "/src/main/resources/assets/ico/ico.png"));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}