package ece1779.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.ServletContext;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.transform.DeregisterInstancesFromLoadBalancerRequestMarshaller;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

import ece1779.ec2.WorkerRecord;

public class LoadBalancerLibrary {
	private static LoadBalancerLibrary instance = null;
	private HashMap<String, WorkerRecord> workerPool = new HashMap<String, WorkerRecord>(); 
	private HashMap<String, WorkerRecord> inactiveWorkerPool = new HashMap<String, WorkerRecord>();
	private HashMap<String, WorkerRecord> startupWorkerPool = new HashMap<String, WorkerRecord>();
	int currPoolSize = 0; //number of active workers
	int inactivePoolSize = 0; //number of inactive workers
	
	private static final String workerAMIid = "ami-dc039db5"; //"ami-394add50";

	String currentInstanceID = "";
	//Manager params from web.xml
	String managerInstanceID = "";
	String defaultWorkerPoolSize = "2";
	String manualWorkerPoolSize = "2";
	String cpuThresholdGrowing = "50";
	String cpuThresholdShrinking = "10";
	String ratioExpandPool = "2";
	String ratioShrinkPool = "2";
	String configFilePath = "/var/lib/tomcat6/webapps/ece1779-img-project1/WEB-INF/config.xml";
	String poolResizeDelay = "60";
	private String managerInstanceIP;
	
	private String[] skippedInstances = {"i-6a4df319", "i-8e99d9fd"};
	private String loadBalancerName = "ECE1779Project1Group1";
	
	private static long lastBalance = 0l;
	
	private final String defaultWorkerInstanceID = "ami-dc039db5";
	
	public LoadBalancerLibrary()
	{
	}
	
	public static LoadBalancerLibrary getInstance()
	{
		if(instance==null) instance = new LoadBalancerLibrary();
		return instance;
	}
	
	public HashMap<String, WorkerRecord> getWorkerPool()
	{
		return workerPool;
	}

    public static String retrieveInstanceId() throws Exception 
    {
    	String EC2Id = "";
    	String inputLine;
    	URL EC2MetaData = new URL("http://169.254.169.254/latest/meta-data/instance-id");
    	URLConnection EC2MD = EC2MetaData.openConnection();
    	BufferedReader in = new BufferedReader(
    			new InputStreamReader(
    					EC2MD.getInputStream()));
    	while ((inputLine = in.readLine()) != null)
    	{	
    		EC2Id = inputLine;
    	}
    	in.close();
    	return EC2Id;
    }
    
    public void updateWorkerStats(ServletContext servletContext)
    {
    	managerInstanceID = servletContext.getInitParameter("managerInstanceID");
//    	if(!workerPool.containsKey(managerInstanceID))
//    	{
//    		WorkerRecord newRecord = new WorkerRecord();
//    		newRecord.setActive(true);
//    		newRecord.setStopped(false);
//    		newRecord.setLastInactivated(0);
//    		newRecord.setInstanceID(managerInstanceID);
//    		workerPool.put(managerInstanceID, newRecord);
//    	}
//
//    	if(!workerPool.containsKey(defaultWorkerInstanceID))
//    	{
//    		WorkerRecord newRecord = new WorkerRecord();
//    		newRecord.setActive(true);
//    		newRecord.setStopped(false);
//    		newRecord.setLastInactivated(0);
//    		newRecord.setInstanceID(defaultWorkerInstanceID);
//    		workerPool.put(defaultWorkerInstanceID, newRecord);
//    	}
    	
    	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)servletContext.getAttribute("AWSCredentials");

    	AmazonCloudWatch cw = new AmazonCloudWatchClient(awsCredentials);
    	AmazonEC2Client ec2 = new AmazonEC2Client(awsCredentials);

