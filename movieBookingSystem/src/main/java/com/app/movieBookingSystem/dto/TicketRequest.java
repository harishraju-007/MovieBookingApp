package com.app.movieBookingSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class TicketRequest {
    @NotBlank(message = "Movie name is required")
    private String movieName;

    @NotBlank(message = "Theatre name is required")
    private String theatreName;

    @NotNull(message = "Number of tickets is required")
    @Min(value = 1, message = "You must book at least 1 ticket")
    private Integer numberOfTickets;

    @NotBlank(message = "Seat numbers are required")
    // Regex ensures a format like A1 or A1, B2 or K15
    @Pattern(regexp = "^([A-K][0-9]{1,2})(,\\s*[A-K][0-9]{1,2})*$", 
             message = "Seats must follow the format: RowLetterSeatNumber (e.g., A1, B2)")
    private String seatNumbers;
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
    public Integer getNumberOfTickets() {
        return numberOfTickets;
    }
    public void setNumberOfTickets(Integer numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }
    public String getSeatNumbers() {
        return seatNumbers;
    }
    public void setSeatNumbers(String seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
    
}
