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
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.io.IOException;

import java.util.*;

public class AcademicAgent extends Agent {
	private AID fileReadAgent;
	// Put agent initializations here
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("display-academics");
		sd.setName("Academic Information");
		dfd.addServices(sd);
		System.out.println("Hello! Academic-agent "+getAID().getName()+" is ready.");
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}	
		// Add the behaviour serving queries from buyer agents
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
		System.out.println("Academic-agent "+getAID().getName()+" terminating.");
	}
	/**
	   Inner class OfferRequestsServer.
	   This is the behaviour used by Academic-information agents to serve incoming requests 
	   for information from display agents.
	 */
	private class RequestsServer extends CyclicBehaviour {
		private String record, sapid;
		private String fileName="Academic";
		private ACLMessage msg,msgr,reply,replyFRA;
		private MessageTemplate mt, mt1,mtr;
		private int step=0;
		public void action() {
			switch(step){
				case 0:
					//get agents providing read-file Service
					DFAgentDescription dfdg = new DFAgentDescription();
					ServiceDescription sdg = new ServiceDescription();
					sdg.setType("read-file");
					dfdg.addServices(sdg);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, dfdg); 
						//System.out.println("Academic Agent Found the following agents:");
						for (int j = 0; j < result.length; ++j) {
							fileReadAgent = result[0].getName();
							//System.out.println(fileReadAgent.getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					step=1;
					break;
		
				case 1:
					mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
					mtr = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
					msg = myAgent.receive(mt);
					msgr = myAgent.receive(mtr);
					if (msg != null){
						reply = msg.createReply();
						step=2;
					}
					if (msgr != null){
						reply = msgr.createReply();
						step=2;
					}
					break;
				case 2:
					if (msg != null ||msgr != null ) {
						// CFP Message received. Process it
						//System.out.println("Request Received at AcademicAgent: "+System.nanoTime());
						if(msg!=null){
							System.out.println("Request Received at AcademicAgent: "+System.nanoTime());
							sapid = msg.getContent();
						}
						if(msgr!=null){
							System.out.println("Recovery Request Received at AcademicAgent: "+System.nanoTime());
							String d = msgr.getContent();
							String[] d1 = d.split(" ");
							fileName=d1[1];
							sapid = d1[0];
						}
						try{
							//sending request to FileReadAgent
							Query query = new Query(fileName+".txt",sapid);
							ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
							cfp.addReceiver(fileReadAgent);
							cfp.setContentObject(query);
							cfp.setConversationId("readinfo");
							cfp.setReplyWith("cfp"+System.nanoTime()); // Unique value
							System.out.println("Request sent to FileReadAgent from AcademicAgent: "+System.nanoTime());
							myAgent.send(cfp);
							// Prepare the template to get response
							mt1 = MessageTemplate.and(MessageTemplate.MatchConversationId("readinfo"),
									MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
						}
						catch(IOException e){
							System.out.println("IO Exception"+e);
							System.out.println(e.getMessage());
						}
						catch(NullPointerException e){
							System.out.println("Null Academic Agent");
						}
						step=3;
					}
					else {
						block();
					}
					break;
				case 3:
					try{
						replyFRA = myAgent.receive(mt1);
						if(replyFRA != null)
						{
							System.out.println("Response Received at AcademicAgent from FileReadAgent: " +System.nanoTime());
							if (replyFRA.getPerformative() == ACLMessage.INFORM) {
								// This is required data 
								record = replyFRA.getContent();
								// The requested data is available for reply. Reply with the data
								reply.setPerformative(ACLMessage.INFORM);
								reply.setContent(record);
								reply.setConversationId("info");
							}
							if (replyFRA.getPerformative() == ACLMessage.REFUSE) {
								if(replyFRA.getContent().equals("info-not-available")){
									reply.setPerformative(ACLMessage.REFUSE);
									reply.setContent("info-not-available");
								}
								else{
									System.out.println("Fault Detected: No File Exist");
									fileName = fileName+"1";
									step = 1;
								}
							}
							// reply to DisplayInformationAgent
							if (record == null) {
								// The requested data is NOT available for reply.
								reply.setPerformative(ACLMessage.REFUSE);
								reply.setContent("not-available");
							}
						step++;
						}
					}
					catch(NullPointerException e){
						System.out.println("Null Academic Agent");
					}
		
					break;
				case 4:
					System.out.println("Response sent from AcademicAgent to DisplayInformationAgent: "+System.nanoTime());
					myAgent.send(reply);
					fileName="Academic";
					step=0;
					break;
			}
		}
	}  // End of inner class RequestsServer
}
