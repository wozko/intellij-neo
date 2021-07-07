/*
 *  Use of this source code is governed by the Apache 2.0 license that can be
 *  found in the LICENSE file.
 */

package org.neodapps.plugin.ui.details.contracts.invoke;

/**
 * Actions of an invoke item component.
 */
public interface InvokeItemAction {
  void markInvokeItemModified();

  void deleteInvokeItem();
}
