/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.services.chain;

import io.neow3j.types.Hash160;
import java.math.BigInteger;

/**
 * Represents a token holder with balance.
 */
public class TokenBalance {
  private final Hash160 hash;
  private final BigInteger totalSupply;
  private final Integer decimals;
  private final String symbol;
  private final BigInteger balance;

  /**
   * Creates a token data holder.
   *
   * @param hash        token hash.
   * @param totalSupply token total supply
   * @param decimals    token decimals
   * @param symbol      token symbol
   * @param balance     token balance
   */
  public TokenBalance(Hash160 hash, BigInteger totalSupply, Integer decimals, String symbol,
                      BigInteger balance) {
    this.hash = hash;
    this.totalSupply = totalSupply;
    this.decimals = decimals;
    this.symbol = symbol;
    this.balance = balance;
  }

  public Hash160 getHash() {
    return hash;
  }

  public BigInteger getTotalSupply() {
    return totalSupply;
  }

  public Integer getDecimals() {
    return decimals;
  }

  public String getSymbol() {
    return symbol;
  }

  public BigInteger getBalance() {
    return balance;
  }

  /**
   * Get the balance without decimals zeros.
   *
   * @return returns the balance without decimals zeros
   */
  public BigInteger getBalanceWithoutDecimals() {
    if (balance.equals(BigInteger.ZERO)) {
      return balance;
    }
    return balance.divide(BigInteger.valueOf((long) Math.pow(10, decimals)));
  }
}
