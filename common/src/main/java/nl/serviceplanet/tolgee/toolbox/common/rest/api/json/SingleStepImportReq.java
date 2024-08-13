package nl.serviceplanet.tolgee.toolbox.common.rest.api.json;

import nl.serviceplanet.tolgee.toolbox.common.model.MessageFormatType;

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
		public enum Format {
			JSON_ICU, JSON_JAVA, JSON_PHP, JSON_RUBY, JSON_C, PO_PHP, PO_C, PO_JAVA, PO_ICU, PO_RUBY,
			STRINGS, STRINGSDICT, APPLE_XLIFF, PROPERTIES_ICU, PROPERTIES_JAVA, PROPERTIES_UNKNOWN,
			ANDROID_XML, FLUTTER_ARB, YAML_RUBY, YAML_JAVA, YAML_ICU, YAML_PHP, YAML_UNKNOWN, XLIFF_ICU,
			XLIFF_JAVA, XLIFF_PHP, XLIFF_RUBY;

			public static Format fromMessageFormatType(MessageFormatType formatType) {
				return switch (formatType) {
					case PROPERTIES_ICU -> Format.PROPERTIES_ICU;
					case PROPERTIES_JAVA -> Format.PROPERTIES_JAVA;
					default -> throw new UnsupportedOperationException("MessageFormatType: " + formatType);
				};
			}
		}

		private String fileName;
		private String namespace;
		private Format format;
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

		public Format getFormat() {
			return format;
		}

		public void setFormat(Format format) {
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
