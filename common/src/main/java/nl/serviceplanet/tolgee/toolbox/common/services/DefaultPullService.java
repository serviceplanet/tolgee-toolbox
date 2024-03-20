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
package nl.serviceplanet.tolgee.toolbox.common.services;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ConfigService;
import nl.serviceplanet.tolgee.toolbox.common.config.api.LocalePlaceholder;
import nl.serviceplanet.tolgee.toolbox.common.config.api.Project;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ProjectFile;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeProjectLanguage;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeNamespace;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.TolgeeRestClient;
import nl.serviceplanet.tolgee.toolbox.common.services.api.PullService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Singleton
public final class DefaultPullService extends AbstractService implements PullService {

	private static final Logger log = LoggerFactory.getLogger(DefaultPullService.class);

	private final ConfigService configService;
	
	private final TolgeeRestClient tolgeeRestClient;

	@Inject
	public DefaultPullService(ConfigService configService, TolgeeRestClient tolgeeRestClient) {
		this.configService = configService;
		this.tolgeeRestClient = tolgeeRestClient;
	}

	@Override
	public void pullMessages(Path basePath) throws IOException {
		ImmutableCollection<Project> projects = configService.loadProjects(basePath);

		for (Project project : projects) {
			// Retrieve all languages and namespaces the project in Tolgee has.
			ImmutableSet<TolgeeProjectLanguage> tolgeeProjectLanguages = tolgeeRestClient.projectLanguages(project.tolgeeApiURI(),
					configService.getTolgeeApiKey(),
					project.tolgeeProjectId());
			ImmutableSet<TolgeeNamespace> tolgeeNamespaces = tolgeeRestClient.projectNamespaces(project.tolgeeApiURI(),
					configService.getTolgeeApiKey(),
					project.tolgeeProjectId());

			Optional<TolgeeNamespace> tolgeeNamespaceOpt = tolgeeNamespaces.stream()
					.filter(tolgeeNamespace -> tolgeeNamespace.name().equals(project.namespace()))
					.findFirst();

			if (project.missingNamespaceFail() && tolgeeNamespaceOpt.isEmpty()) {
				throw new IllegalStateException(
						String.format("Project with ID %s is missing a name space definition while namespace is configured as mandatory.",
						project.tolgeeProjectId()));
			}

			for (TolgeeProjectLanguage tolgeeProjectLanguage : tolgeeProjectLanguages) {
				for (ProjectFile targetProjectFile : project.projectTargets()) {
					if (targetProjectFile.excludedLocales().contains(tolgeeProjectLanguage.locale())) {
						// If the locale has been specified as to be excluded, skip it.
						continue;
					}

					StringBuilder targetFile = new StringBuilder(targetProjectFile.files().projectFileDefinition());
					for (LocalePlaceholder localePlaceholder : targetProjectFile.files().localePlaceholders()) {
						int index = targetFile.indexOf(localePlaceholder.placeholder());

						String localeString = localePlaceholder.convertToString(tolgeeProjectLanguage.locale());
						targetFile.replace(index, index + localePlaceholder.placeholder().length(), localeString);
					}

					Path messageFilePath = project.projectPath().resolve(targetFile.toString());

					log.trace("Downloading translations for project ID {} and namespace '{}' to: '{}'.",
							project.tolgeeProjectId(), project.namespace(), messageFilePath);
					tolgeeRestClient.export(project.tolgeeApiURI(),
							configService.getTolgeeApiKey(),
							project.tolgeeProjectId(),
							tolgeeProjectLanguage.locale(),
							project.namespace(),
							targetProjectFile.messageFormatType(),
							messageFilePath);
				}
			}
		}
	}
}
