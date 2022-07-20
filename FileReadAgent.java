/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package examples.universityerp;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.*;

public class FileReadAgent extends Agent {
	private FileRead fileRead;
	// Put agent initializations here
	protected void setup() {
		// Register the readFile service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("read-file");
		sd.setName("FileData");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Add the behaviour serving queries from requestServer agents
		addBehaviour(new RequestsServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Reading-agent "+getAID().getName()+" terminating.");
	}

	/**
	   Inner class RequestsServer.
	   This is the behaviour used by FileReadAgent agents to serve incoming requests 
	   for data from any agent.
	   If the requested data is in the local file the  agent replies 
	   with a PROPOSE message specifying the required data. Otherwise a REFUSE message is
	   sent back.
	 */
	private class RequestsServer extends CyclicBehaviour {
		private ACLMessage reply;
		private Query query;
		private String data;
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				try{
					// CFP Message received. Process it
					query = (Query)msg.getContentObject();
					System.out.println("Request Received at FileReadAgent: "+query.getFileName()+" at: "+System.nanoTime());
					
					//create reply message
					reply = msg.createReply();
					reply.setConversationId("readinfo");
					
					//Reading data from file	
					FileRead fr = new FileRead(query.getFileName(), query.getSapId());
					data = fr.getData();
				
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(data);
					System.out.println("Data Read at FileReadAgent: "+System.nanoTime());
				}
				catch(NoRecordFoundException ex){
					System.out.println("Fault Detected: Exception caught- No Record Found");
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("info-not-available");
				}
				catch(IOException e){
					System.out.println("Fault Detected: Exception caught:I/O");
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("i/o issue: ");
				}
				catch(UnreadableException ex1){
					System.out.println("Due to Query Class");
				}
				System.out.println("Response sent from FileReadAgent: "+System.nanoTime());
				myAgent.send(reply);
			}
			else {
				block();
			}
			
		}  // End of inner class RequestsServer
	}
}
