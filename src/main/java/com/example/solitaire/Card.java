package com.example.solitaire;

public class Card {
    private final char suit;
    private final char rank;
    private final String path;
    private boolean drawn;

    public Card(char rank, char suit, boolean drawn) {
        this.rank = rank;
        this.suit = suit;
        this.path = System.getProperty("user.dir") + "/src/main/resources/assets/cards/" + rank + suit + ".png";
        this.drawn = drawn;
    }
    public String getCard() {
        return rank + String.valueOf(suit);
    }
    public String getPath() {
        return path;
    }
    public char getSuit() {
        return suit;
    }
    public char getRank() {
        return rank;
    }
    public boolean isDrawn() {
        return drawn;
    }
    public void setDrawn(boolean drawn) {
        this.drawn = drawn;
    }
}
