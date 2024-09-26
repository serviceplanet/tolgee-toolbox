package nl.serviceplanet.tolgee.toolbox.common.rest.api.json;

import nl.serviceplanet.tolgee.toolbox.common.model.ExportMessageFormatType;
import nl.serviceplanet.tolgee.toolbox.common.model.ImportMessageFormatType;

import java.util.List;

public final class SingleStepImportReq {
	public final static class Params {
		public enum ForceMode {
			OVERRIDE, KEEP, NO_FORCE
		}

		private ForceMode forceMode;
		private boolean overrideKeyDescriptions;
		private boolean convertPlaceholdersToIcu;
		private List<FileMapping> fileMappings;
		private List<String> tagNewKeys;
		private boolean removeOtherKeys;

		public boolean getConvertPlaceholdersToIcu() {
			return convertPlaceholdersToIcu;
		}

		public void setConvertPlaceholdersToIcu(boolean convertPlaceholdersToIcu) {
			this.convertPlaceholdersToIcu = convertPlaceholdersToIcu;
		}

		public ForceMode getForceMode() {
			return forceMode;
		}

		public void setForceMode(ForceMode forceMode) {
			this.forceMode = forceMode;
		}

		public boolean getOverrideKeyDescriptions() {
			return overrideKeyDescriptions;
		}

		public void setOverrideKeyDescriptions(boolean overrideKeyDescriptions) {
			this.overrideKeyDescriptions = overrideKeyDescriptions;
		}

		public List<FileMapping> getFileMappings() {
			return fileMappings;
		}

		public void setFileMappings(List<FileMapping> fileMappings) {
			this.fileMappings = fileMappings;
		}

		public List<String> getTagNewKeys() {
			return tagNewKeys;
		}

		public void setTagNewKeys(List<String>  tagNewKeys) {
			this.tagNewKeys = tagNewKeys;
		}

		public boolean isRemoveOtherKeys() {
			return removeOtherKeys;
		}

		public void setRemoveOtherKeys(boolean removeOtherKeys) {
			this.removeOtherKeys = removeOtherKeys;
		}
	}

	public final static class FileMapping {
		private String fileName;
		private String namespace;
		private ImportMessageFormatType format;
		private String languageTag;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getNamespace() {
			return namespace;
		}

		public void setNamespace(String namespace) {
			this.namespace = namespace;
		}

		public ImportMessageFormatType getFormat() {
			return format;
		}

		public void setFormat(ImportMessageFormatType format) {
			this.format = format;
		}

		public String getLanguageTag() {
			return languageTag;
		}

		public void setLanguageTag(String languageTag) {
			this.languageTag = languageTag;
		}
	}
}