    	try {

    	    ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
    	    listMetricsRequest.setMetricName("CPUUtilization");
    	    listMetricsRequest.setNamespace("AWS/EC2");
    	    ListMetricsResult result = cw.listMetrics(listMetricsRequest);
    	    java.util.List<Metric>  metrics = result.getMetrics();

	        boolean resetWorkerPoolHashmap = false;
	        int foundOnlineRecords = 0;
    	    for (Metric metric : metrics) {
    	        String namespace = metric.getNamespace();
    	        String metricName = metric.getMetricName();
    	        List<Dimension> dimensions = metric.getDimensions();
    	        GetMetricStatisticsRequest statisticsRequest = new GetMetricStatisticsRequest();
    	        statisticsRequest.setNamespace(namespace);
    	        statisticsRequest.setMetricName(metricName);
    	        statisticsRequest.setDimensions(dimensions);
    	        Date endTime = new Date();
    	        Date startTime = new Date();
    	        startTime.setTime(endTime.getTime()-1200000);
    	        //Get stats for last 20 minutes
    	        statisticsRequest.setStartTime(startTime);
    	        statisticsRequest.setEndTime(endTime);
    	        statisticsRequest.setPeriod(60);
    	        Vector<String>statistics = new Vector<String>();
    	        statistics.add("Maximum");
    	        statisticsRequest.setStatistics(statistics);
    	        GetMetricStatisticsResult stats = cw.getMetricStatistics(statisticsRequest);
    	        
    	        if(stats.getDatapoints().size()>0)
    	        {
    	        	foundOnlineRecords++;
    	        	WorkerRecord newWorker = new WorkerRecord();
    	        	if(dimensions.size() > 0 && dimensions.get(0).getName().toString().compareTo("InstanceId")==0)
    	        	{
    	        		String instanceId = dimensions.get(0).getValue().toString();

    	        		//Only update if we know about this Instance somehow (started/stopped it)
//    	        		if(workerPool.containsKey(instanceId)
//    	        				|| inactiveWorkerPool.containsKey(instanceId)
//    	        				|| startupWorkerPool.containsKey(instanceId))
//    	        		{
    	        			//Skip inserting array of predefined skipped instances into (active) worker pool
    	        			//Manager instance is inserted so its load can be displayed in manager UI
    	        			boolean skipInsert = false;

    	        			for(int i=0; i<skippedInstances.length; i++)
    	        			{
    	        				if(instanceId.compareTo(skippedInstances[i])==0)
    	        				{
    	        					skipInsert=true;
    	        					break;
    	        				}
    	        			}

    	        			if(!resetWorkerPoolHashmap)
    	        			{
    	        				workerPool.clear();
    	        				resetWorkerPoolHashmap = true;
    	        			}

    	        			if(!skipInsert)
    	        			{
    	        				newWorker.setInstanceID(instanceId);
    	        				newWorker.setCpuLoad(stats.getDatapoints().get(0).getMaximum());

    	        				DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(newWorker.getInstanceID());
    	        				DescribeInstanceStatusResult describeInstanceResult = ec2.describeInstanceStatus(describeInstanceRequest);
    	        				List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
    	        				if (state.size() > 1) { 
    	        					String status = state.get(0).getInstanceState().getName();
    	        					if(status.compareTo(InstanceStateName.Running.toString())==0)
    	        					{
    	        						newWorker.setActive(true);
    	        						newWorker.setStopped(false);
    	        					}
    	        					else if(status.compareTo(InstanceStateName.Stopped.toString())==0)
    	        					{
    	        						newWorker.setActive(false);
    	        						newWorker.setStopped(true);
    	        					}
    	        					else
    	        					{
    	        						newWorker.setActive(false);
    	        						newWorker.setStopped(false);
    	        					}
    	        					workerPool.put(newWorker.getInstanceID(), newWorker);
    	        				}
    	        				else
    	        				{
    	        					//No state returned for instance; perhaps it is terminated or not otherwise active => ignore
    	        				}
    	        			}
//    	        		}
    	        	}
    	        }
    	    }
    	    
//    	    if(foundOnlineRecords==0)
//    	    {
//    	    	//Remove default Worker and Manager elements (which are assumed to be started always)
//    	    	workerPool.remove(managerInstanceID);
//    	    	workerPool.remove(defaultWorkerInstanceID);
//    	    }

        } catch (AmazonServiceException ase) {
        	ase.printStackTrace();
        	//Nothing to print
        } catch (AmazonClientException ace) {
        	ace.printStackTrace();
        	//Nothing to print
        }
    }
    
    public void loadBalance(ServletContext servletContext) throws Exception
    {
		managerInstanceID = servletContext.getInitParameter("managerInstanceID");
		
		currentInstanceID = retrieveInstanceId();
		
		int resizeDelay = 0;
		try
		{
			resizeDelay = Integer.parseInt(poolResizeDelay);
		}
		catch(NumberFormatException e){
			resizeDelay = 10;
		}
		
		if(currentInstanceID.compareTo(managerInstanceID) == 0 &&
		   System.currentTimeMillis() - lastBalance > (resizeDelay*1000))
		{
			//Load balance if need be
			
			//Update worker stats if necessary delay since last load balance has passed
			updateWorkerStats(servletContext);

		//See if any inactive instances need to be added to LB
	        ArrayList<Instance> instanceIDs = new ArrayList<Instance>();
			for(Entry<String, WorkerRecord> w : startupWorkerPool.entrySet())
			{
				if(workerPool.containsKey(w.getValue().getInstanceID()) && !instanceIDs.contains(w.getKey()))
				{
					Instance instance = new Instance(w.getKey());
					instanceIDs.add(instance);
				}
			}

			if(instanceIDs.size()>0)
			{
				try
				{
					AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient((AWSCredentials)servletContext.getAttribute("AWSCredentials"));
			        RegisterInstancesWithLoadBalancerRequest register = new RegisterInstancesWithLoadBalancerRequest();
			        register.setLoadBalancerName(loadBalancerName);
			        register.setInstances(instanceIDs);
			        RegisterInstancesWithLoadBalancerResult registerWithLoadBalancerResult = elb.registerInstancesWithLoadBalancer(register);
			        if(registerWithLoadBalancerResult.getInstances().size()>0)
			        {
			        	List<Instance> resultInstances = registerWithLoadBalancerResult.getInstances();
			        	for(Instance i : instanceIDs)
			        	{
			        		for(Instance lb : resultInstances)
			        		{
			        			if(i.getInstanceId().compareTo(lb.getInstanceId())==0)
			        			{
			        				WorkerRecord removed = startupWorkerPool.remove(i.getInstanceId());
			        				if(removed != null)
			        				{
			        					removed.setActive(true);
			        					removed.setStopped(false);
			        					removed.setLastInactivated(0);
			        					workerPool.put(removed.getInstanceID(), removed);
			        				}
			        				break;
			        			}
			        		}
			        	}
			        }

		        } catch (AmazonServiceException ase) {
		        	ase.printStackTrace();
		        	//Nothing to print
		        } catch (AmazonClientException ace) {
		        	ace.printStackTrace();
		        	//Nothing to print
		        }
			}
			
		//See if need to stop any inactive instances if configurable shutdown time threshold passed
	        ArrayList<String> stopRequestListInstances = new ArrayList<String>();
	        ArrayList<String> recordsToRemoveFromInactive = new ArrayList<String>();
			for(WorkerRecord w : inactiveWorkerPool.values())
			{
				if(System.currentTimeMillis() - w.getLastInactivated() > resizeDelay && !workerPool.containsKey(w.getInstanceID()))
				{
					stopRequestListInstances.add(w.getInstanceID());
				}
				else if(workerPool.containsKey(w.getInstanceID()))
				{
					//inactiveWorkerPool.remove(w.getInstanceID());
					recordsToRemoveFromInactive.add(w.getInstanceID());
				}
			}
			
			//See if there's any logical rebalancing to be done by removing entries from inactive pool
			//that have been reactivated when we updated status of workers
			for(String key : recordsToRemoveFromInactive)
			{
				inactiveWorkerPool.remove(key);
			}
			
			StopInstancesRequest stopInstanceRequest = null;
	        
	        //stop workers from inactive list
	        try
	        {
	    		AmazonEC2Client ec2 = new AmazonEC2Client((AWSCredentials)servletContext.getAttribute("AWSCredentials"));
	        	stopInstanceRequest = new StopInstancesRequest(stopRequestListInstances);
	        	StopInstancesResult stopResult = ec2.stopInstances(stopInstanceRequest);
	        	if(stopResult.getStoppingInstances().size()>0)
		        {
		        	List<InstanceStateChange> resultInstances = stopResult.getStoppingInstances();
		        	for(String i : stopRequestListInstances)
		        	{
		        		for(InstanceStateChange lb : resultInstances)
		        		{
		        			if(i.compareTo(lb.getInstanceId())==0)
		        			{
		        				WorkerRecord stopped = inactiveWorkerPool.get(i);
		        				if(stopped != null)
		        				{
		        					stopped.setStopped(true);
		        					stopped.setLastInactivated(0);
		        				}
		        				break;
		        			}
		        		}
		        	}
		        }

	        } catch (AmazonServiceException ase) {
	        	ase.printStackTrace();
	        	//Nothing to print
	        } catch (AmazonClientException ace) {
	        	ace.printStackTrace();
	        	//Nothing to print
	        }
			
		//See if need to increase/decrease pool size
	        
			double totalLoad = 0.0d;
			int workerCount = 0;
			double avgLoad = 0.0d;
			for(WorkerRecord w : workerPool.values())
			{
				totalLoad += w.getCpuLoad();
				workerCount++;
			}
			
			if(workerCount>0)
			{
				avgLoad = (double)(totalLoad / workerCount);
			}
			
			//call increase/decrease worker methods as appropriate
			int cpuThresholdGrowing = 50;
			int cpuThresholdShrinking = 5;
			int ratioExpand = 2;
			int ratioShrink = 2;
			try
			{
				cpuThresholdGrowing = Integer.parseInt(this.cpuThresholdGrowing);
				cpuThresholdShrinking = Integer.parseInt(this.cpuThresholdShrinking);
				ratioExpand = Integer.parseInt(this.ratioExpandPool);
				ratioShrink = Integer.parseInt(this.ratioShrinkPool);
				
			}
			catch(NumberFormatException e)
			{
				cpuThresholdGrowing = 50;
				cpuThresholdShrinking = 10;
				ratioExpand = 2;
				ratioShrink = 2;
			}
			
			if(avgLoad > cpuThresholdGrowing)
			{
				increaseWorkerPoolSize(currPoolSize * ratioExpand, (AWSCredentials)servletContext.getAttribute("AWSCredentials"));
			}
			else if(avgLoad < cpuThresholdShrinking)
			{
				decreaseWorkerPoolSize(currPoolSize / ratioShrink, (AWSCredentials)servletContext.getAttribute("AWSCredentials"));
			}
			
			//Update last check time for delay check as we don't want to poll stats info too often
			lastBalance = System.currentTimeMillis();
		}
    }
    
    public void clientInvokeCoordLoadBalance(ServletContext servletContext)
    {
		managerInstanceIP = servletContext.getInitParameter("managerInstanceIP");
		if(managerInstanceIP == null || managerInstanceIP.length() == 0)
		{
			managerInstanceIP = "54.235.69.246";
		}
		
    	try {
        	HttpClient httpclient = new DefaultHttpClient();
        	HttpGet httpget = new HttpGet("http://" + managerInstanceIP + ":8080/servlet/LoadBalance");
			HttpResponse response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void loadConfigParameters()
    {
		try {
			File fXmlFile = new File(configFilePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			//getting root element
			NodeList nl = doc.getDocumentElement().getChildNodes();
			Element root = doc.getDocumentElement();
			defaultWorkerPoolSize = root.getElementsByTagName("DefaultWorkerPoolSize").item(0).getTextContent(); 
			manualWorkerPoolSize = root.getElementsByTagName("SavedWorkerPoolSize").item(0).getTextContent();
			cpuThresholdGrowing = root.getElementsByTagName("CpuThresholdGrowing").item(0).getTextContent();
			cpuThresholdShrinking = root.getElementsByTagName("CpuThresholdShrinking").item(0).getTextContent();
			ratioExpandPool = root.getElementsByTagName("RatioExpandPool").item(0).getTextContent();
			ratioShrinkPool = root.getElementsByTagName("RatioShrinkPool").item(0).getTextContent();
			poolResizeDelay = root.getElementsByTagName("PoolResizeDelay").item(0).getTextContent();

			if (manualWorkerPoolSize.isEmpty() ) {
				manualWorkerPoolSize = defaultWorkerPoolSize;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public String getSavedPoolSize()
    {
    	return manualWorkerPoolSize;
    }
    
    public String getCpuThresholdGrowing()
    {
    	return cpuThresholdGrowing;
    }
    
    public String getCpuThresholdShrinking()
    {
    	return cpuThresholdShrinking;
    }
    
    public String getRatioExpandPool()
    {
    	return ratioExpandPool;
    }
    
    public String getRatioShrinkPool()
    {
    	return ratioShrinkPool;
    }
    
    public String getPoolResizeDelay()
    {
    	return poolResizeDelay ;
    }
    
    public void setManualWorkerPoolSize(String enteredPoolsize)
    {

		//saving the ManualWorkerPoolSize to file and update in memory
    	
    	manualWorkerPoolSize = enteredPoolsize;
    	
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(configFilePath);
						
			//getting root element
			Element root = doc.getDocumentElement();

			Node SavedWorkerPoolSize = root.getElementsByTagName("SavedWorkerPoolSize").item(0);
			SavedWorkerPoolSize.setTextContent(enteredPoolsize);
								
			//write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(configFilePath));
			transformer.transform(source, result);

		} catch (Exception e) {
				e.printStackTrace();
			} 
    }
    

	public void setThresholdsAndRatios(String cpuThresholdGrowing2,
			String cpuThresholdShrinking2, String ratioExpandPool2,
			String ratioShrinkPool2, String poolResizeDelay) {

		//saving to config file

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(configFilePath);

			//getting root element
			Element root = doc.getDocumentElement();

			root.getElementsByTagName("CpuThresholdGrowing").item(0).setTextContent(cpuThresholdGrowing2);
			root.getElementsByTagName("CpuThresholdShrinking").item(0).setTextContent(cpuThresholdShrinking2);
			root.getElementsByTagName("RatioExpandPool").item(0).setTextContent(ratioExpandPool2);
			root.getElementsByTagName("RatioShrinkPool").item(0).setTextContent(ratioShrinkPool2);
			root.getElementsByTagName("PoolResizeDelay").item(0).setTextContent(poolResizeDelay);

			//write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(configFilePath));
			transformer.transform(source, result);


		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * Increase pool size to new value using servletContext credentials saved by EC2 Initialization class.
	 * @param newSize
	 * @param credentials
	 */
	public void decreaseWorkerPoolSize(int newSize, AWSCredentials credentials)
	{	
		WorkerRecord worker = new WorkerRecord();
		ArrayList<String> deactivatedInstances = new ArrayList<String>();
		for(String instanceKey : workerPool.keySet())
		{
			if(workerPool.size() > newSize)
			{
				worker = workerPool.get(instanceKey);
				if(worker != null)
				{
					if(worker.isActive())
					{
						worker.setActive(false);
						worker.setStopped(false);
						worker.setLastInactivated(System.currentTimeMillis());
						deactivatedInstances.add(worker.getInstanceID());
						inactiveWorkerPool.put(instanceKey, worker);
					}
				}
			}
		}
		
		//Remove freshly deactivated instances from active pool (and possibly others that are still not fully stopped)
        ArrayList<Instance> deregisterInstanceIDs = new ArrayList<Instance>();
        HashMap<String, WorkerRecord> recordToRemove = new HashMap<String, WorkerRecord>();
		for(String inactiveInstanceKey : deactivatedInstances)
		{
			if(workerPool.containsKey(inactiveInstanceKey))
			{
				//workerPool.remove(inactiveInstanceKey);
				recordToRemove.put(inactiveInstanceKey, workerPool.get(inactiveInstanceKey));
				deregisterInstanceIDs.add(new Instance(inactiveInstanceKey));
			}
		}
		
		//Remove instances we just inactivated from active worker pool
		for(WorkerRecord w : workerPool.values())
		{
			workerPool.remove(w.getInstanceID());
		}
		
		//Remove deactivated instances from LB
		try
		{
			AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(credentials);
	        DeregisterInstancesFromLoadBalancerRequest deregister = new DeregisterInstancesFromLoadBalancerRequest();
	        deregister.setLoadBalancerName(loadBalancerName);
	        deregister.setInstances(deregisterInstanceIDs);
	        DeregisterInstancesFromLoadBalancerResult deregisterFromLoadBalancerResult = elb.deregisterInstancesFromLoadBalancer(deregister);
	        if(deregisterFromLoadBalancerResult.getInstances().size()>0)
	        {
	        	//Nothing to do - already marked instance as stopped locally and tried to remove from LB :)
	        }

        } catch (AmazonServiceException ase) {
        	ase.printStackTrace();
        	//Nothing to print
        } catch (AmazonClientException ace) {
        	ace.printStackTrace();
        	//Nothing to print
        }
	}
	
	/**
	 * Increase pool size to new value using servletContext credentials saved by EC2 Initialization class.
	 * @param newSize
	 * @param credentials
	 */
	public void increaseWorkerPoolSize(int newSize, AWSCredentials credentials)
	{
		//+1 because of manager being there...
		int numToStart = newSize - workerPool.size() + 1;
		numToStart = (numToStart >=20) ? 20 : numToStart;
		
		AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(credentials);
		AmazonEC2Client ec2 = new AmazonEC2Client(credentials);
//		
//		//get the running instances
//        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
//        List<Reservation> reservations = describeInstancesRequest.getReservations();
//        List<Instance> instances = new ArrayList<Instance>();
//
//        for (Reservation reservation : reservations) {
//            instances.addAll((Collection)reservation.getInstances());
//        }
//		
//        //get instance id's
//        String id;
//        List instanceId=new ArrayList();
//        List instanceIdString=new ArrayList();
//        Iterator<Instance> iterator=instances.iterator();
//        while (iterator.hasNext())
//        {
//            id=iterator.next().getInstanceId();
//            instanceId.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(id));
//            instanceIdString.add(id);
//        }
		
        if(numToStart>0)
        {
	        StartInstancesRequest startInstanceRequest = null;
	        ArrayList<String> startRequestListInstances = new ArrayList<String>();
	        
	        //start workers (first from inactive which ARE COMPLETELY STOPPED TO BEGIN WITH)
	        for(String inactiveInstanceID : inactiveWorkerPool.keySet())
	        {
	        	WorkerRecord worker = inactiveWorkerPool.get(inactiveInstanceID);
	        	if(worker.isStopped() && !startRequestListInstances.contains(inactiveInstanceID) && numToStart > 0)
	        	{
	        		startRequestListInstances.add(inactiveInstanceID);
	        		numToStart--;
	        	}
	        }
	        
	        if(startRequestListInstances.size()>0)
	        {
	        	try
	        	{
	        		startInstanceRequest = new StartInstancesRequest(startRequestListInstances);
	        		StartInstancesResult startResult = ec2.startInstances(startInstanceRequest);
	        	} catch (AmazonServiceException ase) {
	        		ase.printStackTrace();
	        		//Nothing to print
	        	} catch (AmazonClientException ace) {
	        		ace.printStackTrace();
	        		//Nothing to print
	        	}
	        }
	        
	        int numStarted=0;
//	        while(numToStart-numStarted>0)
//	        {
	        	try
		        {
	        		for(int i=0; i<numToStart; i++)
	        		{
//				    	RunInstancesRequest request = new RunInstancesRequest(workerAMIid,1,1);
//				    	request.setKeyName("ece1779-group1-instances-"+System.currentTimeMillis());
//				    	request.setInstanceType(InstanceType.M1Small);
	        			RunInstancesRequest request = new RunInstancesRequest()
	        		    .withInstanceType(InstanceType.M1Small)
	        		    .withImageId(workerAMIid)
	        		    .withMinCount(1)
	        		    .withMaxCount(1)
	        		    .withMonitoring(true)
	        		    .withSecurityGroupIds("ece1779Project1")
	        		    .withKeyName("BozhidarKey");

	        			RunInstancesResult createResult = ec2.runInstances(request);
				    	if(createResult.getReservation().getInstances().size() > 0)
				    	{
				    		com.amazonaws.services.ec2.model.Instance instance = createResult.getReservation().getInstances().get(0);
				    		WorkerRecord newWorker = new WorkerRecord();
				    		newWorker.setActive(false);
				    		newWorker.setStopped(false);
				    		newWorker.setCpuLoad(0.0);
				    		newWorker.setInstanceID(instance.getInstanceId());
				    		newWorker.setLastInactivated(0);
				    		startupWorkerPool.put(instance.getInstanceId(), newWorker);
				    		numStarted++;
				    	}
	        		}
		        } catch (AmazonServiceException ase) {
		        	ase.printStackTrace();
		        	//Nothing to print
		        } catch (AmazonClientException ace) {
		        	ace.printStackTrace();
		        	//Nothing to print
		        }
//	        }
        }
	}

}