package com.dtech.minhadieta;

public class Food {
    private final String name;
    private final int calories;
    private final String serving;

    public Food(String name, int calories, String serving) {
        this.name = name;
        this.calories = calories;
        this.serving = serving;
    }

    public String getName() { return name; }
    public int getCalories() { return calories; }
    public String getServing() { return serving; }
}