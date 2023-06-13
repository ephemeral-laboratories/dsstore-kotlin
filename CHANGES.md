
**Version 0.0.8 (13 June 2023)**

- Changed some native params from `ByteArray` to `String`
  to fix unterminated string issues.
- Check for file existence up-front in native code, to get
  a more useful error when that is the case.

**Version 0.0.7 (12 May 2023)**

- Changed `statfs` call to `statfs64`, as newer macOS was
  returning a different structure for the former which did
  not match the docs.

**Version 0.0.6 (9 May 2023)**

- Extracted a public interface for running the generator
  outside of a Gradle task. Gradle doesn't let you create
  tasks programmatically.

**Version 0.0.5 (31 Mar 2023)**

- Initial release
