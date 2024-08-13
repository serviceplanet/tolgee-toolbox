/*
 * Copyright © 2024 Service Planet Rotterdam B.V. (it@ask.serviceplanet.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.serviceplanet.tolgee.toolbox.cli.picocli;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import picocli.CommandLine;

/**
 * Bridge between picocli and Dagger.
 * 
 * @author Jasper Siepkes <siepkes@serviceplanet.nl>
 */
@Singleton
public final class PicoCliDaggerFactory implements CommandLine.IFactory {
	
	private final PullCommand pullCommand;
	
	private final PushCommand pushCommand;

	private final PushSingleStepCommand pushSingleStepCommand;
	
	@Inject
	public PicoCliDaggerFactory(PullCommand pullCommand, 
								PushCommand pushCommand,
								PushSingleStepCommand pushSingleStepCommand) {
		this.pullCommand = pullCommand;
		this.pushCommand = pushCommand;
		this.pushSingleStepCommand = pushSingleStepCommand;
	}

	@Override
	public <K> K create(Class<K> cls) throws Exception {
		if (cls.equals(PullCommand.class)) {
			return (K) pullCommand;
		} else if (cls.equals(PushCommand.class)) {
			return (K) pushCommand;
		} else if (cls.equals(PushSingleStepCommand.class)) {
			return (K) pushSingleStepCommand;
		} else {
			throw new IllegalArgumentException();
		}
	}
}
