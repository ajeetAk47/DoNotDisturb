package com.ak47.doNotDisturb.Model;

public class Word {
    private int id;
    private String word;

    public Word(String word) {
        this.word = word;
    }

    public Word() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
