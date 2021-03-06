package ggc;

import java.io.*;
import java.util.*;
import ggc.exceptions.*;
import ggc.partners.*;
import ggc.products.*;
import ggc.transactions.*;

/**
 * Class Warehouse implements a warehouse.
 */
public class Warehouse implements Serializable {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 202109192006L;

  /** Warehouse's current available balance */
  private double _availableBalance = 0;
  /** Warehouse's current date */
  private int _date = 0;
  /** Set of all the products the warehouse knows */
  private Set<Product> _products = new TreeSet<Product>();
  /** Map matching products with their id */
  private Map<String, Product> _productLookup = new TreeMap<String, Product>(String.CASE_INSENSITIVE_ORDER);
  /** Map matching partners with their id */
  private Map<String, Partner> _partnerLookup = new TreeMap<String, Partner>(String.CASE_INSENSITIVE_ORDER);
  /** Set of all the partners the warehouse has */
  private Set<Partner> _partners = new TreeSet<Partner>();
  /** List of all the transactions made in the warehouse */
  private ArrayList<Transaction> _transactions = new ArrayList<Transaction>();
  /** Warehouse's notification station */
  private NotificationStation _notStation = new NotificationStation();

  // Getters

  /**
   * @@return warehouse's date
   */
  public int getDate() {
    return _date;
  }

  /**
   * @@return warehouse's available balance
   */
  public double getAvailableBalance() {
    return _availableBalance;
  }

  /**
   * @@return warehouse's contabilistic balance
   */
  public double getContabilisticBalance() {
    double contabilisticBalance = _availableBalance;

    for (Transaction t: _transactions) {
      if (!t.paid()) {
        t.updateRealValue(_date);
        contabilisticBalance += t.getRealValue();
      }
    }

    return contabilisticBalance;
  }

  public int getTotalTransactions() {
    return _transactions.size();
  }

  /**
   * @@param days number of days to advance
   * @@throws NoSuchDateException
   */
  public void advanceDate(int days) throws NoSuchDateException {
    if (days > 0)
      _date += days;
    else throw new NoSuchDateException(days);
  }

  /**
   * @@param id product's id
   * @@return product
   * @@throws NoSuchProductException
   */
  public Product lookupProduct(String id) throws NoSuchProductException {
    if (!_productLookup.containsKey(id)) {
      throw new NoSuchProductException(id);
    }
    return _productLookup.get(id);
  }

  /**
   * @@param id partner's id
   * @@return partner
   * @@throws NoSuchProductException
   */
  public Partner lookupPartner(String id) throws NoSuchPartnerException {
    if (!_partnerLookup.containsKey(id)) {
      throw new NoSuchPartnerException(id);
    }
    return _partnerLookup.get(id);
  }

  /**
   * @@param id partner's id
   * @@return partner and its notifications
   * @@throws NoSuchPartnerException
   */
  public ArrayList<String> lookupPartnerPlusNotifications(String id) throws NoSuchPartnerException {
    ArrayList<String> stringList = new ArrayList<String>();
    Partner partner = lookupPartner(id);

    stringList.add(partner.toString());
    for (Notification n: listPartnerNotificationsByMethod(partner, "")) {
      stringList.add(n.toString());
    }

    return stringList;
  }

  /**
   * @@return sorted list of all products
   */
  public Set<Product> listAllProducts() {
    return _products;
  }

  /**
   * @@return sorted list of all batches
   */
  public ArrayList<Batch> listAllBatches() {
    ArrayList<Batch> batchList = new ArrayList<Batch>();

    for (Product p: _products) {
      batchList.addAll(listBatchesByProduct(p));
    }

    batchList.sort(null);

    return batchList;
  }

  /**
   * @@param id id of the partner whose batches are to be listed
   * @@return sorted list of partner's batches
   * @@throws NoSuchPartnerException
   */
  public List<Batch> listBatchesByPartner(String partner) throws NoSuchPartnerException {
    return listBatchesByPartner(lookupPartner(partner));
  }

