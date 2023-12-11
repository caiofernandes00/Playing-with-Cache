from locust import HttpUser, task, between, constant, tag


class MyUser(HttpUser):
    wait_time = between(0.1, 1)
    my_eTag = None

    @tag("create")
    @task
    def post_request(self):
        headers = {
            "Content-Type": "application/json"
        }
        payload = {
            "name": "fake-name"
        }

        response = self.client.post("/orders/etag", json=payload, headers=headers)

    @tag("list")
    @task
    def get_all(self):
        wait_time = between(1, 3)

        params = {"page": 0, "perPage": 10}
        headers = {
            "Content-Type": "application/json",
            "If-None-Match": self.my_eTag
        }

        result = self.client.get("/orders/etag", params=params, headers=headers)

        if result.status_code == 200:
            self.my_eTag = result.headers.get("ETag")
