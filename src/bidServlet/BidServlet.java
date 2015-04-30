package bidServlet;

import java.io.IOException;
import javax.servlet.http.*;
import javax.jdo.PersistenceManager;
import javax.persistence.*;
import cmm529.abay.*;
import cmm529.abay.data.*;
import cmm529.abay.util.*;
import java.util.*;
import com.google.gson.*;

@SuppressWarnings("serial")
public class BidServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try	{
			String bidder = req.getParameter("bidder");
			Long itemId = new Long (req.getParameter("itemId"));
			double amount = Double.parseDouble(req.getParameter("amount"));	
			Bid bid = new Bid (bidder,itemId, amount);
			Bid winningBid;			
			
			// all bids for this particular item
			Collection<Bid> itemBids = getBidsForItem(itemId);
			
			// if this is the first bid, the new bid is winning 
			if (itemBids.size() == 0) {
				winningBid = bid;
			} else {
				// otherwise, find a winning bid from the existing ones
				winningBid = Utility.findWinningBid(itemBids);
				// compare it to the new bid and update the price
				updateItemPrice(itemId, bid, winningBid);
			}
			// save bid
			saveBid(bid);					
			resp.setStatus(200); //everything ok
		} catch (Exception e) {
			resp.sendError(400,e.toString());	//error occurred, exception was thrown
		}
	} //end method

	public void saveBid(Bid bid) {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();		
		manager.persist(bid);
		manager.close();
	} //end method

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try	{
			Long itemId = new Long(req.getPathInfo().substring(1));
			
			Collection<Bid> bids = getBidsForItem(itemId);
			Bid winningBid = Utility.findWinningBid(bids);
			resp.setContentType("application/json");
			resp.getWriter().print(new Gson().toJson(winningBid));
			
			/* another idea how to handle this:
			 * object = new JsonObject();
			 * obj.addProperty("winner" : user)
			 * obj.addProperty("count" : count)
			 * */

		} catch (Exception e) { //other exception, maybe no ID specified		
			Iterable<Bid> items = getBids();
			resp.setContentType("application/json");
			resp.getWriter().print(new Gson().toJson(items));
		}
	} //end method

	public void updateItemPrice(Long itemId, Bid newBid, Bid winningBid) throws Exception {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager = factory.createEntityManager();
		manager.getTransaction().begin();
		Item item = manager.find(Item.class, itemId);		
		double newItemPrice;
			
		// the deadline has passed?
		if (newBid.getDate() > item.getDeadline()) { 
			throw new Exception();
		} else {
			
			if(!newBid.equals(winningBid)) {
			
				// checking if the user is trying to bid with an amount under the current price 
				if (newBid.getOffer() >= item.getCurrentPrice()) {
					newItemPrice = Utility.calcItemPrice(newBid.getOffer(), winningBid.getOffer(), 0.5);
					item.setCurrentPrice(newItemPrice);
				} else {
					// the bid will definitely loose => throw an exception
					throw new Exception();
				}
			}
			// if the newBid is equal to the winningBid, its the first bid => current price remains unchanged
		}
		
		manager.getTransaction().commit();
		System.out.println("Persisted " + manager);
		manager.close();
	
	} //end method
	

	public Bid getBid(Long id) {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();		
		String queryString="select b from Bid b where b.itemId=" + id;
		Query query=manager.createQuery(queryString);
		Bid result=(Bid)query.getSingleResult();
		
		manager.close();	
		return result;
	} //end method

	public Iterable<Bid> getBids() {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();
		
		String queryString="select b from Bid b";
		Query query=manager.createQuery(queryString);
		List<Bid> result=(List<Bid>)query.getResultList();
		manager.close();	
		return result;
	} //end method
	
	public Collection<Bid> getBidsForItem(Long itemId) {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();
		
		String queryString="select b from Bid b where b.itemId=" + itemId;
		Query query=manager.createQuery(queryString);
		List<Bid> result=(List<Bid>)query.getResultList();
		manager.close();	
		return result;
	} //end method

	
	public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try	{
			String id=req.getPathInfo().substring(1);
			deleteBid(id);
		} catch (Exception e) {	//no bid with ID found
			resp.sendError(400,e.toString());	
		}
	} //end method

	public void deleteBid(String id) {
		EntityManagerFactory factory=Persistence.createEntityManagerFactory("transactions-optional");
		EntityManager manager=factory.createEntityManager();
		String queryString="select b from Bid b where b.id=\""+id+"\"";
		Query query=manager.createQuery(queryString);
		Bid bid=(Bid)query.getSingleResult();
		manager.remove(bid);
		manager.close();	
	} //end method
} //end class
