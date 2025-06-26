//  COMPLETE JAVA CODE FOR MOVIE TICKET SYSTEM WITH FULL DATABASE INTEGRATION
// Includes: UserModule, AdminModule, EventModule, BookingModule, SeatSelectionModule, NotificationModule, DBConnection
package project;
import java.sql.*;
import java.util.*;

// DBConnection.java
class DBConnection {
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String Url = "jdbc:mysql://localhost:3306/bookingproject";
            String User = "root";
            String Password = "Sakthidasan@29";
            return DriverManager.getConnection(Url, User, Password);
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
            return null;
        }
    }
}

// NotificationModule.java
//NotificationModule.java
class NotificationModule {
    void sendConfirmation(int userId) {
        System.out.println("Booking confirmation sent to User ID: " + userId);
    }
}
//AdminModule.java
class AdminModule {
    Scanner sc = new Scanner(System.in);

    void adminMenu() {
        System.out.print("Enter Admin Password: ");
        String pwd = sc.nextLine();
        if (!pwd.equals("admin")) {
            System.out.println("Wrong password");
            return;
        }

        while (true) {
            System.out.println("--- Admin Panel ---");
            System.out.println("1. Add Movie");
            System.out.println("2. Add Show");
            System.out.println("3. Add Screen");
            System.out.println("4. View Bookings");
            System.out.println("5. Delete Movie");
            System.out.println("6. Delete Show");
            System.out.println("7. Delete Screen");
            System.out.println("8. Add Theater");
            System.out.println("9. Exit");

            System.out.print("Choice: ");
            int choice = sc.nextInt(); sc.nextLine();

            try (Connection con = DBConnection.getConnection()) {
                switch (choice) {
                    case 1 -> addMovie(con);
                    case 2 -> addShow(con);
                    case 3 -> addScreen(con);
                    case 4 -> viewBookings(con);
                    case 5 -> deleteMovie(con);
                    case 6 -> deleteShow(con);
                    case 7 -> deleteScreen(con);
                    case 8 -> addTheater(con);
                    case 9 -> { return; }
                    default -> System.out.println("Invalid option");
                }
            } catch (SQLException e) {
                System.out.println("SQL Error: " + e);
            }
        }
    }

    void addMovie(Connection con) throws SQLException {
        System.out.print("Title: "); String title = sc.nextLine();
        System.out.print("Genre: "); String genre = sc.nextLine();
        System.out.print("Language: "); String lang = sc.nextLine();
        System.out.print("Duration (in minutes): "); int dur = sc.nextInt(); sc.nextLine();
        System.out.print("Release Date (YYYY-MM-DD): "); String date = sc.nextLine();

        PreparedStatement ps = con.prepareStatement("INSERT INTO movies(title, genre, language, duration, release_date) VALUES (?, ?, ?, ?, ?)");
        ps.setString(1, title); ps.setString(2, genre); ps.setString(3, lang); ps.setInt(4, dur); ps.setString(5, date);
        ps.executeUpdate();
        System.out.println(" Movie added successfully.");
    }

    void addScreen(Connection con) throws SQLException {
        System.out.print("Theater ID: "); int theaterId = sc.nextInt(); sc.nextLine();
        System.out.print("Screen Name: "); String name = sc.nextLine();
        System.out.print("Total Seats: "); int totalSeats = sc.nextInt(); sc.nextLine();

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO screens(theater_id, screen_name, total_seats) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        ps.setInt(1, theaterId);
        ps.setString(2, name);
        ps.setInt(3, totalSeats);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        int screenId = rs.getInt(1);

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int perRow = 10;
        int rowCount = (int) Math.ceil(totalSeats / (double) perRow);

        PreparedStatement seatStmt = con.prepareStatement("INSERT INTO seats(screen_id, seat_number) VALUES (?, ?)");
        int count = 0;
        for (int i = 0; i < rowCount && i < alphabet.length(); i++) {
            for (int j = 1; j <= perRow && count < totalSeats; j++) {
                String seatNo = alphabet.charAt(i) + String.valueOf(j);
                seatStmt.setInt(1, screenId);
                seatStmt.setString(2, seatNo);
                seatStmt.executeUpdate();
                count++;
            }
        }
        System.out.println(" Screen and seats created successfully.");
    }

