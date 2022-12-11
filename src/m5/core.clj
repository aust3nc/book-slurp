(ns m5.core)

(require '[clojure.java.io :as io]) 
(require '[clojure.string :as string])

(def common "common-english-words.csv")
(def dostoevsky "https://www.gutenberg.org/cache/epub/28054/pg28054.txt")
(def importance "https://www.gutenberg.org/cache/epub/844/pg844.txt")
(def output "output.txt") 

(defn- read-book
  "Read a book from a URL and return a string."
  [book] 
  (with-open [raw (java.util.zip.GZIPInputStream. ; set a stream to open the file
                   (io/input-stream book))] ; open the file; save the stream to raw
    (slurp raw))) ; slurp the stream -- read the file into a string

(defn- read-common
  "Read a list of common words and return a set."
  [] 
  (-> (slurp common)
      (string/split #",")
      set))

; The thread-first macro, -> passes the result of the first expression as the first argument to the second expression, and so on.
; After read-common is evaluated in lazy-order, which combines the perks of applicative and normal-order evaluation,
; the process will look like this:
; (set (string/split (slurp common) #","))

(defn- grab-frequent
  "Read a book and return a map of words and their frequencies."
  [book common-words] 
  (->> (read-book book) ; read the book & return a string 
       (re-seq #"[\w|'-]+") ; regex filter to split the string into a sequence of words
       (map #(string/lower-case %)) ; lower-case words
       (remove common-words) ; remove common words
       frequencies ; count the words; return a map of words and their frequencies
       (sort-by val >))) ; sort the map by value in descending order

; The thread-last macro, ->> passes the result of the first expression as the last argument to the second expression, and so on.
; Grab-frequent will look like this:
; (sort-by val > (frequencies (remove common-words (map #(string/lower-case %) (re-seq #"[\w|'-]+" (read-book book))))))

(defn freq-string
  "Combines [[grab-frequent]] and [[read-common]] to return a string of words and their frequencies."
  [book]
  (grab-frequent book (read-common))) ; read book and common words; return a map of words and their frequencies


;; (defn write-freq
;;   [file str]
;;   (spit file str :append true))

;; (defn write-freq
;;   [file str]
;;   (if-not (.exists file)
;;     (spit file str :append true)
;;     (io/delete-file file :then (write-freq file str))))

;; (defn delete-and-write-freq
;;   [file str]
;;   (io/delete-file file :then ( file str)))

;; (defn write-freq
;;   [file str]
;;   (let [e (.exists (io/file file))]
;;     (if-not e
;;       (
;;      (spit file str :append true)
;;       (io/delete-file e :then (write-freq file str)))))

;; (defn exists?
;;   [file]
;;   (if (.exists (io/file file))
;;     true
;;     nil))

(defn exists? 
  "Check if a file exists."
  [file]
  (if (.exists (io/file file)) ; if the file exists
    true ; return true
    nil)) ; else return nil

(defn write 
  "Write a string to a file."
  [file str]
  (spit file str :append true)) ; spit is the opposite of slurp; it writes a string to a file

(defn delete-and-write-freq
  "Delete a file and write a string to it."
  [file str]
  (if (io/delete-file file) ; if the file is deleted
    (write file str) ; write the string to the file
    (println "error deleting file"))) ; else print an error message

(defn write-freq 
  "Checks if a file exists and either writes a string to it or deletes it and writes a string to a new file."
  [file str]
  (if (exists? file)
    ;; (spit file str :append true)
    ;; (println "fax")
    (delete-and-write-freq file str)
    (write file str)))
    ;; (io/delete-file file :then (write-freq file str))))

(defn -main 
  [book]
  (write-freq output (str (freq-string book))))

(-main dostoevsky)
(-main importance)

