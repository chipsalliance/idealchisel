// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.idealchisel

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef._
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef._

class InstanceInjector extends SyntheticMembersInjector {
  private val targetObject = "chisel3.experimental.hierarchy"

  // add an implicit converter from Instance[T] to T, then IDEA will be happy
  override def injectFunctions(source: ScTypeDefinition): Seq[String] = {
    source match {
      case o: ScObjectImpl if o.isPackageObject && targetObject.equals(o.qualifiedName) =>
        Seq("implicit def from[T](instance: Instance[T]): T = ???")
      case _ => Seq.empty
    }
  }
}
