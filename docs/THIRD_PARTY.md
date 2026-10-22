# Dependencies and attribution

## Original game and artwork

Toghrul Hasanli individually developed the game and confirms that all game artwork, including the four PNG files under `src/main/resources/assets/`, is his original work. No downloaded or copied game assets have been identified or claimed. This statement does not claim ownership of Java, Maven, JUnit, or other external tools.

## Third-party components

The project declares no external runtime libraries. Swing, AWT, Java2D, and Java serialization are supplied by the user's installed JDK. The JDK and Maven are not bundled in the repository or game JAR.

| Component | Role | License / upstream reference |
| --- | --- | --- |
| JUnit Jupiter 5.10.0 (API, params, engine) and JUnit Platform 1.10.0 (commons, engine) | Test dependencies | [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl/epl-v20.html); [JUnit project](https://junit.org/junit5/) |
| OpenTest4J 1.3.0 | Transitive test dependency | [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0); [upstream](https://github.com/ota4j-team/opentest4j) |
| API Guardian 1.1.2 | Transitive test dependency | [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0); [upstream](https://github.com/apiguardian-team/apiguardian) |
| Apache Maven, Maven Compiler Plugin 3.13.0, Maven Surefire Plugin 3.2.5, and standard Maven lifecycle plugins | Build and test tooling | [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0); [Maven](https://maven.apache.org/) |

The test-library versions and license declarations were checked against their locally resolved Maven POM metadata. The package is an ordinary application JAR, not a shaded bundle of test or build dependencies.

## Publication scope

The planned source repository includes Maven dependency declarations and original project files. It excludes the Maven cache, dependency JARs, build tools, JDK binaries, and `target/` output. No third-party library source is vendored in this project. These notes acknowledge the external components without applying their licenses to the original game or artwork.

Keep upstream copyright, license, and NOTICE files intact if you later redistribute third-party components. In particular, review EPL 2.0 section 3 for distribution of EPL-covered software, and Apache 2.0 section 4 for license and notice requirements. This source-only dependency-reference upload does not include those external components; a future bundled distribution needs a separate review of its actual contents and upstream notices.

## Project license

No license for the original project has been selected. Public GitHub hosting and an open-source reuse license are separate choices. MIT is an option if the author wants broad reuse of his original code and artwork; no license has been added or granted automatically.
