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
package nl.serviceplanet.tolgee.toolbox.common.model;

/**
 * Message format messageFormatType as defined by Tolgee.
 *
 * @author Jasper Siepkes <siepkes@serviceplanet.n>
 */
public enum MessageFormatType {
	JSON,
	PO,
	APPLE_STRINGS_STRINGSDICT,
	APPLE_XLIFF,
	ANDROID_XML,
	FLUTTER_ARB,
	PROPERTIES_ICU,
	PROPERTIES_JAVA,
	XLIFF;
}
