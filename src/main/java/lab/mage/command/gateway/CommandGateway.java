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
package lab.mage.command.gateway;

import lab.mage.command.domain.CommandCallback;
import lab.mage.command.domain.CommandProcessingException;
import lab.mage.command.internal.CommandBus;
import lab.mage.command.util.CommandConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public final class CommandGateway {

  private final Logger logger;
  private final CommandBus commandBus;

  @Autowired
  public CommandGateway(@Qualifier(CommandConstants.LOGGER_NAME) final Logger logger,
                        final CommandBus commandBus) {
    super();
    this.logger = logger;
    this.commandBus = commandBus;
  }

  public <C> void process(final C command) {
    this.logger.debug("CommandGateway::process-async called for {}.", command.getClass().getSimpleName());
    this.commandBus.dispatch(command);
  }

  public <C, T> CommandCallback<T> process(final C command, Class<T> clazz) throws CommandProcessingException {
    this.logger.debug("CommandGateway::process-sync called for {}.", command.getClass().getSimpleName());
    return new CommandCallback<>(this.commandBus.dispatch(command, clazz));
  }
}
