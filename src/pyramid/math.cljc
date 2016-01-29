(ns pyramid.math)

(defn log2
  [n]
  #?(:clj (/ (Math/log n) (Math/log 2))
     :cljs (.log2 js/Math n)))

(defn ceil
  [n]
  #?(:clj (if (> n Integer/MAX_VALUE)
            (bigint (Math/ceil n))
            (int (Math/ceil n)))
     :cljs (.ceil js/Math n)))

(defn floor
  [n]
  #?(:clj (if (> n Integer/MAX_VALUE)
            (bigint (Math/floor n))
            (int (Math/floor n)))
     :cljs (.floor js/Math n)))

(defn pow
  [base power]
  #?(:clj (Math/pow base power)
     :cljs (.pow js/Math base power)))
