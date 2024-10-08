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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ConfigService;
import nl.serviceplanet.tolgee.toolbox.common.config.api.Project;
import nl.serviceplanet.tolgee.toolbox.common.model.MessageFile;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeImportLanguage;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeProjectLanguage;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.TolgeeRestClient;
import nl.serviceplanet.tolgee.toolbox.common.services.api.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Singleton
public final class DefaultPushService extends AbstractService implements PushService {

	private static final Logger log = LoggerFactory.getLogger(DefaultPushService.class);

	private final ConfigService configService;

	private final TolgeeRestClient tolgeeRestClient;

	@Inject
	public DefaultPushService(ConfigService configService, TolgeeRestClient tolgeeRestClient) {
		this.configService = configService;
		this.tolgeeRestClient = tolgeeRestClient;
	}

	@Override
	public void pushMessages(Project project) throws IOException {
		ImmutableSet<TolgeeProjectLanguage> tolgeeProjectLanguages = tolgeeRestClient.projectLanguages(
				project.tolgeeApiURI(),
				configService.getTolgeeApiKey(),
				project.tolgeeProjectId());

		if (project.missingNamespaceFail() && Strings.isNullOrEmpty(project.namespace())) {
			throw new IllegalStateException(
					String.format("Project with ID %s is missing namespace definition '%s' while namespace is configured as mandatory.",
							project.tolgeeProjectId(), project.namespace()));
		}

		ImmutableSet<MessageFile> messageFiles = findSourceMessageFiles(project.projectPath(), project.projectSources());
		if (messageFiles.isEmpty()) {
			throw new IllegalStateException("Source MessageFiles not found in: " + project.projectPath());
		}

		for (MessageFile messageFile : messageFiles) {
			// We prefix the filename with a UUID, so we can later find the "importFileID" of the file we just
			// uploaded.
			String generatedFileName = UUID.randomUUID() + "_" + messageFile.path().getFileName();

			ImmutableSet<TolgeeImportLanguage> importLanguages = tolgeeRestClient.importAddFile(
					project.tolgeeApiURI(),
					configService.getTolgeeApiKey(),
					project.tolgeeProjectId(),
					messageFile.path(),
					generatedFileName);

			if (!Strings.isNullOrEmpty(project.namespace())) {
				configureNamespace(project, messageFile.path(), importLanguages, generatedFileName);
			}

			configureLanguage(project, messageFile, importLanguages, generatedFileName, tolgeeProjectLanguages);
		}
	}

	@Override
	public void pushMessagesInSingleStep(Project project) throws IOException {
		ImmutableSet<MessageFile> messageFiles = findSourceMessageFiles(project.projectPath(), project.projectSources());
		if (messageFiles.isEmpty()) {
			throw new IllegalStateException("Source MessageFiles not found in: " + project.projectPath());
		}

		for (MessageFile messageFile : messageFiles) {
			if (project.missingNamespaceFail() && Strings.isNullOrEmpty(project.namespace())) {
				throw new IllegalStateException(
						String.format("Project with ID %s is missing namespace definition '%s' while namespace is configured as mandatory.",
								project.tolgeeProjectId(), project.namespace()));
			}

			// We prefix the filename with a UUID, so we can later find the "importFileID" of the file we just
			// uploaded. In the case of [single-step-import] - opposed to [import] - this doesn't seem to lead
			// to traceability in the UI but would be traceable in the tolgee-service database / log-files.
			String generatedFileName = UUID.randomUUID() + "_" + messageFile.path().getFileName();

			tolgeeRestClient.singleStepImport(
					project.tolgeeApiURI(),
					configService.getTolgeeApiKey(),
					project.tolgeeProjectId(),
					messageFile.path(),
					generatedFileName,
					project.namespace(),
					messageFile.locale(),
					messageFile.messageFormatType());
		}
	}

	private void configureLanguage(Project project,
								   MessageFile messageFile,
								   ImmutableSet<TolgeeImportLanguage> importLanguages,
								   String generatedFileName,
								   ImmutableSet<TolgeeProjectLanguage> tolgeeProjectLanguages) throws IOException {
		for (TolgeeImportLanguage importLanguage : importLanguages) {
			if (importLanguage.importFilename().equals(generatedFileName)) {
				long tolgeeLanguageId = -1;
				for (TolgeeProjectLanguage tolgeeProjectLanguage : tolgeeProjectLanguages) {
					if (tolgeeProjectLanguage.locale().equals(messageFile.locale())) {
						tolgeeLanguageId = tolgeeProjectLanguage.tolgeeId();
						break;
					}
				}
				if (tolgeeLanguageId == -1) {
					throw new IllegalStateException(String.format(
							"Could not find Tolgee language ID for locale '%s'.", messageFile.locale()));
				}

				tolgeeRestClient.importSelectLanguage(project.tolgeeApiURI(),
						configService.getTolgeeApiKey(),
						project.tolgeeProjectId(),
						importLanguage.id(),
						tolgeeLanguageId
				);
			}
		}
	}

	private void configureNamespace(Project project,
									Path messageFile,
									ImmutableSet<TolgeeImportLanguage> importLanguages,
									String generatedFileName) throws IOException {
		boolean namespaceConfigured = false;

		for (TolgeeImportLanguage importLanguage : importLanguages) {
			if (importLanguage.importFilename().equals(generatedFileName)) {
				tolgeeRestClient.importSelectNamespace(project.tolgeeApiURI(),
						configService.getTolgeeApiKey(),
						project.tolgeeProjectId(),
						importLanguage.importFileId(),
						project.namespace()
				);

				namespaceConfigured = true;
				break;
			}
		}

		if (!namespaceConfigured) {
			throw new IllegalStateException(String.format(
					"Unable to set namespace on Tolgee import for message file '%s'", messageFile));
		}
	}
}
