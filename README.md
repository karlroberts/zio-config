[//]: # (This file was autogenerated using `zio-sbt-website` plugin via `sbt generateReadme` command.)
[//]: # (So please do not edit it manually. Instead, change "docs/index.md" file or sbt setting keys)
[//]: # (e.g. "readmeDocumentation" and "readmeSupport".)

# ZIO Config

[ZIO Config](https://zio.dev/zio-config/) is a ZIO-based library and act as an extension to core library ZIO's `Config` language.


[![Production Ready](https://img.shields.io/badge/Project%20Stage-Production%20Ready-brightgreen.svg)](https://github.com/zio/zio/wiki/Project-Stages) ![CI Badge](https://github.com/zio/zio-config/workflows/CI/badge.svg) [![Sonatype Releases](https://img.shields.io/nexus/r/https/oss.sonatype.org/dev.zio/zio-config_2.13.svg?label=Sonatype%20Release)](https://oss.sonatype.org/content/repositories/releases/dev/zio/zio-config_2.13/) [![Sonatype Snapshots](https://img.shields.io/nexus/s/https/oss.sonatype.org/dev.zio/zio-config_2.13.svg?label=Sonatype%20Snapshot)](https://oss.sonatype.org/content/repositories/snapshots/dev/zio/zio-config_2.13/) [![javadoc](https://javadoc.io/badge2/dev.zio/zio-config-docs_2.13/javadoc.svg)](https://javadoc.io/doc/dev.zio/zio-config-docs_2.13) [![ZIO Config](https://img.shields.io/github/stars/zio/zio-config?style=social)](https://github.com/zio/zio-config)

Let's enumerate some key features of this library:

- **Support for Various Sources** — It can read flat or nested configurations. Thanks to `IndexedFlat`.
- **Automatic Document Generation** — It can auto-generate documentation of configurations.
- **Automatic Derivation** — It has built-in support for automatic derivation of readers and writers for case classes and sealed traits.
- **Type-level Constraints and Automatic Validation** — because it supports _Refined_ types, we can write type-level predicates which constrain the set of values described for data types.
- **Descriptive Errors** — It accumulates all errors and reports all of them to the user rather than failing fast.
- **Integrations** — Integrations with a variety of libraries


If you are only interested in automatic derivation of configuration, find the details [here](http://zio.dev/zio-config/automatic-derivation-of-config-descriptor).

## Installation

In order to use this library, we need to add the following line in our `build.sbt` file:

```scala
libraryDependencies += "dev.zio" %% "zio-config" % "4.0.0-RC12" 
```

# Quick Start

Let's add these four lines to our `build.sbt` file as we are using these modules in our examples:

```scala
libraryDependencies += "dev.zio" %% "zio-config"          % "4.0.0-RC12"
libraryDependencies += "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC12"
libraryDependencies += "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC12"
libraryDependencies += "dev.zio" %% "zio-config-refined"  % "4.0.0-RC12"
```

There are many examples in [here](https://github.com/zio/zio-config/tree/master/examples/shared/src/main/scala/zio/config/examples) straight away as well.

## Automatic Derivation

More documentations are in website. Here is a simple auto-derivation for a basic configuration class.


```scala
import zio.config.magnolia._

case class AppConfig(name: String, age: Int)

val config: Config[AppConfig] = deriveConfig[AppConfig]

```

## The `to` method for easy manual configurations

```scala
import zio.config._
import zio.Config

final case class AppConfig(port: Int, url: String)

val config = Config.int("PORT").zip(Config.string("URL")).to[AppConfig]

```

## A few handy methods

### CollectAll

```scala
import zio.config._

  final case class Variables(variable1: Int, variable2: Option[Int])

  val listOfConfig: List[Config[Variables]] =
    List("GROUP1", "GROUP2", "GROUP3", "GROUP4")
      .map(group => (Config.int(s"${group}_VARIABLE1") zip Config.int(s"${group}_VARIABLE2").optional).to[Variables])

  val configOfList: Config[List[Variables]] =
    Config.collectAll(listOfConfig.head, listOfConfig.tail: _*)

```

### orElseEither && Constant

```scala
import zio.config._ 

sealed trait Greeting

case object Hello extends Greeting
case object Bye extends Greeting

val configSource = 
  ConfigProvider.fromMap(Map("greeting" -> "Hello"))

val config: Config[Greeting] = 
  Config.constant("Hello").orElseEither(Config.constant("Bye")).map(_.merge)


```


## Different Configuration Sources

More documentations on various sources are in website. Here is an example with `typesafe-HOCON`. 

```scala
import zio.config.typesafe._

val string = 
  s"""
   {
     age : 10
     name : foo
   }
  
  """
  
ConfigProvider.fromHoconString(string).load(deriveConfig[AppConfig])

```

`Yaml`, and `XML` are supported as well, with various others on pipeline.
`Indexed Map` is another Configuration source that zio-config supports.

### Indexed Map, Array datatype, and a some implementation notes

`zio-config` comes up with the idea of `IndexedFlat` allowing you to define indexed configs (see examples below).
However, the constructors of `IndexedFlat` is not exposed to the user for the time being, since it can conflate with some ideas in `zio.core` `Flat`,
and resulted in failures whenever `IndexedFlat` was converted to a `Flat` internally. Example: https://github.com/zio/zio-config/issues/1095

Therefore, some of these ideas around `Indexing` is  pushed back to `ZIO` and incorporated within the `Flat` structure.

See https://github.com/zio/zio/pull/7823 and https://github.com/zio/zio/pull/7891

These changes are to keep the backward compatibility of ZIO library itself. 

#### What does it mean to users?
It implies, for sequence (or list) datatypes, you can use either `<nil>` or `""` to represent empty list in a flat structure.
See the below example where it tries to mix indexing into flat structure. 
We recommend using `<nil>` over `""` whenever you are trying  to represent a real indexed format

Example:

```scala
import zio.config._, magnolia._

final case class Department(name: String, block: Int)

final case class Employee(departments: List[Department], name: String)
final case class Config(employees: List[Employee])

val map =
  Map(
    "employees[0].name" -> "jon",
    "employees[0].departments[0].name" -> "science",
    "employees[0].departments[0].block" -> "10",
    "employees[0].departments[1].name" -> "maths",
    "employees[0].departments[2].block" -> "11",
    "employees[1].name" -> "foo",
    "employees[1].departments" -> "<nil>",
  )
  

ConfigProvider.fromMap(map).load(derivedConfig[Config])


```

Although we support indexing within Flat, formats such as Json/HOCON/XML is far better to work with indexing,
and zio-config supports these formats making use of the above idea.


#### Another simple example of an indexed format

```scala

import zio.config._, magnolia._

final case class Employee(age: Int, name: String)

 val map = 
   Map(
     "department.employees[0].age" -> "10",
     "department.employees[0].name" -> "foo",
     "department.employees[1].age" -> "11",
     "department.employees[1].name" -> "bar",
     "department.employees[2].age" -> "12",
     "department.employees[2].name" -> "baz",
   )


val provider = ConfigProvider.fromMap(map)
val config = Config.listOf("employees", deriveConfig[Employee]).nested("department")
val result = provider.load(config)

```


## Markdown documentation

```scala

generatedDocs(deriveConfig[AppConfig]).toTable.toGithubFlavouredMarkdown

```

## Auto Validation (integration with refined)

```scala

 import zio.config._, refined._
 import eu.timepit.refined.collection.Size
 import eu.timepit.refined.predicates.all.Greater
 import eu.timepit.refined.`W`
 import eu.timepit.refined.types.string.NonEmptyString

 final case class Jdbc(username: NonEmptyString, password: NonEmptyString)

 val jdbc: Config[Jdbc] =
   (refineType[NonEmptyString]("username") zip refineType[NonEmptyString]("password")).to[Jdbc]
   
 // Even better!
 
 val databases = Config.listOf("databases", jdbc)
 val refinedList = refine[Size[Greater[W.`2`.T]]](databases)

```

## Integration with other libraries

`zio-config` is also integrated with `enumeratum`, `cats`, and `scalaz`

#### Enumeratum

Many applications rely on this beautiful library https://github.com/lloydmeta/enumeratum.
Zio-config can directly load it from enumeratum's `enum` without relying on auto-derivation (and rely on Enumeratum's macro indirectly witha additional features).

```scala

 sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {

    val values = findValues

    case object Hello extends Greeting
    case object GoodBye extends Greeting
    case object Hi extends Greeting
    case object Bye extends Greeting

  }


  // Load using zio-config
  import zio.config.enumeratum._

  val mapProvider =
    ConfigProvider.fromMap(Map(
      "greeting" -> "Hello"
    ))

  val config =
    `enum`(Greeting).nested("greeting")

  val pgm: IO[Error, Greeting] =
    mapProvider.load(config)
    
  // Returns Hello  
    

```

#### Scalaz/Cats

Highly polymorphic code end up relying on
typeclasses, and zio-config provides instances for `Config`.

This is a simple example to showcase the capability.

```scala

  import _root_.scalaz._, Scalaz._
  import zio.config.scalaz.instances._

 // Across the application, there can be various effect types, but there is only one addition!
 def add[F[_]: Applicative, A: Monoid](primary: F[A], secondary: F[A]): F[A] =
    primary.<*>(Applicative[F].map(secondary)(secondary => (primary: A) => primary.mappend(secondary)))
    
 // Now even `Config` can take part in this addition given the values of config parameters should be Monoid,
 // instead of using native `zip` and separately implementing addition for various types
 val configResult = add(Config.int("marks1"), Config.int("marks2")))
 
 ConfigProvider.fromMap(Map("marks1" -> "10", "marks2" -> "20")).load(configResult) // returns 30
 

```

In addition to it, it can also load cats/scalaz specific datatypes

```scala

  import zio.config.scalaz._
  import _root_.scalaz.Maybe

  
  val config: Config[Maybe[Int]] = maybe(Config.int("age"))
  
```

## Documentation

Learn more at [ZIO Config homepage](https://zio.dev/zio-config/)!

## Contributing

For the general guidelines, see ZIO [contributor's guide](https://zio.dev/about/contributing).

## Code of Conduct

See the [Code of Conduct](https://zio.dev/about/code-of-conduct)

## Support

Come chat with us on [![Badge-Discord]][Link-Discord].

[Badge-Discord]: https://img.shields.io/discord/629491597070827530?logo=discord "chat on discord"
[Link-Discord]: https://discord.gg/2ccFBr4 "Discord"

## License

[License](LICENSE)
