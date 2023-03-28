Release Instructions
====================

This is still a somewhat manual process.

1. Update version number in `build.gradle.kts` files to the next version
   number in the sequence. Consider [Semantic Versioning](https://semver.org/)
   when choosing the next version number.
2. Commit and push that change
3. Tag that commit with the same version number
4. Push that tag
5. Run the release tasks

    ```shell
    ./gradlew publishAllPublicationsToMavenRepository -PrealRelease=true
    ./gradlew publishPlugins
    ```

6. Go to Nexus and manually Close and Release the staging repo
7. Update version numbers to an appropriate next version number and put
   "-SNAPSHOT" back on the end.
8. Commit and push that change
