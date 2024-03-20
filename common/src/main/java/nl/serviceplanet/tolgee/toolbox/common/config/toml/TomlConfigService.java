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
package nl.serviceplanet.tolgee.toolbox.common.config.toml;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.serviceplanet.tolgee.toolbox.common.config.AbstractConfigService;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ConfigService;
import nl.serviceplanet.tolgee.toolbox.common.config.api.Project;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ProjectFile;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ProjectFilesDefinition;
import nl.serviceplanet.tolgee.toolbox.common.model.MessageFormatType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Jasper Siepkes <siepkes@serviceplanet.nl>
 */
@Singleton
public final class TomlConfigService extends AbstractConfigService implements ConfigService {

	private static final Logger log = LoggerFactory.getLogger(TomlConfigService.class);

	private static final String TOLGEE_TOOLBOX = "tolgee-toolbox.toml";
	
	@Inject
	public TomlConfigService() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ImmutableSet<Project> loadProjects(Path basePath) throws IOException {
		// Find all .tolgee-toolbox files under the base path.
		ImmutableList<Path> configFiles = Files.walk(basePath, 1000)
				.filter(path -> path.getFileName().toString().equals(TOLGEE_TOOLBOX))
				.collect(ImmutableList.toImmutableList());

		ImmutableSet.Builder<Project> projects = ImmutableSet.builder();

		for (Path configFile : configFiles) {
			List<Path> configFileHierarchy = new ArrayList<>();

			// Find all .tolgee-toolbox files which are in parent directories of this .tolgee-toolbox file.
			Path parentPath = configFile.getParent();
			while(parentPath != null) {
				Path parentTolgeeToolboxPath = parentPath.resolve(TOLGEE_TOOLBOX);
				if (configFiles.contains(parentTolgeeToolboxPath)) {
					configFileHierarchy.add(parentTolgeeToolboxPath);
				}

				if (parentPath.getParent().startsWith(basePath)) {
					parentPath = parentPath.getParent();
				} else {
					parentPath = null;
				}
			}

			projects.addAll(createProjects(configFileHierarchy));
		}

		return projects.build();
	}
	
	private static final String TOML_TOLGEE_API_URL = "tolgee.api.url";
	
	private static final String TOML_GENERAL_MISSING_NAME_SPACE_FAIL = "general.missing_namespace_fail";
	
	private static final String TOML_PROJECTS_TOLGEE_ID = "tolgee.id";
	
	private static final String TOML_PROJECTS_NAMESPACE = "tolgee.namespace";
	
	private static final String TOML_PROJECTS_SOURCES = "sources";

	private static final String TOML_PROJECTS_TARGETS = "targets";
	
	private static final String TOML_PROJECTS_SRC_TAR_FILES = "files";

	private static final String TOML_PROJECTS_SRC_TAR_TYPE = "type";

	private static final String TOML_PROJECTS_SRC_TAR_LOCALE = "locale";

	private static final String TOML_PROJECTS_EXCLUDED_LOCALES = "excluded_locales";
	
	/**
	 * Creates {@link Project} instances based on the supplied {@code configFileHierarchy}. Only the projects for the
	 * top level config in the hierarchy are created. The other config files are used to override configs such as
	 * the Tolgee API URL.
	 */
	private List<Project> createProjects(List<Path> configFileHierarchy) throws IOException {
		if (configFileHierarchy.isEmpty()) {
			return Collections.emptyList();
		}

		List<Project> projects = new ArrayList<>();

		URI tolgeeApiUrl = null;
		Boolean missingNamespaceFail = null;

		// Search for inheritable options.
		for (Path configFile : configFileHierarchy) {
			TomlParseResult result = Toml.parse(configFile);
			result.errors().forEach(error -> log.warn(error.toString()));

			if (tolgeeApiUrl == null) {
				String uri = result.getString(TOML_TOLGEE_API_URL);
				if (uri != null) {
					tolgeeApiUrl = URI.create(uri);
				}
			}
			if (missingNamespaceFail == null) {
				missingNamespaceFail = result.getBoolean(TOML_GENERAL_MISSING_NAME_SPACE_FAIL);
			}
		}
		if (missingNamespaceFail == null) {
			missingNamespaceFail = false;
		}

		if (tolgeeApiUrl == null) {
			throw new IllegalStateException(String.format("'%s' setting not found.", TOML_TOLGEE_API_URL));
		}

		// Fetch the projects from the top level .tolgee-toolbox file.
		Path topConfig = configFileHierarchy.getFirst();
		TomlParseResult result = Toml.parse(topConfig);
		result.errors().forEach(error -> log.warn("TOML parsing error: {}", error.toString()));
		
		TomlArray projectsArray = result.getArrayOrEmpty("projects");
		for (int x = 0; x < projectsArray.size(); x++) {
			TomlTable projectTable = projectsArray.getTable(x);

			Long projectId = projectTable.getLong(TOML_PROJECTS_TOLGEE_ID);
			if (projectId == null) {
				throw new IllegalArgumentException(String.format("Config file '%s' is missing '%s'.", 
						topConfig, TOML_PROJECTS_TOLGEE_ID));
			}
			
			String namespace = projectTable.getString(TOML_PROJECTS_NAMESPACE);

			projects.add(new Project(
					topConfig.getParent(),
					tolgeeApiUrl,
					missingNamespaceFail,
					namespace,
					projectId,
					toProjectFiles(projectTable, TOML_PROJECTS_SOURCES),
					toProjectFiles(projectTable, TOML_PROJECTS_TARGETS)));
		}

		return projects;
	}
	
	private ImmutableSet<ProjectFile> toProjectFiles(TomlTable tomlTable, String arrayName) {
		TomlArray sourcesArray = tomlTable.getArrayOrEmpty(arrayName);
		ImmutableSet.Builder<ProjectFile> projectSources = ImmutableSet.builderWithExpectedSize(sourcesArray.size());
		for (int y = 0; y < sourcesArray.size(); y++) {
			TomlTable sources = sourcesArray.getTable(y);
			
			String files = sources.getString(TOML_PROJECTS_SRC_TAR_FILES);
			MessageFormatType messageFormatType = MessageFormatType.valueOf(sources.getString(TOML_PROJECTS_SRC_TAR_TYPE));

			Locale locale = null;
			String localeString = sources.getString(TOML_PROJECTS_SRC_TAR_LOCALE);
			if (!Strings.isNullOrEmpty(localeString)) {
				locale = Locale.forLanguageTag(localeString);
			}

			ImmutableSet<Locale> excludedLocales =
					parseTomlLocaleArray(sources.getArrayOrEmpty(TOML_PROJECTS_EXCLUDED_LOCALES));

			ProjectFilesDefinition projectFilesDefinition = 
					new ProjectFilesDefinition(files, parseLocalePlaceholder(files));

			// FIXME: Check if combination is valid. Otherwise throw IllegalArgumentException.
			
			projectSources.add(new ProjectFile(projectFilesDefinition, messageFormatType, locale, excludedLocales));
		}
		
		return projectSources.build();		
	}

	/**
	 * Parses an TOML array with locales ({@code ["en-US", "nl-NL"]}) to a Set of Java {@link Locale} instances.
	 */
	private ImmutableSet<Locale> parseTomlLocaleArray(TomlArray tomlArray) {
		ImmutableSet.Builder<Locale> localesSet = ImmutableSet.builder();
		for (int z = 0; z < tomlArray.size(); z++) {
			String excludedLocaleString = tomlArray.getString(z);
			localesSet.add(Locale.forLanguageTag(excludedLocaleString));
		}

		return localesSet.build();
	}
}