    void addShow(Connection con) throws SQLException {
        System.out.print("Movie ID: "); int movieId = sc.nextInt();
        System.out.print("Screen ID: "); int screenId = sc.nextInt(); sc.nextLine();
        System.out.print("Show Time (YYYY-MM-DD HH:MM:SS): "); String time = sc.nextLine();

        PreparedStatement ps = con.prepareStatement("INSERT INTO shows(movie_id, screen_id, show_time) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, movieId);
        ps.setInt(2, screenId);
        ps.setString(3, time);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        int showId = rs.getInt(1);

        PreparedStatement getSeats = con.prepareStatement("SELECT seat_id FROM seats WHERE screen_id = ?");
        getSeats.setInt(1, screenId);
        ResultSet seatRs = getSeats.executeQuery();

        PreparedStatement insertShowSeat = con.prepareStatement("INSERT INTO show_seats(show_id, seat_id, is_booked) VALUES (?, ?, false)");
        while (seatRs.next()) {
            insertShowSeat.setInt(1, showId);
            insertShowSeat.setInt(2, seatRs.getInt("seat_id"));
            insertShowSeat.executeUpdate();
        }
        System.out.println(" Show and seats initialized.");
    }

    void deleteMovie(Connection con) throws SQLException {
        System.out.print("Enter Movie ID to delete: ");
        int movieId = sc.nextInt(); sc.nextLine();
        PreparedStatement ps = con.prepareStatement("DELETE FROM movies WHERE movie_id = ?");
        ps.setInt(1, movieId);
        int rows = ps.executeUpdate();
        System.out.println(rows > 0 ? "Movie deleted." : "Movie not found.");
    }

    void deleteShow(Connection con) throws SQLException {
        System.out.print("Enter Show ID to delete: ");
        int showId = sc.nextInt(); sc.nextLine();
        PreparedStatement ps = con.prepareStatement("DELETE FROM shows WHERE show_id = ?");
        ps.setInt(1, showId);
        int rows = ps.executeUpdate();
        System.out.println(rows > 0 ? " Show deleted." : " Show not found.");
    }

    void deleteScreen(Connection con) throws SQLException {
        System.out.print("Enter Screen ID to delete: ");
        int screenId = sc.nextInt(); sc.nextLine();
        PreparedStatement ps = con.prepareStatement("DELETE FROM screens WHERE screen_id = ?");
        ps.setInt(1, screenId);
        int rows = ps.executeUpdate();
        System.out.println(rows > 0 ? " Screen deleted." : " Screen not found.");
    }

    void addTheater(Connection con) throws SQLException {
        System.out.print("Theater Name: ");
        String name = sc.nextLine();
        System.out.print("Location: ");
        String location = sc.nextLine();

        PreparedStatement ps = con.prepareStatement("INSERT INTO theaters(theater_name, location) VALUES (?, ?)");
        ps.setString(1, name);
        ps.setString(2, location);
        int rows = ps.executeUpdate();
        System.out.println(rows > 0 ? " Theater added." : " Failed to add theater.");
    }

    void viewBookings(Connection con) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT b.booking_id, u.name, s.show_time, b.total_amount FROM bookings b JOIN users u ON b.user_id = u.user_id JOIN shows s ON b.show_id = s.show_id"
        );
        System.out.println("--- All Bookings ---");
        while (rs.next()) {
            System.out.println("Booking ID: " + rs.getInt("booking_id") +
                               ", User: " + rs.getString("name") +
                               ", Show Time: " + rs.getString("show_time") +
                               ", Amount: ₹" + rs.getInt("total_amount"));
        }
    }
}
//UserModule.java
class UserModule {
 Scanner sc = new Scanner(System.in);
 Connection con = DBConnection.getConnection();

 void register() {
     try {
         System.out.print("Enter name: "); 
         String name = sc.nextLine();
         System.out.print("Enter email: "); 
         String email = sc.nextLine();
         System.out.print("Enter phone: "); 
         String phone = sc.nextLine();
         System.out.print("Enter password: "); 
         String pass = sc.nextLine();

         PreparedStatement ps = con.prepareStatement(
             "INSERT INTO users(name, email, phone, password) VALUES (?, ?, ?, ?)"
         );
         ps.setString(1, name);
         ps.setString(2, email);
         ps.setString(3, phone);
         ps.setString(4, pass);

         int rows = ps.executeUpdate();
         System.out.println(rows > 0 ? " Registration successful!" : " Registration failed.");
     } catch (SQLException e) {
         System.out.println("Registration error: " + e);
     }
 }

 void login() {
     try {
         System.out.print("Enter email: "); 
         String email = sc.nextLine();
         System.out.print("Enter password: "); 
         String pass = sc.nextLine();

         PreparedStatement ps = con.prepareStatement(
             "SELECT * FROM users WHERE email = ? AND password = ?"
         );
         ps.setString(1, email);
         ps.setString(2, pass);
         ResultSet rs = ps.executeQuery();

         if (rs.next()) {
             System.out.println("Welcome " + rs.getString("name"));
             new BookingModule().userPanel(rs.getInt("user_id"));
         } else {
             System.out.println(" Invalid credentials");
         }
     } catch (SQLException e) {
         System.out.println("Login error: " + e);
     }
 }
}
//EventModule.java
class EventModule {
 Connection con = DBConnection.getConnection();

