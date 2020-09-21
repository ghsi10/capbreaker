# CapBreaker

**Distributing WPA hash cracking.**

Management tool for WPA hash cracking with a distributable software including WebUI.

In order to crack a WPA hash you must upload a valid handshake in  _.cap_  format to the WebUI. The agents software runs the password list in a distributed manner, more agents will result in a faster scan procedure.

For more information on capture handshake, see  [here](https://www.kalitutorials.net/2014/06/hack-wpa-2-psk-capturing-handshake.html).

## Demo

[https://capbreaker.herokuapp.com](https://capbreaker.herokuapp.com/)

## Features

-   List of loaded hashes and current state.
-   Managing a list of loaded hashes and users.

## Prerequisites

-   JDK 1.8.
-   postgresql.
-   Requires python for agent users.

## Deployment

Compile and run with arguments:
- replace the {*parameter*} in the command below with the relevant paramerts.
-     java -jar --login.master.password={admin-password} --spring.datasource.url={postresql-url} --spring.datasource.username={postresql-username} --spring.datasource.password={postgresql-password} cabreaker-server.jar`

## Authors

-   [ghsi10](https://github.com/ghsi10)  - Lead developer
-   [Yevgenykuz](https://github.com/Yevgenykuz)
-   [TamirGit](https://github.com/TamirGit)

