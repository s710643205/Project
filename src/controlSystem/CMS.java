package controlSystem;

import java.util.ArrayList;
import java.util.List;

import building.Building;
import lift.Lift;
import lift.loadState.Loaded;
import time.TimeConverter;

public class CMS{
	private Building building;
	private static CMS instance=new CMS();
	List<Lift> liftList;
	RequestSystem reqSys;
	int time;
	
	private CMS() {
		reqSys=new RequestSystem(this);
		liftList=new ArrayList<Lift>();
		this.building= new Building(20);
	};
	public static CMS getInstance() {
		return instance;
	}
	public void createLift(int capacity) {
		liftList.add(new Lift(capacity));
		System.out.println("Lift created!");
	}
	public RequestSystem getReqSys() {
		return reqSys;
	}

	public void setCurrentTime(int t) {time=t;}
	public int getCurrentTime() {return time;}

	public void assignClosest(int reqf,int reqDir) {//{here have problem: dir is fixed}
		//TODO check if any lift has request on that floor already, if yes then assign that lift
		
		int shortestDistance=Integer.MAX_VALUE;
		Lift assignLift=null;
		int reqFloor=reqf;
		int assignLiftdir=-1;
		for (Lift lift:liftList) {
			int dir=lift.getDirection();
			// if status ok, same dir, not passed
			if (checkAvailablity(lift,reqDir,reqf)) {//check if current lift is able to pick up the passenger in req flr
				int distance= calculateDistance(lift,reqf);
				if(distance<shortestDistance) {//if calculated result is smaller than shortest dis, assign the lift and update shortest dis
					assignLift=lift;
					shortestDistance=distance;
					assignLiftdir=dir;
				}
			}
			else {
				int distance=lift.checkClosestFromPassenger(dir,reqf,reqDir);//check whether the lift will be closest after finishing the current travel
				if(distance<shortestDistance) {
					assignLift=lift;
					shortestDistance=distance;
					assignLiftdir=dir;
				}
			}
		}
		if (assignLift!=null) {
			if (assignLift.getStatus().equals("idle")) {
				assignLift.setStatus(new Loaded());
			}
			
			//lift assign to that floor request and set flag to prevent future allocation
			if(reqDir==1) {
				if(!assignLift.getUpReqFloorList().contains(reqFloor)) {//does not contains up request from that floor before
					assignLift.getUpReqFloorList().add(reqFloor);
				}
				building.getFlrMap().get(reqf).setUpflag(true);
				assignLift.setReqDir(1);
			}	
			else {
				if(!assignLift.getDownReqFloorList().contains(reqFloor)) {//does not contains down request from that floor before
					assignLift.getDownReqFloorList().add(reqFloor);
				}
				building.getFlrMap().get(reqf).setDownflag(true);
				assignLift.setReqDir(0);
			}
				
		}
	}
	private boolean checkAvailablity(Lift lift, int reqDir,int reqf) {
		String status=lift.getStatus();
		if (status.equals("idle"))
			return true;
		else if(status.equals("loaded")) {
			if(sameDir(lift,reqDir)) {
				if(checkPassed(lift,reqDir,reqf)) {
					return false;
				}
				else return true;
			}
		}
		return false;
	}
	private boolean checkPassed(Lift lift,int dir, int reqf) {
		if (dir==1) {//go up
			if(lift.getCurrentFloor()>reqf)
				return true;
			else return false;
		}
		else {//go down
			if(lift.getCurrentFloor()<reqf)
				return true;
			else return false;
		}
	}
	private boolean sameDir(Lift lift, int dir) {
		return lift.getDirection()==dir;
	}
	private int calculateDistance(Lift lift, int reqf) {
		return  Math.abs(reqf-lift.getCurrentFloor());
	}
	
	public boolean curHaveRequest() {
		if(reqSys.getAllReq().isEmpty())
			return false;
		else 
			return true;
	}
	public boolean flrHaveRequest(int f) {
		return this.building.getFlrMap().get(f).haveReq();
	}

	public void operate(int curTime) {
		int i=0;
		System.out.printf("%nCurrent time: %s%n",TimeConverter.fromStoTime(curTime));
		for (Lift lift:liftList) {//operate each lift
			System.out.printf("-----------------------------------%n");
			System.out.printf("lift %s in %s/F (%s)%n",i,lift.getCurrentFloor(),TimeConverter.fromStoTime(curTime));
			lift.getHandler().handleCurrentFloor(lift.getCurrentFloor(),i);//handle all req & passenger on cur flr
			lift.move();//directon handle
			if (lift.getStatus().equals("idle")) {
				System.out.printf("lift %s is idling......%n",i);
			}
			else
				System.out.printf("lift %s moving to %s/F (%s)%n",i,lift.getCurrentFloor(),TimeConverter.fromStoTime(curTime));
			i++;
		}
	}
	public boolean anyLiftRunning() {
		for(Lift lift:liftList) {
			if (!lift.getStatus().equals("idle"))
				return true;
		}
		return false;
	}
	public Building getBuilding() {
		return building;
	}
	
	public List<Lift> getLiftList(){
		return liftList;
		
	}
}
