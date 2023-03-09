package zio.config

import zio._

import java.io.{File, Reader}
import java.nio.file.Path

package object yaml {
  implicit class FromConfigSourceYaml(c: ConfigProvider.type) {
    def fromYamlFile(file: File, enableCommaSeparatedValueAsList: Boolean = false): ConfigProvider =
      YamlConfigProvider.fromYamlFile(file, enableCommaSeparatedValueAsList)

    def fromYamlPath(path: Path, enableCommaSeparatedValueAsList: Boolean = false): ConfigProvider =
      YamlConfigProvider.fromYamlPath(path, enableCommaSeparatedValueAsList)

    def fromYamlReader(
      reader: Reader,
      enableCommaSeparatedValueAsList: Boolean = false
    ): ConfigProvider =
      YamlConfigProvider.fromYamlReader(reader, enableCommaSeparatedValueAsList)

    def fromYamlString(
      yamlString: String,
      enableCommaSeparatedValueAsList: Boolean = false
    ): ConfigProvider =
      YamlConfigProvider.fromYamlString(yamlString, enableCommaSeparatedValueAsList)

    def fromYamlRepr[A](repr: A)(
      loadYaml: A => AnyRef,
      enableCommaSeparatedValueAsList: Boolean = false
    ): ConfigProvider =
      YamlConfigProvider.getIndexedConfigProvider(loadYaml(repr), enableCommaSeparatedValueAsList)
  }

}
