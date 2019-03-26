# CapBreaker

-----

**Redistributing WPA hash cracking.**

Management tool for WPA hash cracking with a distributable software including WebUI.

In order to crack a WPA hash you must upload a valid handshake in *.cap* format to the WebUI.
The agents software runs the password list in a distributed manner, more agents will result in a faster scan procedure.

For more information on capture handshake, see [here](https://www.kalitutorials.net/2014/06/hack-wpa-2-psk-capturing-handshake.html).

-----

.. contents:: :local:

Demo
========
https://capbreaker.herokuapp.com

Features
========
* List of loaded hashes and current state.
* Managing a list of loaded hashes and users.

Prerequisites
=============
* JDK 1.8.
* *postgresql* - Should config *datasource* in *application.properties*.
* Settings can be found in *application.properties* and recommended to be checked before use.

Deployment
==========
* Simply compile and run.

Authors
=======
* `ghsi10 <https://github.com/ghsi10>`_ - Lead developer
* `yevegnykuz <https://github.com/yevegnykuz>`_
* `TamirGit <https://github.com/TamirGit>`_

-----
