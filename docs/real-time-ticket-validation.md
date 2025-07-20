# **Advanced Ticket Validation Feature: Design Document**

## **1\. Introduction & Goals**

### **1.1. Overview**

This document outlines the design and architecture for an advanced, real-time ticket validation system. The primary goal
is to provide authorized event staff with a comprehensive tool to manage attendee check-in efficiently. The system will
feature a dedicated control panel for each event, offering a live view of ticket statuses, powerful search and filtering
capabilities, and real-time statistical feedback on event attendance.

### **1.2. Key Objectives**

* **Empower Staff:** Provide Structure Staff and StaffUser roles with a secure and intuitive interface to validate
  tickets for specific events.
* **Real-Time Collaboration:** Ensure that all staff members using the validation panel see the same data in real-time.
  When one staff member validates a ticket, the status should instantly update for everyone.
* **Efficient Search & Filtering:** Allow staff to quickly find any ticket using various criteria (attendee name, email,
  ticket UUID) and filter the list by status (e.g., VALID, USED, CANCELLED).
* **Live Event Insights:** Display key performance indicators (KPIs) such as the overall fill rate and the number of
  tickets scanned versus the total, updated in real-time.
* **Scalability:** The architecture must handle numerous simultaneous connections from staff members during peak
  check-in times.

## **2\. User Roles & Permissions**

* **Primary Actor:** Structure Staff / StaffUser.
* **Permissions:** A user with this role, who is associated with the structure organizing an event, must be granted
  access to the validation panel for that specific event. Access to other events' panels must be restricted. The
  existing EventSecurityService on the backend should be leveraged to enforce these rules.

## **3\. Frontend Design & User Flow**

### **3.1. Entry Point**

* In the existing EventsPanel component (or equivalent admin/staff event list), each event row will feature a new "
  Validate Tickets" or "Check-in" button.
* This button will only be visible and enabled for users with the appropriate permissions for that specific event.
* Clicking this button will navigate the user to a new, dedicated route, for example:
  /admin/events/{eventId}/validation.

### **3.2. Validation Panel Component**

This new component will serve as the central hub for all validation activities for a single event. It will be composed
of three main sections:

#### **3.2.1. Live Statistics Dashboard**

