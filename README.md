# Welcome to cstceumj

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/cstceum/cstceumj?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/cstceum/cstceumj.svg?branch=master)](https://travis-ci.org/cstceum/cstceumj)
[![Coverage Status](https://coveralls.io/repos/cstceum/cstceumj/badge.png?branch=master)](https://coveralls.io/r/cstceum/cstceumj?branch=master)


# About
cstceumJ is a pure-Java implementation of the cstceum protocol. For high-level information about cstceum and its goals, visit [cstceum.org](https://cstceum.org). The [cstceum white paper](https://github.com/cstceum/wiki/wiki/White-Paper) provides a complete conceptual overview, and the [yellow paper](http://gavwood.com/Paper.pdf) provides a formal definition of the protocol.

We keep cstceumJ as thin as possible. For [JSON-RPC](https://github.com/cstceum/wiki/wiki/JSON-RPC) support and other client features check [cstceum Harmony](https://github.com/cstc-camp/cstceum-harmony).

# Running cstceumJ

##### Adding as a dependency to your Maven project: 

```
   <dependency>
     <groupId>org.cstceum</groupId>
     <artifactId>cstceumj-core</artifactId>
     <version>1.8.0-RELEASE</version>
   </dependency>
```

##### or your Gradle project: 

```
   repositories {
       mavenCentral()
       jcenter()
       maven { url "https://dl.bintray.com/cstceum/maven/" }
   }
   compile "org.cstceum:cstceumj-core:1.8.+"
```

As a starting point for your own project take a look at https://github.com/cstc-camp/cstceumj.starter

##### Building an executable JAR
```
git clone https://github.com/cstceum/cstceumj
cd cstceumj
cp cstceumj-core/src/main/resources/cstceumj.conf cstceumj-core/src/main/resources/user.conf
vim cstceumj-core/src/main/resources/user.conf # adjust user.conf to your needs
./gradlew clean fatJar
java -jar cstceumj-core/build/libs/cstceumj-core-*-all.jar
```

##### Running from command line:
```
> git clone https://github.com/cstceum/cstceumj
> cd cstceumj
> ./gradlew run [-PmainClass=<sample class>]
```

##### Optional samples to try:
```
./gradlew run -PmainClass=org.cstceum.samples.BasicSample
./gradlew run -PmainClass=org.cstceum.samples.FollowAccount
./gradlew run -PmainClass=org.cstceum.samples.PendingStateSample
./gradlew run -PmainClass=org.cstceum.samples.PriceFeedSample
./gradlew run -PmainClass=org.cstceum.samples.PrivateMinerSample
./gradlew run -PmainClass=org.cstceum.samples.TestNetSample
./gradlew run -PmainClass=org.cstceum.samples.TransactionBomb
```

##### Importing project to IntelliJ IDEA: 
```
> git clone https://github.com/cstceum/cstceumj
> cd cstceumj
> gradlew build
```
  IDEA: 
* File -> New -> Project from existing sources…
* Select cstceumj/build.gradle
* Dialog “Import Project from gradle”: press “OK”
* After building run either `org.cstceum.Start`, one of `org.cstceum.samples.*` or create your own main. 

# Configuring cstceumJ

For reference on all existing options, their description and defaults you may refer to the default config `cstceumj.conf` (you may find it in either the library jar or in the source tree `cstceum-core/src/main/resources`) 
To override needed options you may use one of the following ways: 
* put your options to the `<working dir>/config/cstceumj.conf` file
* put `user.conf` to the root of your classpath (as a resource) 
* put your options to any file and supply it via `-Dcstceumj.conf.file=<your config>`
* programmatically by using `SystemProperties.CONFIG.override*()`
* programmatically using by overriding Spring `SystemProperties` bean 

Note that don’t need to put all the options to your custom config, just those you want to override. 

# Special thanks
YourKit for providing us with their nice profiler absolutely for free.

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

![YourKit Logo](https://www.yourkit.com/images/yklogo.png)

# Contact
Chat with us via [Gitter](https://gitter.im/cstceum/cstceumj)

# License
cstceumj is released under the [LGPL-V3 license](LICENSE).

