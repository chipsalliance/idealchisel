// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>

import mill._
import mill.scalalib._

trait IdeaPlugin extends ScalaModule {

  def repo: String

  def jbBase: String

  def jbGroupId: String

  def jbArtifactId: String

  def jbVersion: String

  def jbBuild: String

  def jbZipName: String = s"${jbArtifactId}-${jbVersion}.zip"

  def jbUrl: String = s"${jbBase}/${repo}/${jbGroupId}/${jbArtifactId}/${jbVersion}/${jbZipName}"

  def jb: T[PathRef] = T.persistent {
    val t = T.dest / jbArtifactId
    if (!os.exists(t))
      mill.util.Util.downloadUnpackZip(jbUrl, os.rel / t.last)
    PathRef(t)
  }

  def jbSrcName: String = s"$jbArtifactId-$jbVersion-sources.jar"

  def jbSrcUrl: String = s"$jbBase/$repo/$jbGroupId/$jbArtifactId/$jbVersion/${jbSrcName}"

  def jbSrcJar: T[PathRef] = T.persistent {
    val target = T.dest / jbSrcName
    if (!os.exists(target))
      mill.util.Util.download(jbSrcUrl, os.rel / jbSrcName)
    PathRef(target)
  }

  // see: [[https://plugins.jetbrains.com/plugins/list?pluginId=1347&build=242.*]]
  def pluginUrl(id: Int, version: Int, name: String) = {
    s"https://plugins.jetbrains.com/files/${id}/${version}/${name}"
  }

  def jbJar = T.persistent {
    Agg.from(Seq(jb().path / "lib", jb().path / "lib" / "modules", jb().path / "plugins").flatMap(f => os.walk(f).filter(_.ext == "jar").map(j => PathRef(j))))
  }

  def scalaPlugin = T.persistent {
    val t = T.dest / "scala-intellij-bin-2024.2.25"
    if (!os.exists(t))
      mill.util.Util.downloadUnpackZip(pluginUrl(1347, 595821, "scala-intellij-bin-2024.2.25.zip"), os.rel / t.last)
    Agg.from(Seq(t / "Scala" / "lib").flatMap(f => os.walk(f).filter(_.ext == "jar").map(j => PathRef(j))))
  }

  override def unmanagedClasspath: T[Agg[PathRef]] = T {
    scalaPlugin() ++ jbJar()
  }

  def release = T {
    os.makeDir.all(T.dest / "release" / "lib")
    os.copy.into(
      mill.util.Jvm.createJar(localRunClasspath().map(_.path).filter(os.exists), mill.api.JarManifest.Empty).path,
      T.dest / "release" / "lib"
    )
    os.proc("zip", "-r", s"$artifactName.zip", "release").call(T.dest)
    PathRef(T.dest / s"$artifactName.zip")
  }

