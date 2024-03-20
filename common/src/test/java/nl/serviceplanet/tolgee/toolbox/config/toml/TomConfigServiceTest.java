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
package nl.serviceplanet.tolgee.toolbox.config.toml;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import nl.serviceplanet.tolgee.toolbox.common.config.api.LocalePlaceholder;
import nl.serviceplanet.tolgee.toolbox.common.config.api.Project;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ProjectFile;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ProjectFilesDefinition;
import nl.serviceplanet.tolgee.toolbox.common.config.toml.TomlConfigService;
import nl.serviceplanet.tolgee.toolbox.common.model.MessageFormatType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Jasper Siepkes <siepkes@serviceplanet.nl>
 */
public final class TomConfigServiceTest {

	/**
	 * Tests parsing a single configuration file.
	 */
	@Test
	public void testProject1Config() throws Exception {
		Path basePath = Path.of(System.getProperty("user.dir"),"src", "test", "mock-structure", "test-project-1");

		TomlConfigService tomConfigService = new TomlConfigService();
		ImmutableSet<Project> actualResult = tomConfigService.loadProjects(basePath);

		ProjectFilesDefinition projectSourceFiles = 
				new ProjectFilesDefinition("Messages.properties", ImmutableSet.of());
		ProjectFilesDefinition projectTargetFiles =
				new ProjectFilesDefinition("Messages.properties", ImmutableSet.of());

		ImmutableSet<Project> expectedResult = ImmutableSet.of(
				new Project(
						basePath,
						URI.create("https://127.0.0.1:8080"),
						true,
						"tolgee.server.backend",
						42341,
						ImmutableSet.of(new ProjectFile(projectSourceFiles,
								MessageFormatType.PROPERTIES,
								Locale.of("nl", "NL"),
								ImmutableSet.of())),
						ImmutableSet.of(new ProjectFile(projectTargetFiles,
								MessageFormatType.PROPERTIES,
								Locale.of("nl", "NL"),
								ImmutableSet.of())))
		);

		assertThat(actualResult).containsExactlyElementsIn(expectedResult);
	}

	/**
	 * Tests parsing multiple configuration files in a hierarchy.
	 */
//	@Test
//	public void testProject2Config() throws Exception {
//		Path basePath = Path.of(System.getProperty("user.dir"),"src", "test", "mock-structure", "test-project-2");
//
//		TomlConfigService tomConfigService = new TomlConfigService();
//		ImmutableSet<Project> actualResult = tomConfigService.loadProjects(basePath);
//
//		// Expected project A
//		ProjectFilesDefinition projectASourceFiles =
//				new ProjectFilesDefinition("Messages_${locale separator=underscore, region_case=upper}.properties", 
//						ImmutableSet.of(new LocalePlaceholder("${locale separator=underscore, region_case=upper}", 
//								LocalePlaceholder.Case.Upper, LocalePlaceholder.Separator.Underscore))
//				);
//		ProjectFilesDefinition projectATargetFiles =
//				new ProjectFilesDefinition("Messages_${locale separator=underscore, region_case=lower}.xlf",
//						ImmutableSet.of(new LocalePlaceholder("${locale separator=underscore, region_case=lower}",
//								LocalePlaceholder.Case.Upper, LocalePlaceholder.Separator.Underscore))
//				);
//		Project projectA = new Project(
//				basePath.resolve(Path.of("subdir-a-1", "subdir-a-2")),
//				URI.create("http://127.0.0.1:8080"),
//				false,
//				"tolgee.frontend",
//				14684,
//				ImmutableSet.of(new ProjectFile(projectASourceFiles, "property", null)),
//				ImmutableSet.of(new ProjectFile(projectATargetFiles, "xliff", null)));
//
//		// Expected project B
//		ProjectFilesDefinition projectBSourceFiles =
//				new ProjectFilesDefinition("Messages_${locale separator=underscore, region_case=upper}.properties",
//						ImmutableSet.of(new LocalePlaceholder("${locale separator=underscore, region_case=upper}",
//								LocalePlaceholder.Case.Upper, LocalePlaceholder.Separator.Underscore))
//				);
//		ProjectFilesDefinition projectBTargetFiles =
//				new ProjectFilesDefinition("Messages_${locale separator=underscore, region_case=lower}.xlf",
//						ImmutableSet.of(new LocalePlaceholder("${locale separator=underscore, region_case=lower}",
//								LocalePlaceholder.Case.Upper, LocalePlaceholder.Separator.Underscore))
//				);
//		Project projectB = new Project(
//				basePath.resolve(Path.of("subdir-b-1", "subdir-b-2", "subdir-b-3")),
//				URI.create("http://127.0.0.1:8080"),
//				true,
//				"server.core",
//				53235,
//				ImmutableSet.of(new ProjectFile(projectBSourceFiles, "property", null)),
//				ImmutableSet.of(new ProjectFile(projectBTargetFiles, "xliff", null)));
//
//		ImmutableSet<Project> expectedResult = ImmutableSet.of(projectB, projectA);
//
//		assertThat(actualResult).containsExactlyElementsIn(expectedResult);
//	}
}
