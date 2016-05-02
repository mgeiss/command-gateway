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
import lab.mage.command.annotation.Aggregate;
import lab.mage.command.annotation.CommandHandler;
import lab.mage.command.integration.repository.JournalEntry;
import lab.mage.command.integration.repository.JournalEntryKey;
import lab.mage.command.integration.repository.JournalEntryRepository;
import lab.mage.command.util.CommandConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Aggregate
public final class JournalEntryAggregate {

  public enum OperationHeader {

    CREATED("journal-entry-created");

    private final String name = "operation";
    private final String value;

    OperationHeader(final String value) {
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }

    public Message apply(final Message message) throws JMSException {
      message.setStringProperty(this.getName(), this.getValue());
      return message;
    }

    @Override
    public String toString() {
      return this.getValue();
    }
  }


  private final Gson gson;
  private final JournalEntryRepository journalEntryRepository;
  private final JmsTemplate jmsTemplate;

  @Autowired
  public JournalEntryAggregate(@Qualifier(CommandConstants.COMMAND_SERIALIZER) final Gson gson,
                               final JournalEntryRepository journalEntryRepository,
                               final JmsTemplate jmsTemplate) {
    super();
    this.gson = gson;
    this.journalEntryRepository = journalEntryRepository;
    this.jmsTemplate = jmsTemplate;
  }

  @CommandHandler
  public JournalEntryKey create(final CreateJournalEntryCommand createJournalEntryCommand) {
    final LocalDateTime now = LocalDateTime.now();

    final JournalEntryKey journalEntryKey = new JournalEntryKey();
    journalEntryKey.setBucket(now.format(DateTimeFormatter.ISO_LOCAL_DATE));
    journalEntryKey.setCreatedOn(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));

    final JournalEntry journalEntry = createJournalEntryCommand.journalEntry();
    journalEntry.setJournalEntryKey(journalEntryKey);

    this.journalEntryRepository.save(journalEntry);

    this.jmsTemplate.convertAndSend(gson.toJson(journalEntryKey), OperationHeader.CREATED::apply);

    return journalEntryKey;
  }

  @CommandHandler
  public void fail(final ErroneousJournalCommand erroneousJournalCommand) {
    throw new IllegalArgumentException("I'm broken!");
  }
}
