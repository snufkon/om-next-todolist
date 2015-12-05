(ns om-next-todolist.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(def ENTER_KEY 13)

(def app-state
  (atom {:todos [{:id 1 :title "豚肉を買ってくる"}
                 {:id 2 :title "たまねぎを買ってくる"}
                 {:id 3 :title "にんじんを買ってくる"}
                 {:id 4 :title "じゃがいもを買ってくる"}
                 {:id 5 :title "カレーを作る"}]}))


;; -----------------------------------------------------------------------------
;; Parsing

(defmulti read om/dispatch)

(defmethod read :todos
  [env key params]
  (let [state (:state env)]
    {:value (:todos @state)}))

(defmulti mutate om/dispatch)

(defn- gen-id
  [todos]
  (->> (map :id todos)
       (apply max)
       inc))

(defmethod mutate 'todos/add
  [env key params]
  (let [state (:state env)
        id (gen-id (:todos @state))
        new-todo (assoc params :id id)]
    {:action
     (fn []
       (swap! state update :todos conj new-todo))}))


;; -----------------------------------------------------------------------------
;; Components

(defui TodoItem
  static om/IQuery
  (query [this]
    [:id :title])
  Object
  (render [this]
    (let [props (om/props this)
          title (:title props)]
      (dom/li nil title))))

(def todo-item (om/factory TodoItem))

(defn- handle-key-down
  [component e]
  (when (= (.-keyCode e) ENTER_KEY)
    (let [new-field (.-target e)
          title (.-value new-field)]
      (om/transact! component `[(todos/add ~{:title title})])
      (set! (.-value new-field) ""))))

(defui TodoList
  static om/IQuery
  (query [this]
    (let [subquery (om/get-query TodoItem)]
      [{:todos subquery}]))
  Object
  (render [this]
    (let [props (om/props this)
          todos (:todos props)]
      (dom/div nil
        (dom/input #js {:className "new-todo"
                        :placeholder "What needs to be done?"
                        :onKeyDown #(handle-key-down this %)})
        (apply dom/ul nil
          (map todo-item todos))))))

(def reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler TodoList (gdom/getElement "app"))
