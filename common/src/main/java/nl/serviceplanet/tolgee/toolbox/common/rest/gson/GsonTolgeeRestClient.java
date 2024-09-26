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
package nl.serviceplanet.tolgee.toolbox.common.rest.gson;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ConfigService;
import nl.serviceplanet.tolgee.toolbox.common.model.ExportMessageFormatType;
import nl.serviceplanet.tolgee.toolbox.common.model.ImportMessageFormatType;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeImportLanguage;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeNamespace;
import nl.serviceplanet.tolgee.toolbox.common.model.TolgeeProjectLanguage;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.TolgeeRestClient;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.TolgeeServerCommunicationException;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.TolgeeServerParseException;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.Export;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.ImportAddFilesResp;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.ImportLanguage;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.ImportListResp;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.ImportSelectNamespaceReq;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.ProjectLanguage;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.ProjectLanguagesResp;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.ProjectNamespace;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.ProjectNamespacesResp;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.json.SingleStepImportReq;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Tolgee REST API client based on GSON and Apache HTTP client.
 *
 * @author Jasper Siepkes <siepkes@serviceplanet.nl>
 */
@Singleton
public final class GsonTolgeeRestClient implements TolgeeRestClient {

	private static final Logger log = LoggerFactory.getLogger(GsonTolgeeRestClient.class);

	private final Gson gson = new Gson();

	@Inject
	public GsonTolgeeRestClient(ConfigService configService) {
	}

	private static final String HEADER_API_KEY = "X-API-Key";

	private static final String IMPORT = "/v2/projects/%s/import";
	private static final String SINGLE_STEP_IMPORT = "/v2/projects/%s/single-step-import";

	@Override
	public ImmutableSet<TolgeeImportLanguage> importAddFile(URI apiUri,
															char[] apiKey,
															long projectId,
															Path messageFile,
															String tolgeeMessageFileName) throws IOException {
		log.debug("Uploading import entry.");

		try (CloseableHttpClient httpClient = createHttpClient()) {
			URI fullApiUri = apiUri.resolve(String.format(IMPORT, projectId));
			HttpPost fileUploadPost = new HttpPost(fullApiUri);
			fileUploadPost.setHeader(HEADER_API_KEY, String.valueOf(apiKey));

			HttpEntity multipart = MultipartEntityBuilder.create()
					.addBinaryBody(
							"files",
							messageFile.toFile(),
							ContentType.TEXT_PLAIN,
							tolgeeMessageFileName)
					.build();

			// TODO: Add request JSON.

			fileUploadPost.setEntity(multipart);

			ImportAddFilesResp resp = executeRequestResponse(ImportAddFilesResp.class, httpClient,
					fileUploadPost, "project: " + projectId);

			ImmutableSet.Builder<TolgeeImportLanguage> importEntries = ImmutableSet.builder();
			if (resp.getResult() != null &&
					resp.getResult().getEmbedded() != null &&
					resp.getResult().getEmbedded().getLanguages() != null) {
				for (ImportLanguage importLanguage : resp.getResult().getEmbedded().getLanguages()) {
					importEntries.add(toTolgeeImportLanguage(importLanguage));
				}
			}

			return importEntries.build();
		}
	}

