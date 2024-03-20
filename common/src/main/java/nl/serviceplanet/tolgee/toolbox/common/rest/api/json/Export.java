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

import java.util.List;

public final class Export {

	private List<String> languages;
	private String format;
	private String structureDelimiter;
	private List<Integer> filterKeyId;
	private List<Integer> filterKeyIdNot;
	private String filterTag;
	private String filterKeyPrefix;
	private List<String> filterState;
	private List<String> filterNamespace;
	private boolean zip = false;
	private String messageFormat;
	private boolean supportArrays = false;

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getStructureDelimiter() {
		return structureDelimiter;
	}

	public void setStructureDelimiter(String structureDelimiter) {
		this.structureDelimiter = structureDelimiter;
	}

	public List<Integer> getFilterKeyId() {
		return filterKeyId;
	}

	public void setFilterKeyId(List<Integer> filterKeyId) {
		this.filterKeyId = filterKeyId;
	}

	public List<Integer> getFilterKeyIdNot() {
		return filterKeyIdNot;
	}

	public void setFilterKeyIdNot(List<Integer> filterKeyIdNot) {
		this.filterKeyIdNot = filterKeyIdNot;
	}

	public String getFilterTag() {
		return filterTag;
	}

	public void setFilterTag(String filterTag) {
		this.filterTag = filterTag;
	}

	public String getFilterKeyPrefix() {
		return filterKeyPrefix;
	}

	public void setFilterKeyPrefix(String filterKeyPrefix) {
		this.filterKeyPrefix = filterKeyPrefix;
	}

	public List<String> getFilterState() {
		return filterState;
	}

	public void setFilterState(List<String> filterState) {
		this.filterState = filterState;
	}

	public List<String> getFilterNamespace() {
		return filterNamespace;
	}

	public void setFilterNamespace(List<String> filterNamespace) {
		this.filterNamespace = filterNamespace;
	}

	public boolean isZip() {
		return zip;
	}

	public void setZip(boolean zip) {
		this.zip = zip;
	}

	public String getMessageFormat() {
		return messageFormat;
	}

	public void setMessageFormat(String messageFormat) {
		this.messageFormat = messageFormat;
	}

	public boolean isSupportArrays() {
		return supportArrays;
	}

	public void setSupportArrays(boolean supportArrays) {
		this.supportArrays = supportArrays;
	}
}
