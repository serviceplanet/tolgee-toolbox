package nl.serviceplanet.tolgee.toolbox.config.toml;

import com.google.common.collect.ImmutableSet;
import nl.serviceplanet.tolgee.toolbox.common.Constants;
import nl.serviceplanet.tolgee.toolbox.common.config.ConfigUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public final class ConfigUtilTest {

	@Test
	public void fileFinderTest() throws Exception {
		Path basePath = Path.of("src", "test", "mock-structure");

		ImmutableSet<Path> files = ConfigUtil.findConfigFiles(basePath, Path.of(Constants.TOOLBOX_FILENAME));
	}
}
