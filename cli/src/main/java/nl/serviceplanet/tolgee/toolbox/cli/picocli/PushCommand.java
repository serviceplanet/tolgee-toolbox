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
import nl.serviceplanet.tolgee.toolbox.common.services.api.PushService;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
		name = "push",
		description = "Push (upload) translations to Tolgee. In the Tolgee web application this is called 'Import'."
)
public final class PushCommand implements Runnable {
	
	private final PushService pushService;

	@CommandLine.Option(
			names = "--base-path",
			description = "Optional path to the project. If omitted the current working directory is used.")
	private Path basePathArg;
	
	@Inject
	public PushCommand(PushService pushService) {
		this.pushService = pushService;
	}
	
	@Override
	public void run() {
		Path basePath = basePathArg;
		if (basePath == null) {
			basePath = Path.of(System.getProperty("user.dir"));
		}

		try {
			// FIXME: Properly give feedback to user.
			pushService.pushMessages(basePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
