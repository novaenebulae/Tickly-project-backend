# **API Documentation: Decoupled User and Structure Creation Flow**

This document outlines the revised, simplified API workflow for user registration and structure creation. The new model
decouples these two processes to provide a more intuitive and flexible user journey.

**Core Philosophy:**

1. **Universal Registration**: All users, without exception, register as SPECTATOR. The registration process is simple
   and focused solely on creating the user's identity.
2. **Conditional Action**: After logging in, any SPECTATOR who is not yet associated with a structure will have the
   option to create one.
3. **Role Evolution**: A user's role evolves from SPECTATOR to a staff role (like STRUCTURE\_ADMINISTRATOR) only when
   they either create a structure or accept a team invitation.

This approach eliminates complexity from the initial sign-up, improving the onboarding experience.

### **Scenario 1: Standard User Registration**

This is the single entry point for all new users.

#### **Sequence of Actions**

1. **Registration**: A new user fills out a simple registration form.
2. **Email Validation**: The user receives an email and must click a link to validate their account.
3. **First Login**: Upon successful validation, the user is logged in and receives their first JWT. They now have the
   SPECTATOR role.

#### **Step 1.1: User Registration Request**

* **Endpoint**: POST /api/v1/auth/register
* **Controller**: AuthController.java
* **Description**: Creates a new user account with the default SPECTATOR role.

##### **Request Body (UserRegistrationDto.java)**

The createStructure flag is no longer used and should be removed from the DTO and frontend forms.

{  
"firstName": "Bob",  
"lastName": "Johnson",  
"email": "bob.johnson@email.com",  
"password": "a-secure-password-123",  
"termsAccepted": true  
}

#### **Step 1.2: Email Validation & Login**

* **Endpoint**: GET /api/v1/auth/validate-email?token={validation-token}
* **Controller**: AuthController.java
* **Description**: Validates the user's email. On success, the API returns the first authentication token.

##### **Success Response (200 OK)**

The response body is an AuthResponseDto confirming the user is now a validated SPECTATOR.

{  
"accessToken": "ey...\[JWT\]...",  
"tokenType": "Bearer",  
"expiresIn": 86400000,  
"userId": 15,  
"email": "bob.johnson@email.com",  
"role": "SPECTATOR",  
"structureId": null,  
"needsStructureSetup": false  
}

* **Key Information**: The role is SPECTATOR and structureId is null. The frontend uses this information to display a "
  Create a Structure" option to the user.

### **Scenario 2: A SPECTATOR Creates a Structure**

This flow is initiated by a validated, logged-in user who does not yet belong to a structure.

#### **Sequence of Actions**

1. **User Action**: The SPECTATOR clicks the "Create a Structure" button in the application.
2. **API Request**: The frontend sends a request with the structure's details to the creation endpoint.
3. Backend Logic: The server validates that the user is a SPECTATOR and then:  
   a. Creates the new structure.  
   b. Upgrades the user's role from SPECTATOR to STRUCTURE\_ADMINISTRATOR.  
   c. Associates the user with the newly created structure.
4. **Re-Authentication Required**: The server's response indicates that the user's permissions have changed, and a new
   JWT is required.

#### **Step 2.1: Structure Creation Request**

* **Endpoint**: POST /api/v1/structures
* **Controller**: StructureController.java
* **Authentication**: Authorization: Bearer \<accessToken\> is **required**.
* **Authorization**: The backend must verify that the authenticated user has the SPECTATOR role and that their
  structureId is null.

##### **Request Body (StructureCreationDto.java)**

The request body remains the same as before, containing all necessary structure details.

{  
"name": "The City Auditorium",  
"typeIds": \[1, 5\],  
"address": {  
"street": "456 Market Avenue",  
"city": "Metz",  
"zipCode": "57000",  
"country": "France"  
},  
"email": "contact@cityauditorium.com"  
}

#### **Step 2.2: Success Response & Role Change**

* **Response Body (StructureCreationResponseDto.java)**  
  {  
  "id": 25,  
  "name": "The City Auditorium",  
  "message": "Structure created successfully.",  
  "needsReAuth": true  
  }

* **Critical Action for Frontend**: The needsReAuth: true flag is essential. The user's role has changed from SPECTATOR
  to STRUCTURE\_ADMINISTRATOR. The original JWT is now obsolete. The frontend **must** immediately perform a re-login (
  e.g., call /api/v1/auth/refresh-token endpoint) to acquire a new JWT containing the updated role and the new
  structureId, and then refresh all the user data stored in the frontend app. Without this new token, all subsequent
  administrative actions will fail.

#### **Error Responses**

* **401 Unauthorized**: Missing or invalid JWT.
* **403 Forbidden**: The user is not a SPECTATOR or is already associated with a structure.
* **400 Bad Request**: The StructureCreationDto is invalid.

### **Scenario 3: A SPECTATOR Joins a Team**

This flow describes how a user transitions from SPECTATOR to a staff role by accepting an invitation. This scenario is
already implemented and does not need any actions.

1. **Invitation**: An existing STRUCTURE\_ADMINISTRATOR invites bob.johnson@email.com to their team.
2. **User Action**: Bob, who already has a SPECTATOR account, clicks the invitation link in his email.
3. **API Request**: The link triggers a call to POST /api/v1/team/invitations/accept?token={invitation-token}.
4. **Backend Logic**: The server validates the token, finds Bob's SPECTATOR account, and **upgrades his role** (e.g., to
   ORGANIZATION\_SERVICE), associating him with the structure.
5. **Success Response**: The API returns an InvitationAcceptanceResponseDto containing a **new JWT**. This token
   reflects Bob's new role and structureId. From now on, he will no longer see the "Create a Structure" option.