package ctsws.hello;

import java.io.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@WebServlet(name = "helloServlet", value = "/rest/hello")
public class HelloServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    @Produces(MediaType.APPLICATION_JSON)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        ServletContext ctx = request.getServletContext();
        String path = ctx.getRealPath("/");

        // Hello
        PrintWriter out = response.getWriter();
        String retVal = "{\"status\" : \"success\", \"message\" : \"" + path + "\"}";
        out.print(retVal);

        return;
    }

    public void destroy() {
    }
}