	@Override
	public void singleStepImport(URI apiUri,
								 char[] apiKey,
								 long projectId,
								 Path messageFile,
								 String tolgeeMessageFileName,
								 String namespace,
								 Locale locale,
								 ImportMessageFormatType formatType) throws IOException {
		log.debug("Uploading single-step-import entry.");

		try (CloseableHttpClient httpClient = createHttpClient()) {
			URI fullApiUri = apiUri.resolve(String.format(SINGLE_STEP_IMPORT, projectId));
			log.debug("Uploading single-step-import entry to URL: {}", fullApiUri);
			HttpPost fileUploadPost = new HttpPost(fullApiUri);
			fileUploadPost.setHeader(HEADER_API_KEY, String.valueOf(apiKey));

			SingleStepImportReq.FileMapping fileMapping = new SingleStepImportReq.FileMapping();
			fileMapping.setFileName(tolgeeMessageFileName);
			fileMapping.setNamespace(namespace);
			fileMapping.setLanguageTag(locale.toLanguageTag());
			fileMapping.setFormat(formatType);

			SingleStepImportReq.Params reqParams = new SingleStepImportReq.Params();
			reqParams.setForceMode(SingleStepImportReq.Params.ForceMode.OVERRIDE);
			reqParams.setOverrideKeyDescriptions(false);
			reqParams.setConvertPlaceholdersToIcu(formatType.name().endsWith("_ICU"));
			reqParams.setFileMappings(List.of(fileMapping));
			reqParams.setTagNewKeys(List.of());
			reqParams.setRemoveOtherKeys(false);

			String paramsJons = gson.toJson(reqParams);
			log.info("import-single-step request: {}", paramsJons);

			HttpEntity multipart = MultipartEntityBuilder.create()
					.addBinaryBody(
							"files",
							messageFile.toFile(),
							ContentType.TEXT_PLAIN,
							tolgeeMessageFileName
					)
					.addTextBody("params", paramsJons, ContentType.APPLICATION_JSON)
					.build();

			fileUploadPost.setEntity(multipart);

			String resp = executeRequestResponse(String.class, httpClient,
					fileUploadPost, "project: " + projectId);

			log.info("import-single-step response: {}", resp);
		}
	}

	private TolgeeImportLanguage toTolgeeImportLanguage(ImportLanguage importLanguage) {
		return new TolgeeImportLanguage(importLanguage.getId(),
				importLanguage.getImportFileName(),
				importLanguage.getImportFileId());
	}

	@Override
	public ImmutableSet<TolgeeProjectLanguage> importList(URI apiUri, char[] apiKey, long projectId) throws IOException {
		log.debug("Retrieving list of import entries.");

		ImmutableSet.Builder<TolgeeProjectLanguage> importEntries = ImmutableSet.builder();

		try (CloseableHttpClient httpClient = createHttpClient()) {
			URI fullApiUri = apiUri.resolve(String.format(IMPORT, projectId));
			HttpGet importGet = new HttpGet(fullApiUri);
			importGet.setHeader(HEADER_API_KEY, String.valueOf(apiKey));

			ImportListResp resp = executeRequestResponse(ImportListResp.class, httpClient,
					importGet, "project: " + projectId);
			for (ProjectLanguage projectLanguage : resp.getEmbedded().getLanguages()) {
				importEntries.add(toTolgeeProjectLanguage(projectLanguage));
			}
		}

		return importEntries.build();
	}

	private static final String IMPORT_SELECT_NAMESPACE = "/v2/projects/%s/import/result/files/%s/select-namespace";

	@Override
	public void importSelectNamespace(URI apiUri,
									  char[] apiKey,
									  long projectId,
									  long importFileId,
									  String namespace) throws IOException {
		log.debug("Setting namespaces on import entry.");

		try (CloseableHttpClient httpClient = createHttpClient()) {
			URI fullApiUri = apiUri.resolve(String.format(IMPORT_SELECT_NAMESPACE, projectId, importFileId));
			HttpPut importSelectNamespacePut = new HttpPut(fullApiUri);
			importSelectNamespacePut.setHeader(HEADER_API_KEY, String.valueOf(apiKey));

			ImportSelectNamespaceReq selectNamespace = new ImportSelectNamespaceReq();
			selectNamespace.setNamespace(namespace);
			HttpEntity stringEntity = new StringEntity(gson.toJson(selectNamespace), ContentType.APPLICATION_JSON);
			importSelectNamespacePut.setEntity(stringEntity);

			executeRequest(httpClient, importSelectNamespacePut, "importLanguageId: " + importFileId);
		}
	}

	private static final String IMPORT_SELECT_LANGUAGE = "/v2/projects/%s/import/result/languages/%s/select-existing/%s";

