package com.app.movieBookingSystem.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.movieBookingSystem.dto.MovieRequest;
import com.app.movieBookingSystem.dto.TicketRequest;
import com.app.movieBookingSystem.dto.UserRequest;
import com.app.movieBookingSystem.model.Movie;
import com.app.movieBookingSystem.model.Ticket;
import com.app.movieBookingSystem.model.User;
import com.app.movieBookingSystem.repository.MovieRepository;
import com.app.movieBookingSystem.repository.TicketRepository;
import com.app.movieBookingSystem.repository.UserRepository;
import com.app.movieBookingSystem.security.JwtUtil;

import jakarta.transaction.Transactional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepo;
    @Autowired
    private TicketRepository ticketRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public String updateTicketStatus(String movieName) {
        // 1. Find the movie details (Total capacity)
        Movie movie = movieRepo.findByMovieName(movieName);
        int totalCapacity = 100;
        if (movie == null) {
            return "Movie not found";
        }

        // 2. Fetch ALL tickets associated with this movie
        List<Ticket> allTickets = ticketRepo.findByMovieName(movieName);

        // 3. Sum the 'numberOfTickets' from every ticket in the list
        // This handles 0, 1, or multiple tickets correctly
        int totalBooked = allTickets.stream()
                .mapToInt(Ticket::getNumberOfTickets)
                .sum();

        // 4. Calculate remaining capacity
        // Note: Use a separate variable or field for 'initialCapacity' if
        // 'totalTickets'
        // in your DB is being overwritten, otherwise the math will fail on the next
        // update.
        int available = totalCapacity - totalBooked;

        // 5. Update Status
        if (available <= 0) {
            movie.setTotalTickets(0); // Don't allow negative numbers
            movie.setTicketStatus("SOLD OUT");
        } else {
            movie.setTotalTickets(available);
            movie.setTicketStatus("BOOK ASAP");
        }

        movieRepo.save(movie);
        return "Status updated. Total booked: " + totalBooked + ", Available: " + available;
    }

    public List<Movie> getAllMovies() {
        return movieRepo.findAll();
    }

    public List<Movie> searchMovie(String moviename) {
        return movieRepo.findByMovieNameContaining(moviename);
    }

    // public String bookTicket(String moviename, TicketRequest ticketRequest) {
    // Ticket newTicket = new Ticket();
    // // Movie movie = movieRepo.findByMovieName(moviename);
    // // Long movieId = movie.getMovieId();
    // newTicket.setMovieName(moviename);
    // newTicket.setTheatreName(ticketRequest.getTheatreName());
    // newTicket.setNumberOfTickets(ticketRequest.getNumberOfTickets());
    // newTicket.setSeatNumbers(ticketRequest.getSeatNumbers());
    // ticketRepo.save(newTicket);
    // // Trigger status update logic
    // updateTicketStatus(moviename);
    // return "Tickets booked successfully";
    // }
    // public String bookTicket(String moviename, TicketRequest ticketRequest) {
    // // 1. Check if the movie exists first
    // Movie movie = movieRepo.findByMovieName(moviename);
    // if (movie == null) {
    // return "Booking failed: Movie '" + moviename + "' not found.";
    // }

    // // 2. SEAT VALIDATION LOGIC
    // // Get all seats currently in the database for this movie
    // String alreadyBookedString = getBookedSeatsString(moviename);

    // // Split into a List and trim spaces to ensure accurate comparison
    // List<String> alreadyBookedList =
    // Arrays.stream(alreadyBookedString.split(","))
    // .map(String::trim)
    // .filter(s -> !s.isEmpty())
    // .toList();

    // // Split the NEW requested seats
    // List<String> requestedSeats =
    // Arrays.stream(ticketRequest.getSeatNumbers().split(","))
    // .map(String::trim)
    // .filter(s -> !s.isEmpty())
    // .toList();

    // // Check if any requested seat is already in the booked list
    // for (String seat : requestedSeats) {
    // if (alreadyBookedList.contains(seat)) {
    // return "Booking failed: Seat " + seat + " is already booked by someone
    // else.";
    // }
    // }

    // // 3. CAPACITY VALIDATION (Optional but recommended)
    // if (movie.getTotalTickets() < ticketRequest.getNumberOfTickets()) {
    // return "Booking failed: Not enough tickets available. Only " +
    // movie.getTotalTickets() + " left.";
    // }

    // // 4. SAVE TICKET
    // Ticket newTicket = new Ticket();
    // newTicket.setMovieName(moviename);
    // newTicket.setTheatreName(ticketRequest.getTheatreName());
    // newTicket.setNumberOfTickets(ticketRequest.getNumberOfTickets());
    // newTicket.setSeatNumbers(ticketRequest.getSeatNumbers());

    // ticketRepo.save(newTicket);

    // // 5. UPDATE MOVIE STATUS
    // // Note: Ensure your updateTicketStatus method uses the math we fixed
    // earlier!
    // updateTicketStatus(moviename);

    // return "Tickets booked successfully for " + moviename;
    // }
    public String bookTicket(String moviename, TicketRequest ticketRequest) {
        // 1. Check if the movie exists
        Movie movie = movieRepo.findByMovieName(moviename);
        if (movie == null) {
            return "Booking failed: Movie '" + moviename + "' not found.";
        }

        // 2. SEAT COUNT VALIDATION (Requested by Harry)
        // Process requested seats into a list
        List<String> requestedSeats = Arrays.stream(ticketRequest.getSeatNumbers().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // Verify if number of seats entered matches the ticket count
        if (requestedSeats.size() != ticketRequest.getNumberOfTickets()) {
            return "Booking failed: You selected " + requestedSeats.size() +
                    " seats but requested " + ticketRequest.getNumberOfTickets() + " tickets.";
        }

        // 3. SEAT CLASH VALIDATION
        String alreadyBookedString = getBookedSeatsString(moviename);
        List<String> alreadyBookedList = Arrays.stream(alreadyBookedString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for (String seat : requestedSeats) {
            if (alreadyBookedList.contains(seat)) {
                return "Booking failed: Seat " + seat + " is already booked by someone else.";
            }
        }

        // 4. CAPACITY VALIDATION
        if (movie.getTotalTickets() < ticketRequest.getNumberOfTickets()) {
            return "Booking failed: Not enough tickets available. Only " + movie.getTotalTickets() + " left.";
        }

        // 5. SAVE TICKET
        Ticket newTicket = new Ticket();
        newTicket.setMovieName(moviename);
        newTicket.setTheatreName(ticketRequest.getTheatreName());
        newTicket.setNumberOfTickets(ticketRequest.getNumberOfTickets());
        // Joining them back ensures a clean "A1, A2, A3" format in DB
        newTicket.setSeatNumbers(String.join(", ", requestedSeats));

        ticketRepo.save(newTicket);

        // 6. UPDATE MOVIE STATUS
        updateTicketStatus(moviename);

        return "Tickets booked successfully for " + moviename;
    }

    public Movie addMovie(MovieRequest movieRequest) {
        Movie newMovie = new Movie();
        newMovie.setMovieName(movieRequest.getMovieName());
        newMovie.setTheatreName(movieRequest.getTheatreName());
        newMovie.setTotalTickets(movieRequest.getTotalTickets());
        newMovie.setTicketStatus(movieRequest.getTicketStatus());
        return movieRepo.save(newMovie);
    }

    @Transactional
    public boolean deleteMovie(String movieName, String movieId) {
        // 1. Find the movie
        Optional<Movie> movie = movieRepo.findById(movieId);

        if (movie.isPresent()) {
            // 2. Find all tickets as a List (Correct way to avoid ClassCastException)
            List<Ticket> tickets = ticketRepo.findByMovieName(movieName);

            // 3. Delete them if the list isn't empty
            if (tickets != null && !tickets.isEmpty()) {
                ticketRepo.deleteAll(tickets);
            }

            // 4. Finally delete the movie
            movieRepo.deleteById(movieId);
            return true;
        }
        return false;
    }

    public String getBookedSeatsString(String movieName) {
        // Use the IgnoreCase version to avoid 404s due to typing
        List<Ticket> tickets = ticketRepo.findByMovieNameIgnoreCase(movieName);

        if (tickets == null || tickets.isEmpty()) {
            return "";
        }

        return tickets.stream()
                .map(Ticket::getSeatNumbers)
                .filter(Objects::nonNull)
                .flatMap(seats -> Arrays.stream(seats.split(",\\s*")))
                .distinct()
                .collect(Collectors.joining(", "));
    }

    @Transactional
    public void deleteAllTickets() {
        ticketRepo.deleteAll();
    }

    @Transactional
    public void deleteAllMovies() {
        movieRepo.deleteAll();
    }

    public String registerUser(UserRequest userRequest) {
        // 1. Check if user already exists by Login ID
        if (userRepo.findByLoginId(userRequest.getLoginId()) != null) {
            return "User with login ID " + userRequest.getLoginId() + " already exists!";
        }

        // 2. Enforce Single Admin Rule
        String requestedRole = userRequest.getRole();
        if ("ROLE_ADMIN".equalsIgnoreCase(requestedRole)) {
            // Check if any user already has the ROLE_ADMIN role
            if (userRepo.existsByRole("ROLE_ADMIN")) {
                return "Registration failed: An administrator already exists. Only one admin is allowed.";
            }
        }

        User user = new User();

        // 3. Encrypt the password
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // 4. Map DTO to Entity
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setLoginId(userRequest.getLoginId());
        user.setContactNumber(userRequest.getContactNumber());

        // 5. Set Role with Defaulting
        if (requestedRole == null || requestedRole.isEmpty()) {
            user.setRole("ROLE_USER");
        } else {
            user.setRole(requestedRole.toUpperCase());
        }

        // 6. Save to the users table
        userRepo.save(user);

        return "User registered successfully";
    }

    public String login(String loginId, String password, String role) {
        // 1. Fetch user from DB
        User user = userRepo.findByLoginId(loginId);

        // 2. Verify: User exists, Password matches, AND Role matches
        if (user != null &&
                passwordEncoder.matches(password, user.getPassword()) &&
                user.getRole().equalsIgnoreCase(role)) {

            // 3. Generate Token
            return jwtUtil.generateToken(user.getLoginId());
        }

        // Return null if any of the three checks fail
        return null;
    }
}