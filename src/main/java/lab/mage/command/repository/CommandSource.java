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
package lab.mage.command.repository;

import lab.mage.command.util.CommandConstants;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = CommandConstants.COMMAND_SOURCE_TABLE_NAME)
public final class CommandSource {

  @PrimaryKey
  private CommandSourceKey commandSourceKey;
  @Column("command")
  private String command;
  @Column("processed")
  private Boolean processed;
  @Column("failed")
  private Boolean failed;
  @Column("failure_message")
  private String failureMessage;

  public CommandSource() {
    super();
  }

  public CommandSourceKey getCommandSourceKey() {
    return commandSourceKey;
  }

  public void setCommandSourceKey(CommandSourceKey commandSourceKey) {
    this.commandSourceKey = commandSourceKey;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public Boolean getProcessed() {
    return processed;
  }

  public void setProcessed(Boolean processed) {
    this.processed = processed;
  }

  public Boolean getFailed() {
    return failed;
  }

  public void setFailed(Boolean failed) {
    this.failed = failed;
  }

  public String getFailureMessage() {
    return failureMessage;
  }

  public void setFailureMessage(String failureMessage) {
    this.failureMessage = failureMessage;
  }
}
