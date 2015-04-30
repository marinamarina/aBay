package itemServlet;

import java.io.IOException;

import javax.servlet.http.*;
import javax.persistence.*;

import cmm529.abay.*;
import cmm529.abay.data.*;
import cmm529.abay.util.*;

import java.util.*;

import com.google.gson.*;

@SuppressWarnings("serial")
public class ItemServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try	{
			String seller=req.getParameter("seller");
			String title=req.getParameter("title");
			String desc=req.getParameter("desc");
			double price=Double.parseDouble(req.getParameter("price"));
			
			long deadline=Long.parseLong(req.getParameter("deadline"));
			Item item=new Item(seller, title, desc, price, deadline);
			saveItem(item);
			resp.setStatus(200); //everything ok
		} catch (Exception e) {
			resp.sendError(400,e.toString());	//error occurred
		}
	} //end method

	public void saveItem(Item item) {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();
		manager.persist(item);
		manager.close();
	} //end method

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try	{
			Long id = new Long(req.getPathInfo().substring(1));
			Item item = getItem(id);
			resp.setContentType("application/json");
			resp.getWriter().print(new Gson().toJson(item));
		} catch (NoResultException e) {	//no item with ID found

		} catch (Exception e)	{ //other exception, maybe no ID specified
		
			Iterable<Item> items = getItems();
			resp.setContentType("application/json");
			resp.getWriter().print(new Gson().toJson(items));
		}
	} //end method

	public static Item getItem(Long id) {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();
		String queryString="select i from Item i where i.id=" + id;
		Query query=manager.createQuery(queryString);
		Item result=(Item)query.getSingleResult();
		manager.close();	
		return result;
	} //end method

	public Iterable<Item> getItems() {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();
		String queryString="select i from Item i";
		Query query=manager.createQuery(queryString);
		List<Item> result=(List<Item>)query.getResultList();
		manager.close();	
		return result;
	} //end method

	
	public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try	{
			String id=req.getPathInfo().substring(1);
			deleteItem(id);
		} catch (Exception e) {	//no city with ID found
			resp.sendError(400,e.toString());	
		}
	} //end method

	public void deleteItem(String id) {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();
		String queryString="select i from Item i where i.id=\""+id+"\"";
		Query query=manager.createQuery(queryString);
		Item item=(Item)query.getSingleResult();
		manager.remove(item);
		manager.close();	
	} //end method
} //end class
