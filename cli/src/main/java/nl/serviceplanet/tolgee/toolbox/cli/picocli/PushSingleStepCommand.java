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
import nl.serviceplanet.tolgee.toolbox.common.config.api.ConfigService;
import nl.serviceplanet.tolgee.toolbox.common.config.api.Project;
import nl.serviceplanet.tolgee.toolbox.common.services.api.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
		name = "push-single-step",
		description = "Push (upload) translations to Tolgee. In the Tolgee web application this is called 'Import-Single-Step'."
)
public final class PushSingleStepCommand implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(PushSingleStepCommand.class);

	private final ConfigService configService;
	private final PushService pushService;

	@CommandLine.Option(
			names = "--base-path",
			description = "Optional path to the project. If omitted the current working directory is used.")
	private Path basePathArg;

	@Inject
	public PushSingleStepCommand(PushService pushService, ConfigService configService) {
		this.pushService = pushService;
		this.configService = configService;
	}
	
	@Override
	public void run() {
		Path basePath = basePathArg;
		if (basePath == null) {
			basePath = Path.of(System.getProperty("user.dir"));
		}

		try {
			for (Project project : configService.loadProjects(basePath)) {
				pushService.pushMessagesInSingleStep(project);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
