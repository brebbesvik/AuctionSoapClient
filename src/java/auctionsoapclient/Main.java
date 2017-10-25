/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auctionsoapclient;


import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.jms.Topic;
import soap.Product;
import soap.AuctionWS_Service;
import soap.AuctionWS;
import soap.SetBidStatus;


/**
 *
 * @author Ben
 */
public class Main {

    private static List<Product> products = new ArrayList<>();
    
    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private static ConnectionFactory connectionfactory;
    
 /*   @Resource(lookup = "")
    private static Queue queue;*/
    
    @Resource(lookup = "java:app/BidWinnerTopic")
    private static Topic topic;
    
    
    
    private static String formatTimeLeft(Product p) {
        Long millis = p.getWhenBiddingCloses().getTime() - System.currentTimeMillis();
        
        String timeLeft = String.format("%02d days %02d hours %02d minutes %02d seconds", TimeUnit.MILLISECONDS.toDays(millis),
        TimeUnit.MILLISECONDS.toHours(millis)   % TimeUnit.DAYS.toHours(1),
        TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        

        return timeLeft;

    }
       
    private static void printAuctions() {
        System.out.println("Products\n");
        for(Product product : products) {
            System.out.println(
                    "Product id: " + product.getId() + "\n" +
                    "Name: " + product.getName() + "\n" +
                    "Current bid: " + product.getCurrentBid().getAmount() + 
                    " by " + product.getCurrentBid().getBidder().getName() + "\n" +
                    "Time left: " + formatTimeLeft(product));
            
            System.out.println();
        }
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Initializing the webservices
        AuctionWS_Service webservice = new AuctionWS_Service();
        AuctionWS auctionws = webservice.getAuctionWSPort(); 
        
        // Setting up the JMS to get updates when auctions ends
        Destination destination = null;
        try { 
            destination = (Destination) topic;
        } catch (JMSRuntimeException e) {
            System.err.println("Error: failed at setting destination");
            System.exit(1);
        }
        try {
            JMSContext context = connectionfactory.createContext();
            JMSConsumer consumer = context.createConsumer(destination);
            TextListener listener = new TextListener();
            consumer.setMessageListener(listener);
        } catch (JMSRuntimeException e) {
            System.err.print("Error occured!");
            System.exit(1);
        }
        
        
        
        // Bid on a product
        System.out.println("Get all ongoing auctions:");
        products = auctionws.getActiveAuctions();
        printAuctions();
        
        Kattio io = new Kattio(System.in, System.out);
        System.out.println("Enter id (number) for the product you want to bid on:");
        int productid = io.getInt();
        
        System.out.println("Enter the amount (dollars) you want to bid:");
        double amount = io.getDouble(); 
      
        // amount, customerid, productid
        SetBidStatus setbidstatus = auctionws.bidForAuction(amount, 1, productid);
        System.out.println(setbidstatus.getExplanation());
        
     io.close();
        System.out.println("Client ends");
    }

}
