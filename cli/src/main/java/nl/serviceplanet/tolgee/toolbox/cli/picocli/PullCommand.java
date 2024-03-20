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

import nl.serviceplanet.tolgee.toolbox.common.services.api.PullService;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
		name = "pull",
		description = "Pull (download) translations from Tolgee. In the Tolgee web application this is called 'Export'."
)
public final class PullCommand implements Runnable {
	
	private final PullService pullService;

	@CommandLine.Option(
			names = "--base-path",
			description = "Optional path to the project. If omitted the current working directory is used.")
	private Path basePathArg;
	
	@Inject
	public PullCommand(PullService pullService) {
		this.pullService = pullService;
	}
	
	@Override
	public void run() {
		Path basePath = basePathArg;
		if (basePath == null) {
			basePath = Path.of(System.getProperty("user.dir"));
		}

		try {
			// FIXME: Properly give feedback to user.
			pullService.pullMessages(basePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
