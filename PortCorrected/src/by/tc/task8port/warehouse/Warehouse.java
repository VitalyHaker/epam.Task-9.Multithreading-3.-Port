package by.tc.task8port.warehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Warehouse {
	private List<Container> containerList;
	private int size;
	private Lock lock; 		//Используется только в Berth, здесь нет

	public Warehouse(int size) {
		containerList = new ArrayList<Container>(size);
		lock = new ReentrantLock();
		this.size = size;
	}

	public boolean addContainer(Container container) {	
		boolean result = false;  // проверка на первый контейнер
		if((containerList.size() + 1 <= size)){
			result = containerList.add(container);
		}
		return result;
	}

	public boolean addContainer(List<Container> containers) {
		boolean result = false;
		if(containerList.size() + containers.size() <= size){
			result = containerList.addAll(containers);
		}
		return result;
	}

	public Container getContainer() {
		if (containerList.size() > 0) {
			return containerList.remove(0);
		}
		return null;
	}

	public List<Container> getContainer(int amount) {
		if (containerList.size() >= amount) {			
			List<Container> cargo = new ArrayList<Container>(containerList.subList(0, amount));
			containerList.removeAll(cargo);
			return cargo;
		}
		return null;
	}
	
	public int getSize(){
		return size;
	}
	
	public int getRealSize(){
		return containerList.size();
	}
	
	public int getFreeSize(){
		return size - containerList.size();
	}
	
	public Lock getLock(){
		return lock;
	}	
}
