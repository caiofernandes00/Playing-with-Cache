from locust import HttpUser, task, between, constant, tag
import os

use_chace = os.getenv("CLIENT_USE_CACHE", "false").lower() == "true"

class Product(HttpUser):
    wait_time = between(0.1, 1)
    _etag = None

    @tag("create")
    @task
    def post_request(self):
        headers = {
            "Content-Type": "application/json"
        }
        payload = {
            "name": "fake-name",
            "price": 10,
        }

        self.client.post("/products", json=payload, headers=headers)

    @tag("list")
    @task
    def get_all(self):
        params = {"page": 0, "perPage": 10}
        headers = {
            "Content-Type": "application/json",
            "If-None-Match": self._etag
        }

        response = self.client.get("/products", params=params, headers=headers)
        self._etag = response.headers.get('ETag', None) 

