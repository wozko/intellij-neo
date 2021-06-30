package org.neodapps.plugin.services.chain;

import java.util.Objects;

/**
 * A custom Nep17 balance with symbol.
 */
public class AssetBalance {
  private final String symbol;
  private final String address;
  private final int decimals;
  private final double balance;

  /**
   * A class represents asset balance of an address.
   *
   * @param symbol   asset symbol
   * @param address  asset address
   * @param decimals asset decimals
   * @param balance  asset balance
   */
  public AssetBalance(String symbol,
                      String address,
                      int decimals,
                      double balance) {
    this.symbol = symbol;
    this.address = address;
    this.decimals = decimals;
    this.balance = balance;
  }

  public String getSymbol() {
    return symbol;
  }

  public double getBalance() {
    return balance;
  }

  public int getDecimals() {
    return decimals;
  }

  public String getAddress() {
    return address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AssetBalance)) {
      return false;
    }
    AssetBalance that = (AssetBalance) o;
    return Objects.equals(symbol, that.symbol)
        && Objects.equals(balance, that.balance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbol, balance);
  }
}
