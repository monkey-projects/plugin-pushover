# MonkeyCI Pushover Plugin

This is a [MonkeyCI](https://monkeyci.com) plugin that provides a build job
that sends a message to [Pushover](https://pushover.net).  This makes it
possible to send notifications from your builds.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/com.monkeyci/plugin-pushover.svg)](https://clojars.org/com.monkeyci/plugin-pushover)
First include it in your build `deps.edn`:

```clojure
{:deps {com.monkeyci/plugin-pushover {:mvn/version "<VERSION>"}}}
```

In order to add a job to your build, invoke the `pushover-msg` function:

```clojure
(require '[monkey.ci.plugin.pushover :as pp])

[my-other-jobs
 ;; Add a job to your build that will send the message
 (pp/pushover-msg {:msg "Hi, this is a message from pushover"})] 
```

At the very least, you should provide a `msg`, which can either be a string,
or a function.  If it's a function, it will be passed the build context, so
you can generate a message depending on the situation.  Any other options
(apart from the credentials, see below), are either directly passed to the
Pushover API, or the build job.

The default job id is `pushover`, but you can override this by specifying
the `:id` property.

## Credentials

In order to push a message, you need to have an account.  By default, the plugin
will fetch the user and token from the build parameters.  The default parameter
keys are `pushover-user` and `pushover-token`.  But you can override these
by specifying the `:user-param` and `:token-param` options.  Like so:

```clojure
(pp/pushover-msg {:msg "some test message"
                  :user-param "overridden-user"
		  :token-param "overridden-token"})
;; This will create a job that will post to Pushover using the user and token
;; fetched from the build parameters respectively as "overridden-user" and
;; "overridden-token".
```

You can also directly specify the credentials, although this is not advised from
a security point of view!
```clojure
(pp/pushover-msg {:msg "test message"
                  :user "test-user"
		  :token "test-token"})
```

## Dependencies

You can make the job dependent on another job by adding them to the `:dependencies`
option.  It will be automatically passed on to the job configuration.
```clojure
;; This job will only be executed if "other-job" has succeeded.
(pp/pushover-msg {:msg "test message" :dependencies ["other-job"]})
```

## License

Copyright (c) 2024 by [Monkey Projects](https://www.monkey-projects.be)

[MIT License](LICENSE)