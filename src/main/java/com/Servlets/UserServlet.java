package com.Servlets;

import Database.DBOperations.UserDBActions;
import Database.Connection.DBConnection;
import Database.Cache.UserCache;
import com.ObjectClass.User;
import com.System.TaskLogger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;

@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            logoutUser(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        try (Connection conn = DBConnection.getConnection()) {
            if ("login".equals(action)) {
                loginUser(request, response, conn);
            } else if ("register".equals(action)) {
                registerUser(request, response, conn);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
            }
        } catch (Exception e) {
            TaskLogger.logFatal("User Servlet", "Database error during POST request", "Error: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private void loginUser(HttpServletRequest request, HttpServletResponse response, Connection conn)
            throws IOException, ServletException {
        String userInput = request.getParameter("userInput");
        String password = request.getParameter("password");

        if (userInput == null || password == null || userInput.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "User ID/Username and Password cannot be empty.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        User user = null;

        // First attempt to fetch from cache.
        if (userInput.matches("\\d+")) {
            int id = Integer.parseInt(userInput);
            user = UserCache.getUserById(id);
            if (user == null) {
                user = UserDBActions.getUserById(conn, id);
                if (user != null) {
                    UserCache.cacheUser(user);
                }
            }
        } else {
            user = UserCache.getUserByUsername(userInput);
            if (user == null) {
                user = UserDBActions.getUserByUsername(conn, userInput);
                if (user != null) {
                    UserCache.cacheUser(user);
                }
            }
        }

        if (user != null && user.getPassword().equals(password)) { // Consider hashing passwords
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            // Redirect based on access level.
            String access = user.getAccess();
            switch (access.toLowerCase()) {
                case "customer":
                    response.sendRedirect("customer_portal.jsp");
                    break;
                case "employee":
                    response.sendRedirect("employee.jsp");
                    break;
                case "manager":
                    response.sendRedirect("admin.jsp");
                    break;
                default:
                    response.sendRedirect("default.jsp");
                    break;
            }
        } else {
            request.setAttribute("errorMessage", "Invalid User ID/Username or Password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    private void logoutUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("logout.jsp");
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "Username and Password cannot be empty.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        boolean success = UserDBActions.addUser(conn, username, password, "customer");

        if (success) {
            User newUser = UserDBActions.getUserByUsername(conn, username);
            if (newUser == null) {
                TaskLogger.logError("Username: " + username, "UserServlet registerUser", "Running",
                        new Exception("Error: User not found after registration."));
                request.setAttribute("errorMessage", "Registration successful, but user details could not be retrieved.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                return;
            }
            HttpSession session = request.getSession();
            session.setAttribute("user", newUser);
            // Write-Through: Cache the new user's profile.
            UserCache.cacheUser(newUser);
            response.sendRedirect("customer_portal.jsp");
        } else {
            request.setAttribute("errorMessage", "Registration failed.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
}
