# **Backend Statistics Service: Implementation Plan**

This document outlines the technical design and step-by-step implementation plan for creating a new statistics service
within the Tickly backend. The goal is to provide secure, aggregated data to the frontend administrator panel for KPIs
and charts.

### **1\. Feature Overview & Technical Design**

The new statistics module will introduce a dedicated controller, service, and data transfer objects (DTOs) to handle
statistical calculations. It will leverage existing repositories to query the database.

#### **1.1. Core Components**

* StatisticsController.java: A new REST controller to expose secure endpoints for fetching statistics.
* StatisticsService.java: A new service class containing the business logic to compute all statistical data.
* StatisticsRepository.java: A new repository (or custom methods in existing repositories) to execute complex SQL/JPQL
  queries for data aggregation.
* **DTOs**: A new set of DTOs in a dto/statistics/ package to structure the API responses for the frontend.

#### **1.2. Security**

All endpoints will be secured. Access will be granted only to authenticated users with the STRUCTURE\_ADMINISTRATOR
role. The service will verify that the requested structureId or eventId belongs to the administrator's structure.

### **2\. API Endpoints and Data Models (DTOs)**

Two primary endpoints will be created to serve all the required statistics.

#### **2.1. New DTOs**

First, create the following DTO classes. These will define the contract with the frontend.

* ChartJsDataDto.java: A generic DTO for chart data.  
  public class ChartJsDataDto {  
  private String chartType; // e.g., "doughnut", "line", "bar"  
  private List\<String\> labels;  
  private List\<ChartJsDataset\> datasets;  
  }

  public class ChartJsDataset {  
  private String label;  
  private List\<Number\> data;  
  private List\<String\> backgroundColor; // For bar/doughnut charts  
  private String borderColor; // For line charts  
  private boolean fill;  
  }

* StructureDashboardStatsDto.java: For the main dashboard KPIs.  
  public class StructureDashboardStatsDto {  
  private long upcomingEventsCount;  
  private long totalTicketsReserved;  
  private long totalExpectedAttendees;  
  private double averageAttendanceRate; // Percentage  
  private ChartJsDataDto topEventsChart;  
  private ChartJsDataDto attendanceByCategoryChart;  
  }

* EventStatisticsDto.java: For event-specific statistics.  
  public class EventStatisticsDto {  
  private long eventId;  
  private String eventName;  
  private ChartJsDataDto zoneFillRateChart;  
  private ChartJsDataDto reservationsOverTimeChart;  
  private ChartJsDataDto ticketStatusChart;  
  }

* ZoneFillRateDataPointDto.java: A helper DTO for the zone fill rate chart.  
  public class ZoneFillRateDataPointDto {  
  private String zoneName;  
  private long ticketsSold;  
  private int capacity;  
  }

#### **2.2. API Endpoints**

1. **Get Structure Dashboard Statistics**
    * **Endpoint:** GET /api/v1/statistics/structure/{structureId}/dashboard
    * **Description:** Returns a consolidated object containing all KPIs and global charts for a structure's main
      dashboard.
    * **Security:** Requires STRUCTURE\_ADMINISTRATOR role for the given {structureId}.
    * **Response Body:** StructureDashboardStatsDto
2. **Get Event-Specific Statistics**
    * **Endpoint:** GET /api/v1/statistics/event/{eventId}
    * **Description:** Returns detailed charts and statistics for a single event.
    * **Security:** Requires STRUCTURE\_ADMINISTRATOR role for the structure that owns the given {eventId}.
    * **Response Body:** EventStatisticsDto

### **3\. Implementation Tasks**

Here is the step-by-step guide to implement the feature.

#### **Task 1: Create the DTOs**

1. Create a new package com.tickly.backend.dto.statistics.
2. Inside this package, create all the Java classes defined in section 2.1 (ChartJsDataDto, ChartJsDataset,
   StructureDashboardStatsDto, EventStatisticsDto, ZoneFillRateDataPointDto).

#### **Task 2: Create the Statistics Repository**

1. Create a new interface StatisticsRepository.java extending JpaRepository or create a new class to hold native
   queries. This repository is ideal for complex, read-only aggregation queries that are difficult to express in JPQL.
