package com.Servlets;

import Database.Connection.DBConnection;
import Database.DBOperations.UserDBActions;
import com.ObjectClass.User;
import com.google.gson.Gson;
import com.System.SysLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;

@WebServlet("/EmployeeServlet")
public class EmployeeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = DBConnection.getConnection()) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            List<User> employees = UserDBActions.getEmployees(conn);
            String json = new Gson().toJson(employees);
            out.print(json);
            out.flush();
        } catch (Exception e) {
            SysLogger.logSevere("EmployeeServlet: Database error during GET request - Error: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        try (Connection conn = DBConnection.getConnection()) {
            if ("add".equals(action)) {
                addEmployee(request, response, conn);
            } else if ("delete".equals(action)) {
                deleteEmployee(request, response, conn);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
            }
        } catch (Exception e) {
            SysLogger.logSevere("EmployeeServlet: Database error during POST request - Error: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private void addEmployee(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
            return;
        }

        boolean success = UserDBActions.addUser(conn, username, password, "employee");
        SysLogger.logInfo("EmployeeServlet: Add Employee - "
                + (success ? "Employee added successfully!" : "Employee addition failed.")
                + " - Username: " + username);
        response.sendRedirect("employees.jsp?status=" + (success ? "success" : "failure"));
    }

    private void deleteEmployee(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required.");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            boolean success = UserDBActions.deleteUser(conn, id);
            SysLogger.logInfo("EmployeeServlet: Delete Employee - "
                    + (success ? "Employee deleted successfully." : "Employee deletion failed.")
                    + " - Employee ID: " + id);
            response.sendRedirect("employees.jsp?status=" + (success ? "success" : "failure"));
        } catch (NumberFormatException e) {
            SysLogger.logWarning("EmployeeServlet: Delete Employee - Invalid employee ID: " + idParam + " - Error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee ID.");
        }
    }
}
