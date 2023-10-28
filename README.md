# Olon Web Framework

This is a derivative work of Lift with minimal support and the only focus to keep
dependencies updated as most as possible including new web security
recommendations.

For documentation and samples, please refer to Lift since most of the code is
compatible, just change net.liftweb for the olon package.

Don't forget to change the Boot.scala file to the package bootstrap.olon and also ensure you set the servlet filter to olon.http.LiftFilter on the servlet container configuration.

Give it a check to examples for a reference.

## Credit

This framework is a derivative work of [Lift](https://liftweb.net/), a very big thank you to
the Lift author and all the collaborators.

Security recommendations are based on [MDN](https://developer.mozilla.org/en-US/docs/Web/Security), a big thank you to Mozilla for sharing such a great resources for all.

## Publish new releases

* Ensure the GPG key csaltos@talenteca.io is published at <https://keyserver.ubuntu.com/>, <https://keys.openpgp.org> and <https://pgp.mit.edu/> with a correct expiration date.

> Use the command `gpg --keyserver keys.openpgp.org --send-keys FFE4D7AF28EC0EA5C8338752B7D6E88FA79E18BD` to upload and update a new expiration date if required.

* Bump the version to an official release at the `version` value in the `build.sbt` file.

* Run `clean` and `test` on an SBT session

* If all tests are OK run `publishSigned` in the SBT session

* Login at <https://s01.oss.sonatype.org/> with the user csaltos@talenteca.io and under the menu item "Staging Profile" and click refresh to show the recent signed deployment.

* Check the signed deployment is OK and then select "Close".

* If the closed deployment is OK then select "Release" and check everything is OK.

> For more reference check the pages <https://central.sonatype.org/publish/release/> <https://central.sonatype.org/publish/requirements/> <https://central.sonatype.org/publish/requirements/gpg/>