(ns om.core)

(defmacro pure [obj children]
  `(om.core/Pure. ~obj ~children))

(defmacro component
  "Sugar over reify for quickly putting together components that
   only need to implement om.core/IRender and don't need access to
   the owner argument."
  [& body]
  `(reify
     om.core/IRender
     (~'render [this#]
       ~@body)))

(defmacro allow-reads [& body]
  `(binding [om.core/*read-enabled* true]
    ~@body))

(defmacro check [& body]
  `(if om.core/*read-enabled*
     (do
       ~@body)
     (throw
       (js/Error.
         (str "Cannot manipulate cursor outside of render phase, only "
              "om.core/transact!, om.core/update!, and om.core/read operations allowed")))))

(defmacro safe-transact! [cursor korks f & args]
  `(om.core/allow-reads
     (om.core/-transact! ~cursor
       (fn [state# path#]
         (if-not (sequential? ~korks)
           (update-in state# (conj path# ~korks) ~f ~@args)
           (update-in state# (into path# ~korks) ~f ~@args))))))

(defmacro safe-update! [cursor f & args]
  `(om.core/allow-reads
     (om.core/-transact! ~cursor
       (fn [state# path#]
         (if (empty? path#)
           (~f state# ~@args)
           (update-in state# path# ~f ~@args))))))

(defmacro tag [pure t]
  `(let [pure# ~pure]
     (set! (.-constructor pure#) (goog/getUid ~t))
     pure#))
