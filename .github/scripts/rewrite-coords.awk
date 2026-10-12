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
{
  if ($0 ~ /bbnativeplayersdk:bbnativeplayersdk:/ && $0 ~ /-SNAPSHOT/) {
    sub(/bbnativeplayersdk:bbnativeplayersdk:[^"']*/, "bbnativeplayersdk:bbnativeplayersdk:" ACC)
  } else if ($0 ~ /bbnativeplayersdk:bbnativeplayersdk:/) {
    sub(/bbnativeplayersdk:bbnativeplayersdk:[^"']*/, "bbnativeplayersdk:bbnativeplayersdk:" PROD)
  } else if ($0 ~ /bbnativeplayersdk:bbnativeshared:/) {
    sub(/bbnativeplayersdk:bbnativeshared:[^"']*/, "bbnativeplayersdk:bbnativeshared:" PROD)
  } else if ($0 ~ /bbnativeplayersdk:bbnativeplayersdk-core:/) {
    sub(/bbnativeplayersdk:bbnativeplayersdk-core:[^"']*/, "bbnativeplayersdk:bbnativeplayersdk-core:" PROD)
  }
  print
}
