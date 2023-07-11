(ns ile.story.model
  (:require [clojure.spec.alpha :as s]))

(s/def :mission.content/result vector?)
(s/def :mission.content/hidden-html string?)
(s/def :mission.content/hidden-css string?)
(s/def :mission.content/mode #{:html :css})
(s/def :mission.content/difficulty #{:easy :medium :hard})
(s/def :mission.content/wrong-blocks vector?)

(s/def :ile/mission.content
  (s/keys :req [:mission.content/result]
          :opt [:mission.content/wrong-blocks
                :mission.content/hidden-html
                :mission.content/hidden-css]))

(s/def :ile/persistable-mission.content
  (s/merge :ile/mission.content
           (s/keys :req [:xt/id])))

(s/def :mission/world keyword?)
(s/def :mission/step int?)
(s/def :mission/name string?)
(s/def :mission/content (s/coll-of :ile/mission.content))
(s/def :mission/story-before vector?)
(s/def :mission/story-after vector?)

(s/def :ile/mission
  (s/keys :req [:mission/world
                :mission/step
                :mission/name
                :mission/content]
          :opt [:mission/story-after
                :mission/story-before]))

(s/def :ile/persistable-mission
  (s/merge :ile/mission
           (s/keys :req [:xt/id])))