  /**
   * @@param partner partner whose batches are to be listed
   * @@return sorted list of partner's batches
   */
  public List<Batch> listBatchesByPartner(Partner partner) {
    ArrayList<Batch> batchList = new ArrayList<Batch>(partner.getBatches());
    batchList.sort(null);

    return batchList;
  }

  /**
   * @@param id id of the product which batches are to be listed
   * @@return sorted list of batches
   * @@throws NoSuchProductException
   */
  public List<Batch> listBatchesByProduct(String partner) throws NoSuchProductException{
    return listBatchesByProduct(lookupProduct(partner));
  }

  /**
   * @@param product product which batches are to be listed
   * @@return sorted list of batches
   */
  public List<Batch> listBatchesByProduct(Product product) {
    ArrayList<Batch> batchList = new ArrayList<Batch>(product.getBatches());
    batchList.sort(null);

    return batchList;
  }


    /**
   * @@param id partner's id whose notifications are to be listed
   * @@return list of all selected partner's notifications
   * @@throws NoSuchPartnerException
   */
  public List<Notification> listPartnerNotifications(String id) throws NoSuchPartnerException{
    return lookupPartner(id).listAllNotifications();
  }

    /**
   * @@param id partner's id whose notifications are to be listed
   * @@param method method to filter search
   * @@return list of specific method selected partner's notifications
   * @@throws NoSuchProductException
   */
  public List<Notification> listPartnerNotificationsByMethod(String id, String method) throws NoSuchPartnerException{
    return listPartnerNotificationsByMethod(lookupPartner(id), method);
  }

    /**
   * @@param partner partner whose notifications are to be listed
   * @@param method method to filter search
   * @@return list of specific method selected partner's notifications
   * @@throws NoSuchProductException
   */
  public List<Notification> listPartnerNotificationsByMethod(Partner partner, String method) throws NoSuchPartnerException{
    return partner.listAllNotificationsByMethod(method);
  }

  /**
   * @@return list of all partners
   */
  public Set<Partner> listAllPartners() {
    return _partners;
  }

  /**
   * @@param id partners's id
   * @@param name partner's name
   * @@param address partner's address
   * @@throws DuplicatePartnerException
   */
  public void registerNewPartner(String id, String name, String address) throws DuplicatePartnerException {
    try {
      lookupPartner(id);
    } catch (NoSuchPartnerException e) {
      Partner newPartner = new Partner(id, name, address);
      _partners.add(newPartner);
      _partnerLookup.put(id, newPartner);
      _notStation.addMailbox(newPartner.getMailbox());
      return;
    }

    throw new DuplicatePartnerException(id);
  }

  /**
   * @@param id   product's id
   * @@param price product's price
   * @@param stock product's stock
   * @@param return registered product
   */

  public ProductSimple registerProductSimple(String id, float price, int stock) {
    ProductSimple product = null;

    try {
      product = (ProductSimple) lookupProduct(id);
    } catch (NoSuchProductException e) {
      product = new ProductSimple(id);
      _productLookup.put(id, product);
      _products.add(product);
    }

    if (product.getMaxPrice() < price) {
      product.setMaxPrice(price);
    }

    return product;
  }

  /**
   * @@param id product's id
   * @@param recipe product's recipe
   * @@param multiplier product's multiplier
   * @@param price product's price
   * @@param stock product's stock
   * @@return registered product
   */
  public ProductDerivative registerProductDerivative(String id, Recipe recipe, float multiplier, float price, int stock) {
    ProductDerivative product;

    try {
      product = (ProductDerivative) lookupProduct(id);
    } catch (NoSuchProductException e) {
      product = new ProductDerivative(id, recipe, multiplier);
      _productLookup.put(id, product);
      _products.add(product);
    }

    if (product.getMaxPrice() < price) {
      product.setMaxPrice(price);
    }

    return product;
  }