  // From SBT Plugins
  // API from [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/Keys.scala]].
  // TODO: clean up
  //
  //  /** Name of the plugin you're developing. */
  //  def intellijPluginName: T[String]
  //
  //  /** Number of IntelliJ Platform build to use in project. */
  //  def intellijBuild: T[String]
  //
  //  /** Edition of Intellij Platform to use in project. */
  //  def intellijPlatform: T[IntelliJPlatform]
  //
  //  private def intellijBuildInfo: T[BuildInfo] = ???
  //
  //  /** Version and variant of JetBrains Runtime to download and install. */
  //  def jbrInfo: T[JbrInfo]
  //
  //  /** Directory where IntelliJ Platform binaries and sources are downloaded. */
  //  def intellijDownloadDirectory: T[PathRef]
  //
  //  /** List of IntelliJ platform plugin to depend on. */
  //  def intellijPlugins: T[IntellijPlugin]
  //
  //  /** List of IntelliJ platform plugins to include in tests at runtime. */
  //  def intellijExtraRuntimePluginsInTests: T[Seq[IntellijPlugin]]
  //
  //  /** Flag indicating whether IntelliJ Platform sources should be downloaded too. */
  //  def intellijDownloadSources: T[Boolean]
  //
  //  /** Flag indicating whether to add sources to IntelliJ Platform SDK libraries. */
  //  def intellijAttachSources: T[Boolean]
  //
  //  /** Search for plugin ID by plugin name or description. */
  //  def searchPluginId: T[Map[String, (String, Boolean)]]
  //
  //  /** Download Intellij Platform binaries, sources and external plugins for specified build. */
  //  def updateIntellij: T[Unit]
  //
  //  /** Publish IntelliJ Platform plugin on plugins.jetbrains.com */
  //  def publishPlugin: T[Unit]
  //
  //  /** Sign the zipped plugin artifact using your private key and certificate chain. */
  //  def signPlugin: T[PathRef]
  //
  //  /** Enable/Disable plugin signing and set the private key and certificate chain via this setting. */
  //  def signPluginOptions: T[PluginSigningOptions]
  //
  //  /** Default base directory of IntelliJ Platform config directories for this plugin. */
  //  def intellijPluginDirectory: T[PathRef]
  //
  //  /** Directory where downloaded IntelliJ Platform binaries and sources are unpacked. */
  //  def intellijBaseDirectory: T[PathRef]
  //
  //  /** Information about IntelliJ distribution extracted from product-info.json file from IntelliJ Platform root directory */
  //  def productInfo: T[ProductInfo]
  //
  //  /** Classpath containing main IntelliJ Platform jars. */
  //  def intellijMainJars: T[Seq[PathRef]]
  //
  //  /** Classpath containing IntelliJ Platform test framework jars. */
  //  def intellijTestJars: T[Seq[PathRef]]
  //
  //  /** Classpath containing jars of internal IntelliJ Platform plugins used in this project. */
  //  def intellijPluginJars: T[Seq[PluginJars]]
  //
  //  /** Classpath containing jars of extra plugins added to test classpath at runtime. */
  //  def intellijExtraRuntimePluginsJarsInTests: T[Seq[PluginJars]]
  //
  //  /** Attributed classpath containing main IntelliJ Platform jars. */
  //  private def intellijMainJarsClasspath: T[Classpath] = ???
  //
  //  /** Attributed classpath containing IntelliJ Platform test framework jars. */
  //  private def intellijTestJarsClasspath: T[Classpath] = ???
  //
  //  /** Attributed classpath containing jars of internal IntelliJ Platform plugins used in this project. */
  //  private def intellijPluginJarsClasspath: T[Seq[(PluginDescriptor, Classpath)]] = ???
  //
  //  /** Attributed classpath containing jars of extra plugins added to test classpath at runtime. */
  //  private def intellijExtraRuntimePluginsJarsInTestsClasspath: T[Seq[(PluginDescriptor, Classpath)]] = ???
  //
  //  /** IntelliJ Platform's config directory for tests. */
  //  def intellijTestConfigDir: T[PathRef]
  //
  //  /** IntelliJ Platform's system directory for tests. */
  //  def intellijTestSystemDir: T[PathRef]
  //
  //  /** Clean up IntelliJ Platform test system and config directories. */
  //  def cleanUpTestEnvironment: T[Unit]
  //
  //  /** Settings for patching plugin.xml */
  //  def patchPluginXml: T[pluginXmlOptions]
  //
  //  /** Custom IntelliJ Platform JVM options used for running. The final list of all VM options is defined by IntellijVMOptionsBuilder. */
  //  def intellijVMOptions: T[IntellijVMOptions]
  //
  //  /** Runs debug IntelliJ Platform instance with plugin. */
  //  def runIDE: T[Unit] = T {}
  //
  //  /** Options to tune generation of IDEA run configurations. */
  //  def ideaConfigOptions: T[IdeaConfigBuildingOptions]
  //
  //  /** Include scala-library.jar in the artifact and generated run configurations. */
  //  def bundleScalaLibrary: T[Boolean]
  //
  //  /** Generate JVM bytecode to assert that a method is called on the correct IDEA thread
  //   * (supported method annotations: @RequiresBackgroundThread, @RequiresEdt, @RequiresReadLock, @RequiresReadLockAbsence, @RequiresWriteLock)
  //   * */
  //  def instrumentThreadingAnnotations: T[Boolean]
  //
  //  def doPatchPluginXml: T[Unit]
  //
  //  def doProjectSetup: T[Unit]
  //
  //  def createIDEARunConfiguration: T[Unit]
  //
  //  def createIDEAArtifactXml: T[Unit]
}
/*

object sites {
  val INTELLIJ_DEPENDENCIES_REPOSITORY = "https://packages.jetbrains.team/maven/p/ij/intellij-dependencies"
  val INTELLIJ_REPOSITORY_NIGHTLY = "https://www.jetbrains.com/intellij-repository/nightly"
  val INTELLIJ_REPOSITORY_RELEASES = "https://www.jetbrains.com/intellij-repository/releases"
  val INTELLIJ_REPOSITORY_SNAPSHOTS = "https://www.jetbrains.com/intellij-repository/snapshots"
  val MARKETPLACE_REPOSITORY = "https://plugins.jetbrains.com/maven"
  val JETBRAINS_MARKETPLACE = "https://plugins.jetbrains.com"
}

/** [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/IntelliJPlatform.scala]] */
sealed trait IntelliJPlatform {
  val name: String

