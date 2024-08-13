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
package nl.serviceplanet.tolgee.toolbox.cli;

import jakarta.inject.Inject;
import nl.serviceplanet.tolgee.toolbox.cli.dagger.ApplicationComponent;
import nl.serviceplanet.tolgee.toolbox.cli.dagger.DaggerApplicationComponent;
import nl.serviceplanet.tolgee.toolbox.cli.picocli.PicoCliDaggerFactory;
import nl.serviceplanet.tolgee.toolbox.cli.picocli.TolgeeToolboxCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * Main entry point for the Tolgee Toolbox command line application.
 */
public final class Main {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		// Ensure all uncaught exceptions end up in the logs and are not silently discarded when the thread exits
		// because of this exception.
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
		
		ApplicationComponent component = DaggerApplicationComponent.create();
		Main main = component.main();
		
		int exitCode = main.run(args);
		System.exit(exitCode);
	}
	
	private final PicoCliDaggerFactory picoCliDaggerFactory;
	
	@Inject
	public Main(PicoCliDaggerFactory picoCliDaggerFactory) {
		this.picoCliDaggerFactory = picoCliDaggerFactory;
	}
	
	private int run(String[] args) {
		int exitCode = new CommandLine(new TolgeeToolboxCommand(), picoCliDaggerFactory)
				.setUsageHelpAutoWidth(true)
				.execute(args);

		return exitCode;
	}

	/**
	 * Used to log uncaught exceptions. Exceptions should always be uncaught and not let threads just die. Letting
	 * threads die without logging the exception are silent killers which starve thread pools. Therefore every exception
	 * must be caught and at the very least be logged at trace level.
	 */
	private static final class UncaughtExceptionLogger implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread thread, Throwable e) {
			log.error("Thread '{}' with id {} died because of an uncaught exception. Threads must not die because " +
							"of uncaught exceptions. This is a bug and must be fixed.",
					thread.getName(), thread.threadId(), e);
		}
	}
}