  /**
   * @@param price selected price
   * @@return batches under given price
   */
  public List<Batch> listBatchesUnderGivenPrice(float price) {
    List<Batch> batchList = new ArrayList<Batch>();

    for (Batch b : listAllBatches()) {
      if (b.getPrice() < price) {
        batchList.add(b);
      }
    }

    return batchList;
  }

  /**
   * @@param partnerStr   partner's id
   * @@param productStr product's id
   * @@throws NoSuchPartnerException
   * @@throws NoSuchProductException 
   */

  public void toggleProductNotifications(String partnerStr, String productStr) throws NoSuchPartnerException, NoSuchProductException {
    Partner partner = lookupPartner(partnerStr);
    Product product = lookupProduct(productStr);
    partner.getMailbox().toggleBlockedProduct(product);
  }


  /**
   * @@param product selected product
   * @@return selected product's cheapest batch
   */
  public Batch getCheapestBatch(Product product) {
    List<Batch> batchesByProduct = listBatchesByProduct(product);
    Batch cheapestBatch = null;
    float cheapestPrice = -1;

    for (Batch b : batchesByProduct) {
      if ((cheapestPrice == -1 || b.getPrice() < cheapestPrice) && b != null) {
        cheapestBatch = b;
        cheapestPrice = b.getPrice();
      }
    }

    return cheapestBatch;
  }

  /**
   * @@param batch batch to be removed
   */




  /**
   * @@param product product to be consumed
   * @@param quantity product's quantity to be consumed
   * @@return price of all consumed products
   */
  public float consumeProducts(Product product, int quantity) { // Returns the price of all of the products summed together
    float price = 0;
    Batch currentBatch;
    product.addStock(-quantity);

    while (quantity > 0) {
      currentBatch = getCheapestBatch(product);

      if (currentBatch.getStock() <= quantity) {
        price += currentBatch.getStock() * currentBatch.getPrice();
        quantity -= currentBatch.getStock();
        currentBatch.destroy();
      } else {
        price += quantity * currentBatch.getPrice();
        currentBatch.addStock(-quantity);
        quantity = 0;
      }

    }

    return price;
  }

  /**
   * @@param product product to be looked up
   * @@param partner selected partner
   * @@param price selected price
   * @@return similar batch, if it exists, else return null 
   */

  public Batch lookupSimilarBatch(Product product, Partner partner, float price) {
    for (Batch b : listBatchesByProduct(product)) {
      if (b.getProduct() == product && b.getPartner() == partner && b.getPrice() == price) {
        return b;
      }
    }

    return null;
  }

  /**
   * @@param product product to be crafted
   * @@param partner partner associated with product
   * @@param quantity quantity to be crafted
   */

  public void craftProduct(ProductDerivative product, Partner partner, int quantity) {
    Recipe recipe = product.getRecipe();

    while (quantity > 0) { // While quantity requested hasn't been reached
      float price = 0;

      for (Product p : recipe.getProducts()) { // Consume each of the recipe's products
        if (p.getStock() < recipe.getProductQuantity(p)) {
          craftProduct((ProductDerivative) p, partner, recipe.getProductQuantity(p) - p.getStock());
        }

        price += consumeProducts(p, recipe.getProductQuantity(p)); // Price gets summed
      }

      price *= (1 + product.getMultiplier()); // Calculate price of the new batch

      registerNewBatch(product, partner, price, 1);
      quantity--;
    }
  }

  /**
   * @@param partnerStr   partner's id
   * @@param productStr product's id
   * @@param amount product's amount to be sold
   * @@param deadline payment deadline
   * @@return sale made
   * @@throws NotEnoughProductsException
   * @@throws NoSuchPartnerException
   * @@throws NoSuchProductException
   */

  public Sale attemptSale(String partnerStr, String productStr, int amount, int deadline) throws NotEnoughProductsException, NoSuchPartnerException, NoSuchProductException {
    Partner partner = lookupPartner(partnerStr);
    Product product = lookupProduct(productStr);
    return attemptSale(partner, product, amount, deadline);
  }
   
