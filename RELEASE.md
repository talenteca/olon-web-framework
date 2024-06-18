# Publish new releases

* Ensure the GPG key csaltos@talenteca.io is published at <https://keyserver.ubuntu.com/>, <https://keys.openpgp.org> and <https://pgp.mit.edu/> with a correct expiration date.

> Use the command `gpg --keyserver keys.openpgp.org --send-keys FFE4D7AF28EC0EA5C8338752B7D6E88FA79E18BD` to upload and update a new expiration date if required.

* Decrypt the file `.credentials.asc` into `.credentials` using GPG

* Replace the username and password at the file `.credentials` with a fresh user
  token from `https://s01.oss.sonatype.org/` signing in with
  the user csaltos@talenteca.io and then select "Profile" and choose
  "User Token" from the menu box.

* Bump the version to an official release at the `version` value in the `build.sbt` file.

> Only official versions are available to public release, do not use SNAPSHOT version
  for official releases.

* Run `clean` and `test` on an SBT session

* If all tests are OK run `publishSigned` in the SBT session

* Login at <https://s01.oss.sonatype.org/> with the user csaltos@talenteca.io and under the menu item "Staging Profile" and click refresh to show the recent signed deployment.

* Check the signed deployment is OK and then select "Close".

* If the closed deployment is OK then select "Release" and check everything is OK.

> **IMPORTANT:** Once a release is published there is no way back, Sonatype will always keep an official release, ensure the build and release is correct before publishing.

> For more reference check the pages <https://central.sonatype.org/publish/release/> <https://central.sonatype.org/publish/requirements/> <https://central.sonatype.org/publish/requirements/gpg/>