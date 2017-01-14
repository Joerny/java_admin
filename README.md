# JavaAdmin

[![Build Status](https://app.snap-ci.com/Joerny/java_admin/branch/master/build_image)](https://app.snap-ci.com/Joerny/java_admin/branch/master)

JavaAdmin is a simple PoC for a CRUD GUI that is based on Spring boot and entities that are live in this application. This is not a complete, ready-to-use-in-production solution but a starting point for your own admin interface.

## Installation
The project is build with Maven.

```sh
mvn install
```

## Architecture

The application consists mainly of the ``JavaAdminController``, a corresponding command object (both in ``com.joerny.javaadmin.controller``) and the JSP pages. Additionally, you find in ``com.joerny.javaadmin.example`` some sample entities.

## Development setup

All necessary tools and libraries will be loaded via Maven. To run the test suite run the normal task:

```sh
mvn test
```

A simple [Spring Boot](https://projects.spring.io/spring-boot/) web application is included. It can be run by

```sh
mvn spring-boot:run
```

It runs under ``localhost:8080``. You can see the overview of the enities by calling ``localhost:8080/java-admin/overview``.

## Release History

* 0.2
    * Added support for entities as child elements
    * Views with menu
    * Introduction of service layer
    * Better handling of Enums
* 0.1
    * Added support for ``java.util.Date``
    * Cleanup in HTML code and structure
    * Better handling of NULL values
* 0.0.1
    * Initial release

## Meta

Jörn Stampehl – joern@stampehl.de

Distributed under the MIT license. See ``license.txt`` for more information.

[https://github.com/joerny/java_admin](https://github.com/joerny/java_admin)
