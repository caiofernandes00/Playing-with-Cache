from locust import HttpUser, task, between, constant, tag
import os
import datetime

from helper import Helper


use_chace = os.getenv("CLIENT_USE_CACHE", "false").lower() == "true"
helper = Helper()

class Customer(HttpUser):
    wait_time = between(0.1, 1)
    _etag = None
    _last_time_fetched = None
    _no_cache = None
    _max_age = None
    _response = None
    
    @tag("create")
    @task
    def post_request(self):
        headers = {
            "Content-Type": "application/json"
        }
        payload = {
            "name": "fake-name",
            "age": 10,
        }

        self.client.post("/customers", json=payload, headers=headers)

    @tag("list")
    @task
    def get_all(self):
        params = {"page": 0, "perPage": 10}
        headers = {
            "Content-Type": "application/json",
            "If-None-Match": self._etag
        }
        
        if helper.should_retrieve_from_cache(use_chace, self._no_cache, self._max_age, self._response, self._last_time_fetched):
            return


        response = self.client.get("/customers", params=params, headers=headers)
        self._cache_control = response.headers.get('Cache-Control', None)
        self._no_cache = self._cache_control is not None and "no-cache" in self._cache_control
        self._max_age = self._cache_control is not None and "max-age" in self._cache_control
        
        self._etag = response.headers.get('ETag', None)
        self._last_time_fetched = datetime.utcnow()