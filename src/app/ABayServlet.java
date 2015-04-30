package app;


import java.io.IOException;

import javax.servlet.http.*;

import javax.persistence.*;

import java.util.*;

import com.google.gson.*;

@SuppressWarnings("serial")
public class ABayServlet extends HttpServlet
{
public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
{
	resp.setContentType("text/plain");
	resp.getWriter().println("Hello World");
} //end method
} //end class
