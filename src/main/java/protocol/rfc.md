# RFC {#rfc}

## 1. Introduction and table of contents

This document specifies the protocol used in the project BazAds.

1. [Introduction](#1-introduction)
2. [Request from clients to the central server](#request_from_client_to_server)
    1. [Get the public key of the central server](#get_server_public_key)
    2. [Sign up](#sign_up)
    3. [Sign in](#sign_in)
    4. [Sign out](#sign_out)
    5. [Create a sale](#create_sale)
    6. [Update a sale](#update_sale)
    7. [Delete a sale](#delete_sale)
    8. [List of domains](#domains_list)
    9. [Sales from a domain](#sales_from_domain)
3. [Request from the central server to client](#request_from_server_to_client)
    1. [Get the public key of the central server](#get_server_public_key_responses)
        1. [Success](#get_server_public_key_success)
        2. [Failure](#get_server_public_key_failure)
    2. [Sign up](#sign_up_responses)
        1. [Success](#sign_up_success)
        2. [Failure](#sign_up_failure)
    3. [Sign in](#sign_in_responses)
        1. [Success](#sign_in_success)
        2. [Failure](#sign_in_failure)
    4. [Sign out](#sign_out_responses)
        1. [Success](#sign_out_success)
        2. [Failure](#sign_out_failure)
    5. [Create a sale](#create_sale_responses)
        1. [Success](#create_sale_success)
        2. [Failure](#create_sale_failure)
    6. [Update a sale](#update_sale_responses)
        1. [Success](#update_sale_success)
        2. [Failure](#update_sale_failure)
    7. [Delete a sale](#delete_sale_responses)
        1. [Success](#delete_sale_success)
        2. [Failure](#delete_sale_failure)
    8. [List of domains](#domains_list_responses)
        1. [Success](#domains_list_success)
        2. [Failure](#domain_list_failure)
    9. [Sales from a domain](#sales_from_domain_responses)
        1. [Success](#sales_from_domain_success)
        2. [Failure](#sales_from_domain_failure)
4. [Serialization and deserialization of the requests](#serialization_deserialization)

## 2. Request from clients to the central server server {#request_from_client_to_server}

### 2.1 Get the public key of the central server {#get_server_public_key}

All exchanges between a client and the central server are encrypted. To that end, both the client and server has a public and private key pair. After exchanging public keys and performing key agreement, a shared secret is derived and used to encrypt all subsequent communications.

#### Request

`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER (client's public key)`

| Variable                  | Type         |
| :-----------------------: | :----------: |
| Client's public key       | PublicKey    |

#### Expected responses

[`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK`](#get_server_public_key_success)

[`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO`](#get_server_public_key_failure)

#### Sequence diagram

@mermaid{request_public_key_of_central_server}

[Back to top](#rfc)

### 2.2 Sign up {#sign_up}

A client requests to sign up by sending its mail, name and password.

#### Request

`SIGN_UP (mail, name, password)`

| Variable   | Type      |
| :--------: | :-------: |
| mail       | String    |
| name       | String    |
| password   | String    |

#### Expected responses

[`SIGN_UP_OK`](#sign_up_success)

[`SIGN_UP_KO`](#sign_up_failure)

#### Sequence diagram

@mermaid{sign_up}

[Back to top](#rfc)

### 2.3 Sign in {#sign_in}

A client requests to sign in by its mail and password to sign in. In addition, it sends a boolean value to determine if, in case of successful sign in, the server send also the list of the dommains.

#### Request

`SIGN_IN (mail, password, sendDomainList)`

| Variable         | Type       |
| :--------------: | :--------: |
| mail             | String     |
| password         | String     |
| sendDomainList   | Boolean    |

#### Expected responses

[`SIGN_IN_OK`](#sign_in_success)

[`SIGN_IN_KO`](#sign_in_failure)

#### Sequence diagram

@mermaid{sign_in}

[Back to top](#rfc)

### 2.4 Sign out {#sign_out}

A client just have to notify the server that he signs out.

#### Request

`SIGN_OUT`

#### Expected responses

[`SIGN_OUT_OK`](#sign_out_success)

[`SIGN_OUT_KO`](#sign_out_failure)

#### Sequence diagram

@mermaid{sign_out}

[Back to top](#rfc)

### 2.5 Create a sale {#create_sale}

A client has to send the domain, the name, the content and the price for the new sale.

#### Request

`CREATE_SALE (domain, title, content, price)`

| Variable         | Type       |
| :--------------: | :--------: |
| domain           | Domain     |
| title            | String     |
| content          | String     |
| price            | int        |

#### Expected responses

[`CREATE_SALE_OK`](#create_sale_success)

[`CREATE_SALE_KO`](#create_sale_failure)

#### Sequence diagram

@mermaid{create_sale}

[Back to top](#rfc)

### 2.6 Update a sale {#update_sale}

A client has to send the new title, the new content, the new price of the sale and the id of the sale being updated. The domain can't be changed.

#### Request

`UPDATE_SALE (title, content, price, id)`

| Variable         | Type       |
| :--------------: | :--------: |
| title            | String     |
| content          | String     |
| price            | int        |
| id               | int        |

#### Expected responses

[`UPDATE_SALE_OK`](#update_sale_success)

[`UPDATE_SALE_KO`](#update_sale_failure)

#### Sequence diagram

@mermaid{update_sale}

[Back to top](#rfc)

### 2.7 Delete a sale {#delete_sale}

A client has to send the id of the sale he wants to delete. A client can delete a sale only if he's its owner.

#### Request

`DELETE_SALE (id)`

| Variable         | Type       |
| :--------------: | :--------: |
| id               | int        |

#### Expected responses

[`DELETE_SALE_OK`](#delete_sale_success)

[`DELETE_SALE_KO`](#delete_sale_failure)

#### Sequence diagram

@mermaid{delete_sale}

[Back to top](#rfc)

### 2.8 List of domains {#domains_list}

A client just have to notify the server to the server that he wants the list of the domains. 

#### Request

`DOMAINS_LIST`

#### Expected responses

[`DOMAINS_LIST_OK`](#domains_list_success)

[`DOMAINS_LIST_KO`](#domains_list_failure)

#### Sequence diagram

@mermaid{domains_list}

[Back to top](#rfc)

### 2.9 Sales from a domain {#sales_from_domain}

A client has to send the domains from which he wants to retrieve the sales.

#### Request

`SALES_FROM_DOMAIN (domain)`

| Variable         | Type       |
| :--------------: | :--------: |
| domain           | Domain     |

#### Expected responses

[`SALE_FROM_DOMAIN_OK`](#sales_from_domain_success)

[`SALE_FROM_DOMAIN_KO`](#sales_from_domain_failure)

#### Sequence diagram

@mermaid{sales_from_domain}

[Back to top](#rfc)

## 3. Request from the central server to client {#request_from_server_to_client}

### 3.1 Get the public key of the central server {#get_server_public_key_responses}

### 3.1.1 Success {#get_server_public_key_success}

The server sends the public key to the client.

#### Request

`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK (server's public key)`

| Variable            | Type       |
| :-----------------: | :--------: |
| Server's public key | PublicKey  |

Answer to request [`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER`](#get_server_public_key).

[Back to top](#rfc)

### 3.1.2 Failure {#get_server_public_key_failure}

The server sends an error message to the client.

Reason of failure:
- the server process the incoming request but don't answer to them.

#### Request

`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| Message     | String     |

Answer to request [`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER`](#get_server_public_key).

[Back to top](#rfc)

### 3.2 Sign up {#sign_up_responses}

### 3.2.1 Success {#sign_up_success}

In case of sign up success, the server sends a confirmation of sign up with the mail and the name of the client.

#### Request

`SIGN_UP_OK (mail, name)`

| Variable    | Type       |
| :---------: | :--------: |
| mail        | String     |
| name        | String     |

Answer to request [`SIGN_UP`](#sign-up).

[Back to top](#rfc)

### 3.2.2 Failure {#sign_up_failure}

In case of sign up failure, the server sends a message to the client indicating the error.

Reason of failures:
- the server process the incoming request but don't answer to them;
- the mail is not a valid format;
- the mail is already taken;
- the name is not valid.

#### Request

`SIGN_UP_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| message     | String     |

Answer to request [`SIGN_UP`](#sign_up).

[Back to top](#rfc)

## 3.3 Sign in {#sign_in_responses}

### 3.3.1 Success {#sign_in_success}

In case of sign in success, the server sends a confirmation of sign in with the name of the client.

#### Request

`SIGN_IN_OK (name)`

| Variable    | Type       |
| :---------: | :--------: |
| name        | String     |

Answer to request [`SIGN_IN`](#sign_in).

[Back to top](#rfc)

### 3.3.2 Failure {#sign_in_failure}

In case of sign in failure, the server sends an error message to the client.

Reason of failure:
- the server process the incoming request but don't answer to them;
- the mail is not registered;
- the mail password is invalid.

#### Request

`SIGN_IN_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| message     | String     |

Answer to request [`SIGN_IN`](#sign_in).

[Back to top](#rfc)

### 3.4 Sign out {#sign_out_responses}

### 3.4.1 Success {#sign_out_success}

The server send a confirmation of sign out.

#### Request

`SIGN_OUT_OK`

Answer to request [`SIGN_OUT`](#sign_out).

[Back to top](#rfc)

### 3.4.2 Failure {#sign_out_failure}

The server send an error message to the client.

Reason of failure:
- the server process the incoming request but don't answer to them.

#### Request

`SIGN_OUT_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| message     | String     |

Answer to request [`SIGN_OUT](#`sign_out).

[Back to top](#rfc)

## 3.5 Create a sale {#create_sale_responses}

### 3.5.1 Success {#create_sale_success}

The server send the confirmation of the creation of the sale with the title of the sale.

#### Request

`CREATE_SALE_OK (title)`

| Variable    | Type       |
| :---------: | :--------: |
| Title       | String     |

Answer to request [`CREATE_SALE`](#create_sale).

[Back to top](#rfc)

### 3.5.2 Failure {#create_sale_failure}

The server send an error message to the client.

Reason of failure:
- the server process the incoming request but don't answer to them.

#### Request

`CREATE_SALE_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| Message     | String     |

Answer to request [`CREATE_SALE`](#create_sale).

[Back to top](#rfc)

## 3.6 Update a sale {#update_sale_responses}

### 3.6.1 Success {#update_sale_success}

The server send the confirmation of the update of the sale.

#### Request

`UPDATE_SALE_OK`

Answer to request [`UPDATE_SALE`](#update_sale).

[Back to top](#rfc)

### 3.6.2 Failure {#update_sale_failure}

The server send an error message to the client.

Reason of failure:
- the server process the incoming request but don't answer to them.

#### Request

`UPDATE_SALE_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| Message     | String     |

Answer to request [`UPDATE_SALE`](#update_sale).

[Back to top](#rfc)

## 3.7 Delete a sale {#delete_sale_responses}

### 3.7.1 Success {#delete_sale_success}

The server send the confirmation of the deletion of the sale.

#### Request

`DELETE_SALE_OK`

Answer to request [`DELETE_SALE`](#delete_sale).

[Back to top](#rfc)

### 3.7.2 Failure {#delete_sale_failure}

The server send an error message to the client.

Reason of failure:
- the server process the incoming request but don't answer to them.

#### Request

`DELETE_SALE_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| Message     | String     |

Answer to request [`DELETE_SALE`](#delete_sale).

[Back to top](#rfc)

## 3.8 List of domains {#domains_list_responses}

### 3.8.1 Success {#domains_list_success}

The server send the list of the domains.

#### Request

`DOMAIN_LIST_OK (domains)`

| Variable    | Type       |
| :---------: | :--------: |
| domains     | Domains[]  |

Answer to request [`DOMAINS_LIST`](#domains_list).

[Back to top](#rfc)

### 3.8.2 Failure {#domains_list_failure}

The server send an error message to the client.

#### Request

Reason of failure:
- the server process the incoming request but don't answer to them.

`DOMAIN_LIST_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| Message     | String     |

Answer to request [`DOMAINS_LIST`](#domains_list).

[Back to top](#rfc)

## 3.9 Sales from a domain {#sales_from_domain_responses}

### 3.9.1 Success {#sales_from_domain_success}

The server send the list of the sales on the specific domain.

#### Request

`SALE_FROM_DOMAIN_OK (sales)`

| Variable    | Type         |
| :---------: | :----------: |
| sales       | Annonce[]    |

Answer to request [`SALE_FROM_DOMAIN`](#sales_from_domain).

[Back to top](#rfc)

### 3.9.2 Failure {#sales_from_domain_failure}

The server send an error message to the client.

#### Request

Reason of failure:
- the server process the incoming request but don't answer to them.

`SALE_FROM_DOMAIN_KO (message)`

| Variable    | Type       |
| :---------: | :--------: |
| Message     | String     |

Answer to request [`SALE_FROM_DOMAIN`](#sales_from_domain).

[Back to top](#rfc)

## 4. Serialization and deserialization of the requests {#serialization_deserialization}

The following diagram shows the serialization and deseriialization of a request. It holds when the request is encrypted, that is everytime except for the request `REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER` and their associated responses `REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK` and `REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO`.

@mermaid{request_serialization_deserialization}

[Back to top](#rfc)