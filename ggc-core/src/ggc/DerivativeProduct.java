package ggc;

// FIXME import classes (cannot import from pt.tecnico or ggc.app)
import java.io.*;
import java.util.*;
import ggc.exceptions.*;

public class DerivativeProduct extends Product {

    private Recipe _recipe;
    private float _multiplier;

    public DerivativeProduct(String id, Recipe recipe, float multiplier) {
        setId(id);
        _recipe = recipe;
        _multiplier = multiplier;
    }

    // Getters
    public Recipe getRecipe() {
        return _recipe;
    }

    public float getMultiplier() {
        return _multiplier;
    }

    // Setters
    public void setRecipe(Recipe recipe) {
        _recipe = recipe;
    }

    public void setMultiplier(float multiplier) {
        _multiplier = multiplier;
    }

    @Override
    public String toString() {
        return getId() + "|" + (int)getMaxPrice() + "|" + getStock() + "|" + _multiplier + "|" + _recipe.toString();
    }

}