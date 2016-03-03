package com.sixbynine.waterwheels.model;

public enum PlaceGroup {
    TORONTO("Toronto"),
    WATERLOO("Waterloo"),
    MARKHAM_RICHMOND_HILL("Markham/Richmond Hill"),
    SCARBOROUGH("Scarborough"),
    MISSISSAUGA("Mississauga"),
    LONDON("London");

    private final String name;

    PlaceGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
