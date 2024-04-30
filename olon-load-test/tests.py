from locust import HttpUser, task

class Tests(HttpUser):

  @task
  def home_test(self):
    self.client.get("/index")

  @task
  def static_content_test(self):
    self.client.get("/static/index")
