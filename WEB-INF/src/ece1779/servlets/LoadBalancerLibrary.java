package ece1779.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;

public class LoadBalancerLibrary {
	private LoadBalancerLibrary instance = null;
	private HashMap<Integer, WorkerRecord> workerPool = new HashMap<Integer, WorkerRecord>(); 
	int currPoolSize = 0; //number of active workers
	int inactivePoolSize = 0; //number of inactive workers
	
	//Manager params from web.xml
	String managerInstanceID = "";
	
	public LoadBalancerLibrary()
	{
		if(instance==null) instance=new LoadBalancerLibrary();
	}
	
	public LoadBalancerLibrary getInstance()
	{
		if(instance==null) instance = new LoadBalancerLibrary();
		return instance;
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

	private class WorkerRecord
	{
		String instanceID = "";
		float cpuLoad = 0.0f;
		boolean isActive = true;
	}
}