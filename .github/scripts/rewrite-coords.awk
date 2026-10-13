# Rewrite the com.bluebillywig.bbnativeplayersdk:* version coordinates in app/build.gradle.
#
# ACC = acceptance version (X.Y-N-SNAPSHOT), PROD = production version (X.Y.Z), passed with -v.
# Only the version token is replaced (the run of non-quote characters after the artifact's trailing
# ':'), so a line's commented/uncommented state and everything else on it are preserved. The
# trailing ':' after the artifact name is what keeps bbnativeplayersdk from also matching
# bbnativeplayersdk-core. Rules (from the jk-update-apk skill):
#   bbnativeplayersdk  with -SNAPSHOT  -> acceptance
#   bbnativeplayersdk  without -SNAPSHOT -> production
#   bbnativeshared / bbnativeplayersdk-core -> production
#
# Three properties this deliberately does NOT get wrong:
#
#   * The -SNAPSHOT test reads the matched version TOKEN, never the whole line. Testing $0 would
#     mean a trailing comment like `// production (was 8.60-2-SNAPSHOT)` on an ACTIVE production
#     coordinate silently rewrote it to the acceptance version — and the file already carries
#     `// acceptance` / `// production` comments on exactly these lines.
#
#   * Each artifact gets its own pass over the line and replaces EVERY occurrence, rather than an
#     if/else-if chain of single sub() calls. Two coordinates on one line would otherwise leave the
#     second on the old version, mixing SDK versions inside one APK with nothing said.
#
#   * `n` counts the substitutions and END exits non-zero when it is still 0. An awk that matched
#     nothing exits 0 and emits a byte-identical file, which is indistinguishable from a successful
#     rewrite — the build would then quietly produce the OLD SDK while the workflow went on to
#     commit "upped bbnativeplayersdk to <version>". The caller hard-gates on this job, so that
#     green run is worse than a red one.

function rewrite(artifact, snapshot_to, plain_to,    key, klen, out, rest, pos, ver) {
  key = "bbnativeplayersdk:" artifact ":"
  klen = length(key)
  out = ""
  rest = $0
  while ((pos = index(rest, key)) > 0) {
    out = out substr(rest, 1, pos + klen - 1)
    rest = substr(rest, pos + klen)
    match(rest, /^[^"']*/)
    ver = substr(rest, 1, RLENGTH)
    rest = substr(rest, RLENGTH + 1)
    out = out ((ver ~ /-SNAPSHOT/) ? snapshot_to : plain_to)
    n++
  }
  $0 = out rest
}

{
  rewrite("bbnativeplayersdk", ACC, PROD)
  rewrite("bbnativeshared", PROD, PROD)
  rewrite("bbnativeplayersdk-core", PROD, PROD)
  print
}

END {
  if (n == 0) {
    print "rewrite-coords.awk: no com.bluebillywig.bbnativeplayersdk coordinate matched app/build.gradle." > "/dev/stderr"
    print "rewrite-coords.awk: refusing to report success — the build would use the OLD SDK version." > "/dev/stderr"
    exit 3
  }
  printf "rewrite-coords.awk: rewrote %d coordinate(s)\n", n > "/dev/stderr"
}
