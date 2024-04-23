# BazAds

:dart: BazAds is a platform for the exchange of goods and services between individuals.

:computer: BazAds is written in Java and uses the TCP protocol for communication between the client and the server. The graphical interface is implemented with a GUI designer and uses the Swing library.

:bulb: A key feature of BazAds is the secure exchange of data between the client and the server. The data is encrypted using the AES algorithm and the key exchange is done using the Diffie-Hellman algorithm.

:rocket: Ready to explore my project? Checkout the [documentation](#documentation)!

## Why this project?

- Get my hand dirty by delving into the implementation of theoritical concepts I learn in my computer science classes.
- Experiment the test-driven development methodology.
- Improve my skills on asynchronous programming in Java.
- Learn how to use the TCP protocol to communicate between a client and a server.
- Become familiar with the Swing library for the graphical interface.
- Enhance my knowledges about Java primitives related to cryptography.
- Maybe the most important: **challenge myself** to build a project from scratch :grinning:.

## Features

- BazAds allows you to publish sales for goods and services.
- You can create, update and remove a sale.
- A communication protocol has been created from scratch to exchange data between the client and the server.

## Test-Driven development

BazAds is developed using the test-driven development methodology. Some functions, including the protocol eschange between the client and the server, are tested using JUnit test. Some manipulations on the graphical user interface are also tested.

To run the tests: `mvn clean compile test`. I currently use Apache Maven 3.8.7.