# Olon Web Framework

This is a derivative work of Lift with minimal support and the only focus to keep
dependencies updated as most as possible including new web security
recommendations.

For documentation and samples, please refer to Lift since most of the code is
compatible, just change net.liftweb for the olon package.

Don't forget to change the Boot.scala file to the package bootstrap.olon and also ensure you set the servlet filter to olon.http.LiftFilter on the servlet container configuration.

# Credit

This framework is a derivative work of [Lift](https://liftweb.net/), a very big thank you to
the Lift author and all the collaborators.

Security recommendations are based on [MDN](https://developer.mozilla.org/en-US/docs/Web/Security), a big thank you to Mozilla for sharing such a great resources for all.