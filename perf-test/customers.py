from locust import HttpUser, task, between, constant, tag

class Customer(HttpUser):
    wait_time = between(0.1, 1)

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

        response = self.client.post("/customers", json=payload, headers=headers)

    @tag("list")
    @task
    def get_all(self):
        wait_time = between(1, 3)

        params = {"page": 0, "perPage": 10}
        headers = {
            "Content-Type": "application/json"
        }
        self.client.get("/customers", params=params, headers=headers)
