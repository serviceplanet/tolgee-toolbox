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
package nl.serviceplanet.tolgee.toolbox.common.rest.api;

import com.google.common.collect.ImmutableSet;
import nl.serviceplanet.tolgee.toolbox.common.model.MessageFormatType;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeImportLanguage;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeNamespace;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeProjectLanguage;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Allows for interaction with the Tolgee REST API.
 *
 * Unless otherwise noted all methods are blocking.
 *
 * @author Jasper Siepkes <siepkes@serviceplanet.nl>
 */
public interface TolgeeRestClient {

	/**
	 * Uploads a file to the Tolgee import.
	 *
	 * Blocks the caller thread until completed.
	 *
	 * See: https://tolgee.io/api#tag/Import/operation/addFiles_1
	 * 
	 * @param messageFile The path to the actual message file we want to upload.
	 * @param tolgeeMessageFileName The file name used by Tolgee in the import screen.
	 */
	ImmutableSet<TolgeeImportLanguage> importAddFile(URI apiUri,
													 char[] apiKey,
													 long projectId,
													 Path messageFile,
													 String tolgeeMessageFileName) throws IOException;

	/**
	 * Lists all the entries in Tolgees importer for a project.
	 * 
	 * @param apiUri
	 * @param apiKey
	 * @param projectId
	 * @return
	 * @throws IOException
	 */
	ImmutableSet<TolgeeProjectLanguage> importList(URI apiUri, char[] apiKey, long projectId) throws IOException;

	/**
	 * Configures the namespace of an entry in the Tolgee importer.
	 *
	 * @param apiUri
	 * @param apiKey
	 * @param projectId
	 * @param importFileId
	 * @param namespace
	 * @throws IOException
	 */
	void importSelectNamespace(URI apiUri, 
							   char[] apiKey, 
							   long projectId,
							   long importFileId, 
							   String namespace) throws IOException;

	/**
	 * Configures the language of an entry in the Tolgee importer.
	 *
	 * @param apiUri
	 * @param apiKey
	 * @param projectId
	 * @param importLanguageId
	 * @param existingLanguageId
	 * @throws IOException
	 */
	void importSelectLanguage(URI apiUri,
							  char[] apiKey,
							  long projectId,
							  long importLanguageId,
							  long existingLanguageId) throws IOException;

	/**
	 * Returns all {@code Locale}'s a project uses.
	 *
	 * See: https://tolgee.io/api#tag/Languages/operation/getAll_8
	 */
	ImmutableSet<TolgeeProjectLanguage> projectLanguages(URI apiUri, char[] apiKey, long projectId) throws IOException;

	/**
	 * Returns all namespaces a project users.
	 */
	ImmutableSet<TolgeeNamespace> projectNamespaces(URI apiUri, char[] apiKey, long projectId) throws IOException;

	/**
	 * Retrieves a translation file with all translations. Uses Tolgee's export function.
	 */
	void export(URI apiUri,
				char[] apiKey,
				long projectId,
				Locale locale,
				String namespace,
				MessageFormatType messageFormatType,
				Path savePath) throws IOException;

}
