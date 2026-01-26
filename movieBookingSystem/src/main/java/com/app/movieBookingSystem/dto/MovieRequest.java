package com.app.movieBookingSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MovieRequest {

   @NotBlank(message = "Movie name cannot be blank")
    private String movieName;

    @NotBlank(message = "Theatre name cannot be blank")
    private String theatreName;

    @NotNull(message = "Total tickets count is required")
    @Min(value = 1, message = "Total tickets must be at least 1")
    private Integer totalTickets;

    @NotBlank(message = "Initial ticket status is required")
    private String ticketStatus;
    
    public String getMovieName() {
        return movieName;
    }
    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }
    public String getTheatreName() {
        return theatreName;
    }
    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }
    public Integer getTotalTickets() {
        return totalTickets;
    }
    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }
    public String getTicketStatus() {
        return ticketStatus;
    }
    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    
    
}