  /**
   * @@param partner partner associated with sale
   * @@param product product being sold
   * @@param amount product's amount to be sold
   * @@param deadline payment deadline
   * @@return sale made
   * @@throws NotEnoughProductsException
   */

  public Sale attemptSale(Partner partner, Product product, int amount, int deadline) throws NotEnoughProductsException {
    if (product.getStock() >= amount) { // Check if product stock is directly enough to sell
      float price = consumeProducts(product, amount);
      Sale sale = new Sale(getTotalTransactions(), partner, product, amount, price, price, deadline); // FIXME
      _transactions.add(sale);
      partner.addSale(sale);

      return sale;
    } else { // If product stock isn't enough, check if difference between stock and requested amount can be crafted
      int stockNeeded = amount - product.getStock(); // Calculate difference

      if (product.enoughStock(amount)) { // Check if it can be crafted
        if (product.getRecipe() != null) {
          craftProduct((ProductDerivative) product, partner, stockNeeded); // Craft the product
          return attemptSale(partner, product, amount, deadline); // Try to sell again
        }
      } else {
        product.throwFirstMissingSimpleProduct(stockNeeded);
      }
    }

    // If none of the above are successful, throw exception
    throw new NotEnoughProductsException(product.getId(), amount, product.getStock());
  }

  /**
   * @@param partnerStr partner's id
   * @@param productStr product's id
   * @@param amount product's amount to be acquired
   * @@param price product's price
   * @@return acquisition made
   * @@throws NoSuchPartnerException
   * @@throws NoSuchProductException
   */

  public Acquisition acquire(String partnerStr, String productStr, int amount, float price, boolean newProduct) throws NoSuchPartnerException, NoSuchProductException{
    Partner partner = lookupPartner(partnerStr);
    Product product = lookupProduct(productStr);
    return acquire(partner, product, amount, price, newProduct);
  }

  /**
   * @@param partner partner associated with acquisition
   * @@param product product being acquired
   * @@param amount product's amount to be acquired
   * @@param price product's price
   * @@return acquisition made
   */

  public Acquisition acquire(Partner partner, Product product, int amount, float price, boolean newProduct) {
    _availableBalance -= amount * price;

    // Look up the cheapest batch to check for notifications
    Batch cheapestBatch = getCheapestBatch(product);

    // If stock was 0, then emit a notification for NEW
    if (!newProduct && product.getStock() == 0) { _notStation.emitNotification(new Notification("NEW", product, price)); }

    // If new price is cheaper than old cheapest batch, then emit a notification for BARGAIN
    if (cheapestBatch != null && cheapestBatch.getPrice() > price) { _notStation.emitNotification(new Notification("BARGAIN", product, price)); }

    registerNewBatch(product, partner, price, amount);

    Acquisition acquisition = new Acquisition(getTotalTransactions(), partner, product, amount, price, _date);

    _transactions.add(acquisition);
    lookupAcquisitionsByPartner(partner).add(acquisition);

    return acquisition;
  }


  /**
   * @@param partnerStr partner's id
   * @@param productStr product's id
   * @@param amount product's amount to be broken down
   * @@return breakdown made
   * @@throws NotEnoughProductsException
   * @@throws NoSuchPartnerException
   * @@throws NoSuchProductException
   */

  public Breakdown attemptBreakdown(String partnerStr, String productStr, int amount) throws NotEnoughProductsException, NoSuchPartnerException, NoSuchProductException {
    Partner partner = lookupPartner(partnerStr);
    Product product = lookupProduct(productStr);
    return attemptBreakdown(partner, product, amount);
  }


  /**
   * @@param partner partner associated with breakdown
   * @@param product product being broken down
   * @@param amount product's amount 
   * @@return breakdown made
   * @@throws NotEnoughProductsException
   */

