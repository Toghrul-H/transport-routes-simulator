# Validation and publication

## Validation — 22 October 2026

- Environment: Windows 11, Azul OpenJDK 25.0.1, Apache Maven 3.9.11; project compiled with release target 23.
- `mvn package`: **BUILD SUCCESS**, producing `target/TransportRoutesTransNetSim-1.0-SNAPSHOT.jar`.
- Existing JUnit suite: **59 tests, 0 failures, 0 errors, 0 skipped**. No test sources were changed.
- Packaged application launched with `java -cp target/TransportRoutesTransNetSim-1.0-SNAPSHOT.jar com.transportroutes.Main`; the Transport Tycoon window and minimap navigation were observed. A real initial-state screenshot is included.
- This launch smoke check does not establish complete gameplay or save/load correctness.
- Visual presentation follow-up: constructed an actual scenario through game controls with two stops, a garage, city blocks, a signal, a bus, and a cargo truck. Captured five portfolio screenshots and a 50-frame, 12.67-second GIF from the running application. Media is integrated with relative links in the README; the initial-state screenshot is retained.
- Authorship presentation reflects Toghrul Hasanli's explicit clarification that he individually designed and implemented the entire game. README and CV describe his personal software development work. No Git history, source attribution, or third-party notices have been rewritten.
- The README was parsed in GitHub-flavored Markdown mode locally: six image elements and four tables were generated. All referenced PNGs and all 50 GIF frames decoded successfully. Active relative documentation/media links resolve. A GitHub-hosted preview has not been checked because the repository has not been uploaded; an attempted local browser preview was unavailable.
- Local Markdown file/image links and the POM XML were checked successfully. The packaged JAR contains the entry-point class and all four resource PNGs.
- Ignore rules were verified using Git against a disposable repository under ignored `target/ignore-validation/`; local saves, build output, logs, and NetBeans actions were excluded. The actual project was not initialized as a Git repository.
- Initial offline packaging failed because the local Maven cache lacked JAR-plugin dependencies. Allowing Maven to download those dependencies resolved packaging; no gameplay changes were needed.
- Final upload review: 47 files selected by the current ignore rules, approximately 4.3 MB total. The only root entries are .gitignore, README.md, pom.xml, src/, and docs/. Local saves, build output, logs, temporary recording/preview files, and NetBeans actions are excluded. Active local links and every media frame decode successfully; a targeted secret-pattern scan found no matches. All 29 Java source/test files match the pre-media-review hashes. This documentation-only review did not rerun gameplay tests.
- Artwork provenance is confirmed by the author. Dependency license metadata and the packaged JAR contents were inspected: the application JAR contains only project classes, with no bundled third-party classes. Attribution notes are in THIRD_PARTY.md.

| Test class | Tests | Failures / errors / skipped |
| --- | ---: | --- |
| GameTest | 15 | 0 / 0 / 0 |
| EconomyManagerTest | 5 | 0 / 0 / 0 |
| TileTest | 14 | 0 / 0 / 0 |
| TileTypeTest | 3 | 0 / 0 / 0 |
| GarageTest | 4 | 0 / 0 / 0 |
| RouteTest | 7 | 0 / 0 / 0 |
| VehicleTest | 11 | 0 / 0 / 0 |

Detailed local test reports are in the ignored `target/surefire-reports/` directory. The local `build-validation.log` is also ignored.

## Local Windows tool setup