* **Location:** Prominently displayed at the top of the panel.
* **Content:**
    * **Event Fill Rate:** A percentage gauge or large number showing (used tickets / total sold tickets) \* 100\.
    * **Scanned Tickets Counter:** A clear display showing number of used tickets out of total sold tickets (e.g., "
      542 / 1200").
    * **Tickets Remaining:** A count of tickets with the status VALID that have not yet been checked in.
* **Behavior:** All statistics on this dashboard must update automatically in real-time without requiring a page
  refresh.

#### **3.2.2. Controls: Search and Filtering**

* **Location:** Positioned below the statistics dashboard.
* **Elements:**
    * **Search Bar:** A single input field for a global search. The user can type a spectator's first name, last name,
      email, or the ticket's UUID. The search should trigger automatically after a short debounce interval (e.g., 300ms)
      to avoid excessive API calls.
    * **Status Filter:** A dropdown or tab group allowing staff to filter the ticket list by status: All, Valid, Used,
      Cancelled.

#### **3.2.3. Paginated Ticket List**

* **Location:** The main content area of the panel.
* **Display:** A data table (e.g., mat-table) will display the list of tickets.
* **Columns:** Each row will represent a single ticket and display the following information:
    * **Participant:** First Name and Last Name.
    * **Ticket ID:** The last 8 characters of the ticket's UUID for quick identification.
    * **Status:** A color-coded chip or badge indicating the ticket's current status (VALID, USED, CANCELLED).
    * **Audience Zone:** The name of the zone the ticket is for.
    * **Validation Time:** The timestamp of when the ticket was validated (empty if not yet used).
    * **Actions:** A column containing the "Validate" button.
* **Behavior:**
    * The "Validate" button will be enabled only for tickets with the status VALID.
    * Clicking "Validate" triggers the validation process. The button should enter a disabled/loading state until the
      action is confirmed.
    * The list will be paginated to ensure performance, loading a manageable number of tickets at a time (e.g., 20 per
      page).

## **4\. Backend Architecture & API**

The backend will expose a combination of REST endpoints for actions and data retrieval, and a WebSocket for real-time
communication.

### **4.1. REST Endpoints**

To maintain a logical and RESTful API structure, the new endpoints will be placed within the existing controllers where
they fit most naturally. Management actions related to an event's resources (like its tickets) will be nested under the
events path.

* **GET /api/v1/events/{eventId}/management/tickets**
    * **Description:** Retrieves a paginated and filtered list of all tickets for a specific event, designed for staff
      management purposes. This clearly separates it from public or user-facing ticket lists.
    * **Controller:** EventController.java
    * **Authorization:** Restricted to authorized staff for the given {eventId} (authorized for the staff of the event
      structure).
    * **Query Parameters:**
        * page (integer, default: 0): The page number for pagination.
        * size (integer, default: 20): The number of items per page.
        * status (string, optional): Filters tickets by TicketStatus enum (e.g., "VALID").
        * search (string, optional): The search term to query against participant name, email, and ticket UUID.
    * **Response:** ResponseEntity\<PaginatedResponseDto\<TicketResponseDto\>\>
* **POST /api/v1/events/{eventId}/management/tickets/{ticketId}/validate**
    * **Description:** Marks a specific ticket as USED. This endpoint is designed for manual validation from the staff
      panel (e.g., clicking a button on a row).
    * **Controller:** EventController.java
    * **Authorization:** Restricted to authorized staff for the given {eventId}.
    * **Request Body:** None required.
    * **Response:** ResponseEntity\<TicketValidationResponseDto\>. The existing TicketValidationResponseDto can be
      reused as it perfectly fits the need.
    * **Side Effects:**
        1. Updates the ticket's status to USED in the database.
        2. Broadcasts the updated ticket and event statistics via WebSocket.

### **4.2. WebSocket Architecture**

We will use Spring WebSocket with a STOMP message broker.

* **Connection Endpoint:** A single WebSocket endpoint will be established at /ws-tickly.
* **Topics:** Communication will be organized by event-specific topics to ensure clients only receive relevant updates.
    * **/topic/event/{eventId}/ticket-update**: When a ticket is validated, the updated TicketResponseDto object is
      broadcast to this topic. All connected clients for that event will receive this message and update the specific
      ticket in their list.
    * **/topic/event/{eventId}/statistics**: After a ticket is validated, a new EventTicketStatisticsDto is broadcast to
      this topic. Clients will use this to update the live statistics dashboard.

## **5\. Security Considerations**

* **Endpoint Security:** All REST endpoints under the new EventTicketController must be secured using Spring Security.
  The @PreAuthorize annotation, combined with the existing EventSecurityService, will be used to verify that the
  authenticated user is a staff member of the structure organizing the event.
* **WebSocket Security:** The WebSocket connection endpoint should also be secured. Spring Security's WebSocket
  integration can be used to ensure that only authenticated users can establish a connection and subscribe to topics.
  Subscriptions can be further intercepted to verify that the user is authorized for the specific {eventId} in the topic
  path.

## **6\. Data Models (DTOs)**

* **TicketValidationResponseDto**:
    * ticketId (String)
    * message (String)
    * newStatus (TicketStatus)
    * validationTimestamp (Date)
* **EventTicketStatisticsDto**:
    * eventId (long) - ID of the event
    * eventName (String) - Name of the event
    * totalTickets (long) - Total number of tickets for the event (valid + used)
    * scannedTickets (long) - Number of scanned (used) tickets for the event
    * remainingTickets (long) - Number of remaining (valid) tickets for the event
    * fillRate (double) - Percentage of used tickets compared to total tickets (0-100)

## **7\. Summary of Workflow**

1. An authorized **Staff Member** navigates to the events list in the admin panel.
2. They click the **"Validate Tickets"** button for an upcoming or ongoing event.
3. The frontend application navigates to the **Validation Panel** (/admin/events/{eventId}/validation).
4. The component makes an initial HTTP GET request to /api/v1/events/{eventId}/tickets to fetch the first page of
   tickets.
5. Simultaneously, the frontend establishes a **WebSocket connection** to /ws-tickly and subscribes to the topics
   /topic/event/{eventId}/ticket-update and /topic/event/{eventId}/statistics.
6. The staff member finds a ticket by scrolling, searching, or filtering.
7. They click the **"Validate"** button for a VALID ticket.
8. An HTTP POST request is sent to /api/v1/events/{eventId}/tickets/{ticketId}/validate.
9. The backend validates the request, updates the ticket status to USED, and saves it.
10. The backend then broadcasts the updated ticket object and the new event statistics on their respective WebSocket
    topics.
11. All connected staff members' screens are **updated in real-time**: the ticket's status changes in the list, and the
    statistics dashboard reflects the new check-in.