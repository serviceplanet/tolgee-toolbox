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

import com.google.common.collect.ImmutableSet;
import nl.serviceplanet.tolgee.toolbox.common.config.api.LocalePlaceholder;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ProjectFile;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ProjectFilesDefinition;
import nl.serviceplanet.tolgee.toolbox.common.model.MessageFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractService {

	private final Logger log = LoggerFactory.getLogger(AbstractService.class);

	public AbstractService() {
	}

	protected ImmutableSet<MessageFile> findSourceMessageFiles(Path projectPath, Collection<ProjectFile> sourceFiles) throws IOException {
		ImmutableSet.Builder<MessageFile> messageFilesBuilder = ImmutableSet.builder();

		for (ProjectFile sourceFile : sourceFiles) {
			Pattern filePattern = projectFileToPattern(sourceFile.files());

			try (Stream<Path> fileStream = Files.walk(projectPath, 200)) {
				messageFilesBuilder.addAll(fileStream
						.filter(path -> filePattern.matcher(path.getFileName().toString()).matches())
						.map(path -> toMessageFile(path, sourceFile))
						.collect(ImmutableSet.toImmutableSet()));
			}
		}

		ImmutableSet<MessageFile> messageFiles = messageFilesBuilder.build();
		if (log.isDebugEnabled()) {
			String messageFileList = messageFiles.stream()
					.map(MessageFile::toString)
					.collect(Collectors.joining("\n"));

			log.trace("Found the following messages files:\n{}", messageFileList);
		}

		return messageFiles;
	}

	/**
	 * Generates a regex (i.e. a {@link Pattern}) to match message files for the specified
	 * {@link ProjectFilesDefinition}.
	 */
	private Pattern projectFileToPattern(ProjectFilesDefinition projectFilesDefinition) {
		// Everything between \Q and \E is escaped in a regex. We don't want the dots, etc. in the path to behave
		// like a regex wildcard.
		StringBuilder fullRegex = new StringBuilder("\\Q");
		fullRegex.append(projectFilesDefinition.projectFileDefinition());

		for (LocalePlaceholder localePlaceholder : projectFilesDefinition.localePlaceholders()) {
			String regex = "\\E" + localePlaceholderToRegex(localePlaceholder) + "\\Q";
			int index = fullRegex.indexOf(localePlaceholder.placeholder());
			if (index < 0) {
				throw new IllegalStateException(String.format("Can't find '%s' in '%s'.",
						localePlaceholder.placeholder(), fullRegex));
			}

			fullRegex.replace(index, index + localePlaceholder.placeholder().length(), regex);
		}
		fullRegex.append("\\E");

		log.trace("Generated regex '{}' for location '{}'.", fullRegex, projectFilesDefinition.projectFileDefinition());

		return Pattern.compile(fullRegex.toString());
	}

	private static final String REGEX_SEP_DASH = "-";

	private static final String REGEX_SEP_UNDERSCORE = "_";

	private static final String REGEX_COUNTRY_LOWER = "([a-z]{2})";

	private static final String REGEX_COUNTRY_UPPER = "([A-Z]{2})";

	/**
	 * Primarily extracts the Locale from {@link Path} based on the placeholders defined in the
	 * specified {@link ProjectFile}.
	 */
	private MessageFile toMessageFile(Path path, ProjectFile sourceFile) {
		if (sourceFile.files().localePlaceholders().isEmpty()) {
			// If there are no placeholders, use the locale which was specified in the configuration.
			return new MessageFile(path, sourceFile.locale(), sourceFile.messageFormatType());
		}

		// Try to match a full locale definition such as 'en_US' in the filename.
		LocalePlaceholder localePlaceholder = sourceFile.files().localePlaceholders().iterator().next();

		// TODO: If you use multiple locale placeholders in the filename, for example "foo_en-US-bar_en-US.xliff"
		//  this doesn't work.
		int placeholderIndex = sourceFile.files().projectFileDefinition().indexOf(localePlaceholder.placeholder());
		String regexStart = "\\Q" + sourceFile.files().projectFileDefinition().substring(0, placeholderIndex) + "\\E";

		// Example regex: Messages_([a-z]{2})(?:_([A-Z]{2}))?\.properties
		StringBuilder regexBuilder = new StringBuilder(regexStart);
		regexBuilder.append(REGEX_COUNTRY_LOWER);
		regexBuilder.append("(?:");
		switch (localePlaceholder.separator()) {
			case Dash -> regexBuilder.append(REGEX_SEP_DASH);
			case Underscore -> regexBuilder.append(REGEX_SEP_UNDERSCORE);
			case null -> throw new IllegalArgumentException("Separator must never be 'null'.");
			default -> throw new IllegalArgumentException(String.format("Unknown separator '%s'.",
					localePlaceholder.separator()));
		}
		switch (localePlaceholder.regionCase()) {
			case Lower -> regexBuilder.append(REGEX_COUNTRY_LOWER);
			case Upper -> regexBuilder.append(REGEX_COUNTRY_UPPER);
			case null -> throw new IllegalArgumentException("Region case must never be 'null'.");
			default -> throw new IllegalArgumentException(String.format("Unknown region case '%s'.",
					localePlaceholder.separator()));
		}
		regexBuilder.append(")?");

		Matcher matcher = Pattern.compile(regexBuilder.toString()).matcher(path.toString());
		if (matcher.find()) {
			Locale locale = Locale.of(matcher.group(1));

			if (matcher.group(2) != null) {
				locale = Locale.of(matcher.group(1), matcher.group(2));
			}

			return new MessageFile(path, locale, sourceFile.messageFormatType());
		} else {
			throw new IllegalStateException();
		}
	}

	private String localePlaceholderToRegex(LocalePlaceholder localePlaceholder) {
		StringBuilder regexBuilder = new StringBuilder();

		regexBuilder.append(REGEX_COUNTRY_LOWER);

		switch (localePlaceholder.separator()) {
			case Dash -> regexBuilder.append(REGEX_SEP_DASH);
			case Underscore -> regexBuilder.append(REGEX_SEP_UNDERSCORE);
			case null -> throw new IllegalArgumentException("Separator must never be 'null'.");
			default -> throw new IllegalArgumentException(String.format("Unknown separator '%s'.",
					localePlaceholder.separator()));
		}

		switch (localePlaceholder.regionCase()) {
			case Lower -> regexBuilder.append(REGEX_COUNTRY_LOWER);
			case Upper -> regexBuilder.append(REGEX_COUNTRY_UPPER);
			case null -> throw new IllegalArgumentException("Region case must never be 'null'.");
			default -> throw new IllegalArgumentException(String.format("Unknown region case '%s'.",
					localePlaceholder.separator()));
		}

		return regexBuilder.toString();
	}
}
