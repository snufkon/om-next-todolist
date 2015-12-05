(ns om-next-todolist.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(def ENTER_KEY 13)

(def app-state
  (atom {:todos [{:id 1 :title "豚肉を買ってくる"       :completed true}
                 {:id 2 :title "たまねぎを買ってくる"   :completed true}
                 {:id 3 :title "にんじんを買ってくる"   :completed false}
                 {:id 4 :title "じゃがいもを買ってくる" :completed false}
                 {:id 5 :title "カレーを作る"           :completed false}]}))


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

(defn- id->index
  [id todos]
  (-> (for [[index todo] (map-indexed vector todos)
            :when (= id (:id todo))]
        index)
      first))

(defmethod mutate 'todo/toggle
  [env key params]
  (let [state (:state env)
        id (:id params)
        index (id->index id (:todos @state))]
    {:action
     (fn []
       (swap! state update-in [:todos index :completed] not))}))


;; -----------------------------------------------------------------------------
;; Components

(defui TodoItem
  static om/IQuery
  (query [this]
    [:id :title :completed])
  Object
  (render [this]
    (let [{:keys [id title completed]} (om/props this)
          class (if completed "completed" "")]
      (dom/li nil
        (dom/input #js {:type "checkbox"
                        :className "toggle"
                        :checked (and completed "checked")
                        :onChange #(om/transact! this `[(todo/toggle {:id ~id})])})
        (dom/span #js {:className class} title)))))

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