  public Breakdown attemptBreakdown(Partner partner, Product product, int amount) throws NotEnoughProductsException {
    if (product.getStock() < amount) { throw new NotEnoughProductsException(product.getId(), amount, product.getStock()); } // If not enough stock, fails
    if (product.getRecipe() == null) { return null; } // If product is simple, don't do anything

    Recipe recipe = product.getRecipe();

    float price = 0;
    price += consumeProducts(product, amount);
    Receipt receipt = new Receipt(product.getRecipe(), amount);
    float productPrice;

    for (Product p: recipe.getProducts()) { // Go to each product in the recipe

      if (p.getStock() == 0) { // If there is no batch, create one with highest price ever registered
        productPrice = p.getMaxPrice();
      } else { // If there is a batch, create one with the same price.
        productPrice = getCheapestBatch(p).getPrice();
      }

      registerNewBatch(p, partner, productPrice, amount * recipe.getProductQuantity(p));
      receipt.productSetPrice(p, productPrice); // Set the price in the receipt
      price -= productPrice * amount * recipe.getProductQuantity(p); // Subtract the product price from the final price
    }

    Breakdown breakdown = new Breakdown(getTotalTransactions(), partner, product, amount, price, getDate(), receipt);
    pay(breakdown);
    partner.addBreakdown(breakdown);
    _transactions.add(breakdown);

    return breakdown;
  }

  /**
   * @@param id transaction id
   * @@throws NoSuchTransactionException
   */

  public void pay(int id) throws NoSuchTransactionException{
    Transaction transaction = lookupTransaction(id);
    pay(transaction);
  }

  /**
   * @@param id transaction to be paid
   * @@throws NoSuchTransactionException
   */

  public void pay(Transaction transaction) {
    if (!transaction.paid()){
    transaction.markAsPaid(_date);

    _availableBalance += transaction.getRealValue();
    }
  }

  /**
   * @@param partner id of the partner to be searched
   * @@return list of selected partner paid sales
   * @@throws NoSuchPartnerException
   */
  public ArrayList<Transaction> lookupPaidSalesByPartner(String partner) throws NoSuchPartnerException {
    return lookupPaidSalesByPartner(lookupPartner(partner));
  }

  /**
   * @@param partner partner to be searched
   * @@return list of selected partner paid sales
   */
  public ArrayList<Transaction> lookupPaidSalesByPartner(Partner partner) {
    return partner.getPaidSales();
  }


  /**
   * @@param partner id of the partner to be searched
   * @@return list of selected partner sales
   * @@throws NoSuchPartnerException
   */
  public ArrayList<Transaction> lookupSalesByPartner(String partner) throws NoSuchPartnerException{
    return lookupSalesByPartner(lookupPartner(partner));
  }


  /**
   * @@param partner partner to be searched
   * @@return list of selected partner sales
   */
  public ArrayList<Transaction> lookupSalesByPartner(Partner partner) {
    for (Transaction t: partner.getSales()) {
      t.updateRealValue(_date);
    }

    return partner.getSales();
  }

      

  /**
   * @@param partner partner associated with acquisition
   * @@param product product being acquired
   * @@param price product's price 
   * @@param stock product's stock
   * @@throws NoSuchPartnerException
   * @@throws NoSuchProductsException
   */      
  public void acquireNewProductSimple(String partner, String product, float price, int stock) throws NoSuchPartnerException, NoSuchProductException{
    registerProductSimple(product, price, stock);
    acquire(partner, product, stock, price, true);
  }


