package gestv;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.ws.rs.core.MultivaluedMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.net.httpserver.HttpServer;
import mbean.DynamicMBeanMirrorFactory;
import mbean.MBSAConnection;
import mbean.MBSAConnections;
import model.MongoDBConnection;

public class MBeanServerMaster {
	
    private MBeanServer masterMbeanServer;
    private static final String BASE_URI = "http://0.0.0.0:9999/mbs/";
    public static final String INSTRUMENTING_SERVER_IP = "192.168.119.35";
    public static final String INSTRUMENTING_SERVER_PORT = "9998";
    public static final String INSTRUMENTING_SERVER_WS_PORT = "9997";
    private static HttpServer server; 
    
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		MBeanServerMaster mmaster = new MBeanServerMaster();
	}

	public MBeanServerMaster() {
		Logger.getLogger("javax.management.remote").setLevel(Level.OFF);		
		masterMbeanServer = ManagementFactory.getPlatformMBeanServer();
		DynamicMBeanMirrorFactory.setMBeanMasterServer(masterMbeanServer);

		try {
			MongoDBConnection mdbc = MongoDBConnection.getInstance();
						
			for (String s : mdbc.getAllColls()) {
			    System.out.println(s);
			}
			
			//mdbc.setColl("atr_hst");
			//ObjectId objid = new ObjectId(); 
			//BasicDBObject doc = new BasicDBObject("atr_id", "1").append("value", "1000").append("tstamp", objid.getTime());
			//mdbc.insert_doc(doc);
			
			/*DBCursor cursor = mdbc.getAllDocs();
			try {
			   while(cursor.hasNext()) {
			       System.out.println(cursor.next());
			   }
			} finally {
			   cursor.close();
			}*/

		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		try {
            server = HttpServerFactory.create(BASE_URI);
            server.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		updateEstado();
		connDaemon();
		menu();
		//server.stop(0);
        //System.exit(0);
	}
	
	private void updateEstado() {
		MongoDBConnection mdbc;
		try {
			mdbc = MongoDBConnection.getInstance();
	    	mdbc.setColl("man_rscs");
			BasicDBObject doca = new BasicDBObject().append("_type" , "Serv");
			BasicDBObject docb = new BasicDBObject();
			docb.append("$set", new BasicDBObject().append("on", false));
			mdbc.updateAll_doc(doca, docb);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void connDaemon() {
		try {
			MongoDBConnection mdbc = MongoDBConnection.getInstance();
			DB db = mdbc.getDb();
			
			
			MBSAConnection connection;
			connection = new MBSAConnection(INSTRUMENTING_SERVER_IP, INSTRUMENTING_SERVER_PORT, "SNMPInstrumentingServer", "SNMPInstrumentingServer");
			connection.connect();
			if(connection.getConn()!=null){
				connection.getConn().addConnectionNotificationListener(new DynamicMBeanMirrorFactory(), null, null);
				MBSAConnections.add(connection);
			}

			DBCollection coll = db.getCollection("man_rscs");
			BasicDBObject query = new BasicDBObject("mngbl", true);
			
			DBCursor cursor = coll.find(query);
			DBObject obj,obj1;
			String domain,type,ip;
			Client client = Client.create();
			WebResource webResource = client.resource("http://"+INSTRUMENTING_SERVER_IP+":"+INSTRUMENTING_SERVER_WS_PORT+"/snmp_mbs/register");
			try {
			   while(cursor.hasNext()) {
				   obj=cursor.next();
				   obj1=(DBObject) obj.get("conn");
				   String port = null;
				   if(obj1.get("port").getClass().getCanonicalName().equals("java.lang.Integer"))
					   port=Integer.toString(((Integer) obj1.get("port")));
				   else
					   port=Integer.toString(((Double) obj1.get("port")).intValue());
				   ip=(String)obj1.get("ip");
				   domain=(String)obj.get("domain");
				   type=(String)obj.get("name");
				   if(domain.equals("SNMPInstrumentingServer")){
					   MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
					   queryParams.add("domain", "SNMPInstrumentingServer");
					   queryParams.add("type", type);
					   ClientResponse s = webResource.queryParams(queryParams).post(ClientResponse.class, queryParams);
					   if(s.getEntity(String.class).equals("ok"))
						   DynamicMBeanMirrorFactory.register(ip, port, domain, type);
				   }else
					   DynamicMBeanMirrorFactory.register(ip, port, domain, type);
			   }
			} finally {
			   cursor.close();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void menu(){
		String ip,port,domain,type;
		Scanner scanner = new Scanner(System.in);
		//DynamicMBeanMirrorFactory.register("192.168.119.35", "10001", "broadcaster", "Webservices");
		//DynamicMBeanMirrorFactory.register("192.168.119.35", "9998", "SNMPInstrumentingServer", "BroadcasterServer");
		//DynamicMBeanMirrorFactory.removeAll(MBSAConnections.searchConnection("192.168.119.35", "10001"));
		//DynamicMBeanMirrorFactory.setMonitor("broadcaster", "Webservices", "ga1", "518bbbb58a3d1ed2aa000083", "qos", "off");
		//DynamicMBeanMirrorFactory.setMonitor("broadcaster", "Webservices", "ga1", "518bbbb58a3d1ed2aa000083", "qos", "on");
		//DynamicMBeanMirrorFactory.setAlertable("broadcaster", "Webservices", "inact");
		//DynamicMBeanMirrorFactory.setAlertable("broadcaster", "Webservices", "act");
		while (true) {
			System.out.println("1. Registrar MBeanServer");
			System.out.println("2. Remover MBeanServer");
			System.out.println("3. Salir");
			int selection = scanner.nextInt();
			if (selection == 3) {
				break;
			}
			switch (selection) {
			case 1:
				System.out.println("Registrando MbeanServer");
				scanner.nextLine();
				System.out.println("ip=");
				ip=scanner.nextLine();
				System.out.println("port=");
				port=scanner.nextLine();
				System.out.println("domain=");
				domain=scanner.nextLine();
				System.out.println("type=");
				type=scanner.nextLine();
				DynamicMBeanMirrorFactory.register(ip, port, domain, type);
				break;
			case 2:
				System.out.println("Removiendo MbeanServer");
				scanner.nextLine();
				System.out.println("ip=");
				ip=scanner.nextLine();
				System.out.println("port=");
				port=scanner.nextLine();
				MBSAConnection connection=MBSAConnections.searchConnection(ip, port);
				if(connection!=null){
					DynamicMBeanMirrorFactory.removeAll(connection);
					System.out.println("Removida la conexión a la dirección "+ip+":"+port);
				}else
					System.out.println("No se encontró ninguna conexión a la dirección "+ip+":"+port);
				break;	
			default:
				System.out.println("opcion no valida.");
				break;
			}
		}
		updateEstado();
		scanner.close();
        server.stop(0);
        System.exit(0);
	}

	
	/*public void register(String dirip, String port, String domain, String type, String name){
		MBSAConnection connection=MBSAConnections.searchConnection(dirip, port);
		if(connection==null){
			connection = new MBSAConnection(dirip, port, domain);
			connection.connect();
			if(connection.getConn()!=null){
				connection.getConn().addConnectionNotificationListener(this, null, null);
				MBSAConnections.add(connection);
				importAll(connection, type, name);				
			}
		}else{
			System.out.println("Ya existe una conexión en la dirección "+dirip+":"+port);
			connection.connect();
			if(connection.getConn()!=null){
				importAll(connection, type, name);				
			}
		}
	}
	
	private void importAll(MBSAConnection connection, String type, String nom){ 
		
		if(connection.getConn()!=null){
	        Set<ObjectName> names = connection.queryMbeanDomain(type, nom);
			ObjectName mirrorName = null;
	        for (ObjectName name : names) {
	            try {
	                mirrorName = new ObjectName(""+name);
	                MyDynamicMBeanMirror mirror = DynamicMBeanMirrorFactory.newMBeanMirror(connection.getAgentMbeanServer(), name);
	                System.out.println("name = "+mirrorName);
	                if(!mirrorName.toString().equals("JMImplementation:type=MBeanServerDelegate")){
		                masterMbeanServer.registerMBean(mirror, mirrorName);
		            	mirror.addNotificationListener(attlist, null, null);
		            	System.out.println("MBean "+mirrorName+" registrado.");
	                }
	            } catch (IllegalArgumentException e) {
	            	System.out.println("El MBeanServerAgent \""+mirrorName+"\" no presenta interfaz de notificaciones");
	            } catch (InstanceAlreadyExistsException e) {
					System.out.println("El MBean "+mirrorName+" ya se encuentra registrado.");
				} catch (MBeanRegistrationException e) {
					e.printStackTrace();
				} catch (NotCompliantMBeanException e) {
					e.printStackTrace();
				} catch (InstanceNotFoundException e) {
					e.printStackTrace();
				} catch (IntrospectionException e) {
					e.printStackTrace();
				} catch (MalformedObjectNameException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        
	        names = connection.queryMbeanServices("Services");
	        for (ObjectName name : names) {
	            try {
	                mirrorName = new ObjectName(""+name);
	                connection.getAgentMbeanServer().addNotificationListener(mirrorName, listener, null, null);
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
	        }
		}
    }

	@Override
	public void handleNotification(Notification notification, Object arg1) {
		JMXConnectionNotification notif = (JMXConnectionNotification)notification;
		JMXConnector conn = (JMXConnector)notif.getSource();
		if(notif.getType().equals("jmx.remote.connection.closed")){
			try {
				conn.removeConnectionNotificationListener(this);
				MBSAConnection connection=MBSAConnections.searchConnection(conn);
				removeAll(connection);
				System.out.println("La conexión RMI se cayó");
				//reconnect(connection);
			} catch (ListenerNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void reconnect(MBSAConnection connection, String type, String name){
		while(connection.getConn()==null){
			System.out.println("Reconectando...");
			connection.connect();
			if(connection.getConn()!=null){
				connection.getConn().addConnectionNotificationListener(this, null, null);				
				MBSAConnections.add(connection);
				importAll(connection, type, name);				
			} else{
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void removeAll(MBSAConnection connection){
		Set<ObjectName> names = connection.getMbeanNames();
        for (ObjectName name : names) {
            try {
            	if(!name.toString().equals("JMImplementation:type=MBeanServerDelegate"))
            		masterMbeanServer.unregisterMBean(name);
            } catch (IllegalArgumentException e) {
            	System.out.println("El MBean \""+name+"\" no presenta interfaz de notificaciones");
            } catch (MBeanRegistrationException e) {
				e.printStackTrace();
			} catch (InstanceNotFoundException e) {
				e.printStackTrace();
			}
        }
		MBSAConnections.removeConnection(connection);
		connection.setConn(null);
	}*/
	
	/*
	private void createLocalMBean(){
		String domain = "broadcaster";
		String name = "attrs1";
		String type = "servicios";
		java.net.URL r = this.getClass().getResource("/");
		DynamicMBeanFactory.getDynamicBean(domain, name, type,r.getPath(),null);
	}
	*/
}