  def edition: String = name.takeRight(2)

  def platformPrefix: String

  override def toString: String = name
}

object IntelliJPlatform {
  object IdeaCommunity extends IntelliJPlatform {
    override val name = "ideaIC"

    override def platformPrefix: String = "Idea"
  }

  object IdeaUltimate extends IntelliJPlatform {
    override val name = "ideaIU"

    override def platformPrefix: String = ""
  }

  object PyCharmCommunity extends IntelliJPlatform {
    override val name: String = "pycharmPC"

    override def platformPrefix: String = "PyCharmCore"
  }

  object PyCharmProfessional extends IntelliJPlatform {
    override val name: String = "pycharmPY"

    override def platformPrefix: String = "Python"
  }

  object CLion extends IntelliJPlatform {
    override val name: String = "clion"

    override def edition: String = name

    override def platformPrefix: String = "CLion"
  }

  object MPS extends IntelliJPlatform {
    override val name: String = "mps"

    override def edition: String = name

    override def platformPrefix: String = ""
  }
}

/** https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/download/package.scala */
case class BuildInfo(buildNumber: String, edition: IntelliJPlatform)

/** https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/JbrInfo.scala */
final case class JbrVersion(major: String, minor: String)

final case class JbrKind(value: String)

final case class JbrPlatform(os: String, arch: String)

case class JbrInfo(version: JbrVersion, kind: JbrKind, platform: JbrPlatform)

/** [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/IntellijPlugin.scala]] */
case class IntellijPlugin()

/** [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/PluginSigningOptions.scala]] */
case class PluginSigningOptions(enabled: Boolean,
                                certFile: Option[os.Path],
                                privateKeyFile: Option[os.Path],
                                keyPassphrase: Option[String])

/** [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/productInfo/ProductInfo.scala#L63]] */
case class Launch(
                   os: String,
                   arch: String,
                   launcherPath: String,
                   javaExecutablePath: Option[String],
                   vmOptionsFilePath: String,
                   startupWmClass: Option[String],
                   bootClassPathJarNames: Seq[String],
                   additionalJvmArguments: Seq[String],
                 )

case class ProductInfo(
                        name: String,
                        version: String,
                        versionSuffix: Option[String],
                        buildNumber: String,
                        productCode: String,
                        modules: Seq[String],
                        launch: Seq[Launch],
                        layout: Seq[LayoutItem]
                      )

case class LayoutItem(
                       name: String,
                       kind: LayoutItemKind,
                       classPath: Option[Seq[String]]
                     )

sealed trait LayoutItemKind

object LayoutItemKind {
  case object Plugin extends LayoutItemKind

  case object PluginAlias extends LayoutItemKind

  case object ProductModuleV2 extends LayoutItemKind

  case object ModuleV2 extends LayoutItemKind

  case class Unknown(value: String) extends LayoutItemKind
}

case class PluginJars(descriptor: PluginDescriptor, pluginRoot: PathRef, pluginJars: Seq[PathRef])

/** sbt.Keys.Classpath */
case class Classpath()

/** [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/download/plugin/PluginDescriptor.scala]] */
final case class Dependency(id: String, optional: Boolean)

case class PluginDescriptor(id: String,
                            vendor: String,
                            name: String,
                            version: String,
                            sinceBuild: String,
                            untilBuild: String,
                            dependsOn: Seq[Dependency] = Seq.empty)

/** [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/pluginXmlOptions.scala]] */
case class pluginXmlOptions(version: String, sinceBuild: String, untilBuild: String, pluginDescription: String, changeNotes: String)

/** [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/runIdea/IntellijVMOptions.scala]] */
case class IntellijVMOptions()

/** [[https://github.com/JetBrains/sbt-idea-plugin/blob/master/ideaSupport/src/main/scala/org/jetbrains/sbtidea/IdeaConfigBuildingOptions.scala#L6]] */
case class IdeaConfigBuildingOptions()

*/