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
package nl.serviceplanet.tolgee.toolbox.common.services.api;

import nl.serviceplanet.tolgee.toolbox.common.config.api.Project;

import java.io.IOException;
import java.nio.file.Path;

public interface PullService {

	/**
	 * Pulls all message files of a specified project location from Tolgee. Meaning they are retrieved via the
	 * "Export" functionality in Tolgee and then written to local files.
	 */
	void pullMessages(Project project) throws IOException;

}
