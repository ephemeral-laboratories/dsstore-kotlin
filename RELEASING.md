Release Instructions
====================

This is still a somewhat manual process.

1. **Make sure you're building with JDK 11. Currently, this is not
   automatically ensured by the build.**

2. Update version number in `gradle.properties` to the next version
   number in the sequence. Consider [Semantic Versioning](https://semver.org/)
   when choosing the next version number.
3. Commit and push that change
4. Tag that commit with the same version number
5. Push that tag
6. Run the release tasks

    ```shell
    ./gradlew publishAllPublicationsToMavenRepository -PrealRelease=true
    ./gradlew publishPlugins
    ```

7. Go to Nexus and manually Close and Release the staging repo
8. Update version number to an appropriate next version number and put
   "-SNAPSHOT" back on the end.
9. Commit and push that change
