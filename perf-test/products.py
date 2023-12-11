from locust import HttpUser, task, between, constant, tag

class Product(HttpUser):
    wait_time = between(0.1, 1)

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

        response = self.client.post("/products", json=payload, headers=headers)

    @tag("list")
    @task
    def get_all(self):
        wait_time = between(1, 3)

        params = {"page": 0, "perPage": 10}
        headers = {
            "Content-Type": "application/json"
        }
        self.client.get("/products", params=params, headers=headers)
