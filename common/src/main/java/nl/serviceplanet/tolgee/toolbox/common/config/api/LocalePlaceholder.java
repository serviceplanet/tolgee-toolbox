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
package nl.serviceplanet.tolgee.toolbox.common.config.api;

import java.util.Locale;

/**
 * Represents the locale placeholder used in configuration files. For example
 * {@code ${locale separator=underscore, region_case=lower}}.
 *
 * @author Jasper Siepkes <siepkes@serviceplanet.nl>
 */
public record LocalePlaceholder(String placeholder,
								Case regionCase,
								Separator separator) {

	public enum Case { Lower, Upper }

	public enum Separator { Underscore, Dash }

	/**
	 * @param placeholder The unmodified placeholder. For example {@code ${locale separator=underscore, region_case=lower}}.
	 * @param regionCase
	 * @param separator
	 */
	public LocalePlaceholder {
	}

	public String convertToString(Locale locale) {
		StringBuilder localeString = new StringBuilder();

		localeString.append(locale.getLanguage().toLowerCase());

		switch (separator) {
			case Dash -> localeString.append("-");
			case Underscore -> localeString.append("_");
		}

		switch (regionCase) {
			case Lower -> localeString.append(locale.getCountry().toLowerCase());
			case Upper -> localeString.append(locale.getCountry().toUpperCase());
		}

		return localeString.toString();
	}


}
