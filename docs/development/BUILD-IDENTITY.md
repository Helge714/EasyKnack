# Build identity (developer reference)

`mvn package` (also `install` / `deploy`) generates
`META-INF/easyknack/build.properties` during `prepare-package`. GLSwing independently
generates `META-INF/glswing/build.properties`. Source versions remain in Maven.
Incremental IDE compilation and `mvn compile` do not allocate package identities.

When Eclipse is compiling the same checkout, use
`mvn -Deasyknack.buildDirectory=target/validation package` to isolate Maven's
classes and package output from Eclipse's `target/classes`. The default remains
`target`; this option does not modify tracked versions or IDE settings.

An isolated validation build does not update Eclipse's generated node registry.
After adding or removing `@RegisterNode` classes, also regenerate the default
`target/generated-sources/annotations` registry and compile it for the IDE launch.
Maven Update Project and Refresh alone do not run this annotation processor.
Check the registry used by `target/classes`, not only the isolated test output.

Properties include the Maven version/artifact, UTC build time, full Git commit,
dirty status (including untracked, non-ignored files), build kind and identifier.
Git absence, inaccessible repositories and source archives produce `unknown`
Git fields, never an assumed clean state.

Local IDs combine a UTC timestamp and random suffix; they are not sequential CI
numbers. GitHub Actions IDs include the workflow run number, run attempt and run
ID. This identifies a CI run, not proof of an approved release. Other CI systems
currently use a local-style ID. Published release versions/tags remain deliberate.

`BuildInfo.application()` and `BuildInfo.core()` expose packaged identities to
diagnostics. Class-directory launches explicitly show `IDE / unpackaged` rather
than old metadata left behind by a previous package build. Missing metadata in an
older dependency is reported as unavailable. The audio diagnostics CSV includes
both identities. Help → About EasyKnack shows both identities in a selectable text
view. Installer versions use the numeric Maven product version; CI release identity
and publication remain separate work.

The generator requires a JDK with Java source-file launching (17 or newer here).
It only writes into the build output. Each package invocation creates a fresh
identity, so the complete JAR is intentionally not byte-for-byte reproducible.