 void browseEvents() {
     try {
         ResultSet rs = con.createStatement().executeQuery(
             "SELECT m.movie_id, m.title, m.genre, s.show_id, s.show_time, sc.screen_name " +
             "FROM movies m " +
             "JOIN shows s ON m.movie_id = s.movie_id " +
             "JOIN screens sc ON s.screen_id = sc.screen_id"
         );

         System.out.println("\n--- Available Events ---");
         while (rs.next()) {
             System.out.println(
                 "Movie ID: " + rs.getInt("movie_id") +
                 ", Title: " + rs.getString("title") +
                 ", Genre: " + rs.getString("genre") +
                 ", Show ID: " + rs.getInt("show_id") +
                 ", Time: " + rs.getString("show_time") +
                 ", Screen: " + rs.getString("screen_name")
             );
         }
     } catch (SQLException e) {
         System.out.println("Error loading events: " + e);
     }
 }
}
//BookingModule.java
class BookingModule {
 Scanner sc = new Scanner(System.in);
 Connection con = DBConnection.getConnection();

 void userPanel(int userId) {
     while (true) {
         System.out.println("\n--- User Menu ---");
         System.out.println("1. Browse Events");
         System.out.println("2. Book Ticket (Select Seats)");
         System.out.println("3. View Profile");
         System.out.println("4. Logout");
         System.out.print("Enter choice: ");
         int ch = sc.nextInt(); sc.nextLine();

         switch (ch) {
             case 1 -> new EventModule().browseEvents();
             case 2 -> new SeatSelectionModule().selectSeat(userId);
             case 3 -> viewProfile(userId);
             case 4 -> {
                 System.out.println("Logging out...");
                 return;
             }
             default -> System.out.println("Invalid option");
         }
     }
 }

 void viewProfile(int userId) {
     try {
         PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE user_id = ?");
         ps.setInt(1, userId);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             System.out.println("\n--- Profile ---");
             System.out.println("Name: " + rs.getString("name"));
             System.out.println("Email: " + rs.getString("email"));
             System.out.println("Phone: " + rs.getString("phone"));
         }
     } catch (SQLException e) {
         System.out.println("Error loading profile: " + e);
     }
 }
}

// (Rest of the code remains unchanged except for updating SeatSelectionModule)

// SeatSelectionModule.java
//SeatSelectionModule.java
class SeatSelectionModule {
    Scanner sc = new Scanner(System.in);
    Connection con = DBConnection.getConnection();

