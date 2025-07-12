package com.example.solitaire;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class SolitaireView {
    private final String backPath = System.getProperty("user.dir") + "/src/main/resources/assets/cards/back.png";
    private final String refreshPath = System.getProperty("user.dir") + "/src/main/resources/assets/suits/refresh.png";
    private final double width = 75.0;
    private final double height = 105.0;
    private int moves = 0;
    private static final SolitaireModel model = new SolitaireModel();
    private final SequentialTransition seqT = new SequentialTransition();

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    private void clearPane(Pane pane) {
        seqT.stop();
        seqT.getChildren().clear();
        Iterator<Node> nodes = pane.getChildren().iterator();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            if (node.getId() == null) continue;
            if (node.getId().contains("card") | node.getId().contains("hidden") |
                    node.getId().contains("anim") | node.getId().contains("clone"))
                nodes.remove();
        }
    }

    public void drawCards(Pane pane, ArrayList<Rectangle> rectangles, Label movesLabel) {
        clearPane(pane);
        drawStacks(pane, rectangles, true);
        updateMoves(movesLabel, true);
        drawDeck(pane, movesLabel, false);
    }

    private void drawStacks(Pane pane, ArrayList<Rectangle> rectangles, boolean animate) {
        for (Rectangle r : rectangles) {
            ArrayList<Card> stack = model.getStacks().get(rectangles.indexOf(r));
            for (Card card : stack) {
                Image face = new Image(card.isDrawn() ? card.getPath() : backPath);
                ImageView imageView = new ImageView(face);
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
                if (card.isDrawn()) imageView.setId("card" + card.getCard());
                else imageView.setId("hidden");
                imageView.toFront();
                pane.getChildren().add(imageView);
                if (animate) {
                    imageView.setLayoutX(64.0);
                    imageView.setLayoutY(50.0);
                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.millis(80),
                            new KeyValue(imageView.layoutXProperty(), r.getLayoutX()),
                            new KeyValue(imageView.layoutYProperty(), r.getLayoutY() + height * stack.indexOf(card) / 5)
                            )
                    );
                    seqT.getChildren().add(timeline);
                }
                else {
                    imageView.setLayoutX(r.getLayoutX());
                    imageView.setLayoutY(r.getLayoutY() + height * stack.indexOf(card) / 5);
                }
            }
        }
        seqT.play();
    }

    private void drawSuited(Pane pane, ArrayList<Rectangle> suitsRecs, boolean drag) {
        for (Rectangle r : suitsRecs) {
            ArrayList<Card> stack = model.getSuits().get(suitsRecs.indexOf(r));
            for (Card card : stack) {
                Image face = new Image(card.getPath());
                ImageView imageView = new ImageView(face);
                imageView.setLayoutX(r.getLayoutX());
                imageView.setLayoutY(r.getLayoutY());
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
                if (drag) imageView.setId("card" + card.getCard() + "+");
                else {
                    imageView.setId("anim");
                }
                imageView.toFront();
                pane.getChildren().add(imageView);
            }
        }
    }

    public void updateMoves(Label movesLabel, boolean isNewGame) {
        if (isNewGame) moves = 0;
        else moves++;
        movesLabel.setText("Сделано ходов: " + moves);
    }

    public void drawDeck(Pane pane, Label movesLabel, boolean isRefreshed) {
        Image back = new Image(backPath);
        ImageView imageView = new ImageView(back);
        imageView.setLayoutX(64.0);
        imageView.setLayoutY(50.0);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setId("deck");
        pane.getChildren().add(imageView);
        if (isRefreshed) updateMoves(movesLabel, false);
    }

    public void drawNewCard(Pane pane, Card card, Rectangle drawnCards, Label movesLabel) {
        Image newCard = new Image(card.getPath());
        ImageView imageView = new ImageView(newCard);
        imageView.setLayoutX(64.0);
        imageView.setLayoutY(50.0);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        pane.getChildren().add(imageView);
        imageView.setId("card" + card.getCard());
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(50),
                        new KeyValue(imageView.layoutXProperty(), drawnCards.getLayoutX())
                ));
        timeline.play();
        updateMoves(movesLabel, false);
    }

    public void removeDeckImage(Pane pane) {
        Iterator<Node> nodes = pane.getChildren().iterator();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            if (node.getId() == null) continue;
            if (node.getId().equals("deck")) nodes.remove();
        }
        Image refresh = new Image(refreshPath);
        ImageView imageView = new ImageView(refresh);
        imageView.setLayoutX(80.0);
        imageView.setLayoutY(81.0);
        imageView.setFitWidth(44.0);
        imageView.setFitHeight(44.0);
        imageView.setId("refresh");
        pane.getChildren().add(imageView);
    }

    public void removeRefreshImage (Pane pane) {
        Iterator<Node> nodes = pane.getChildren().iterator();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            if (node.getId() == null) continue;
            if (node.getId().equals("refresh")) nodes.remove();
        }
    }

    public void setCardToFront(Pane pane, String id) {
        for (Node node : pane.getChildren()) {
            if (node.getId() == null) continue;
            if (node.getId().equals(id)) Platform.runLater(node::toFront);
        }
    }

    public void reDraw(Pane pane, ArrayList<Rectangle> stacksRectangles, ArrayList<Rectangle> suitsRectangles, Rectangle drawnCards, boolean drag, Label movesLabel) {
        clearPane(pane);
        drawStacks(pane, stacksRectangles, false);
        drawDrawn(pane, drawnCards);
        if (!drag) movesLabel.setText("");
        drawSuited(pane, suitsRectangles, drag);
        if (model.getDeck().isEmpty() & model.getDrawn().isEmpty()) {
            removeDeckImage(pane);
            removeRefreshImage(pane);
        } else if (model.getDeck().isEmpty()) removeDeckImage(pane);
        else drawDeck(pane, movesLabel, false);
    }

    private void drawDrawn(Pane pane, Rectangle drawnCards) {
        for (Card card : model.getDrawn()) {
        Image face = new Image(card.isDrawn() ? card.getPath() : backPath);
        ImageView imageView = new ImageView(face);
        imageView.setLayoutX(drawnCards.getLayoutX());
        imageView.setLayoutY(drawnCards.getLayoutY());
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setId("card" + card.getCard());
        imageView.toFront();
        pane.getChildren().add(imageView);
        }
    }

    public void removeDrawnCards(Pane pane, Rectangle drawnCards) {
        Iterator<Node> nodes = pane.getChildren().iterator();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            if (node.getId() == null) continue;
            if (node.getId().contains("card") &
                    node.getLayoutX() == drawnCards.getLayoutX() &
                    node.getLayoutY() == drawnCards.getLayoutY())
                nodes.remove();
        }
    }

    public String saveWindow() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Сохранение игры");
        File selectedDir = directoryChooser.showDialog(null);
        if (selectedDir != null) return selectedDir.getAbsolutePath();
        return null;
    }

    public String loadWindow() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BIN files (*.bin)", "*.bin");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) return selectedFile.getAbsolutePath();
        return null;
    }

    public void winAnimation(Pane pane) {
        for (Node n : pane.getChildren()) {
            if (n.getId() == null) continue;
            if (n.getId().contains("anim")) {
                double newX = 500 + Math.random() * (500) - width / 2;
                double newY = 700 - height;
                double xOffset = n.getLayoutX() - newX;
                for (int i = 0; i < 20; i++) {
                    KeyValue moveX, moveY;
                    if (i == 0) moveX = new KeyValue(n.layoutXProperty(), newX);
                    else if (i == 19) moveX = new KeyValue(n.layoutXProperty(), xOffset > 0 ? -100 : 1100);
                    else moveX  = new KeyValue(n.layoutXProperty(), newX - i * xOffset / 2);
                    moveY = new KeyValue(n.layoutYProperty(), i % 2 == 0 ? newY : newY - newY / (i + 1),
                            new Interpolator() {
                                @Override
                                protected double curve(double v) {
                                    return Math.pow(v, 2);
                                }
                            }
                    );
                    KeyFrame moveAnimKF = new KeyFrame(Duration.millis(120), moveX, moveY);
                    Timeline moveAnim = new Timeline(moveAnimKF);
                    seqT.getChildren().add(i, moveAnim);
                }
            }
        }
        seqT.play();
    }
}
