# BazAds

:dart: Welcome to BazAds, a platform designed for the seamsless the exchange of goods and services between individuals.

:computer: BazAds is written in Java and implements the TCP protocol for communication between clients and the server. The graphical interface is created with a GUI designer and is powered by the Swing library.

:bulb: A key feature of BazAds is the secure exchange of data between the client and the server. The data is encrypted using the Advanced Encryption Standard (AES) algorithm. Furthermore, the key exchange is done using the Diffie-Hellman algorithm.

:rocket: Ready to explore my project? Checkout the [documentation](https://groumage.github.io/PetitesAnnonces/Doxygen/index.html)! Don't hesitate to explore the code itself, it has a well self-explanatory structure and documentation :smile:.

## Why this project?

- Dive deep into the practical implementation of theoretical concepts learned in my computer science classes.
- Embrace the discipline of test-driven development (TDD).
- Enhance my skills in Java asynchronous programming.
- Become familiar with the Swing library for graphical interfaces.
- Improve my knowledge about Java primitives related to cryptography.
- The most important: **challenge myself** to build a project from scratch :grinning:.

## Features

- A communication protocol has been created from scratch: the RFC is available [here](https://groumage.github.io/PetitesAnnonces/Doxygen/rfc_top.html).
- BazAds allows you to publish sales for goods and services.
- Clients can create, update, and remove a sale.
- All exchanges are encrypted.

## Test-Driven development

BazAds follows a test-driven development methodology. JUnit tests are used to verify client-server protocol exchanges: the server's behavior is tested regarding some requests sent by a fictitious client. Furthermore, some GUI manipulations are also tested.

### How to run the tests?

Pre-requisites:
- Apache Maven 3.8.7
- Java 17

`mvn clean compile test`