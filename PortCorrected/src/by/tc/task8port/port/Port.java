package by.tc.task8port.port;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import by.tc.task8port.ship.Ship;
import by.tc.task8port.warehouse.Container;
import by.tc.task8port.warehouse.Warehouse;

public class Port {
	
	private final static Logger logger = Logger.getRootLogger();
	
	private BlockingQueue<Berth> berthList; // очередь причалов
	private volatile Warehouse portWarehouse; // хранилище порта, указываем volatile

//	отсутствует синхронизация usedBerths, необходимо изменить 
//	коллекцию Map на ConcurrentMap
	private ConcurrentMap<Ship, Berth> usedBerths; // какой корабль у какого причала стоит

	public Port(int berthSize, int warehouseSize) {
		portWarehouse = new Warehouse(warehouseSize); // создаем пустое хранилище
		berthList = new ArrayBlockingQueue<Berth>(berthSize); // создаем очередь причалов
		for (int i = 0; i < berthSize; i++) { // заполняем очередь причалов непосредственно самими причалами
			berthList.add(new Berth(i, portWarehouse));
		}
//		ошибка в синхронизации usedBerths, непотокобезопасную реализацию HashMap
//		необходимо заменить на ConcurrentHashMap
		usedBerths = new ConcurrentHashMap<Ship, Berth>(); // создаем объект, который будет
		// хранить связь между кораблем и причалом
		logger.debug("Порт создан.");
	}
	
	public void setContainersToWarehouse(List<Container> containerList){
		portWarehouse.addContainer(containerList);
	}

	public boolean lockBerth(Ship ship) {
		Berth berth;
		try {
			berth = berthList.take();
			usedBerths.put(ship, berth);
		} catch (InterruptedException e) {
			logger.debug("Кораблю " + ship.getName() + " отказано в швартовке.");
			return false;
		}		
		return true;
	}
	
	
	public boolean unlockBerth(Ship ship) {
		Berth berth = usedBerths.get(ship);
		
		try {
			usedBerths.remove(ship); // изменили порядок, т.к. сначала снимаем связь причала
			berthList.put(berth); // с кораблем, а затем освобождаем причал		
		} catch (InterruptedException e) {
			logger.debug("Корабль " + ship.getName() + " не смог отшвартоваться.");
			return false;
		}		
		return true;
	}
	
	public Berth getBerth(Ship ship) throws PortException {
		
		Berth berth = usedBerths.get(ship);
		if (berth == null){
			throw new PortException("Try to use Berth without blocking.");
		}
		return berth;		
	}
}