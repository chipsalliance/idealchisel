// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>
package org.chipsalliance.idealchisel

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScObject, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.SyntheticMembersInjector

class InstanceInjector extends SyntheticMembersInjector {
  private def isInstantiable(source: ScTypeDefinition) =
    source.hasAnnotation("chisel3.experimental.hierarchy.instantiable")

  // add an companion object for all @instantiable modules.
  override def needsCompanionObject(source: ScTypeDefinition): Boolean = isInstantiable(source)

  // add an implicit converter from Instance[SomeModule] to SomeModule, then IDEA will be happy
  override def injectFunctions(source: ScTypeDefinition): Seq[String] = source match {
    case c: ScObject =>
      c.baseCompanion
        .flatMap(scTypeDefinition =>
          Option.when(isInstantiable(scTypeDefinition))(
            s"implicit def from(instance: chisel3.experimental.hierarchy.Instance[${source.name}]): ${source.name} = ???"
          )
        )
        .toSeq
    case _ => Nil
  }
}
