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
package nl.serviceplanet.tolgee.toolbox.common.rest.api.json;

public final class ImportLanguage {

	private long id;

	private String name;

	private Long existingLanguageId;

	private String existingLanguageTag;

	private String existingLanguageAbbreviation;

	private String existingLanguageName;

	private String importFileName;

	private long importFileId;

	private int importFileIssueCount;

	private String namespace;

	private int totalCount;

	private int conflictCount;

	private int resolvedCount;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getExistingLanguageId() {
		return existingLanguageId;
	}

	public void setExistingLanguageId(Long existingLanguageId) {
		this.existingLanguageId = existingLanguageId;
	}

	public String getExistingLanguageTag() {
		return existingLanguageTag;
	}

	public void setExistingLanguageTag(String existingLanguageTag) {
		this.existingLanguageTag = existingLanguageTag;
	}

	public String getExistingLanguageAbbreviation() {
		return existingLanguageAbbreviation;
	}

	public void setExistingLanguageAbbreviation(String existingLanguageAbbreviation) {
		this.existingLanguageAbbreviation = existingLanguageAbbreviation;
	}

	public String getExistingLanguageName() {
		return existingLanguageName;
	}

	public void setExistingLanguageName(String existingLanguageName) {
		this.existingLanguageName = existingLanguageName;
	}

	public String getImportFileName() {
		return importFileName;
	}

	public void setImportFileName(String importFileName) {
		this.importFileName = importFileName;
	}

	public long getImportFileId() {
		return importFileId;
	}

	public void setImportFileId(long importFileId) {
		this.importFileId = importFileId;
	}

	public int getImportFileIssueCount() {
		return importFileIssueCount;
	}

	public void setImportFileIssueCount(int importFileIssueCount) {
		this.importFileIssueCount = importFileIssueCount;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getConflictCount() {
		return conflictCount;
	}

	public void setConflictCount(int conflictCount) {
		this.conflictCount = conflictCount;
	}

	public int getResolvedCount() {
		return resolvedCount;
	}

	public void setResolvedCount(int resolvedCount) {
		this.resolvedCount = resolvedCount;
	}
}
