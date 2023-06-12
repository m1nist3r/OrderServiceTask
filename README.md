# OrderServiceTask

## Getting Started

To run application you need to run docker-compose up -d from project directory

On start up creates two users:
- test01@order.com:12345678 (USER)
- admin@order.com:12345678 (ADMIN)

And one order of user - test01@order.com


## Swagger
You can access Swagger API documentation with this url: http://localhost:8080/v3/api-docs

By default, two endpoints accessible to create user and to login
When logged in response you get token which allows to Authorize and get access to Order Api.