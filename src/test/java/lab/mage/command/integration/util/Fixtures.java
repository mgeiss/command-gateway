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
package lab.mage.command.integration.util;

import lab.mage.command.integration.repository.JournalEntry;

public final class Fixtures {

  public static final JournalEntry SAMPLE_JOURNAL_ENTRY = new JournalEntry();

  static {
    Fixtures.SAMPLE_JOURNAL_ENTRY.setDebtor("08154711");
    Fixtures.SAMPLE_JOURNAL_ENTRY.setCreditor("08154712");
    Fixtures.SAMPLE_JOURNAL_ENTRY.setCurrencyCode("UGX");
    Fixtures.SAMPLE_JOURNAL_ENTRY.setAmount(100.00D);
  }

  private Fixtures() {
    super();
  }
}
