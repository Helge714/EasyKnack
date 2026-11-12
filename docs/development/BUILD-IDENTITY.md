# Build identity (developer reference)

## Version baseline

The first EasyKnack release is frozen as `0.1.0` on 2026-11-12. It covers the
accumulated features and intentional pre-release compatibility changes recorded
in the release notes. GLSwing remains independently versioned at `0.2.0-SNAPSHOT`;
the package pins and includes its exact reviewed runtime and corresponding sources.
After freezing this release, ordinary development continues at `0.1.1-SNAPSHOT`.
The release is manually built with an explicitly local identity; official release
CI remains future work. Publication status is recorded in the release report.

Assess subsequent changes against the selected release scope: compatible fixes
require patch releases, compatible features minor releases, and breaking changes
minor releases during 0.x development. Changes already covered by the current
planned version do not trigger another increment per edit or build. Release tags,
publication and removal of SNAPSHOT remain deliberate release steps.

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
diagnostics. Class-directory launches read the product version from their owning checkout's
Maven POM and show `<version> — IDE / unpackaged`. This lookup is anchored to the
loaded class directory, including isolated validation output; it never uses the
process working directory or old package metadata. A missing version is reported
as `unknown`. IDE launches do not allocate build IDs. Missing metadata in an
older dependency is reported as unavailable. The audio diagnostics CSV includes
both identities. Help → About EasyKnack shows both identities in a selectable text
view. Installer versions use the numeric Maven product version; CI release identity
and publication remain separate work.

The generator requires a JDK with Java source-file launching (17 or newer here).
It only writes into the build output. Each package invocation creates a fresh
identity, so the complete JAR is intentionally not byte-for-byte reproducible.
