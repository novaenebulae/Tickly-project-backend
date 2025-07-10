# Tickly Project Improvement Tasks

This document contains a comprehensive list of actionable improvement tasks for the Tickly project. Each task is marked
with a checkbox [ ] that can be checked off when completed.

## Architecture Improvements

### Security

[ ] Fix security vulnerability in ticket validation (TicketServiceImpl.java line 181)
[ ] Implement comprehensive permission checks for all sensitive operations
[ ] Add rate limiting for authentication endpoints to prevent brute force attacks
[ ] Implement API request throttling to prevent DoS attacks
[ ] Conduct a thorough security audit of the application

### Scalability

[ ] Containerize the application for easier deployment and scaling :

- Nginx Reverse proxy (Load Balancer) to Nginx static server + Java Container connected to MySQL server
- Only have to add the nginx reverse proxy as the rest is actually containerized.

### Resilience

[ ] Enhance error handling

## Code Quality Improvements

### Testing

[ ] Increase unit test coverage (aim for at least 60%)
[ ] Set up automated testing in CI/CD pipeline

### Code Organization

[ ] Refactor large service classes into smaller, more focused components
[ ] Review and improve separation of concerns in service implementations
[ ] Standardize exception handling across the application
[ ] Implement consistent logging strategy with appropriate log levels
[ ] Organize imports and remove unused dependencies

### Documentation

[ ] Complete API documentation with examples for all endpoints
[ ] Add comprehensive Javadoc for all public methods
[ ] Document database schema and relationships

### Code Maintainability

[ ] Refactor duplicate code into reusable utilities
[ ] Improve naming conventions for better code readability
[ ] Add more meaningful comments for complex business logic

## Feature Improvements

### User Experience

[X] Implement email verification workflow for new user registrations
[ ] Add password strength requirements and validation
[ ] Enhance error messages to be more user-friendly

### Business Logic

## DevOps Improvements

### CI/CD

[ ] Set up automated build and test pipeline
[ ] Implement infrastructure as code for all environments
[ ] Set up proper environment separation (dev, test, staging, production)

[ ] Setup backend deployment environment :

HTTPS =>  Nginx reverse proxy -> Java container -> MySQL
| | |
|--> Nginx Static -> Static files |-> Private files

### Deployment

[ ] Optimize Docker container configuration