  /**
   * @@param partnerStr id of partner associated with acquisition
   * @@param productStr id of product being acquired
   * @@param price product's price 
   * @@param stock product's stock
   * @@param products list of products which make up derivative product
   * @@param productQuantities list of quantities for recipe
   * @@param multiplier derivative product price multiplier
   * @@throws NoSuchPartnerException
   * @@throws NoSuchProductsException
   */      
  public void acquireNewProductDerivative(String partnerStr, String productStr, float price, int stock, ArrayList<String> products, ArrayList<Integer> productQuantities, float multiplier) throws NoSuchPartnerException, NoSuchProductException {
      Partner partner = lookupPartner(partnerStr);
      Recipe recipe = new Recipe();

      for (int i = 0; i < products.size(); i++) {
        recipe.addProduct(lookupProduct(products.get(i)), productQuantities.get(i));
      }

      registerProductDerivative(productStr, recipe, multiplier, price, stock);
      Product product = lookupProduct(productStr);

      acquire(partner, product, stock, price, true);
  }  



  /**
   * @@param partner id of selected partner
   * @@return acquisitions by partner 
   * @@throws NoSuchPartnerException
   */      
  public ArrayList<Transaction> lookupAcquisitionsByPartner(String partner) throws NoSuchPartnerException{
    return lookupAcquisitionsByPartner(lookupPartner(partner));
  }

  /**
   * @@param partner selected partner
   * @@return acquisitions by partner 
   */  
  public ArrayList<Transaction> lookupAcquisitionsByPartner(Partner partner) {
    return partner.getAcquisitions();

  }

  /**
   * @@param id transaction id
   * @@return selected transaction 
   * @@throws NoSuchTransactionException
   */  
  public Transaction lookupTransaction(int id) throws NoSuchTransactionException {
    if (id >= _transactions.size() || id < 0) { throw new NoSuchTransactionException(id); }

    return _transactions.get(id);
  }


  // -------------------------------------------------------------------------------------------------------------------

  /**
   * @@param product product associated with batch
   * @@param partner partner associated with batch
   * @@param price product's price
   * @@param stock product's stock
   * @@throws UnavailableFileException
   */
  public void registerNewBatch (Product product, Partner partner, float price, int stock){
    Batch batch = new Batch(product, partner, price, stock);

    product.addStock(stock);
    if (product.getMaxPrice() < price) { product.setMaxPrice(price); }

    partner.addBatch(batch);
    product.addBatch(batch);
  }

  /**
   * @param txtfile filename to be loaded.
   * @throws IOException
   * @throws BadEntryException
   * @throws DuplicatePartnerException
   * @throws NoSuchPartnerException
   * @throws NoSuchProductException
   */
  public void importFile (String txtfile) throws
          IOException, BadEntryException, DuplicatePartnerException, NoSuchPartnerException, NoSuchProductException {
    BufferedReader in = new BufferedReader(new FileReader(txtfile));
    String s;
    while ((s = in.readLine()) != null) {
      String line = new String(s.getBytes(), "UTF-8");
      String[] fields = line.split("\\|");

      switch (fields[0]) {
        case "PARTNER" -> {
          registerNewPartner(fields[1], fields[2], fields[3]);
        }

        case "BATCH_S" -> {
          String id = fields[1];
          String partnerId = fields[2];
          float price = Float.parseFloat(fields[3]);
          int stock = Integer.parseInt(fields[4]);

          ProductSimple product = registerProductSimple(id, price, stock);
          Partner partner = lookupPartner(partnerId);

          registerNewBatch(product, partner, price, stock);



        }
        case "BATCH_M" -> {
          String id = fields[1];
          String partnerId = fields[2];
          float price = Float.parseFloat(fields[3]);
          int stock = Integer.parseInt(fields[4]);
          float multiplier = Float.parseFloat(fields[5]);

          String[] recipeStrings = fields[6].split("#");
          Recipe recipe = new Recipe();

          for (int i = 0; i < recipeStrings.length; i++) {
            String[] ss = recipeStrings[i].split(":");
            recipe.addProduct(lookupProduct(ss[0]), Integer.parseInt(ss[1]));
          }

          ProductDerivative product = registerProductDerivative(id, recipe, multiplier, price, stock);
          Partner partner = lookupPartner(partnerId);
          registerNewBatch((Product) product, partner, price, stock);
        }
        default -> throw new BadEntryException(fields[0]);
      }
    }
  }
}

