package com.example.solitaire;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SolitaireModel {
    private static final ArrayList<Card> deckArrayList = new ArrayList<>();
    private static final ArrayList<Card> drawnArrayList = new ArrayList<>();
    private static final ArrayList<ArrayList<Card>> stacksArrayList = new ArrayList<>(7);
    private static final ArrayList<ArrayList<Card>> suitsArrayList = new ArrayList<>(4);
    private static final SolitaireView view = new SolitaireView();
    private final String ranks = "A23456789TJQK";
    private final String suits = "HSDC";
    private boolean stacksChanged;
    public void shuffleDeck() {
        clearStacks();
        createEmptyStacks();
        createDeck();
        Collections.shuffle(deckArrayList, new Random());
        String reds = ranks, blacks = ranks;
        ArrayList<Card> deckAndOpened = new ArrayList<>();
        for (Card card : deckArrayList) {
            if (suits.indexOf(card.getSuit()) % 2 == 0 & reds.indexOf(card.getRank()) >= 0) {
                deckAndOpened.add(card);
                reds = reds.replace(String.valueOf(card.getRank()), "");
            }
            else if (suits.indexOf(card.getSuit()) % 2 != 0 & blacks.indexOf(card.getRank()) >= 0) {
                deckAndOpened.add(card);
                blacks = blacks.replace(String.valueOf(card.getRank()), "");
            }
        }
        deckArrayList.removeAll(deckAndOpened);
        for (int i = 0; i < 5; i++) {
            deckAndOpened.add(deckArrayList.get(0));
            deckArrayList.remove(0);
        }
        for (int i = 0; i < 7; i++) {
            ArrayList<Card> stack = new ArrayList<>(deckArrayList.subList(0, i));
            deckArrayList.removeAll(stack);
            stack.add(deckAndOpened.get(0));
            deckAndOpened.remove(0);
            stack.get(stack.size() - 1).setDrawn(true);
            stacksArrayList.get(i).addAll(stack);
        }
        deckArrayList.addAll(deckAndOpened);
        stacksChanged = false;
    }

    private void createEmptyStacks() {
        for (int i = 0; i < 7; i++) {
            ArrayList<Card> stack = new ArrayList<>(), suit = new ArrayList<>();
            stacksArrayList.add(stack);
            if (i < 4) suitsArrayList.add(suit);
        }
    }

    private void clearStacks() {
        deckArrayList.clear();
        stacksArrayList.clear();
        suitsArrayList.clear();
        drawnArrayList.clear();
    }
    public void drawCards(Pane pane, ArrayList<Rectangle> rectangles, Label movesLabel) {
        view.drawCards(pane, rectangles, movesLabel);
    }
    public ArrayList<Card> getDeck() {
        return deckArrayList;
    }
    public ArrayList<Card> getDrawn() {
        return drawnArrayList;
    }
    public ArrayList<ArrayList<Card>> getStacks() {
        return stacksArrayList;
    }
    public ArrayList<ArrayList<Card>> getSuits() {
        return suitsArrayList;
    }
    public void createDeck() {
        for (char r : ranks.toCharArray()) {
            for (char s : suits.toCharArray()) {
                deckArrayList.add(new Card(r, s, false));
            }
        }
    }
    public String[] getStackSublistCards(String cardId) {
        ArrayList<Card> cardsArrayList = new ArrayList<>();
        for (ArrayList<Card> stack : stacksArrayList) for (Card card : stack) if (cardId.contains(card.getCard()))
                    cardsArrayList.addAll(stack.subList(stack.indexOf(card) + 1, stack.size()));
        String[] cards = new String[12];
        if (!cardsArrayList.isEmpty()) for (Card card : cardsArrayList) cards[cardsArrayList.indexOf(card)] = card.getCard();
        return cards;
    }

    public void drawCard(Pane pane, Rectangle drawnCards, Label movesLabel) {
        if (!deckArrayList.isEmpty()){
            drawnArrayList.add(deckArrayList.get(0));
            deckArrayList.remove(0);
            drawnArrayList.get(drawnArrayList.size() - 1).setDrawn(true);
            view.drawNewCard(pane, drawnArrayList.get(drawnArrayList.size() - 1), drawnCards, movesLabel);
            if (deckArrayList.isEmpty()) view.removeDeckImage(pane);
        }
    }
    public void getDeckBack(Pane pane, Rectangle drawnCards, Label movesLabel) {
        updateDrawn(pane, drawnCards);
        deckArrayList.addAll(drawnArrayList);
        for (Card card : deckArrayList) card.setDrawn(false);
        drawnArrayList.clear();
        view.drawDeck(pane, movesLabel, true);
    }

    public void updateDrawn(Pane pane, Rectangle drawnCards) {
        for (Node node : pane.getChildren()) {
            if (node.getId() == null) continue;
            drawnArrayList.removeIf(n -> node.getId().contains(n.getCard()) & node.getLayoutX() != drawnCards.getLayoutX() &
                    node.getLayoutY() != drawnCards.getLayoutY());
        }
        view.removeDrawnCards(pane, drawnCards);
    }

    public void updateCard(Pane pane, String id) {
        ArrayList<ArrayList<Card>> allDrawn = new ArrayList<>(stacksArrayList);
        allDrawn.add(drawnArrayList);
        for (ArrayList<Card> stack : allDrawn) for (Card card : stack) {
            if (id.contains(card.getCard())) {
                view.setCardToFront(pane, id);
            }
        }
    }

    public void methodChoice(String cardId, String placeId, boolean isOnly) {
        if (placeId.contains("+")) suitSet(cardId, placeId, isOnly);
        else cardSet(cardId, placeId);
    }

    public void cardSet(String cardId, String placeId) {
        int cardStackIndex = -1, placeStackIndex = -1, cardIndex = -1;
        if (areCardsStackable(cardId, placeId)) {
            ArrayList<ArrayList<Card>> allDrawn = new ArrayList<>(stacksArrayList);
            allDrawn.addAll(suitsArrayList);
            allDrawn.add(drawnArrayList);
            for (ArrayList<Card> stack : allDrawn) {
                for (Card card : stack) {
                    if (stack.isEmpty()) continue;
                    if (cardId.contains(card.getCard())) {
                        if (stack == drawnArrayList) {
                            cardStackIndex = stacksArrayList.size() + suitsArrayList.size();
                            cardIndex = drawnArrayList.indexOf(card);
                        } else if (stacksArrayList.contains(stack)) {
                            cardStackIndex = stacksArrayList.indexOf(stack);
                            cardIndex = stack.indexOf(card);
                        } else {
                            cardStackIndex = stacksArrayList.size() + suitsArrayList.indexOf(stack);
                            cardIndex = suitsArrayList.get(suitsArrayList.indexOf(stack)).indexOf(card);
                        }
                    }
                    if (placeId.contains(card.getCard()) & stack.indexOf(card) == stack.size() - 1) {
                        placeStackIndex = stacksArrayList.indexOf(stack);
                    }
                }
            }
            if (cardIndex >= 0 & cardStackIndex >= 0 & placeStackIndex >= 0) {
                if (cardStackIndex < stacksArrayList.size()) {
                    stacksArrayList.get(placeStackIndex).add(stacksArrayList.get(cardStackIndex).get(cardIndex));
                    stacksArrayList.get(cardStackIndex).remove(stacksArrayList.get(cardStackIndex).get(cardIndex));
                    if (stacksArrayList.get(cardStackIndex).size() > 0)
                        stacksArrayList.get(cardStackIndex).get(stacksArrayList.get(cardStackIndex).size() - 1).setDrawn(true);
                    stacksChanged = true;
                } else if (cardStackIndex < stacksArrayList.size() + suitsArrayList.size()) {
                    cardStackIndex -= stacksArrayList.size();
                    stacksArrayList.get(placeStackIndex).add(suitsArrayList.get(cardStackIndex).get(cardIndex));
                    suitsArrayList.get(cardStackIndex).remove(suitsArrayList.get(cardStackIndex).get(cardIndex));
                    stacksChanged = true;
                }
                else {
                    stacksArrayList.get(placeStackIndex).add(drawnArrayList.get(cardIndex));
                    drawnArrayList.remove(drawnArrayList.get(cardIndex));
                    stacksChanged = true;
                }
            }
        }
    }

    private boolean areCardsStackable(String cardId, String placeId) {
        if (cardId.contains("+")) cardId = cardId.replace("+", "");
        if (placeId.contains("+")) return false;
        char[] card = Arrays.copyOfRange(cardId.toCharArray(), cardId.length() - 2, cardId.length());
        char[] place = Arrays.copyOfRange(placeId.toCharArray(), cardId.length() - 2, cardId.length());
        return ranks.indexOf(place[0]) - ranks.indexOf(card[0]) == 1 &
                suits.indexOf(place[1]) % 2 != suits.indexOf(card[1]) % 2;
    }

    public void reDraw(Pane pane, ArrayList<Rectangle> stacksRectangles, ArrayList<Rectangle> suitsRectangles, Rectangle drawnCards, Label movesLabel) {
        int suited = 0;
        for (ArrayList<Card> stack : suitsArrayList) suited += stack.size();
        if (stacksChanged) {
            view.updateMoves(movesLabel, false);
            view.reDraw(pane, stacksRectangles, suitsRectangles, drawnCards, suited != 52, movesLabel);
            stacksChanged = false;
        }
        if (suited == 52) view.winAnimation(pane);
    }

    public void kingSet(String cardId, int stackIndex) {
        int cardStackIndex = -1, cardIndex = -1;
        if (stacksArrayList.get(stackIndex).isEmpty() & cardId.contains("K")) {
            ArrayList<ArrayList<Card>> allDrawn = new ArrayList<>(stacksArrayList);
            allDrawn.addAll(suitsArrayList);
            allDrawn.add(drawnArrayList);
            for (ArrayList<Card> stack : allDrawn) for (Card card : stack) {
                if (cardId.contains(card.getCard())) {
                    if (stack == drawnArrayList) {
                        cardStackIndex = stacksArrayList.size() + suitsArrayList.size();
                        cardIndex = drawnArrayList.indexOf(card);
                    } else if (stacksArrayList.contains(stack)) {
                        cardStackIndex = stacksArrayList.indexOf(stack);
                        cardIndex = stack.indexOf(card);
                    } else {
                        cardStackIndex = stacksArrayList.size() + suitsArrayList.indexOf(stack);
                        cardIndex = suitsArrayList.get(suitsArrayList.indexOf(stack)).indexOf(card);
                    }
                }
            }
            if (cardIndex >= 0 & cardStackIndex >= 0 & stackIndex >= 0) {
                if (cardStackIndex < stacksArrayList.size()) {
                    stacksArrayList.get(stackIndex).add(stacksArrayList.get(cardStackIndex).get(cardIndex));
                    stacksArrayList.get(cardStackIndex).remove(stacksArrayList.get(cardStackIndex).get(cardIndex));
                    if (stacksArrayList.get(cardStackIndex).size() > 0)
                        stacksArrayList.get(cardStackIndex).get(stacksArrayList.get(cardStackIndex).size() - 1).setDrawn(true);
                    stacksChanged = true;
                } else if (cardStackIndex < stacksArrayList.size() + suitsArrayList.size()) {
                    cardStackIndex -= stacksArrayList.size();
                    stacksArrayList.get(stackIndex).add(suitsArrayList.get(cardStackIndex).get(cardIndex));
                    suitsArrayList.get(cardStackIndex).remove(suitsArrayList.get(cardStackIndex).get(cardIndex));
                    stacksChanged = true;
                }
                else {
                    stacksArrayList.get(stackIndex).add(drawnArrayList.get(cardIndex));
                    drawnArrayList.remove(drawnArrayList.get(cardIndex));
                    stacksChanged = true;
                }
            }
        }
    }

    public void suitSet(String cardId, String suit, boolean isOnly) {
        int cardStackIndex = -1, cardIndex = -1;
        if (cardId.contains(suit) & isOnly) {
            ArrayList<ArrayList<Card>> allDrawn = new ArrayList<>(stacksArrayList);
            allDrawn.add(drawnArrayList);
            for (ArrayList<Card> stack : allDrawn) for (Card card : stack) {
                if (cardId.contains(card.getCard())) {
                    if (stack == drawnArrayList) {
                        cardStackIndex = stacksArrayList.size();
                        cardIndex = drawnArrayList.indexOf(card);
                    } else {
                        cardStackIndex = stacksArrayList.indexOf(stack);
                        cardIndex = stack.indexOf(card);
                    }
                }
            }
            if (cardIndex >= 0 & cardStackIndex >= 0) {
                if (suitsArrayList.get(suits.indexOf(suit)).isEmpty()) {
                    if (cardId.contains("A")) {
                        if (cardStackIndex < stacksArrayList.size()) {
                            suitsArrayList.get(suits.indexOf(suit)).add(stacksArrayList.get(cardStackIndex).get(cardIndex));
                            stacksArrayList.get(cardStackIndex).remove(stacksArrayList.get(cardStackIndex).get(cardIndex));
                            if (stacksArrayList.get(cardStackIndex).size() > 0)
                                stacksArrayList.get(cardStackIndex).get(stacksArrayList.get(cardStackIndex).size() - 1).setDrawn(true);
                        } else {
                            suitsArrayList.get(suits.indexOf(suit)).add(drawnArrayList.get(cardIndex));
                            drawnArrayList.remove(drawnArrayList.get(cardIndex));
                        }
                        stacksChanged = true;
                    }
                } else {
                    if (cardStackIndex < stacksArrayList.size()) {
                        if (doCardsFit(stacksArrayList.get(cardStackIndex).get(cardIndex).getCard(), suits.indexOf(suit))) {
                            suitsArrayList.get(suits.indexOf(suit)).add(stacksArrayList.get(cardStackIndex).get(cardIndex));
                            stacksArrayList.get(cardStackIndex).remove(stacksArrayList.get(cardStackIndex).get(cardIndex));
                            if (stacksArrayList.get(cardStackIndex).size() > 0)
                                stacksArrayList.get(cardStackIndex).get(stacksArrayList.get(cardStackIndex).size() - 1).setDrawn(true);
                            stacksChanged = true;
                        }
                    } else if (doCardsFit(drawnArrayList.get(cardIndex).getCard(), suits.indexOf(suit))) {
                        suitsArrayList.get(suits.indexOf(suit)).add(drawnArrayList.get(cardIndex));
                        drawnArrayList.remove(drawnArrayList.get(cardIndex));
                        stacksChanged = true;
                    }
                }
            }
        }
    }

    private boolean doCardsFit(String card, int suitIndex) {
        char rank = card.charAt(card.length() - 2);
        ArrayList<Card> suited = new ArrayList<>(suitsArrayList.get(suitIndex));
        return ranks.indexOf(rank) - ranks.indexOf(suited.get(suited.size() - 1).getRank()) == 1;
    }

    public void saveGame() {
        ArrayList<ArrayList<Card>> allCards = new ArrayList<>(stacksArrayList);
        allCards.addAll(suitsArrayList);
        allCards.add(drawnArrayList);
        allCards.add(deckArrayList);
        Date date = new Date();
        String formDate = date.toString().replace(':', '-');
        String path = view.saveWindow();
        if (path != null) {
            try (FileWriter writer = new FileWriter(path + "/save " + formDate + ".bin", false)) {
                for (ArrayList<Card> stack : allCards) {
                    for (Card card : stack) {
                        writer.write(card.getCard() + (card.isDrawn() ? '1' : '0'));
                    }
                    writer.append('\n');
                }
                writer.write(view.getMoves());
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadGame() {
        clearStacks();
        createEmptyStacks();
        String path = view.loadWindow();
        if (path != null) {
            try (FileReader reader = new FileReader(path)) {
                int index = 0, c;
                while ((c = reader.read()) != -1) {
                    if ((char) c == '\n') index++;
                    else {
                        char rank = (char) c;
                        char suit = (char) reader.read();
                        boolean drawn = (char) reader.read() == '1';
                        if (index < 7) stacksArrayList.get(index).add(new Card(rank, suit, drawn));
                        else if (index < 11) suitsArrayList.get(index - 7).add(new Card(rank, suit, drawn));
                        else if (index < 12) drawnArrayList.add(new Card(rank, suit, drawn));
                        else if (index == 12) deckArrayList.add(new Card(rank, suit, drawn));
                        else view.setMoves(c - 1);
                    }
                }
                stacksChanged = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
