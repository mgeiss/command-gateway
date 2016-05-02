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
package lab.mage.command.integration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import lab.mage.command.config.EnableCommandProcessing;
import lab.mage.command.domain.CommandCallback;
import lab.mage.command.domain.CommandProcessingException;
import lab.mage.command.gateway.CommandGateway;
import lab.mage.command.integration.domain.CreateJournalEntryCommand;
import lab.mage.command.integration.domain.ErroneousJournalCommand;
import lab.mage.command.integration.repository.JournalEntry;
import lab.mage.command.integration.repository.JournalEntryKey;
import lab.mage.command.integration.util.Fixtures;
import lab.mage.command.repository.CommandSource;
import lab.mage.command.util.CommandConstants;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.cql.CqlIdentifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {
        CommandGatewayIntegrationTest.TestConfiguration.class
    }
)
@Component
public class CommandGatewayIntegrationTest {

  @Configuration
  @EnableCommandProcessing
  @ComponentScan(
      basePackages = {
          "lab.mage.command.integration.domain",
          "lab.mage.command.integration.repository"
      }
  )
  @EnableCassandraRepositories(
      basePackages = {
          "lab.mage.command.integration.repository"
      }
  )
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }
  }

  private static final String JOURNAL_ENTRY_TABLE_NAME = "journal_entries";

  @Autowired
  @Qualifier(CommandConstants.LOGGER_NAME)
  private Logger logger;

  @Autowired
  private CassandraAdminOperations cassandraAdminOperations;

  @Autowired
  private CassandraOperations cassandraOperations;

  @Autowired
  private CommandGateway commandGateway;

  public CommandGatewayIntegrationTest() {
    super();
  }

  @BeforeClass
  public static void setupEnvironment() throws Exception {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    final Cluster cluster = Cluster.builder()
        .addContactPoints(CommandConstants.CASSANDRA_CONTACT_POINTS_DEFAULT)
        .withPort(Integer.valueOf(CommandConstants.CASSANDRA_PORT_DEFAULT)).build();
    final Session session = cluster.connect();
    session.execute("CREATE KEYSPACE IF NOT EXISTS " +
        CommandConstants.CASSANDRA_KEYSPACE_DEFAULT +
        " WITH replication = {" +
        "'class': 'SimpleStrategy', 'replication_factor': '3'" +
        "};");
    session.close();
    cluster.close();
  }

  @AfterClass
  public static void tearDownEnvironment() throws Exception {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
  }

  @Before
  public void setupTest() throws Exception {
    this.cassandraAdminOperations.createTable(true, CqlIdentifier.cqlId(CommandConstants.COMMAND_SOURCE_TABLE_NAME),
        CommandSource.class, new HashMap<>());
    this.cassandraAdminOperations.createTable(true, CqlIdentifier.cqlId(CommandGatewayIntegrationTest.JOURNAL_ENTRY_TABLE_NAME),
        JournalEntry.class, new HashMap<>());
  }

  @After
  public void cleanUpTest() throws Exception {
    this.cassandraAdminOperations.dropTable(CqlIdentifier.cqlId(CommandConstants.COMMAND_SOURCE_TABLE_NAME));
    this.cassandraAdminOperations.dropTable(CqlIdentifier.cqlId(CommandGatewayIntegrationTest.JOURNAL_ENTRY_TABLE_NAME));
  }

  @Test
  public void shouldHandleCommandSynchronously() throws Exception {
    final CommandCallback<JournalEntryKey> callback =
        this.commandGateway.process(new CreateJournalEntryCommand(Fixtures.SAMPLE_JOURNAL_ENTRY), JournalEntryKey.class);
    final JournalEntryKey journalEntryKey = callback.get();
    this.logger.debug("Synchronous command processed!");
    Assert.assertNotNull(journalEntryKey);
    Assert.assertTrue(this.cassandraOperations.exists(JournalEntry.class, journalEntryKey));
  }

  @Test
  public void shouldHandleCommandAsynchronously() throws Exception {
    this.commandGateway.process(new CreateJournalEntryCommand(Fixtures.SAMPLE_JOURNAL_ENTRY));
    this.logger.debug("Asynchronous command processed!");
    Thread.sleep(500L);
    Assert.assertTrue(this.cassandraOperations.count(JournalEntry.class) == 1);
  }

  @Test(expected = CommandProcessingException.class)
  public void shouldFailUnknownCommand() throws Exception {
    final CommandCallback<Void> callback = this.commandGateway.process("unknown command", Void.TYPE);
    callback.get();
    Assert.fail();
  }

  @Test(expected = CommandProcessingException.class)
  public void shouldFailInternalException() throws Exception {
    final CommandCallback<Void> callback =
        this.commandGateway.process(new ErroneousJournalCommand(Fixtures.SAMPLE_JOURNAL_ENTRY), Void.TYPE);
    callback.get();
    Assert.fail();
  }
}
