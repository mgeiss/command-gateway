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
package lab.mage.command.integration.domain;

import com.google.gson.Gson;
import lab.mage.command.integration.repository.JournalEntryKey;
import lab.mage.command.integration.repository.JournalEntryRepository;
import lab.mage.command.util.CommandConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public final class JournalEventListener {

  private final Logger logger;
  private final Gson gson;
  private final JournalEntryRepository journalEntryRepository;

  @Autowired
  public JournalEventListener(@Qualifier(CommandConstants.LOGGER_NAME) final Logger logger,
                              @Qualifier(CommandConstants.COMMAND_SERIALIZER) final Gson gson,
                              final JournalEntryRepository journalEntryRepository) {
    super();
    this.logger = logger;
    this.gson = gson;
    this.journalEntryRepository = journalEntryRepository;
  }

  @JmsListener(
      destination = "mage-command-v1",
      selector = "operation = 'journal-entry-created'"
  )
  public JournalEntryKey notify(final String payload) {
    final JournalEntryKey journalEntryKey = gson.fromJson(payload, JournalEntryKey.class);
    this.logger.debug("JournalEventListener::notify called!");
    Assert.notNull(journalEntryKey);
    Assert.isTrue(this.journalEntryRepository.exists(journalEntryKey));
    return journalEntryKey;
  }
}