	@Override
	public void importSelectLanguage(URI apiUri,
									 char[] apiKey,
									 long projectId,
									 long importLanguageId,
									 long existingLanguageId) throws IOException {
		log.debug("Setting language on import entry.");

		try (CloseableHttpClient httpClient = createHttpClient()) {
			URI fullApiUri = apiUri.resolve(String.format(IMPORT_SELECT_LANGUAGE, projectId, importLanguageId, existingLanguageId));
			HttpPut httpPut = new HttpPut(fullApiUri);
			httpPut.setHeader(HEADER_API_KEY, String.valueOf(apiKey));

			executeRequest(httpClient, httpPut, "importLanguageId: " + importLanguageId);
		}
	}

	private static final String PROJECT_LOCALES = "/v2/projects/%s/languages";

	@Override
	public ImmutableSet<TolgeeProjectLanguage> projectLanguages(URI apiUri, char[] apiKey, long projectId) throws IOException {
		log.debug("Retrieving project languages.");

		ImmutableSet.Builder<TolgeeProjectLanguage> projectLocales = ImmutableSet.builder();

		try (CloseableHttpClient httpClient = createHttpClient()) {
			URI fullApiUri = apiUri.resolve(String.format(PROJECT_LOCALES, projectId));
			HttpGet projectLocalesGet = new HttpGet(fullApiUri);
			projectLocalesGet.setHeader(HEADER_API_KEY, String.valueOf(apiKey));

			ProjectLanguagesResp resp = executeRequestResponse(ProjectLanguagesResp.class, httpClient,
					projectLocalesGet, "project: " + projectId);
			for (ProjectLanguage projectLanguage : resp.getEmbedded().getLanguages()) {
				projectLocales.add(toTolgeeProjectLanguage(projectLanguage));
			}
		}

		return projectLocales.build();
	}

	private TolgeeProjectLanguage toTolgeeProjectLanguage(ProjectLanguage projectLanguage) {
		String tolgeeTag = projectLanguage.getTag();
		String[] tolgeeTagSplit = tolgeeTag.split("-");

		Locale locale;
		if (tolgeeTagSplit.length == 2) {
			locale = Locale.of(tolgeeTagSplit[0], tolgeeTagSplit[1]);
		} else if (tolgeeTagSplit.length == 1) {
			locale = Locale.of(tolgeeTagSplit[0]);
		} else {
			throw new IllegalStateException(String.format("Unknown language tag '%s' in Tolgee response.", tolgeeTag));
		}

		return new TolgeeProjectLanguage(projectLanguage.getId(), locale);
	}

	private static final String PROJECT_NAMESPACES = "/v2/projects/%s/used-namespaces";

	@Override
	public ImmutableSet<TolgeeNamespace> projectNamespaces(URI apiUri, char[] apiKey, long projectId) throws IOException {
		log.debug("Retrieving project namespaces.");

		ImmutableSet.Builder<TolgeeNamespace> projectNamespaces = ImmutableSet.builder();

		try (CloseableHttpClient httpClient = createHttpClient()) {
			URI fullApiUri = apiUri.resolve(String.format(PROJECT_NAMESPACES, projectId));
			HttpGet projectLocalesNamespaces = new HttpGet(fullApiUri);
			projectLocalesNamespaces.setHeader(HEADER_API_KEY, String.valueOf(apiKey));

			ProjectNamespacesResp resp = executeRequestResponse(ProjectNamespacesResp.class, httpClient,
					projectLocalesNamespaces, "project: " + projectId);
			for (ProjectNamespace projectNamespace : resp.getEmbedded().getNamespaces()) {
				projectNamespaces.add(toTolgeeNamespace(projectNamespace));
			}
		}

		return projectNamespaces.build();
	}

	private TolgeeNamespace toTolgeeNamespace(ProjectNamespace projectNamespace) {
		return new TolgeeNamespace(projectNamespace.getId(), projectNamespace.getName());
	}

	private static final String EXPORT = "/v2/projects/%s/export";

