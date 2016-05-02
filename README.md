# Command Gateway - tiny CQRS implementation

## Abstract
Based on Apache Cassandra and Apache ActiveMQ this implementation utilizes Spring to provide a tiny implementation to handle commands for CQRS pattern implementations.

## Usage
A running sample can be found at the test package, [CommandGaetwayIntegrationTest](https://github.com/mgeiss/command-gateway/blob/master/src/test/java/lab/mage/command/integration/CommandGatewayIntegrationTest.java) .

### Commands and CommandHandlers
Any POJO can be a command, no specialties.

Following the Domain Driven Design, a domain object is used to collect command handlers for a specific object. Such a class is annotated with @Aggregate and handling methods are marked with @CommandHandler. A handling method offers only one parameter, the command to handle, and can return any type.
 
    ...
    
    @Aggregate
    public final class JournalEntryAggregate {
    
    ...
    
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
    
    ...
    
      @CommandHandler
      public void fail(final ErroneousJournalCommand erroneousJournalCommand) {
        throw new IllegalArgumentException("I'm broken!");
      }
    }
    
    ...
    
### CommandGateway
To execute a command, simply call one of CommandGateway's process methods. All commands will be processed asynchronously. If a return type is specified, a CommandCallback is returned, to allow synchronous behavior.

    ...
    
    // asynchronous processing
    this.commandGateway.process(new CreateJournalEntryCommand(Fixtures.SAMPLE_JOURNAL_ENTRY));
    
    ...
    
    // processing with synchronous behavior
    final CommandCallback<JournalEntryKey> callback =
          this.commandGateway.process(new CreateJournalEntryCommand(Fixtures.SAMPLE_JOURNAL_ENTRY), JournalEntryKey.class);
      final JournalEntryKey journalEntryKey = callback.get();
    
    ...

## Versioning
The version numbers follow the [Semantic Versioning](http://semver.org/) scheme.

In addition to MAJOR.MINOR.PATCH the following postfixes are used to indicate the development state.

* snapshot - A release currently in development. 
* m - A _milestone_ release include specific sets of functions and are released as soon as the functionality is complete.
* rc - A _release candidate_ is a version with potential to be a final product, considered _code complete_.
* ga - _General availability_ indicates that this release is the best available version and is recommended for all usage.

The versioning layout is {MAJOR}.{MINOR}.{PATCH}-{INDICATOR}[.{PATCH}]. Only milestones and release candidates can  have patch versions. Some examples:

1.2.3-snapshot  
1.3.5-m.1  
1.5.7-rc.2  
2.0.0-ga

## License
See [LICENSE](LICENSE) file.