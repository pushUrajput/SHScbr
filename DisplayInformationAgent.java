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
import java.util.Arrays;

public class DisplayInformationAgent extends Agent {
	// The sap id of students to display information
	private String sapId;
	// The list of known information agents
	private AID[] callingAgents;
	//private AID financeAgent,academicAgent,enrollAgent;
	// The GUI by means of which the user can pass student sap id
	private DisplayInformationGui myGui;
	private String[] services;
	

	// Put agent initializations here
	protected void setup() {
		// Create and show the GUI 
		myGui = new DisplayInformationGui(this);
		myGui.showGui();
		services = new String[]{"display-academics","display-finance","display-enroll"}; 
		// Printout a welcome message
		System.out.println("Hello! Display-agent "+getAID().getName()+" is ready.");
		// Add a TickerBehaviour that schedules a request to multiple agents every minute
		addBehaviour(new TickerBehaviour(this, 60000) {
			protected void onTick() {
				System.out.println("Next Iteration");
			}
		} );
	}
	public void display(String id){
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				sapId=id;
				System.out.println("Fetching Information for "+sapId);
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				callingAgents = new AID[services.length];
				for(int i=0; i<services.length; ++i){
					sd.setType(services[i]);
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("Found the following agents:");
						for (int j = 0; j < result.length; ++j) {
							callingAgents[i] = result[j].getName();
							System.out.println(callingAgents[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}
					// Perform the request
				myAgent.addBehaviour(new RequestPerformer());
			}
		} );
	}
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Display-agent "+getAID().getName()+" terminating.");
	}

	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by Information Provider agents to different type of agent.
	 */
	private class RequestPerformer extends Behaviour {
		private int repliesCnt = 0; // The counter of replies from calling agents
		private MessageTemplate mt; // The template to receive replies
		private String[] finalRecord = new String[3];
		private String[] received = new String[3];
		private long sendTime=0;
		private int step = 0;
		private boolean recovery=false,flag=true;
		public void action() {
			switch (step) {
				case 0:
					// Send the cfp to all sellers
					if(!recovery)
					{
						ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
						for (int i = 0; i < callingAgents.length; ++i) {
							cfp.addReceiver(callingAgents[i]);
						} 
						cfp.setContent(sapId);
						cfp.setConversationId("info");
						cfp.setReplyWith("cfp"+System.nanoTime()); // Unique value
						System.out.println("Display Request Time: "+System.nanoTime());
						myAgent.send(cfp);
						// Prepare the template to get information
						mt = MessageTemplate.and(MessageTemplate.MatchConversationId("info"),
								MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
						step = 1;
						sendTime=System.nanoTime();
						System.out.println("Display Request Sent Time: "+sendTime);
					}
					if(recovery)
					{
						String []localnames = new String[]{"AA","AE","AF"};
						String localname="", notlocalname="",fileName="";
						System.out.println("Recovery Step Taken ");
						ACLMessage rqt = new ACLMessage(ACLMessage.REQUEST);
						for(int c = 0; c < received.length;c++){
							if(Arrays.asList(received).contains(localnames[c])){
									localname = localnames[c];
							}
							else{
									notlocalname = localnames[c];
							}
							System.out.println(notlocalname);
						}
						rqt.addReceiver(new AID(localname,AID.ISLOCALNAME));
						if(notlocalname.equals("AA"))
							rqt.setContent(sapId+" Academic");
						else if(notlocalname.equals("AE"))
							rqt.setContent(sapId+" Enroll");
						else
							rqt.setContent(sapId+" Finance");
						rqt.setConversationId("info");
						rqt.setReplyWith("rqt"+System.nanoTime()); // Unique value
						System.out.println("Recovery Display Request Sent Time: "+System.nanoTime());
						myAgent.send(rqt);
						// Prepare the template to get information
						mt = MessageTemplate.and(MessageTemplate.MatchConversationId("info"),
								MessageTemplate.MatchInReplyTo(rqt.getReplyWith()));
						step = 1;
					}
					break;
				case 1:
					// Receive all information
					//System.out.println("CCCCCCCC"+(System.nanoTime()- sendTime));
					//System.out.println("CCCCCCCC"+repliesCnt+"CCCCC"+callingAgents.length);
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						System.out.println("Got Reply From: "+reply.getSender().getLocalName()+" at: "+System.nanoTime());
						// Reply received
						
						if (reply.getPerformative() == ACLMessage.INFORM || reply.getPerformative() == ACLMessage.REFUSE) {
							// This is an the infromation from correponding agent 
							String info = reply.getContent();
							//System.out.println(info);
							finalRecord[repliesCnt] = info;
							received[repliesCnt]=reply.getSender().getLocalName();
							repliesCnt++;
							//System.out.println("AAAAAAA"+(System.nanoTime()- sendTime));
						}
					}
					if (repliesCnt >= callingAgents.length) {
						// We received all replies
						step = 2; 
					}
					else if(((System.nanoTime()- sendTime) > 5000000000l)&&flag){
						System.out.println("Agent Not Available");
						System.out.println("Recovery Step Started: "+System.nanoTime());
						recovery=true;
						flag=false;
						step=0;
					}
					else {
						//block();
					}
					break;
				case 2: 
					for(int itr=0;itr<3;itr++)
					{
						System.out.println(finalRecord[itr]);
					}
					step = 3;
					break;
			} 
		}
		public boolean done() {
			if(step==3)
				return true;
			return false;
		}
	}  // End of inner class RequestPerformer
}
