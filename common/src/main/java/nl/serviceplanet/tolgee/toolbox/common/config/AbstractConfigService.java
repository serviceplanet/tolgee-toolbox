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
package nl.serviceplanet.tolgee.toolbox.common.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ConfigService;
import nl.serviceplanet.tolgee.toolbox.common.config.api.LocalePlaceholder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains basic implementation of a config service which all config services usually share.
 *
 * @author Jasper Siepkes <siepkes@serviceplanet.nl>
 */
public abstract class AbstractConfigService implements ConfigService {

	private static final String TOLGEE_API_KEY_ENV = "TOLGEE_TOOLBOX_API_KEY";

	@Override
	public char[] getTolgeeApiKey() {
		String tolgeeApikey = System.getenv(TOLGEE_API_KEY_ENV);
		if (Strings.isNullOrEmpty(tolgeeApikey)) {
			throw new IllegalStateException(String.format("Environmental variable '%s' must contain the Tolgee API key.",
					TOLGEE_API_KEY_ENV));
		}

		return tolgeeApikey.toCharArray();
	}

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{locale(?:\\s+([^}]+))?\\}");

	public ImmutableSet<LocalePlaceholder> parseLocalePlaceholder(String text) {
		ImmutableSet.Builder<LocalePlaceholder> placeholders = ImmutableSet.builder();
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);

		while (matcher.find()) {
			String placeholderText = matcher.group(0);
			String argsString = matcher.group(1);
			if (argsString == null || !argsString.contains("separator=") || !argsString.contains("region_case=")) {
				throw new IllegalArgumentException("Missing required placeholder arguments in: " + placeholderText);
			}

			String separatorArg = null, regionCaseArg = null;
			for (String arg : argsString.split(",\\s*")) {
				if (arg.startsWith("separator=")) {
					separatorArg = arg.substring("separator=".length());
				} else if (arg.startsWith("region_case=")) {
					regionCaseArg = arg.substring("region_case=".length());
				}
			}

			if (separatorArg == null) {
				throw new IllegalArgumentException(String.format("Missing requires placeholder 'separator' in placeholder: '%s'", placeholderText));
			}
			if (regionCaseArg == null) {
				throw new IllegalArgumentException(String.format("Missing requires placeholder 'region_case' in placeholder: '%s'", placeholderText));
			}

			LocalePlaceholder.Separator separator = parseSeparator(separatorArg);
			LocalePlaceholder.Case regionCase = parseCase(regionCaseArg);

			placeholders.add(new LocalePlaceholder(placeholderText, regionCase, separator));
		}

		return placeholders.build();
	}

	private LocalePlaceholder.Separator parseSeparator(String separatorArg) {
		return switch (separatorArg.toLowerCase()) {
			case "underscore" -> LocalePlaceholder.Separator.Underscore;
			case "dash" -> LocalePlaceholder.Separator.Dash;
			default -> throw new IllegalArgumentException(String.format("Invalid 'separator' argument: '%s'.", separatorArg));
		};
	}

	private LocalePlaceholder.Case parseCase(String caseArg) {
		return switch (caseArg.toLowerCase()) {
			case "lower" -> LocalePlaceholder.Case.Lower;
			case "upper" -> LocalePlaceholder.Case.Upper;
			default -> throw new IllegalArgumentException(String.format("Invalid 'region_case' argument: '%s'.", caseArg));
		};
	}

}
