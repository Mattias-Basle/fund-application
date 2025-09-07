# Context
This project aims at mimicking a small fund transferring system.
In it, you can create your own user and accounts, deposit, withdraw and transfer funds.
Different currencies are supported for mimicking 'international transfers'.

# Getting Started
## Initialize the project
### Requirements
To run the project successfully you need to have bot **git** and **docker** installed on your machine. 
You can find the documentation on how to install them here:

* [Git install documentation](https://github.com/git-guides/install-git)
* [Docker engine install documentation](https://docs.docker.com/engine/install/)

### Set It Up
This project being hosted on a public GitHub repository, the first thing
is to clone it in your device using the following command with the copied url: 

`git clone <project_url>`

### Run It
Once the project has been cloned, go inside the project's directory then run the following command:

`docker compose up`

The project has been fully dockerized, the command will set up the required infrastructure to run the code
as well as run the SpringBoot application.
Once the containers are up and running, you can start using exposed endpoints [Here](http://localhost:8080/swagger-ui/index.html#/)

# About the project
## Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/3.5.5/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following Docker images:

* [`postgres:latest`](https://hub.docker.com/_/postgres)
* [`redis:latest`](https://hub.docker.com/_/redis)

Feel free to readjust the version if you want the integration tests to run using the same versions 
as your production environment.

## Concurrency support
This project run on JDK 21. Virtual threads have been enabled as most of the tasks are I/O related.
No extra configuration has been put in place so far. However, the project currently support 100 concurrent calls without fail.
You can check it out using [Apache Benchmark](https://httpd.apache.org/docs/2.4/programs/ab.html) (If you are using macOS, this Apache project is already installed by default). 

To do so, run the following command on a running project:

`ab -n 3000 -c 100 <url_endpoint_to_test>`

This will fire 100 concurrent requests to the desired endpoints until it reaches a total of 3000 requests.