    void selectSeat(int userId) {
        try {
            System.out.print("Enter Movie ID: ");
            int movieId = sc.nextInt(); sc.nextLine();

            ResultSet shows = con.createStatement().executeQuery("SELECT show_id, show_time FROM shows WHERE movie_id = " + movieId);
            ArrayList<Integer> showIds = new ArrayList<>();
            while (shows.next()) {
                int sid = shows.getInt("show_id");
                showIds.add(sid);
                System.out.println(sid + ". Time: " + shows.getString("show_time"));
            }

            System.out.print("Select Show ID: ");
            int showId = sc.nextInt(); sc.nextLine();

            ResultSet seatRs = con.createStatement().executeQuery("SELECT ss.seat_id, s.seat_number FROM show_seats ss JOIN seats s ON ss.seat_id = s.seat_id WHERE ss.show_id = " + showId + " AND ss.is_booked = 0");
            ArrayList<Integer> available = new ArrayList<>();
            Map<Integer, String> seatMap = new HashMap<>();

            System.out.println("\nAvailable Seats (Seat ID : Seat Number):");
            while (seatRs.next()) {
                int sid = seatRs.getInt("seat_id");
                String sname = seatRs.getString("seat_number");
                available.add(sid);
                seatMap.put(sid, sname);
                System.out.println("  " + sid + " : " + sname);
            }

            System.out.print("Enter Seat IDs to Book (comma-separated): ");
            String[] input = sc.nextLine().split(",");
            ArrayList<Integer> selected = new ArrayList<>();

            for (String s : input) {
                int sid = Integer.parseInt(s.trim());
                if (available.contains(sid)) {
                    selected.add(sid);
                } else {
                    System.out.println("Seat ID " + sid + " is not available.");
                    return;
                }
            }

            int totalAmount = selected.size() * 200;

            System.out.println("Select Payment Method:");
            System.out.println("1. Credit Card");
            System.out.println("2. Debit Card");
            System.out.println("3. Wallet");
            System.out.print("Enter choice: ");
            int paymentChoice = sc.nextInt(); sc.nextLine();
            String paymentMethod = switch (paymentChoice) {
                case 1 -> "Credit";
                case 2 -> "Debit";
                case 3 -> "Wallet";
                default -> "Unknown";
            };

            // Create Booking
            PreparedStatement bookingStmt = con.prepareStatement("INSERT INTO bookings(user_id, show_id, booking_time, total_amount) VALUES (?, ?, NOW(), ?)", Statement.RETURN_GENERATED_KEYS);
            bookingStmt.setInt(1, userId);
            bookingStmt.setInt(2, showId);
            bookingStmt.setInt(3, totalAmount);
            bookingStmt.executeUpdate();

            ResultSet rs = bookingStmt.getGeneratedKeys();
            rs.next();
            int bookingId = rs.getInt(1);

            for (int sid : selected) {
                con.prepareStatement("UPDATE show_seats SET is_booked = 1 WHERE show_id = " + showId + " AND seat_id = " + sid).executeUpdate();
                con.prepareStatement("INSERT INTO booking_seats(booking_id, seat_id) VALUES (" + bookingId + ", " + sid + ")").executeUpdate();
            }

            con.prepareStatement("INSERT INTO payments(booking_id, payment_method, payment_status) VALUES (" + bookingId + ", '" + paymentMethod + "', 'Confirmed')").executeUpdate();

            // Retrieve user name and show info
            PreparedStatement userStmt = con.prepareStatement("SELECT name FROM users WHERE user_id = ?");
            userStmt.setInt(1, userId);
            ResultSet userRs = userStmt.executeQuery();
            String username = userRs.next() ? userRs.getString("name") : "Unknown";

            PreparedStatement showStmt = con.prepareStatement("SELECT m.title, s.show_time FROM shows s JOIN movies m ON s.movie_id = m.movie_id WHERE s.show_id = ?");
            showStmt.setInt(1, showId);
            ResultSet showRs = showStmt.executeQuery();
            String movieTitle = "", showTime = "";
            if (showRs.next()) {
                movieTitle = showRs.getString("title");
                showTime = showRs.getString("show_time");
            }

            // Confirmation Output
            System.out.println("\n Booking Confirmed!");
            System.out.println("Booking ID     : " + bookingId);
            System.out.println("Username       : " + username);
            System.out.println("Movie          : " + movieTitle);
            System.out.println("Show Time      : " + showTime);
            System.out.println("Seats Booked   : " + selected.size());
            for (int sid : selected) {
                System.out.println("  Seat ID: " + sid + " | Seat No: " + seatMap.get(sid));
            }
            System.out.println("Price per seat : ₹200");
            System.out.println("Total Amount   : ₹" + totalAmount);
            System.out.println("Payment Method : " + paymentMethod);

            new NotificationModule().sendConfirmation(userId);


            // Cancellation option
            System.out.print("Do you want to cancel this booking? (yes/no): ");
            String cancel = sc.nextLine().trim().toLowerCase();
            if (cancel.equals("yes")) {
                cancelBooking(bookingId, selected);
            }

        } catch (Exception e) {
            System.out.println("Booking error: " + e.getMessage());
        }
    }

    void cancelBooking(int bookingId, List<Integer> seatIds) {
        try {
            for (int seatId : seatIds) {
                con.prepareStatement("UPDATE show_seats SET is_booked = 0 WHERE seat_id = " + seatId).executeUpdate();
            }
            con.prepareStatement("DELETE FROM booking_seats WHERE booking_id = " + bookingId).executeUpdate();
            con.prepareStatement("DELETE FROM payments WHERE booking_id = " + bookingId).executeUpdate();
            con.prepareStatement("DELETE FROM bookings WHERE booking_id = " + bookingId).executeUpdate();

            System.out.println(" Booking ID " + bookingId + " and all seats cancelled.");
        } catch (SQLException e) {
            System.out.println("Cancellation error: " + e.getMessage());
        }
    }
}

public class MovieTicketSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserModule userModule = new UserModule();
        AdminModule adminModule = new AdminModule();

        while (true) {
            System.out.println("\n=== Movie Ticket Booking System ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Admin Panel");
            System.out.println("4. Exit");
            System.out.print("Choice: ");
            int choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1 -> userModule.register();
                case 2 -> userModule.login();
                case 3 -> adminModule.adminMenu();
                case 4 -> {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option");
            }
        }
    }
}
