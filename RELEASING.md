Release Instructions
====================

This is still a somewhat manual process.

1. **Make sure you're building with JDK 11. Currently, this is not
   automatically ensured by the build.**

2. Update version number in `gradle.properties` to the next version
   number in the sequence. Consider [Semantic Versioning](https://semver.org/)
   when choosing the next version number.
3. Update `CHANGES.md` to show the same version number and today's date.
4. Commit and push those changes.
5. Tag that commit with the same version number
6. Push that tag
7. Run the release tasks

    ```shell
    ./gradlew publishAllPublicationsToMavenRepository -PrealRelease=true
    ./gradlew publishPlugins
    ```

8. Go to Nexus and manually Close and Release the staging repo
9. Update version number to an appropriate next version number and put
   "-SNAPSHOT" back on the end.
10. Commit and push that change
