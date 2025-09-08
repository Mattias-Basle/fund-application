# Context
This project aims at mimicking a small fund transferring system.
In it, you can create your own user and accounts, deposit, withdraw and transfer funds.
Different currencies are supported for mimicking 'international transfers'.

Disclaimer: the project is not link to any banking system so you won't be able to upgrade your bank account with it (sorry about that).

# Getting Started
## Initialize the project
### Requirements
To run the project successfully you need to have both **git** and **docker** installed on your machine. 
You can find the documentation on how to install them here:

* [Git install documentation](https://github.com/git-guides/install-git)
* [Docker engine install documentation](https://docs.docker.com/engine/install/)

### Set It Up
This project being hosted on a public GitHub repository, the first thing to do
is to clone it in your device using the following command with the copied url: 

`git clone <project_url>`

### Run It
Once the project has been cloned, go inside the project's directory then run the following command:

`docker compose up`

And.... That's it!!

The project has been fully dockerized, so the above command will set up the required infrastructure to run the code
as well as running the SpringBoot application itlself.
Once the containers are up and running, you can start playing around using the exposed endpoints [Here](http://localhost:8080/swagger-ui/index.html#/)

# About the project
## Next Steps
### Improve tests
In its current form the project has clashing configurations between the testcontainers and the batch testing configuration (currently disabled on the test suite but fully working in isolation). An invesgation needs to be done to:
* Regain control over the testcontainers lifecycle, making sure they're down before the test suite keeps on going.
* Isolate the batch testing configuration such that no side effect occurs within the testcontainers

### Improve audit mechanism
Currently, auditing the transactions happening within the application is done solely using logs. Adding a Kafka producer (or similar technology) to send audit information through an event pipe for further use should be a nice add-on.

## Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/3.5.5/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers have been configured to use the following Docker images:

* [`postgres:latest`](https://hub.docker.com/_/postgres)
* [`redis:latest`](https://hub.docker.com/_/redis)

Feel free to readjust the version if you want the integration tests to run using the same versions 
as your production environment.

## Concurrency support
This project runs on JDK 21. Virtual threads have been enabled as most of the tasks are I/O related.
No extra configuration has been put in place so far. However, the project currently support 100 concurrent calls without fail.
You can check it out using [Apache Benchmark](https://httpd.apache.org/docs/2.4/programs/ab.html) (If you are using macOS, this Apache project is already installed by default). 

To do so, run the following command on a running project:

`ab -n 3000 -c 100 <url_endpoint_to_test>`

This will fire 100 concurrent requests to the desired endpoints until it reaches a total of 3000 requests and provides overall statistics.
