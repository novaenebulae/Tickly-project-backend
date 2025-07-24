### **Secure Authentication with Refresh Token Rotation: Implementation Plan**

This document outlines the detailed design and implementation steps for integrating a secure authentication mechanism
using short-lived access tokens and long-lived refresh tokens with a rotation and reuse detection strategy.

### **Part 1: Backend Implementation (Spring Boot)**

The backend is the authority for issuing, validating, and managing token lifecycle.

#### **1.1. Database Schema**

A new table is required to securely store refresh tokens.

* **Create RefreshToken Entity:**
    * id: Long (Primary Key, auto-generated).
    * token: String (Unique, indexed). **Crucially, this will store a hashed version of the token, not the raw token.**
    * expiryDate: Instant. The exact timestamp when the token becomes invalid.
    * user: User (Many-to-One relationship, linked to the users table).
    * revoked: boolean (default false). A flag to indicate if the token has been used or invalidated.
* **Create RefreshTokenRepository:**
    * An interface extending JpaRepository\<RefreshToken, Long\>.
    * Define a method: Optional\<RefreshToken\> findByToken(String token);
    * Define a method to revoke all tokens for a user: void deleteAllByUser(User user);

#### **1.2. Configuration**

Update application.properties to manage token lifecycles.

\# JWT Secret Key (ensure this is stored securely, e.g., in environment variables)  
jwt.secret=your-very-secret-key

\# Access Token expiration in milliseconds (e.g., 15 minutes)  
jwt.access-token.expiration-ms=900000

\# Refresh Token expiration in milliseconds (e.g., 7 days)  
jwt.refresh-token.expiration-ms=604800000

#### **1.3. Service Layer**

* **Create RefreshTokenService:** This service will encapsulate all logic related to refresh tokens.
    * createRefreshToken(User user): Generates a new secure random token, hashes it, creates a RefreshToken entity,
      saves it to the DB, and returns the raw (unhashed) token string.
    * verifyExpiration(RefreshToken token): Checks if a token is expired and throws an exception if it is.
    * findByToken(String token): Finds a token by its (hashed) value in the DB.
    * revokeToken(RefreshToken token): Sets the revoked flag to true for a given token.
    * revokeAllUserTokens(User user): Deletes all refresh tokens associated with a user. Used for critical security
      events.
* **Update JwtTokenProvider:**
    * Modify generateToken() to generateAccessToken(UserDetails userDetails) to make its purpose clear.
    * The refresh token itself does not need to be a JWT; a secure, random string is sufficient and recommended.
* **Update AuthServiceImpl:**
    * Inject RefreshTokenService.
    * Modify the login() method:
        1. After successful authentication, generate the accessToken.
        2. Call refreshTokenService.createRefreshToken(user) to get a new refresh token.
        3. Return both the accessToken and the new refreshToken in the AuthResponseDto.

#### **1.4. Controller Layer (AuthController)**

* **Update /api/auth/login Endpoint:**
    * Ensure the AuthResponseDto returned by this endpoint now includes both accessToken and refreshToken fields.
* **Create POST /api/auth/refresh Endpoint:**
    * This endpoint will accept a request body containing the refreshToken.
    * **Logic:**
        1. Retrieve the raw refreshToken from the request. Hash it to look it up in the database.
        2. Use refreshTokenService.findByToken() to find the token entity.
        3. **Security Check 1:** If the token is not found or is already revoked, it signifies a potential token theft
           or reuse. Immediately call refreshTokenService.revokeAllUserTokens() for the user associated with the (now
           invalid) token and return 401 Unauthorized.
        4. **Security Check 2:** Call refreshTokenService.verifyExpiration(). If expired, return 401 Unauthorized.
        5. If valid, get the associated User.
        6. Perform Rotation:  
           a. Call refreshTokenService.revokeToken() on the old token.  
           b. Generate a new accessToken using jwtTokenProvider.  
           c. Generate a new refreshToken using refreshTokenService.createRefreshToken().
        7. Return a new AuthResponseDto containing the new accessToken and refreshToken.
* **Create POST /api/auth/logout Endpoint:**
    * Accepts the refreshToken in the request body.
    * Finds the token in the database and revokes/deletes it.
    * This ensures the token cannot be used to refresh a session after the user has explicitly logged out.

#### **1.5. Security Configuration (SecurityConfig)**

* Update the security filter chain to permit anonymous access to the new endpoints:
    * .requestMatchers("/api/auth/refresh", "/api/auth/logout").permitAll()

### **Part 2: Frontend Implementation (Angular)**

The frontend will manage token storage and automate the refresh process via an HTTP interceptor.

#### **2.1. Configuration (app-config.ts)**

* Add the new endpoints and a storage key.

// in api.endpoints.auth  
login: 'auth/login',  
register: 'auth/register',  
refreshToken: 'auth/refresh', // Add this  
logout: 'auth/logout', // Add this  
// ...

