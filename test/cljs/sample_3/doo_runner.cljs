(ns sample-3.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [sample-3.core-test]))

(doo-tests 'sample-3.core-test)

