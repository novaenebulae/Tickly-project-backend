package edu.cda.project.ticklybackend.services.impl;

import edu.cda.project.ticklybackend.dtos.statistics.*;
import edu.cda.project.ticklybackend.enums.TicketStatus;
import edu.cda.project.ticklybackend.exceptions.AccessDeniedException;
import edu.cda.project.ticklybackend.exceptions.ResourceNotFoundException;
import edu.cda.project.ticklybackend.models.event.Event;
import edu.cda.project.ticklybackend.models.user.User;
import edu.cda.project.ticklybackend.repositories.event.EventRepository;
import edu.cda.project.ticklybackend.repositories.statistics.StatisticsRepository;
import edu.cda.project.ticklybackend.repositories.ticket.TicketRepository;
import edu.cda.project.ticklybackend.security.EventSecurityService;
import edu.cda.project.ticklybackend.security.StructureSecurityService;
import edu.cda.project.ticklybackend.services.interfaces.StatisticsService;
import edu.cda.project.ticklybackend.utils.AuthUtils;
import edu.cda.project.ticklybackend.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Implementation of the StatisticsService interface.
 * Provides methods for generating statistics for structures and events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final StructureSecurityService structureSecurityService;
    private final EventSecurityService eventSecurityService;
    private final AuthUtils authUtils;

    private static final int TOP_EVENTS_LIMIT = 5;

    @Override
    @Transactional(readOnly = true)
    public StructureDashboardStatsDto getStructureDashboardStats(Long structureId) {
        LoggingUtils.logMethodEntry(log, "getStructureDashboardStats", "structureId", structureId);

        try {
            log.debug("Début de la récupération des statistiques du tableau de bord pour la structure ID: {}", structureId);

            // Security check: Verify that the current user is staff of this structure
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // First check authorization before accessing user details
            log.debug("Vérification des autorisations pour l'accès aux statistiques de la structure ID: {}", structureId);
            if (!structureSecurityService.isStructureStaff(structureId, authentication)) {
                log.warn("Accès refusé aux statistiques pour la structure ID: {} - Utilisateur non autorisé", structureId);
                throw new AccessDeniedException("You are not authorized to access statistics for this structure");
            }
            log.debug("Autorisation validée pour l'accès aux statistiques de la structure ID: {}", structureId);

            // Now get the user details for logging
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            if (currentUser != null) {
                LoggingUtils.setUserId(currentUser.getId());
            }

            // Get current date/time for upcoming events calculation
            LocalDateTime now = LocalDateTime.now();

            // Calculate KPIs
            log.debug("Calcul des KPIs pour la structure ID: {}", structureId);
            long upcomingEventsCount = countUpcomingEvents(structureId, now);
            long totalTicketsReserved = countTotalTicketsReserved(structureId);
            long totalExpectedAttendees = countExpectedAttendees(structureId, now);
            double averageAttendanceRate = calculateAverageAttendanceRate(structureId);
            log.debug("KPIs calculés pour la structure ID: {}: événements à venir={}, tickets réservés={}, participants attendus={}, taux de participation moyen={}%", 
                    structureId, upcomingEventsCount, totalTicketsReserved, totalExpectedAttendees, averageAttendanceRate);

            // Generate charts
            log.debug("Génération des graphiques pour la structure ID: {}", structureId);
            ChartJsDataDto topEventsChart = generateTopEventsChart(structureId);
            ChartJsDataDto attendanceByCategoryChart = generateAttendanceByCategoryChart(structureId);
            log.debug("Graphiques générés avec succès pour la structure ID: {}", structureId);

            // Create and return the DTO
            log.debug("Création du DTO de statistiques pour la structure ID: {}", structureId);
            StructureDashboardStatsDto result = new StructureDashboardStatsDto(
                    upcomingEventsCount,
                    totalTicketsReserved,
                    totalExpectedAttendees,
                    averageAttendanceRate,
                    topEventsChart,
                    attendanceByCategoryChart
            );
            log.info("Statistiques du tableau de bord générées avec succès pour la structure ID: {}", structureId);

            LoggingUtils.logMethodExit(log, "getStructureDashboardStats", result);
            return result;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération des statistiques pour la structure ID: " + structureId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventStatisticsDto getEventStats(Long eventId) {
        LoggingUtils.logMethodEntry(log, "getEventStats", "eventId", eventId);

        try {
            log.debug("Début de la récupération des statistiques pour l'événement ID: {}", eventId);

            // Explicitly handle the case when event is not found to ensure ResourceNotFoundException is thrown
            var eventOpt = eventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                log.warn("Événement non trouvé avec ID: {}", eventId);
                throw new ResourceNotFoundException("Event", "id", eventId);
            }

            Event event = eventOpt.get();
            log.debug("Événement trouvé: ID={}, nom={}", event.getId(), event.getName());

            // Security check: Verify that the current user is authorized to access this event's statistics
            User currentUser = authUtils.getCurrentAuthenticatedUser();
            if (currentUser == null) {
                log.warn("Utilisateur non authentifié tentant d'accéder aux statistiques de l'événement ID: {}", eventId);
                throw new AccessDeniedException("You are not authorized to access statistics for this event");
            }
            LoggingUtils.setUserId(currentUser.getId());

            log.debug("Vérification des autorisations pour l'accès aux statistiques de l'événement ID: {}", eventId);
            if (!eventSecurityService.isOwner(eventId, currentUser)) {
                log.warn("Accès refusé aux statistiques pour l'événement ID: {} - Utilisateur ID: {} non autorisé", 
                        eventId, currentUser.getId());
                throw new AccessDeniedException("You are not authorized to access statistics for this event");
            }
            log.debug("Autorisation validée pour l'accès aux statistiques de l'événement ID: {}", eventId);

            // Get zone fill rates data (used for both chart and fill percentage calculation)
            log.debug("Récupération des taux de remplissage par zone pour l'événement ID: {}", eventId);
            List<ZoneFillRateDataPointDto> zoneFillRates = statisticsRepository.findZoneFillRatesByEventId(eventId);
            log.debug("Récupéré {} zones pour l'événement ID: {}", zoneFillRates.size(), eventId);

            // Generate charts
            log.debug("Génération des graphiques pour l'événement ID: {}", eventId);
            ChartJsDataDto zoneFillRateChart = generateZoneFillRateChart(zoneFillRates);
            ChartJsDataDto reservationsOverTimeChart = generateReservationsOverTimeChart(eventId);
            ChartJsDataDto ticketStatusChart = generateTicketStatusChart(eventId);
            log.debug("Graphiques générés avec succès pour l'événement ID: {}", eventId);

            // Calculate KPIs
            log.debug("Calcul des KPIs pour l'événement ID: {}", eventId);
            double fillPercentage = calculateFillPercentage(zoneFillRates);
            long uniqueReservationAmount = statisticsRepository.countUniqueReservationsByEventId(eventId);
            long attributedTicketsAmount = ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.VALID);
            long scannedTicketsNumber = ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.USED);
            log.debug("KPIs calculés pour l'événement ID: {}: taux de remplissage={}%, réservations uniques={}, tickets attribués={}, tickets scannés={}", 
                    eventId, fillPercentage, uniqueReservationAmount, attributedTicketsAmount, scannedTicketsNumber);

            // Create and return the DTO with all data
            log.debug("Création du DTO de statistiques pour l'événement ID: {}", eventId);
            EventStatisticsDto dto = new EventStatisticsDto(
                    eventId,
                    event.getName(),
                    fillPercentage,
                    uniqueReservationAmount,
                    attributedTicketsAmount,
                    scannedTicketsNumber,
                    zoneFillRateChart,
                    reservationsOverTimeChart,
                    ticketStatusChart
            );

            log.info("Statistiques générées avec succès pour l'événement ID: {}, nom: {}", eventId, event.getName());

            LoggingUtils.logMethodExit(log, "getEventStats", dto);
            return dto;
        } catch (Exception e) {
            LoggingUtils.logException(log, "Erreur lors de la récupération des statistiques pour l'événement ID: " + eventId, e);
            throw e;
        } finally {
            LoggingUtils.clearContext();
        }
    }

    /**
     * Calculate the overall fill percentage for an event.
     * This is the ratio of tickets sold to total capacity across all zones.
     *
     * @param zoneFillRates List of zone fill rate data points
     * @return The fill percentage as a value between 0 and 100
     */
    private double calculateFillPercentage(List<ZoneFillRateDataPointDto> zoneFillRates) {
        LoggingUtils.logMethodEntry(log, "calculateFillPercentage", "zoneFillRates.size", zoneFillRates != null ? zoneFillRates.size() : 0);

        try {
            if (zoneFillRates.isEmpty()) {
                LoggingUtils.logMethodExit(log, "calculateFillPercentage", 0.0);
                return 0.0;
            }

            int totalCapacity = 0;
            long totalTicketsSold = 0;

            for (ZoneFillRateDataPointDto zone : zoneFillRates) {
                totalCapacity += zone.getCapacity();
                totalTicketsSold += zone.getTicketsSold();
            }

            if (totalCapacity == 0) {
                LoggingUtils.logMethodExit(log, "calculateFillPercentage", 0.0);
                return 0.0;
            }

            double result = (double) totalTicketsSold / totalCapacity * 100;
            LoggingUtils.logMethodExit(log, "calculateFillPercentage", result);
            return result;
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    /**
     * Count the number of upcoming events for a structure.
     */
    private long countUpcomingEvents(Long structureId, LocalDateTime now) {
        LoggingUtils.logMethodEntry(log, "countUpcomingEvents", "structureId", structureId, "now", now);

        try {
            // Query to find events that haven't started yet
            long result = eventRepository.countByStructureIdAndStartDateAfterAndDeletedFalse(structureId, now.atZone(ZoneId.systemDefault()).toInstant());
            LoggingUtils.logMethodExit(log, "countUpcomingEvents", result);
            return result;
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    /**
     * Count the total number of reserved tickets for a structure.
     */
    private long countTotalTicketsReserved(Long structureId) {
        LoggingUtils.logMethodEntry(log, "countTotalTicketsReserved", "structureId", structureId);

        try {
            long result = ticketRepository.countByEventStructureIdAndStatusIn(
                    structureId,
                    Arrays.asList(TicketStatus.VALID, TicketStatus.USED) // On passe les enums
            );
            LoggingUtils.logMethodExit(log, "countTotalTicketsReserved", result);
            return result;
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    /**
     * Count the expected number of attendees for upcoming events.
     */
    private long countExpectedAttendees(Long structureId, LocalDateTime now) {
        LoggingUtils.logMethodEntry(log, "countExpectedAttendees", "structureId", structureId, "now", now);

        try {
            long result = ticketRepository.countByEventStructureIdAndEventStartDateAfterAndStatus(
                    structureId,
                    now.atZone(ZoneId.systemDefault()).toInstant(),
                    TicketStatus.VALID
            );
            LoggingUtils.logMethodExit(log, "countExpectedAttendees", result);
            return result;
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    /**
     * Calculate the average attendance rate for past events.
     */
    private double calculateAverageAttendanceRate(Long structureId) {
        LoggingUtils.logMethodEntry(log, "calculateAverageAttendanceRate", "structureId", structureId);

        try {
            // Get past events for this structure
            List<Event> pastEvents = eventRepository.findByStructureIdAndEndDateBeforeAndDeletedFalse(
                    structureId,
                    LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
            );

            if (pastEvents.isEmpty()) {
                LoggingUtils.logMethodExit(log, "calculateAverageAttendanceRate", 0.0);
                return 0.0;
            }

            double totalRate = 0.0;
            int eventCount = 0;

            for (Event event : pastEvents) {
                // Count used tickets (attendees)
                long attendees = ticketRepository.countByEventIdAndStatus(event.getId(), TicketStatus.USED);

                // Count total tickets (valid + used)
                long totalTickets = ticketRepository.countByEventIdAndStatusIn(
                        event.getId(),
                        Arrays.asList(TicketStatus.VALID, TicketStatus.USED)
                );

                if (totalTickets > 0) {
                    totalRate += (double) attendees / totalTickets * 100;
                    eventCount++;
                }
            }

            double result = eventCount > 0 ? totalRate / eventCount : 0.0;
            LoggingUtils.logMethodExit(log, "calculateAverageAttendanceRate", result);
            return result;
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    /**
     * Generate a chart of the top events by ticket sales.
     */
    private ChartJsDataDto generateTopEventsChart(Long structureId) {
        LoggingUtils.logMethodEntry(log, "generateTopEventsChart", "structureId", structureId);

        try {
            List<Map<String, Object>> topEvents = statisticsRepository.findTopEventsByTickets(structureId, TOP_EVENTS_LIMIT);

            List<String> labels = new ArrayList<>();
            List<Number> data = new ArrayList<>();
            List<String> backgroundColors = new ArrayList<>();

            // Generate random colors for the chart
            String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF"};

            for (int i = 0; i < topEvents.size(); i++) {
                Map<String, Object> event = topEvents.get(i);
                labels.add((String) event.get("name"));
                data.add((Number) event.get("ticket_count"));
                backgroundColors.add(colors[i % colors.length]);
            }

            ChartJsDataset dataset = new ChartJsDataset(
                    "Tickets Sold",
                    data,
                    backgroundColors,
                    "#FFFFFF",
                    false
            );

            ChartJsDataDto result = new ChartJsDataDto(
                    "bar",
                    labels,
                    Collections.singletonList(dataset)
            );

            LoggingUtils.logMethodExit(log, "generateTopEventsChart", result);
            return result;
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    /**
     * Generate a chart of attendance by event category.
     */
    private ChartJsDataDto generateAttendanceByCategoryChart(Long structureId) {
        LoggingUtils.logMethodEntry(log, "generateAttendanceByCategoryChart", "structureId", structureId);

        try {
            List<Map<String, Object>> attendanceByCategory = statisticsRepository.findAttendanceByCategory(structureId);

            List<String> labels = new ArrayList<>();
            List<Number> data = new ArrayList<>();
            List<String> backgroundColors = new ArrayList<>();

            // Generate random colors for the chart
            String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40", "#FFCD56"};

            for (int i = 0; i < attendanceByCategory.size(); i++) {
                Map<String, Object> category = attendanceByCategory.get(i);
                labels.add((String) category.get("name"));
                data.add((Number) category.get("attendee_count"));
                backgroundColors.add(colors[i % colors.length]);
            }

            ChartJsDataset dataset = new ChartJsDataset(
                    "Attendees",
                    data,
                    backgroundColors,
                    "#FFFFFF",
                    false
            );

            ChartJsDataDto result = new ChartJsDataDto(
                    "doughnut",
                    labels,
                    Collections.singletonList(dataset)
            );

            LoggingUtils.logMethodExit(log, "generateAttendanceByCategoryChart", result);
            return result;
        } finally {
            // No need to clear context for private methods called by public methods that already handle context
        }
    }

    /**
     * Generate a chart of zone fill rates for an event.
     *
     * @param zoneFillRates List of zone fill rate data points
     * @return A chart data object for zone fill rates
     */
    private ChartJsDataDto generateZoneFillRateChart(List<ZoneFillRateDataPointDto> zoneFillRates) {
        List<String> labels = new ArrayList<>();
        List<Number> fillRateData = new ArrayList<>();
        List<Number> capacityData = new ArrayList<>();
        List<Number> soldData = new ArrayList<>();

        for (ZoneFillRateDataPointDto zone : zoneFillRates) {
            labels.add(zone.getZoneName());
            fillRateData.add(zone.getFillRate());
            capacityData.add(zone.getCapacity());
            soldData.add(zone.getTicketsSold());
        }

        List<ChartJsDataset> datasets = new ArrayList<>();

        // Fill rate dataset (percentage)
        datasets.add(new ChartJsDataset(
                "Fill Rate (%)",
                fillRateData,
                Collections.singletonList("#FF6384"),
                "#FF6384",
                false
        ));

        // Capacity dataset
        datasets.add(new ChartJsDataset(
                "Capacity",
                capacityData,
                Collections.singletonList("#36A2EB"),
                "#36A2EB",
                false
        ));

        // Sold tickets dataset
        datasets.add(new ChartJsDataset(
                "Tickets Sold",
                soldData,
                Collections.singletonList("#FFCE56"),
                "#FFCE56",
                false
        ));

        return new ChartJsDataDto(
                "bar",
                labels,
                datasets
        );
    }

    /**
     * Generate a chart of zone fill rates for an event.
     *
     * @param eventId The ID of the event
     * @return A chart data object for zone fill rates
     */
    private ChartJsDataDto generateZoneFillRateChart(Long eventId) {
        List<ZoneFillRateDataPointDto> zoneFillRates = statisticsRepository.findZoneFillRatesByEventId(eventId);
        return generateZoneFillRateChart(zoneFillRates);
    }

    /**
     * Generate a chart of reservations over time for an event.
     */
    private ChartJsDataDto generateReservationsOverTimeChart(Long eventId) {
        List<Map<String, Object>> reservationsByDay = statisticsRepository.findReservationsByDay(eventId);

        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        for (Map<String, Object> day : reservationsByDay) {
            labels.add(day.get("date").toString());
            data.add((Number) day.get("count"));
        }

        ChartJsDataset dataset = new ChartJsDataset(
                "Reservations",
                data,
                Collections.singletonList("rgba(54, 162, 235, 0.2)"),
                "rgba(54, 162, 235, 1)",
                true
        );

        return new ChartJsDataDto(
                "line",
                labels,
                Collections.singletonList(dataset)
        );
    }

    /**
     * Generate a chart of ticket status distribution for an event.
     */
    private ChartJsDataDto generateTicketStatusChart(Long eventId) {
        List<Map<String, Object>> statusDistribution = statisticsRepository.findTicketStatusDistribution(eventId);

        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        List<String> backgroundColors = new ArrayList<>();

        // Define colors for each status
        Map<String, String> statusColors = new HashMap<>();
        statusColors.put(TicketStatus.VALID.name(), "#4BC0C0");
        statusColors.put(TicketStatus.USED.name(), "#36A2EB");
        statusColors.put(TicketStatus.CANCELLED.name(), "#FF6384");
        statusColors.put(TicketStatus.EXPIRED.name(), "#FFCE56");

        for (Map<String, Object> status : statusDistribution) {
            String statusName = (String) status.get("status");
            labels.add(statusName);
            data.add((Number) status.get("count"));
            backgroundColors.add(statusColors.getOrDefault(statusName, "#9966FF"));
        }

        ChartJsDataset dataset = new ChartJsDataset(
                "Tickets",
                data,
                backgroundColors,
                "#FFFFFF",
                false
        );

        return new ChartJsDataDto(
                "doughnut",
                labels,
                Collections.singletonList(dataset)
        );
    }
}
