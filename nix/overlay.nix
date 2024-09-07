# SPDX-License-Identifier: Apache-2.0
# SPDX-FileCopyrightText: 2024 Jiuyang Liu <liu@jiuyang.me>

final: prev: {
  mill =
    let jre = final.jdk21;
    in (prev.mill.override { inherit jre; }).overrideAttrs
      (_: { passthru = { inherit jre; }; });
  fetchMillDeps = final.callPackage ./pkgs/mill-builder.nix { };
  add-determinism =
    final.callPackage ./pkgs/add-determinism { }; # faster strip-undetereminism
}