	@Override
	public void export(URI apiUri,
					   char[] apiKey,
					   long projectId,
					   Locale locale,
					   String namespace,
					   ExportMessageFormatType messageFormatType,
					   Path savePath) throws IOException {
		log.debug("Retrieving export project for id {}.", projectId);

		try (CloseableHttpClient httpClient = createHttpClient()) {
			URI fullApiUri = apiUri.resolve(String.format(EXPORT, projectId));
			HttpPost httpPost = new HttpPost(fullApiUri);
			httpPost.setHeader(HEADER_API_KEY, String.valueOf(apiKey));

			Export exportJson = new Export();
			exportJson.setLanguages(ImmutableList.of(localeToTolgeeTag(locale)));
			exportJson.setFormat(messageFormatType.toString());
			if (!Strings.isNullOrEmpty(namespace)) {
				exportJson.setFilterNamespace(ImmutableList.of(namespace));
			}

			HttpEntity stringEntity = new StringEntity(gson.toJson(exportJson), ContentType.APPLICATION_JSON);
			httpPost.setEntity(stringEntity);

			httpClient.execute(httpPost, response -> {
				HttpEntity entity = getEntity(response, "project: " + projectId);

				try (InputStream contentInputStream = entity.getContent();
					 OutputStream fileOutputStream = Files.newOutputStream(savePath)) {
					byte[] buffer = new byte[1024];
					for (int length; (length = contentInputStream.read(buffer)) != -1; ) {
						fileOutputStream.write(buffer, 0, length);
					}
				}

				return null;
			});
		}
	}

	private String localeToTolgeeTag(Locale locale) {
		StringBuilder tolgeeTag = new StringBuilder();

		tolgeeTag.append(locale.getLanguage().toLowerCase());
		if (!Strings.isNullOrEmpty(locale.getCountry())) {
			tolgeeTag.append("-");
			// In this place of the API the country needs to be in upper case.
			tolgeeTag.append(locale.getCountry().toUpperCase());
		}

		return tolgeeTag.toString();
	}

	private CloseableHttpClient createHttpClient() {
		return HttpClients.createDefault();
	}

	private String executeRequest(CloseableHttpClient httpClient,
								  ClassicHttpRequest httpRequest,
								  String reference) throws TolgeeServerCommunicationException, IOException {
		return httpClient.execute(httpRequest, response -> {
			HttpEntity entity = getEntity(response, reference);

			String responseString = EntityUtils.toString(entity);
			log.trace("Received response from Tolgee server: {}", responseString);

			return responseString;
		});
	}

	private <T> T executeRequestResponse(Class<T> responseType,
										 CloseableHttpClient httpClient,
										 ClassicHttpRequest httpRequest,
										 String reference) throws TolgeeServerCommunicationException, IOException {
		return httpClient.execute(httpRequest, response -> {
			HttpEntity entity = getEntity(response, reference);

			String responseString = EntityUtils.toString(entity);
			log.trace("Received response from Tolgee server: {}", responseString);

			if (responseType == null || responseType == Void.class) {
				return null;
			}

			if (responseType == String.class) {
				return (T) responseString;
			}

			try {
				return gson.fromJson(responseString, responseType);
			} catch (Exception e) {
				throw new TolgeeServerParseException(
						String.format("Unable to parse JSON response from the Tolgee server (%s).", reference), e);
			}
		});
	}

	private HttpEntity getEntity(ClassicHttpResponse response, String reference) {
		HttpEntity entity = response.getEntity();

		if (response.getCode() >= 200 && response.getCode() <= 299) {
			if (entity == null) {
				throw new TolgeeServerCommunicationException(
						String.format("Tolgee API response (HTTP code %s) did not include an response (%s).",
								response.getCode(), reference));
			}

			return entity;
		} else {
			if (entity != null) {
				String responseBody;
				try {
					responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
				} catch (Exception e) {
					responseBody = null;
				}

				throw new TolgeeServerCommunicationException(String.format(
						"Tolgee API responded request unsuccessful (HTTP code %s) (%s). Error details: %s",
						response.getCode(), reference, responseBody));
			}

			throw new TolgeeServerCommunicationException(String.format(
					"Tolgee API responded request unsuccessful (HTTP code %s) (%s).",
					response.getCode(), reference));
		}
	}
}
