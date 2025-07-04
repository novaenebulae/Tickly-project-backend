# **API Documentation: Structure & User Account Deletion Flow**

This document outlines the API workflows for deleting a structure or a user account associated with a structure. The
process differs significantly based on the user's role (STRUCTURE\_ADMINISTRATOR vs. other StaffUser roles like
ORGANIZATION\_SERVICE) to ensure data integrity and prevent accidental loss of control over a structure.

### **1\. Core Principles & Safeguards**

The deletion logic is built around two key principles:

1. **A structure must always have an owner**: A STRUCTURE\_ADMINISTRATOR cannot delete their own account if they are the
   last remaining administrator of an active structure.
2. **A structure with published events cannot be deleted**: To prevent data inconsistencies, a structure can only be
   deleted if it has no PUBLISHED events. Drafts do not block the deletion.

### **Scenario 1: Structure Administrator Deletes the Entire Structure**

This is the most critical action, resulting in the permanent, irreversible deletion of a structure and the dissolution
of its team. Only the STRUCTURE\_ADMINISTRATOR who owns the structure can perform this action.

* **User Role**: STRUCTURE\_ADMINISTRATOR
* **Primary Service Logic**: StructureServiceImpl.java

#### **Sequence of Actions**

1. **Request**: The administrator sends a DELETE request to the structure endpoint.
2. **Validation**: The backend (StructureServiceImpl) performs several crucial checks:
    * Verifies that the authenticated user is the owner of the structure.
    * **Safeguard**: Checks if there are any associated events with a status of **PUBLISHED**. If any exist, the request
      is rejected. Events in all other statuses do not prevent deletion.
3. **Execution**: If validation passes, the service executes the following operations in a single transaction:
    * **Dissolves the Team**: All team members associated with the structure are converted back to SPECTATOR roles, and
      their link to the structure is severed (TeamManagementServiceImpl.dissolveTeam).
    * **Anonymizes the Structure**: The structure's sensitive information (name, description, contact details) is
      anonymized to maintain data privacy while preserving relational integrity. It is marked as inactive.
    * **Releases the Administrator**: The administrator's account is detached from the structure, and their
      needsStructureSetup flag is set back to true, allowing them to potentially create a new structure in the future.
4. **Confirmation**: A confirmation email is sent to the administrator, notifying them that the structure has been
   successfully deleted.

#### **API Endpoint**

* **Endpoint**: DELETE /api/v1/structures/{structureId}
* **Controller**: StructureController.java
* **Authentication**: Authorization: Bearer \<accessToken\> is required.
* **Authorization**: The user must be the owner of the structure.

#### **Responses**

* **204 No Content**: The structure was successfully deleted and anonymized.
* **403 Forbidden**: The authenticated user is not the owner of the structure.
* **404 Not Found**: The structure with the given structureId does not exist.
* **400 Bad Request**: The deletion is blocked because the structure still has published events. The error message will
  guide the user on how to resolve this.

### **Scenario 2: Structure Administrator Deletes Their Own Account**

A STRUCTURE\_ADMINISTRATOR can request to delete their personal account, but this action is protected by safeguards to
prevent leaving a structure ownerless.

* **User Role**: STRUCTURE\_ADMINISTRATOR
* **Primary Service Logic**: UserServiceImpl.java

#### **Sequence of Actions**

This is a two-step, email-confirmed process.

**Step A: Request Account Deletion**

1. **Request**: The administrator sends a request to delete their own account.
2. **Validation**: The backend (UserServiceImpl.requestAccountDeletion) checks:
    * **Safeguard**: If the user is the **last remaining administrator** for their associated structure. This is done by
      checking the count of other STRUCTURE\_ADMINISTRATOR roles in the team (
      TeamManagementServiceImpl.countAdminsForStructure).
    * If they are the last admin, the request is **rejected** with an informative error message.
    * If other administrators exist, the process continues.
3. **Confirmation Email**: A verification token is generated, and an email is sent to the user with a confirmation link
   to finalize the deletion.

**Step B: Confirm Account Deletion**

1. **Request**: The user clicks the link in the email, which calls the confirmation endpoint with the token.
2. **Execution**: The backend (UserServiceImpl.confirmAccountDeletion) validates the token and proceeds to **anonymize**
   the user's data (name, email, etc.) and associated personal information (tickets, favorites). The user account itself
   is not hard-deleted.
3. **Final Notification**: A final email is sent to the user's original address confirming that their account has been
   deleted.

#### **API Endpoints**

1. **Request Deletion**
    * **Endpoint**: DELETE /api/v1/users/me
    * **Controller**: UserController.java
    * **Responses**:
        * **200 OK**: The request was successful, and the confirmation email has been sent.
        * **400 Bad Request**: The request is blocked because the user is the sole administrator of their structure.
2. **Confirm Deletion**
    * **Endpoint**: DELETE /api/v1/users/confirm-deletion?token={deletion-token}
    * **Controller**: UserController.java
    * **Responses**:
        * **200 OK**: The account has been successfully anonymized.
        * **400 Bad Request**: The token is invalid or has expired.

### **Scenario 3: Staff User (Non-Admin) Deletes Their Account**

A staff member with a role like ORGANIZATION\_SERVICE or RESERVATION\_SERVICE can choose to delete their account at any
time. The system will automatically handle their removal from the team.

* **User Role**: StaffUser (e.g., ORGANIZATION\_SERVICE)
* **Primary Service Logic**: UserServiceImpl.java

#### **Sequence of Actions**

This is a two-step, email-confirmed process that gives the user full autonomy.

**Step A: Request Account Deletion**

1. **Request**: The staff user sends a request to delete their own account via the standard endpoint.
2. **Execution**: The backend (UserServiceImpl.requestAccountDeletion) identifies the user as a StaffUser. Since they
   are not a STRUCTURE\_ADMINISTRATOR, no special ownership checks are needed.
3. **Confirmation Email**: A verification token is generated, and an email is sent to the user with a confirmation link
   to finalize the deletion.

**Step B: Confirm Account Deletion**

1. **Request**: The user clicks the link in the email, which calls the confirmation endpoint with the token.
2. **Execution**: The backend (UserServiceImpl.confirmAccountDeletion) validates the token and performs the following
   actions:
    * **Detaches from Team**: Before anonymization, the system removes the user's TeamMember record, effectively
      detaching them from the structure's team.
    * **Anonymizes the User**: It proceeds to anonymize the user's personal data (name, email, etc.) and associated
      information (tickets, favorites).
3. **Final Notification**: A final email is sent to the user's original address confirming that their account has been
   deleted.

#### **API Endpoints**

The flow uses the same standard endpoints as any other user, providing a consistent experience.

1. **Request Deletion**
    * **Endpoint**: DELETE /api/v1/users/me
    * **Controller**: UserController.java
    * **Response**: **200 OK**.
2. **Confirm Deletion**
    * **Endpoint**: DELETE /api/v1/users/confirm-deletion?token={deletion-token}
    * **Controller**: UserController.java
    * **Response**: **200 OK**.