/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auctionsoapclient;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author Ben
 */
public class TextListener implements MessageListener {
    
    /**
     * Whenever a message is posted to the topic; print it!
     * @param message the message we are about to print
     */
    @Override
    public void onMessage(Message message) {
        try {
            
            System.out.println(message.getBody(String.class));
            
        }
        catch (JMSException e) {
            
        }
    }
    
}
