(ns om-next-todolist.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(def app-state
  (atom {:todos [{:id 1 :title "豚肉を買ってくる"}
                 {:id 2 :title "たまねぎを買ってくる"}
                 {:id 3 :title "にんじんを買ってくる"}
                 {:id 4 :title "じゃがいもを買ってくる"}
                 {:id 5 :title "カレーを作る"}]}))

(defui TodoItem
  Object
  (render [this]
    (let [props (om/props this)
          title (:title props)]
      (dom/li nil title))))

(def todo-item (om/factory TodoItem))

(defui TodoList
  Object
  (render [this]
    (let [props (om/props this)
          todos (:todos props)]
      (apply dom/ul nil
        (map todo-item todos)))))

(def reconciler
  (om/reconciler {:state app-state}))

(om/add-root! reconciler TodoList (gdom/getElement "app"))
