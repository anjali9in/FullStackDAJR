# jwt 
3 parts of jwt
1. Header
2. Payload
3. Signature

1. Header: The header typically consists of two parts: the type of the token (JWT) and the signing algorithm being used, such as HMAC SHA256 or RSA. It is Base64Url encoded to form the first part of the JWT.

    part of header looks like this
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

2. Payload: The payload contains the claims. Claims are statements about an entity (typically, the user) and additional data. There are three types of claims: registered, public, and private claims. The payload is Base64Url encoded to form the second part of the JWT.
    part of payload looks like this
```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022
}
```

3. Signature: To create the signature part, you take the encoded header, the encoded payload, a secret, the algorithm specified in the header, and sign that. The signature is used to verify that the sender of the JWT is who it says it is and to ensure that the message wasn't changed along the way.
 part of signature looks like this
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret)
```
### secret - it is a secret key that is known only to the server and is used to sign the JWT. It should be kept secure and not exposed to clients or third parties. The secret key is used in conjunction with the specified algorithm (e.g., HMAC SHA256) to generate the signature, which ensures the integrity and authenticity of the JWT.
how secret is stored in the server?
The secret key used to sign JWTs should be stored securely on the server to prevent unauthorized access. Here are some best practices for storing the secret key:
1. Environment Variables: Store the secret key in environment variables. This keeps the secret out of your source code and allows for different secrets in different environments (development, staging, production).
2. Configuration Files: Store the secret key in a configuration file that is not checked into version control. Ensure that the configuration file has appropriate permissions set to restrict access.
3. Secret Management Services: Use secret management services or vaults (e.g., AWS Secrets Manager, HashiCorp Vault, Azure Key Vault) to securely store and manage secrets. These services provide encryption, access control, and auditing capabilities

so , the secret key is stored securely on the server using environment variables, configuration files, or secret management services. It is not exposed to clients or third parties and is used in conjunction with the specified algorithm to generate the signature for JWTs, ensuring their integrity and authenticity.



## how jwt works
1. The user logs in with their credentials.
2. The server verifies the credentials and generates a JWT, which is sent back to the user
3. The user stores the JWT (usually in local storage or a cookie) and includes it in the Authorization header of subsequent requests to the server.
4. The server receives the request, extracts the JWT from the Authorization header, and verifies its signature and validity.
5. If the JWT is valid, the server processes the request and sends back the appropriate response. If the JWT is invalid or expired, the server responds with an error (e.g., 401 Unauthorized).

## what jwt produces along with it how it looks
jwt produces an access token and a refresh token. The access token is a short-lived token that is used to access protected resources, while the refresh token is a long-lived token that can be used to obtain a new access token when the current one expires.

example of access token and refresh token looks like this
``` 
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
```

# what is refresh token and how it works
A refresh token is a special kind of token that is used to obtain a new access token without requiring the user to re-authenticate. It is typically issued alongside the access token during the authentication process and has a longer lifespan than the access token.

## how refresh token works
1. When the access token expires, the client sends the refresh token to the server.
2. The server verifies the refresh token and, if valid, issues a new access token (and optionally a new refresh token).
3. The client uses the new access token for subsequent requests.

so access token, jwt and refresh token are used together to provide a secure and efficient authentication mechanism. The access token is used for accessing protected resources, while the refresh token allows the client to obtain a new access token without requiring the user to log in again. This helps improve user experience and security by reducing the need for frequent re-authentication. 

where jwt lyies in the authentication process with refresh token and access token
1. User Authentication: The user logs in with their credentials (username/password).
2. Token Issuance: Upon successful authentication, the server generates an access token (JWT) and a refresh token. The access token is typically short-lived (e.g., 15 minutes), while the refresh token has a longer lifespan (e.g., 7 days).
3. Accessing Protected Resources: The client includes the access token (JWT) in the Authorization header of requests to access protected resources on the server.
4. Token Expiration: When the access token expires, the client uses the refresh token to obtain a new access token from the server.
5. Token Revocation: If the refresh token is revoked or expired, the client must re-authenticate to obtain new tokens.  

How refresh token sent to server in behind or we are sending it manually in somewhere?
So, the refresh token is typically sent to the server automatically by the client application when the access token expires. The client application is responsible for managing the tokens and handling the token refresh process. 

When the access token expires, the client application detects this (usually through a 401 Unauthorized response from the server) and automatically sends a request to the server's token refresh endpoint, including the refresh token in the request body or headers. The server then verifies the refresh token and issues a new access token (and optionally a new refresh token) if the refresh token is valid.

so client means browser? 
Yes, in the context of web applications, the client typically refers to the browser or any front-end application that interacts with the server. The client is responsible for storing the access token and refresh token (usually in local storage, session storage, or cookies) and managing their lifecycle.

When the access token expires, the client (browser) automatically sends a request to the server's token refresh endpoint with the refresh token included. This process is usually handled by the front-end application code, which detects when the access token has expired and initiates the refresh process without requiring user intervention.

yes, the application code how it works in behind to send the refresh token automatically to server when access token expires?
In a typical web application, the front-end code (JavaScript) is responsible for managing the access token and refresh token. Here's how it works behind the scenes to send the refresh token automatically to the server when the access token expires:
1. Token Storage: When the user logs in, the server issues an access token (JWT) and a refresh token. The front-end application stores these tokens securely, often in local storage, session storage, or cookies.
2. Request Interception: The front-end application uses an HTTP client (like Axios, Fetch API, or Angular's HttpClient) to make API requests. It can set up an interceptor or middleware that automatically attaches the access token to the Authorization header of each request.
3. Token Expiration: The access token has a short lifespan (e.g., 15 minutes). When the front-end application makes a request to the server and receives a 401 Unauthorized response, it indicates that the access token has expired.
4. Refresh Token Request: Upon detecting the expired access token, the front-end application automatically sends a request to the server's token refresh endpoint. This request includes the refresh token, which is sent in the request body or headers, depending on the implementation.
5. Server Validation: The server receives the refresh token, validates it, and checks if it is still valid (not expired or revoked). If the refresh token is valid, the server issues a new access token (and optionally a new refresh token) and sends it back to the client.
6. Token Update: The front-end application receives the new access token and updates its stored tokens. It can then retry the original request that failed due to the expired access token, now using the new access token in the Authorization header.
7. Continuous Process: This process continues as long as the refresh token is valid. If the refresh token expires or is revoked, the user will need to re-authenticate (log in again)   


```javascript
// Example using Axios interceptor for sending refresh token automatically dummy
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'https://api.example.com',
});

apiClient.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;
    
        if (error.response.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;
    
        // Get the refresh token from storage
        const refreshToken = localStorage.getItem('refreshToken');
    
        // Send a request to refresh the access token
        const response = await axios.post('https://api.example.com/auth/refresh', { token: refreshToken });
    
        // Update the access token in storage
        localStorage.setItem('accessToken', response.data.accessToken);
    
        // Retry the original request with the new access token
        originalRequest.headers['Authorization'] = `Bearer ${response.data.accessToken}`;
        return apiClient(originalRequest);
        }
    
        return Promise.reject(error);
    }
    );
```


