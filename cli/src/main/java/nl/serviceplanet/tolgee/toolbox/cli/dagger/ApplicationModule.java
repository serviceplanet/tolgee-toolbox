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
package nl.serviceplanet.tolgee.toolbox.cli.dagger;

import dagger.Binds;
import dagger.Module;
import jakarta.inject.Singleton;
import nl.serviceplanet.tolgee.toolbox.common.config.api.ConfigService;
import nl.serviceplanet.tolgee.toolbox.common.config.toml.TomlConfigService;
import nl.serviceplanet.tolgee.toolbox.common.rest.api.TolgeeRestClient;
import nl.serviceplanet.tolgee.toolbox.common.rest.gson.GsonTolgeeRestClient;
import nl.serviceplanet.tolgee.toolbox.common.services.DefaultPullService;
import nl.serviceplanet.tolgee.toolbox.common.services.DefaultPushService;
import nl.serviceplanet.tolgee.toolbox.common.services.api.PullService;
import nl.serviceplanet.tolgee.toolbox.common.services.api.PushService;

/**
 * Tells Dagger which specific implementations we want to use for interfaces.
 *
 * @author Jasper Siepkes <siepkes@serviceplanet.nl>
 */
@Module
public abstract class ApplicationModule {
	
	@Binds
	@Singleton
	abstract TolgeeRestClient bindTolgeeRestClient(GsonTolgeeRestClient impl);
	
	@Binds
	@Singleton
	abstract ConfigService bindConfigService(TomlConfigService impl);
	
	@Binds
	@Singleton
	abstract PushService bindPushService(DefaultPushService impl);
	
	@Binds
	@Singleton
	abstract PullService bindPullService(DefaultPullService impl);
	
}
