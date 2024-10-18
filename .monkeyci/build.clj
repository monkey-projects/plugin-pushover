(require '[monkey.ci.plugin.clj :as clj])
(require '[monkey.ci.plugin.github :as gh])

[(clj/deps-library)
 (gh/release-job {:dependencies ["publish"]})]
