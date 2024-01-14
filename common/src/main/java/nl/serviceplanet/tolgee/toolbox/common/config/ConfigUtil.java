package nl.serviceplanet.tolgee.toolbox.common.config;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

public final class ConfigUtil {

	public static ImmutableSet<Path> findConfigFiles(Path basePath, Path filename) throws IOException {
		return Files.walk(basePath, 5000)
				.filter(path -> path.getFileName().equals(filename))
				.collect(ImmutableSet.toImmutableSet());
	}
}
