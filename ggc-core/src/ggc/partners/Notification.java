package ggc.partners;

import java.io.*;
import java.util.*;
import ggc.exceptions.*;
import ggc.products.*;


public class Notification implements Serializable {

    private static final long serialVersionUID = 202110262346L;

    private String _type;
    private Product _product;
    private float _price;
    private String _method = "";

    public Notification(String type, Product product, float price) { // With a different method
        _product = product;
        _price = price;
        _type = type;


    }

    public String getMethod() { return _method; }

    public Product getProduct() { return _product; }

    @Override
    public String toString() {
        return _type + "|" + _product.getId() + "|" + Math.round(_price);
    }
}