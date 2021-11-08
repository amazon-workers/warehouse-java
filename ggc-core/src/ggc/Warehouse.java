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

  // TODO - javadocs
  private double _availableBalance = 0;
  private double _contabilisticBalance = 0;
  private int _date = 0;
  private Set<Product> _products = new TreeSet<Product>();
  private Map<String, Product> _productLookup = new TreeMap<String, Product>(String.CASE_INSENSITIVE_ORDER);
  private Set<Batch> _batches = new TreeSet<Batch>();
  private Map<Partner, TreeSet<Batch>> _batchesByPartner = new HashMap<Partner, TreeSet<Batch>>();
  private Map<Product, TreeSet<Batch>> _batchesByProduct = new HashMap<Product, TreeSet<Batch>>();
  private Map<String, Partner> _partnerLookup = new TreeMap<String, Partner>(String.CASE_INSENSITIVE_ORDER);
  private Set<Partner> _partners = new TreeSet<Partner>();

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
    return _contabilisticBalance;
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
    if (!_productLookup.containsKey(id)) { throw new NoSuchProductException(id); }
    return _productLookup.get(id);
  }

  /**
   * @@param id partner's id
   * @@return partner
   * @@throws NoSuchProductException
   */
  public Partner lookupPartner(String id) throws NoSuchPartnerException {
    if (!_partnerLookup.containsKey(id)) { throw new NoSuchPartnerException(id); }
    return _partnerLookup.get(id);
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
  public Set<Batch> listAllBatches() {
    return _batches;
  }

  /**
   * @@param partner partner whose batches are to be listed
   * @@return sorted list of partner's batches
   */
  public Set<Batch> listBatchesByPartner(Partner partner) {
    Set<Batch> batchList = _batchesByPartner.get(partner);
    return batchList;
  }

  /**
   * @@param product product which batches are to be listed
   * @@return sorted list of batches
   */
  public Set<Batch> listBatchesByProduct(Product product) {
    Set<Batch> batchList = _batchesByProduct.get(product);
    return batchList;
  }

  /**
   * @@param partner partner whose notifications are to be listed
   * @@return list of all selected partner's notifications
   */
  public Set<Notification> listPartnerNotifications(Partner partner) {
    return partner.listAllNotifications();
  }

  /**
   * @@return sorted list of all partners
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
      _batchesByPartner.put(newPartner, new TreeSet<Batch>());
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
    ProductSimple product;

    try {
      product = (ProductSimple) lookupProduct(id);
    } catch (NoSuchProductException e) {
      product = new ProductSimple(id);
      _productLookup.put(id, product);
      _batchesByProduct.put(product, new TreeSet<Batch>());
      _products.add(product);
    }

    product.addStock(stock);
    if (product.getMaxPrice() < price) { product.setMaxPrice(price); }

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
      _batchesByProduct.put(product, new TreeSet<Batch>());
      _products.add(product);
    }

    product.addStock(stock);
    if (product.getMaxPrice() < price) { product.setMaxPrice(price); }

    return product;
  }

  /**
   * @@param product product associated with batch
   * @@param partner partner associated with batch
   * @@param price product's price
   * @@param stock product's stock
   * @@throws UnavailableFileException
   */
  public void registerNewBatch(Product product, Partner partner, float price, int stock) {
    Batch batch = new Batch(product, partner, price, stock);

    _batches.add(batch);
    _batchesByPartner.get(partner).add(batch);
    _batchesByProduct.get(product).add(batch);
  }

  /**
   * @param txtfile filename to be loaded.
   * @throws IOException
   * @throws BadEntryException
   * @throws DuplicatePartnerException
   * @throws NoSuchPartnerExceprion
   * @throws NoSuchProductException
   */
  public void importFile(String txtfile) throws IOException, BadEntryException, DuplicatePartnerException, NoSuchPartnerException, NoSuchProductException {
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

          for(int i = 0; i < recipeStrings.length; i++) {
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
