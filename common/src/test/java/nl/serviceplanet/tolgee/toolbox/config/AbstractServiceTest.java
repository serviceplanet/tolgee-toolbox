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
package nl.serviceplanet.tolgee.toolbox.config;

import com.google.common.collect.ImmutableSet;
import nl.serviceplanet.tolgee.toolbox.common.config.AbstractConfigService;
import nl.serviceplanet.tolgee.toolbox.common.config.api.LocalePlaceholder;
import nl.serviceplanet.tolgee.toolbox.common.config.api.Project;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class AbstractServiceTest {
	
	@Test
	public void testLocalePlaceholder() throws Exception {
		DummyConfigService placeholderParser = new DummyConfigService();

		ImmutableSet<LocalePlaceholder> actualResult = placeholderParser.parseLocalePlaceholder("foo-directory_${locale separator=dash, region_case=upper}/Messages.properties");

		Set<LocalePlaceholder> expectedResult = Set.of(
				new LocalePlaceholder("${locale separator=dash, region_case=upper}", 
						LocalePlaceholder.Case.Upper, 
						LocalePlaceholder.Separator.Dash)
		);

		assertThat(actualResult).containsExactlyElementsIn(expectedResult);
	}

	@Test
	public void testLocalePlaceholderArgumentsReversed() throws Exception {
		DummyConfigService placeholderParser = new DummyConfigService();

		ImmutableSet<LocalePlaceholder> actualResult = placeholderParser.parseLocalePlaceholder("foo-directory_${locale region_case=upper, separator=dash}/Messages.properties");

		Set<LocalePlaceholder> expectedResult = Set.of(
				new LocalePlaceholder("${locale region_case=upper, separator=dash}",
						LocalePlaceholder.Case.Upper,
						LocalePlaceholder.Separator.Dash)
		);

		assertThat(actualResult).containsExactlyElementsIn(expectedResult);
	}

	@Test
	public void testLocalePlaceholderArgumentMissing() throws Exception {
		DummyConfigService placeholderParser = new DummyConfigService();

		Exception exception = assertThrows(IllegalArgumentException.class, 
				() -> placeholderParser.parseLocalePlaceholder("foo-directory_${locale region_case=upper}/Messages.properties"));
	}
	
	private static class DummyConfigService extends AbstractConfigService {
		@Override
		public ImmutableSet<Project> loadProjects(Path basePath) {
			throw new IllegalStateException("Unimplemented");
		}
	}
}
