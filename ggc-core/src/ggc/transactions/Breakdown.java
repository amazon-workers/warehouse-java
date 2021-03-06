package ggc.transactions;

import java.io.*;
import java.util.*;
import ggc.partners.Partner;
import ggc.products.*;

public class Breakdown extends Sale implements Serializable {

    private static final long serialVersionUID = 202111081640L;
    private Receipt _receipt;

    public Breakdown(int id, Partner partner, Product product, int amount, float baseValue, int deadline, Receipt receipt) {
        super(id, partner, product, amount, baseValue, baseValue, deadline);
        _receipt = receipt;
    }

    @Override
    public String toString() {
        return "DESAGREGAÇÃO|" + getId() + "|" + getPartner().getId() + "|" + getProduct().getId() + "|" +
                getAmount() + "|" + Math.round(getBaseValue()) + "|" + Math.round(getRealValue()) + "|" + getPaidDate() + "|" + _receipt;
    }
}