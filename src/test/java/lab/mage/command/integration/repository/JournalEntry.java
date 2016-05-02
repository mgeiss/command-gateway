/*
 * Copyright 2016 Markus Geiss.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lab.mage.command.integration.repository;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table("journal_entries")
public final class JournalEntry {

  @PrimaryKey
  private JournalEntryKey journalEntryKey;
  @Column("debtor")
  private String debtor;
  @Column("creditor")
  private String creditor;
  @Column("currency_code")
  private String currencyCode;
  @Column("amount")
  private Double amount;

  public JournalEntry() {
    super();
  }

  public JournalEntryKey getJournalEntryKey() {
    return journalEntryKey;
  }

  public void setJournalEntryKey(JournalEntryKey journalEntryKey) {
    this.journalEntryKey = journalEntryKey;
  }

  public String getDebtor() {
    return debtor;
  }

  public void setDebtor(String debtor) {
    this.debtor = debtor;
  }

  public String getCreditor() {
    return creditor;
  }

  public void setCreditor(String creditor) {
    this.creditor = creditor;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }
}
