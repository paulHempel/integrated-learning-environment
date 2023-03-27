(ns ile.views.editor-screen

  (:require [ile.ui.components :as components]
            [ile.core.mock-data :as mock]))

(defn- editor-output []
  "Render an empty browser chrome"
  [:div#browser
   [:div#navbar "www.code.editor"]
   [:iframe#output]
   ])

(defn- editor-tabs []
  "The editor tab bar"
  [:div#editor-tabs
   [:button {:onclick "change_language(\"html\")"}
    "HTML"]
   [:button {:onclick "change_language(\"css\")"}
    "CSS"]])

(defn hidden-code [{:keys [html css]}]
  [:div
   [:#html-base
    (:code/base html)]
   [:#html-snippet
    (:code/snippet html)]
   [:#css-base
    (:code/base css)]])

(defn interaction-buttons [next]
  [:div#editor-interaction
   [:button.circular-button.neutral
    [:img {:src "/img/icons/wiki.svg"
           :title "Wiki"}]]
   [:a.circular-button.check {:href next}
    [:img {:src "/img/icons/checkmark.svg"
           :title "Fertig"}]]])

(defn editor-screen [code notes next]
  [:main.column.full-width
   [:nav.row
    [:a {:href "/"} "Zurück"]
    ]
   [:div#editor-screen
    [:aside#editor-sidebar
     (editor-tabs)
     [:div#editor-wrapper
      [:div#editor.w-full.h-full.language-html {:id "editor"}
       (:code/line (:html code))]
      (interaction-buttons next)]
     (hidden-code code)
     (when notes
       (components/notes-widget notes))]

    [:div#editor-output
     (editor-output)]]

   ; add js editor scripts
   [:script {:src "/js/src-min-noconflict/ace.js"
             :type "text/javascript"
             :charset "utf-9"}]
   [:script {:src "/js/editor.js"}]])