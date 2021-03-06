package x1.stomp.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Quote implements Serializable {
  private static final long serialVersionUID = -6139640371442481033L;

  private Share share;
  private Float price;
  private String currency;

  public Quote() {
  }

  public Quote(Share share) {
    this.share = share;
  }

  public Share getShare() {
    return share;
  }

  public void setShare(Share share) {
    this.share = share;
  }

  public Float getPrice() {
    return price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public String toString() {
    return "Quote [share=" + share + ", price=" + price + ", currency=" + currency + "]";
  }

}
