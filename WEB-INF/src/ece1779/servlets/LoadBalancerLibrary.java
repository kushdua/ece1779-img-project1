package ece1779.servlets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;

public class LoadBalancerLibrary {
	private static LoadBalancerLibrary instance = null;
	private HashMap<String, WorkerRecord> workerPool = new HashMap<String, WorkerRecord>(); 
	int currPoolSize = 0; //number of active workers
	int inactivePoolSize = 0; //number of inactive workers
	
	//Manager params from web.xml
	String managerInstanceID = "";
	String currentInstanceID = "";
	
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
    	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)servletContext.getAttribute("AWSCredentials");

    	AmazonCloudWatch cw = new AmazonCloudWatchClient(awsCredentials);
    	AmazonEC2Client ec2 = new AmazonEC2Client(awsCredentials);

    	try {

    	    ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
    	    listMetricsRequest.setMetricName("CPUUtilization");
    	    listMetricsRequest.setNamespace("AWS/EC2");
    	    ListMetricsResult result = cw.listMetrics(listMetricsRequest);
    	    java.util.List<Metric>  metrics = result.getMetrics();

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
    	        
    	        if(stats.getDatapoints().size()>0  )
    	        {
    	        	WorkerRecord newWorker = new WorkerRecord();
    	        	if(dimensions.size() > 0 && dimensions.get(0).getName().toString().compareTo("InstanceId")==0)
    	        	{
    	        		newWorker.setInstanceID(dimensions.get(0).getValue().toString());
    	        		newWorker.setCpuLoad(stats.getDatapoints().get(0).getMaximum());
    	        		
    	        		DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(newWorker.getInstanceID());
    	        		DescribeInstanceStatusResult describeInstanceResult = ec2.describeInstanceStatus(describeInstanceRequest);
    	        		List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
    	        		while (state.size() < 1) { 
    	        		    // Do nothing, just wait, have thread sleep if needed
    	        		    describeInstanceResult = ec2.describeInstanceStatus(describeInstanceRequest);
    	        		    state = describeInstanceResult.getInstanceStatuses();
    	        		}
    	        		String status = state.get(0).getInstanceState().getName();
    	        		if(status.compareTo(InstanceStateName.Running.toString())==0)
    	        		{
    	        			newWorker.isActive=true;
    	        		}
        	        	workerPool.put(newWorker.getInstanceID(), newWorker);
    	        	}
    	        }
    	    }
    	} catch (AmazonServiceException ase) { } catch (AmazonClientException ace) { }
    }
    
    public void loadBalance(ServletContext servletContext) throws Exception
    {
		managerInstanceID = servletContext.getInitParameter("managerInstanceID");
		currentInstanceID = retrieveInstanceId();
		if(currentInstanceID.compareTo(managerInstanceID) == 0)
		{
			//Load balance if need be
			
		}
    }
	
	/**
	 * Increase pool size to new value using servletContext credentials saved by EC2 Initialization class.
	 * @param newSize
	 * @param credentials
	 */
	private void increaseWorkerPoolSize(int newSize, AWSCredentials credentials)
	{
		AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(credentials);
		AmazonEC2Client ec2 = new AmazonEC2Client(credentials);
		
		//get the running instances
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        List<Instance> instances = new ArrayList<Instance>();

        for (Reservation reservation : reservations) {
            instances.addAll((Collection)reservation.getInstances());
        }
		
        //get instance id's
        String id;
        List instanceId=new ArrayList();
        List instanceIdString=new ArrayList();
        Iterator<Instance> iterator=instances.iterator();
        while (iterator.hasNext())
        {
            id=iterator.next().getInstanceId();
            instanceId.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(id));
            instanceIdString.add(id);
        }
		
		//TODO mark some workers as inactive so requests don't get forwarded to them... mark as active if need to reactivate in the future
		//register the instances to the balancer
        RegisterInstancesWithLoadBalancerRequest register =new RegisterInstancesWithLoadBalancerRequest();
        register.setLoadBalancerName("loader");
        register.setInstances((Collection)instanceId);
        RegisterInstancesWithLoadBalancerResult registerWithLoadBalancerResult= elb.registerInstancesWithLoadBalancer(register);
	}

	public class WorkerRecord
	{
		private String instanceID = "";
		private double cpuLoad = 0.0f;
		private boolean isActive = true;
		
		public String getInstanceID() {
			return instanceID;
		}
		
		public void setInstanceID(String instanceID) {
			this.instanceID = instanceID;
		}
		
		public double getCpuLoad() {
			return cpuLoad;
		}
		
		public void setCpuLoad(double cpuLoad) {
			this.cpuLoad = cpuLoad;
		}
		
		public boolean isActive() {
			return isActive;
		}
		
		public void setActive(boolean isActive) {
			this.isActive = isActive;
		}
	}
}