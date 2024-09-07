// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>

import mill._
import mill.scalalib._
import mill.define.{Command, TaskModule}
import mill.scalalib.publish._
import mill.scalalib.scalafmt._
import mill.scalalib.TestModule.Utest
import mill.util.Jvm
import coursier.maven.MavenRepository
import $file.common

object v {
  val scala = "2.13.14"
  val mainargs = ivy"com.lihaoyi::mainargs:0.5.0"
  val oslib = ivy"com.lihaoyi::os-lib:0.9.1"
  val upickle = ivy"com.lihaoyi::upickle:3.3.1"
}

object idealchisel extends millbuild.common.IdeaPlugin {
  def scalaVersion = v.scala

  def repo: String = "releases"

  def jbBase: String = s"https://www.jetbrains.com/intellij-repository"

  def jbGroupId: String = "com/jetbrains/intellij/idea"

  def jbArtifactId: String = "ideaIC"

  def jbVersion: String = "2024.2.1"

  def jbBuild: String = "242.21829.142"

}