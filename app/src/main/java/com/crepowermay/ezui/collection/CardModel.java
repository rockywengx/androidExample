package com.crepowermay.ezui.collection;

public class CardModel<T> implements CardModelInterface<T> {

    private String title;
    private T content;

    public CardModel(String title, T content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public T getContent() {
        return content;
    }

}