If Java 8 is selected by PATH and Maven is not found, the tools used on the validation machine were bundled with NetBeans. In PowerShell, the following changes apply only to that terminal session:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Apache NetBeans\jdk'
$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\Apache NetBeans\java\maven\bin;$env:Path"
java -version
mvn -version
mvn package
java -cp target/classes com.transportroutes.Main
```

These paths are machine-specific setup help, not paths embedded in the game. On other machines, use your own JDK 23+ and Maven installation.

## Review before making the repository public

- Check any applicable university publication requirements before release.
- Toghrul Hasanli confirms that all game artwork, including the four PNG assets, is his original work and was not downloaded or copied from the internet.
- No project LICENSE has been selected. Decide whether to grant reuse rights through a license; this is separate from making the repository public. Do not choose a license automatically during repository creation.
- Review [third-party dependency notes](THIRD_PARTY.md). The upload contains dependency declarations, not copies of external library binaries or source.
- Review the README and the [CV entry](PORTFOLIO.md) for the individual project presentation and accurate technical scope.
- Keep the included gameplay screenshots and GIF alongside the README so relative image links continue to resolve.
- `.gitignore` excludes local saves (`/saves/`, root `.dat` files and `.sav` files), build output, logs, common IDE metadata, local NetBeans actions, and common secret-file patterns. Existing files remain on disk.
- The initial source scan found no obvious plaintext credentials or personal absolute paths in game code. This is a limited review, not a security audit. Ignore patterns do not remove files already tracked by Git.

## Create the empty GitHub repository

1. Sign in to your personal account and open [New repository](https://github.com/new).
2. Select your personal account as Owner and enter `transport-routes-simulator` as the repository name.
3. Use this description: **Independently developed Java transport simulation with Swing, Java2D rendering, BFS routing, passenger and cargo mechanics, and save/load persistence.**
4. Select **Public**.
5. Leave automatic README, .gitignore, and license initialization disabled. These files are managed locally; a project license has not been selected.
6. Click **Create repository** and copy its HTTPS URL.

Follow GitHub's official guides for [creating a repository](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-new-repository) and [uploading an existing local project](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github).

## Correct repository root

Use the inner project folder, not its parent. On the preparation machine:

```powershell
Set-Location -LiteralPath 'C:\Users\Toghrul\Desktop\togrul\Universitet\software tech\team-6-master\team-6-master'
Get-ChildItem -Force
```

The GitHub root must contain:

```text
.gitignore
README.md
pom.xml
src/
docs/
```

Do not upload an enclosing `team-6-master/` folder or a ZIP of the project. The local folder name need not match the GitHub repository name.

## Git commands — supplied for review, not executed

Run these from the actual project root containing `pom.xml`. First review the contents locally:

```sh
git init -b main
git status --short --ignored
git check-ignore -v save.dat save1.dat savegame.dat saves/save1.dat target/classes/com/transportroutes/Main.class nbactions.xml
git add --dry-run .
```

After reviewing the files and resolving publication permissions, stage and inspect the exact snapshot:

```sh
git add .
git diff --cached --stat
git diff --cached
git status --short
```

Only when you decide to commit:

```sh
git commit -m "Prepare Transport Routes portfolio repository"
```

Create an empty GitHub repository named `transport-routes-simulator` without an autogenerated README, .gitignore, or license. Replace `YOUR_USERNAME` below with your GitHub username. Only execute the push after final review:

```sh
git remote add origin https://github.com/YOUR_USERNAME/transport-routes-simulator.git
git remote -v
git push -u origin main
```

`git add .` includes the source, test code, resources, documentation, all six PNG screenshots (including the earlier initial-state capture), and the gameplay GIF. Git applies .gitignore automatically. Do not use `git add -f`; browser uploads do not provide this same local ignore filtering.

No repository initialization, staging, commit, remote creation, or push was performed in the project by this preparation task. The commit and push commands above are for use only after explicit approval. Authenticate through Git's normal GitHub sign-in flow; never put credentials in project files or remote URLs.

## Verify after the approved upload

1. Confirm that README.md, pom.xml, src/, and docs/ are directly at the GitHub root.
2. Check that README images load and the GIF animates, including the gallery images at full size.
3. Confirm that target/, saves/, .dat files, logs, and nbactions.xml are absent.
4. Add the About description and topics from [PORTFOLIO.md](PORTFOLIO.md).
5. Copy the public URL, `https://github.com/YOUR_USERNAME/transport-routes-simulator`, and use it as the project link in your Europass CV. Do this after the public page is verified.