2. Add the following methods with native SQL queries. These queries are based on the data.sql schema.
    * **For KPIs (in** EventRepository **and** TicketRepository **or new** StatisticsRepository**)**:
        * countByStructureIdAndStartDateAfter(Long structureId, LocalDateTime now): Counts upcoming events.
        * countByEventStructureIdAndStatusIn(Long structureId, List\<String\> statuses): Counts total reserved tickets.
        * countByEventStructureIdAndEventStartDateAfterAndStatus(Long structureId, LocalDateTime now, String status):
          Counts expected attendees.
        * A custom query for average attendance rate for past events.
    * **For Charts (in new** StatisticsRepository**)**:
        * findZoneFillRatesByEventId(Long eventId): Returns a List\<ZoneFillRateDataPointDto\>.  
          SELECT eaz.name as zoneName, eaz.allocated\_capacity as capacity, COUNT(t.id) as ticketsSold  
          FROM event\_audience\_zone eaz  
          LEFT JOIN tickets t ON eaz.id \= t.event\_audience\_zone\_id AND t.status IN ('VALID', 'USED')  
          WHERE eaz.event\_id \= :eventId  
          GROUP BY eaz.id, eaz.name, eaz.allocated\_capacity;

        * findReservationsByDay(Long eventId): Returns a list of objects with reservation\_date and count.  
          SELECT DATE(reservation\_date) as date, COUNT(\*) as count  
          FROM tickets  
          WHERE event\_id \= :eventId AND status IN ('VALID', 'USED')  
          GROUP BY DATE(reservation\_date)  
          ORDER BY date ASC;

        * findTicketStatusDistribution(Long eventId):  
          SELECT status, COUNT(\*) as count FROM tickets WHERE event\_id \= :eventId GROUP BY status;

        * findTopEventsByTickets(Long structureId, int limit):  
          SELECT e.name, COUNT(t.id) as ticket\_count  
          FROM events e JOIN tickets t ON e.id \= t.event\_id  
          WHERE e.structure\_id \= :structureId AND t.status IN ('VALID', 'USED')  
          GROUP BY e.id, e.name ORDER BY ticket\_count DESC LIMIT :limit;

        * findAttendanceByCategory(Long structureId):  
          SELECT ec.name, COUNT(t.id) as attendee\_count  
          FROM tickets t  
          JOIN event\_has\_categories ehc ON t.event\_id \= ehc.event\_id  
          JOIN event\_categories ec ON ehc.category\_id \= ec.id  
          JOIN events e ON t.event\_id \= e.id  
          WHERE e.structure\_id \= :structureId AND t.status IN ('VALID', 'USED')  
          GROUP BY ec.id, ec.name ORDER BY attendee\_count DESC;

#### **Task 3: Create the Statistics Service**

1. Create StatisticsService.java.
2. Inject the required repositories (EventRepository, TicketRepository, StatisticsRepository, etc.).
3. Implement the public methods:
    * getStructureDashboardStats(Long structureId): This method will call the different repository methods defined in
      Task 2, assemble the results, and build the StructureDashboardStatsDto object, including the ChartJsDataDto for
      the two global charts.
    * getEventStats(Long eventId): This method will call the repository methods for a single event, perform any
      necessary transformations (like calculating cumulative sums for the reservations-over-time chart), and build the
      EventStatisticsDto response.

#### **Task 4: Create the Statistics Controller**

1. Create StatisticsController.java with the base path /api/v1/statistics.
2. Inject the StatisticsService.
3. Implement the two endpoints defined in section 2.2.
4. Add @PreAuthorize annotations to secure the endpoints, ensuring the user has the STRUCTURE\_ADMINISTRATOR role and is
   authorized for the requested resource.
    * **Example Security Check in Service Layer:**  
      public StructureDashboardStatsDto getStructureDashboardStats(Long structureId) {  
      // Get current user from SecurityContextHolder  
      // Check if user.getStructureId() matches the requested structureId  
      // If not, throw AccessDeniedException  
      ...  
      }

By following these steps, you will have a robust, secure, and well-structured backend service ready to deliver valuable
insights to your users.