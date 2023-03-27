(ns ile.middleware
  (:require
    [clojure.tools.logging :as log]
    [ile.env :refer [defaults]]
    [ile.layout :as layout]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [ile.middleware.formats :as formats]
    [ring-ttl-session.core :refer [ttl-memory-store]]
    [ring.middleware.defaults :as ring-defaults]
    [ile.layout :refer [error-page]]
    [buddy.auth :refer [authenticated?]]
    [ring.middleware.session :refer [wrap-session]]
    [buddy.auth.backends :as backends]
    [buddy.auth.middleware :refer [wrap-authentication]]))

(defn wrap-internal-error [handler]
  (let [error-result (fn [^Throwable t]
                       (log/error t (.getMessage t))
                       (error-page {:status  500
                                    :title   "Something very bad has happened!"
                                    :message "We've dispatched a team of highly trained gnomes to take care of the problem."}))]
    (fn wrap-internal-error-fn
      ([req respond _]
       (handler req respond #(respond (error-result %))))
      ([req]
       (try
         (handler req)
         (catch Throwable t
           (error-result t)))))))

(defn wrap-csrf
  "Checks each post request for a valid csfr (anti-forgery) token.
  Add a `ile.views.core/hidden-anti-forgery-field` to each form to prevent unwanted errors."
  [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title  "Invalid anti-forgery token"})}))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn wrap-render-rum
  "Create HTML response if Hiccup vector present"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (vector? response) (layout/render-page response) response))))

(defn wrap-unauthorized-login-redirect
  "Redirect unauthenticated users to the login-page. Or do nothing."
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (let [original-uri (:uri request)]
        (ring.util.response/redirect
          (if (or (= original-uri "/")
                  (= original-uri "/logout"))
            "/login"
            (str "/login?next=" original-uri)))))))


(defn wrap-signed-in [handler]
  (fn [{:keys [session] :as req}]
    (if (some? (:uid session))
      (handler req)
      {:status  303
       :headers {"location" "/"}})))

(defn wrap-ile-defaults [handler]
  (ring-defaults/wrap-defaults
    handler
    (-> ring-defaults/site-defaults
        (assoc-in [:security :anti-forgery] false)
        (assoc-in [:session :store] (ttl-memory-store (* 60 30)))
        ; enabling cookies for authentication cross-site requests
        (assoc-in [:session :cookie-attrs :same-site] :lax))))

(def backend (backends/session))

(defn wrap-base [handler]
  (clojure.pprint/pprint (:request handler))
  (-> ((:middleware defaults) handler)
      (wrap-authentication backend)
      wrap-session
      wrap-ile-defaults
      wrap-internal-error))