// in auth config  
tokenKey: 'APP\_AUTH\_TOKEN',  
refreshTokenKey: 'APP\_REFRESH\_TOKEN', // Add this  
keepLoggedInKey: 'APP\_KEEP\_LOGGED\_IN',  
// ...

#### **2.2. Models (auth.model.ts)**

* Update AuthResponseDto to include the refreshToken.

export interface AuthResponseDto {  
accessToken: string;  
refreshToken: string; // Add this  
expiresIn: number;  
// ... other user properties  
}

#### **2.3. Service Layer**

* **Update AuthApiService (auth-api.service.ts):**
    * Add a refreshToken(token: string): Observable\<AuthResponseDto\> method that makes a POST request to the
      /api/auth/refresh endpoint.
    * Add a logout(token: string): Observable\<void\> method that makes a POST request to /api/auth/logout.
* **Update AuthService (auth.service.ts):**
    * **Token Storage:**
        * The accessToken should remain in memory (e.g., a signal or private variable).
        * The refreshToken must be stored persistently in localStorage.
    * **Update login():**
        * On successful login, call a new private method this.storeTokens(response).
    * **Create storeTokens(response: AuthResponseDto):**
        * Stores the accessToken in the service's memory/signal.
        * Stores the refreshToken in localStorage using APP\_CONFIG.auth.refreshTokenKey.
    * **Update logout():**
        1. Retrieve the refreshToken from localStorage.
        2. Call authApi.logout(refreshToken).
        3. In the finalize block of the observable pipe, call clearAuthData() to remove all tokens from storage and
           reset state, regardless of API call success.
    * **Create refreshToken() method:** This will be called by the interceptor. It should handle the logic of calling
      the API and storing the new tokens.

#### **2.4. HTTP Interceptor (jwt.interceptor.ts)**

This is the core of the frontend logic. It needs to be updated to handle token expiration and refresh automatically.

* **Modify jwtInterceptor:**
    * The interceptor will now need to handle 401 errors to trigger the refresh flow.
    * It must also handle concurrent requests while a refresh is in progress to avoid multiple refresh calls.
* **Detailed Interceptor Logic:**
    1. **Add accessToken to request:** This logic remains the same.
    2. **Error Handling (pipe with catchError):**
        * If the request fails with a 401 Unauthorized error:  
          a. Check if a token refresh is already in progress. If so, queue the failed request until the new token is
          available.  
          b. If not, start the refresh process by calling authService.refreshToken().  
          c. On successful refresh:  
          i. A new accessToken is now available.  
          ii. Retry the original failed request with the new token.  
          iii. Complete the queued requests.  
          d. On failed refresh:  
          i. The refreshToken is invalid.  
          ii. Call authService.logout() to clean up the session.  
          iii. Redirect the user to the login page.  
          iv. Propagate the original error.
        * If the error is not a 401, re-throw the error.

### **Part 3: Implementation Task List**

#### **Backend Tasks**

* [ ] **DB:** Create the RefreshToken entity class with all specified fields and relationships.
* [ ] **DB:** Create the RefreshTokenRepository interface with required methods.
* [ ] **Config:** Update application.properties with JWT secret and expiration times for both token types.
* [ ] **Service:** Create the RefreshTokenService with methods for creating, verifying, finding, and revoking tokens.
* [ ] **Service:** Update AuthServiceImpl to use RefreshTokenService during the login process.
* [ ] **Controller:** Update AuthController's login response to include the refreshToken.
* [ ] **Controller:** Implement the POST /api/auth/refresh endpoint with full rotation and reuse detection logic.
* [ ] **Controller:** Implement the POST /api/auth/logout endpoint.
* [ ] **Security:** Update SecurityConfig to allow public access to /refresh and /logout.
* [ ] **Task:** (Optional but recommended) Create a @Scheduled task to periodically delete expired/revoked tokens from
  the database.

#### **Frontend Tasks**

* [ ] **Config:** Update app-config.ts with new endpoints and the refreshTokenKey.
* [ ] **Model:** Add the refreshToken property to the AuthResponseDto interface in auth.model.ts.
* [ ] **API Service:** Add refreshToken() and logout() methods to AuthApiService.
* [ ] **Auth Service:** Update AuthService to handle refreshToken storage in localStorage.
* [ ] **Auth Service:** Modify the login() method to store both tokens.
* [ ] **Auth Service:** Implement the public logout() method to call the API and clear local state.
* [ ] **Auth Service:** Implement the refreshToken() logic to be used by the interceptor.
* [ ] **Interceptor:** Refactor jwt.interceptor.ts to include the catchError logic for handling 401 responses.
* [ ] **Interceptor:** Implement the request queuing mechanism to handle concurrent API calls during a token refresh.
* [ ] **Testing:** Thoroughly test the login, logout, session expiration, and automatic refresh flows. Test the reuse
  detection security feature.