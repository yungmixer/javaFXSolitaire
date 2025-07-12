package com.example.solitaire;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;

public class SolitaireController {
    private static final SolitaireModel model = new SolitaireModel();
    @FXML
    public Rectangle H;
    @FXML
    public Rectangle S;
    @FXML
    public Rectangle D;
    @FXML
    public Rectangle C;
    @FXML
    public Rectangle drawnCards;
    @FXML
    public Rectangle deckPlace;
    @FXML
    private Rectangle stack1;
    @FXML
    private Rectangle stack2;
    @FXML
    private Rectangle stack3;
    @FXML
    private Rectangle stack4;
    @FXML
    private Rectangle stack5;
    @FXML
    private Rectangle stack6;
    @FXML
    private Rectangle stack7;
    @FXML
    private Pane pane;
    @FXML
    private MenuItem newGameMenuItem;
    @FXML
    private MenuItem saveGameMenuItem;
    @FXML
    private MenuItem loadGameMenuItem;
    @FXML
    public Label movesLabel;
    private double startX, startY, moveX, moveY;
    private final ImageView[] childImages = new ImageView[12];
    private final ArrayList<Rectangle> stacksRectangles = new ArrayList<>(7);
    private final ArrayList<Rectangle> suitsRectangles = new ArrayList<>(4);

    @FXML
    private void initialize() {
        addStacks();
        addSuitsStacks();
        shuffleDeck();
        setListeners();
    }

    private void shuffleDeck() {
        model.shuffleDeck();
        model.drawCards(pane, stacksRectangles, movesLabel);
        addListeners();
    }

    private void replaceChildCards(String id) {
        for (int i = 0; i < childImages.length; i++) {
            if (childImages[i] == null) continue;
            if (i == 0) model.cardSet(childImages[i].getId(), (id));
            else model.cardSet(childImages[i].getId(), childImages[i - 1].getId());
        }
    }
    private void setListeners() {
        saveGameMenuItem.setOnAction(actionEvent -> model.saveGame());
        loadGameMenuItem.setOnAction(actionEvent -> {
            model.loadGame();
            model.reDraw(pane, stacksRectangles, suitsRectangles, drawnCards, movesLabel);
            addListeners();
        });
        newGameMenuItem.setOnAction(actionEvent -> shuffleDeck());
        for (Rectangle r : stacksRectangles) {
            r.setOnDragDetected(e -> r.startFullDrag());
            r.setOnMouseDragEntered(e -> {
                model.kingSet(((Node)e.getGestureSource()).getId(), stacksRectangles.indexOf(r));
                replaceChildCards(((Node)e.getGestureSource()).getId());
            });
        }
        for (Rectangle s : suitsRectangles) {
            s.setOnDragDetected(e -> s.startFullDrag());
            s.setOnMouseDragEntered(e ->
                    model.suitSet(((Node)e.getGestureSource()).getId(), s.getId(), areChildImagesAllNull()));
        }
    }

    private void addListeners() {
        for (Node n : pane.getChildren()) {
            if (n.getId() == null) continue;
            if (n.getId().contains("card")) drawnHandle(n);
            if (n.getId().equals("deck")) drawNewCard(n);
            if (n.getId().equals("refresh")) refreshDeck(n);
        }
    }

    private void refreshDeck(Node node) {
        node.setOnMouseClicked(e -> {
            model.getDeckBack(pane, drawnCards, movesLabel);
            addListeners();
        });
    }

    private void drawNewCard(Node node) {
        node.setOnMouseClicked(e -> {
            model.drawCard(pane, drawnCards, movesLabel);
            addListeners();
        });
    }

    private void drawnHandle(Node node) {
        node.setOnMousePressed(e -> {
            startX = node.getLayoutX();
            startY = node.getLayoutY();
            moveX = e.getSceneX() - node.getLayoutX();
            moveY = e.getSceneY() - node.getLayoutY();
        });
        node.setOnMouseDragged(e -> {
            setChildImages(node.getId());
            model.updateCard(pane, node.getId());
            node.setLayoutX(e.getSceneX() - moveX);
            node.setLayoutY(e.getSceneY() - moveY);

            for (int i = 0; i < childImages.length; i++) {
                if (childImages[i] == null) continue;
                model.updateCard(pane, childImages[i].getId());
                childImages[i].setLayoutX(e.getSceneX() - moveX);
                childImages[i].setLayoutY(e.getSceneY() - moveY + childImages[i].getFitHeight() * (i + 1) / 5);
            }
        });
        node.setOnDragDetected(e -> {
            node.startFullDrag();
            node.setMouseTransparent(true);
        });
        node.setOnMouseDragEntered(e -> {
            model.methodChoice(((Node)e.getGestureSource()).getId(), ((Node)e.getSource()).getId(), areChildImagesAllNull());
            replaceChildCards(((Node)e.getGestureSource()).getId());
        });
        node.setOnMouseReleased(e -> {
            node.setLayoutX(startX);
            node.setLayoutY(startY);
            model.reDraw(pane, stacksRectangles, suitsRectangles, drawnCards, movesLabel);
            for (int i = 0; i < childImages.length; i++) {
                if (childImages[i] == null) continue;
                childImages[i].setLayoutX(startX);
                childImages[i].setLayoutY(startY + childImages[i].getFitHeight() * (i + 1) / 5);
            }
            Arrays.fill(childImages, null);

            node.setMouseTransparent(false);
            addListeners();
        });
    }

    private void setChildImages(String cardId) {
        String[] cards = model.getStackSublistCards(cardId);
        for (Node n : pane.getChildren()) for (int i = 0; i < childImages.length; i++) {
            if (n.getId() == null | cards[i] == null) continue;
            if (n.getId().contains(cards[i])) childImages[i] = (ImageView) n;
        }
    }

    private void addStacks() {
        stacksRectangles.add(stack1);
        stacksRectangles.add(stack2);
        stacksRectangles.add(stack3);
        stacksRectangles.add(stack4);
        stacksRectangles.add(stack5);
        stacksRectangles.add(stack6);
        stacksRectangles.add(stack7);
    }

    private void addSuitsStacks() {
        suitsRectangles.add(H);
        suitsRectangles.add(S);
        suitsRectangles.add(D);
        suitsRectangles.add(C);
    }

    private boolean areChildImagesAllNull() {
        for (ImageView childImage : childImages) {
            if (childImage != null) return false;
        }
        return true;
    }
}