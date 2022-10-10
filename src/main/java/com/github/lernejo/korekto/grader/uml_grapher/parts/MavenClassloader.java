package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;
import org.apache.maven.cli.MavenExposer;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static com.github.lernejo.korekto.toolkit.misc.ThrowingFunction.sneaky;

@SubjectForToolkitInclusion
public class MavenClassloader {

    public static ClassLoader build(MavenExposer exposer, Exercise exercise) {
        Path mavenLocalRepoDirectory = exercise.getRoot().getParent().getParent().resolve(".m2");
        Path pomPath = exercise.getRoot().resolve("pom.xml");
        List<Dependency> dependencies = exposer.resolveEffectiveRuntimeDependencies(pomPath);
        Stream<Path> runtimeDependencies = dependencies
            .stream()
            .filter(d -> !"test".equals(d.getScope()))
            .map(d -> toPath(d))
            .map(mavenLocalRepoDirectory::resolve);
        List<URL> classPath = Stream.concat(
                runtimeDependencies,
                Stream.of(exercise.getRoot().resolve("target").resolve("classes"))
            )
            .map(p -> p.toUri())
            .map(sneaky(uri -> uri.toURL()))
            .toList();
        return new URLClassLoader(
            classPath.toArray(new URL[0]),
            MavenClassloader.class.getClassLoader().getParent()
        );
    }

    private static Path toPath(Dependency d) {
        Artifact artifact = d.getArtifact();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        return Paths.get(artifact.getGroupId().replace('.', File.separatorChar))
            .resolve(artifactId)
            .resolve(version)
            .resolve(artifactId + '-' + version + ".jar");
    }
}
